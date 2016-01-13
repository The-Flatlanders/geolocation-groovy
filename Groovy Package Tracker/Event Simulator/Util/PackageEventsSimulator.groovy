
@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7' )
import groovyx.net.http.*
import groovy.time.*


class Track {
    String name;
    String uuid;
    long originalStartTime;
    long originalEndTime;
    long startTime;
    long endTime;
    def destinationLat;
    def destinationLon;
    def orderedUpdates;
}


START = 1;
UPDATE = 2;
DELIVERY = 3;

class Event {
    def eventType;
    Track track;
    long updateTime;
    def updateBody;
}




def cli = new CliBuilder(usage:'groovy PackageEventsSimulator.groovy [options] [gpxFiles]', header:'Options:');
cli.help(longOpt:"help", 'Print this message.');
cli.c(longOpt:"compressTo",
      args:1,
      argName:'minutes',
      'Compress all of the times so that the first start time and last end time fits within the specified number of minutes.');
cli.m(longOpt:"matchStarts",
      'Shift all of the start times to match the first start time.');
cli.n(longOpt:"now",
      'Shift all of the start times to now. (Incompatible with the -s --shiftToEndIn option)');
cli.s(longOpt:"shiftToEndIn",
      args:1,
      argName:'minutes',
      'Shift all of the times so that the last end time is the specified minutes from sending start. (Incompatible with the -n --now option)');

def parsedCLI = cli.parse(args);

if(parsedCLI.arguments().isEmpty()) {
    cli.usage();
    return;
}
else if(parsedCLI.n && (parsedCLI.s instanceof String)) {
    cli.usage();
    return;
}






// the handle node method is called later as we visit each xml node in each
// file provided
tracks = [];
currentTrack = null;
needAStartEvent = false;
nodeStack = [];
def handleNode(node) {

    if(node.name.equals("name") && nodeStack.last().equals("trk")) {

        // start new track from GPX data.. 
        currentTrack = new Track();
        currentTrack.name = node.text();
        currentTrack.orderedUpdates = [];

        tracks.push(currentTrack);

        needAStartEvent = true;
    }
    else if(node.name.equals("trkpt")) {

        def body = [:];

        body["lat"] = node.attributes.get("lat");
        body["lon"] = node.attributes.get("lon");
        try {
            node.children().each{
                if(it.name().equals("ele")) {
                   body["ele"] = it.text();
               }
               else if(it.name().equals("time")) {
                   body["time"] = it.text();
               }
            }

        } catch(Throwable t) { /*NOP*/ }


        // create a track creation event if we need it
        if(needAStartEvent) {
            needAStartEvent = false;
            def newEvent = new Event();
            newEvent.eventType = START;
            newEvent.track = currentTrack;
            newEvent.updateTime =
                javax.xml.bind.DatatypeConverter.parseDateTime(body["time"]).getTimeInMillis();

            currentTrack.startTime = newEvent.updateTime;

            currentTrack.orderedUpdates.push(newEvent);
        }

        // normal track update
        def newEvent = new Event();
        newEvent.eventType = UPDATE;
        newEvent.track = currentTrack;
        newEvent.updateTime =
            javax.xml.bind.DatatypeConverter.parseDateTime(body["time"]).getTimeInMillis();
        newEvent.updateBody = body;
        currentTrack.orderedUpdates.push(newEvent);
    }

    nodeStack.push(node.name());
    node.childNodes().each(this.&handleNode);
    nodeStack.pop();
}




//loop over each of the files provided on the command line
parsedCLI.arguments().each {
    println "Parsing "+it+" ...";
    def slurper = new XmlSlurper().parse(it);

    nodeStack.push(slurper.name());
    slurper.children().each(this.&handleNode);
    nodeStack.clear();
}




//loop over the now in memory tracks to close them out and get/set min/max
//values
long firstStartTime = Long.MAX_VALUE;
long lastEndTime = -1;
long longestIndividualTrackTime = -1;
def longestIndividualTrack = null;

tracks.each { track ->

    def lastTrackLocationUpdateEvent = track.orderedUpdates.last();
    track.endTime = lastTrackLocationUpdateEvent.updateTime;
    track.originalStartTime = track.startTime;
    track.originalEndTime = track.endTime;
    track.destinationLat = lastTrackLocationUpdateEvent.updateBody["lat"];
    track.destinationLon = lastTrackLocationUpdateEvent.updateBody["lon"];


    //create a delivery event as the last event for this track
    def newEvent = new Event();
    newEvent.eventType = DELIVERY;
    newEvent.track = track;
    newEvent.updateTime = track.endTime;
    newEvent.updateBody = [ delivered:"true" ];
    track.orderedUpdates.push(newEvent);



    //find the first an last times of the entire set... we use these in the
    //optional time shifting calcs in the next loop below
    if(track.startTime < firstStartTime) {
        firstStartTime = track.startTime;
    }
    if(track.endTime > lastEndTime) {
        lastEndTime = track.endTime;
    }
    if((track.endTime - track.startTime) > longestIndividualTrackTime) {
        longestIndividualTrackTime = (track.endTime - track.startTime);
        longestIndividualTrack = track;
    }
}




//each of the events that we will provide over the REST interface are sorted
//by this heap
def eventHeap =
    new PriorityQueue<Event>(
         10000,
         new Comparator<Event>() {
             public int compare(Event e1, Event e2) {
                 e1.updateTime <=> e2.updateTime
             }
         });



Calendar calendar = Calendar.getInstance();
long shiftAmount = 0;
if(parsedCLI.s instanceof String) {
 
    double shiftToEndInMinutes = Double.valueOf(parsedCLI.s);
    long shiftToEndInMillis = shiftToEndInMinutes * 60 * 1000;

    long now = System.currentTimeMillis();
    long desiredEnd = now + shiftToEndInMillis;

    shiftAmount = desiredEnd - lastEndTime;
    calendar.setTimeInMillis(desiredEnd);
    println "Shifting last end time to "+javax.xml.bind.DatatypeConverter.printDateTime(calendar)+"....   ";
}

if(parsedCLI.n) {
    long now = System.currentTimeMillis();
    long desiredEnd = now + longestIndividualTrackTime;

    shiftAmount = desiredEnd - lastEndTime;
    println "Shifting all start times to NOW....   ";
}
else if(parsedCLI.m) {
    calendar.setTimeInMillis(longestIndividualTrack.originalStartTime);
    println "Shifting all start times to "+javax.xml.bind.DatatypeConverter.printDateTime(calendar)+"....   ";
}

double compressionMultiplier = 1.0d;
if(parsedCLI.c instanceof String) {

    double compressToMinutes = Double.valueOf(parsedCLI.c);
    long compressToMillis = compressToMinutes * 60 * 1000;
    long originalTotalTime = lastEndTime - firstStartTime;
    def startDate = new Date(firstStartTime);
    def endDate = new Date(lastEndTime);
    if(parsedCLI.n || parsedCLI.m) {
        originalTotalTime = longestIndividualTrackTime;
        startDate = new Date(longestIndividualTrack.startTime);
        endDate = new Date(longestIndividualTrack.endTime);
    }

    if(compressToMillis < originalTotalTime) {
        compressionMultiplier = ((double)compressToMillis / (double)originalTotalTime); //
        println "Compressing "+TimeCategory.minus(endDate, startDate)+" into "+compressToMinutes+" minutes...  ";
    }
}




println "Sorting all events... ";
tracks.each { track ->

    def originalTotalTime = track.originalEndTime - track.originalStartTime;
    if(parsedCLI.n || parsedCLI.m) {
        //shift first...
        def startSyncShift = longestIndividualTrack.originalStartTime - track.originalStartTime;
        track.startTime += shiftAmount + startSyncShift;

        //... then compress everything relative to the shifted START
        track.endTime = (long)(track.startTime + (originalTotalTime * compressionMultiplier));
    }
    else {
        //shift first...
        track.endTime += shiftAmount;

        //... then compress everything relative to the shifted END
        track.startTime = (long)(track.endTime - (originalTotalTime * compressionMultiplier));
    }


    for(def event : track.orderedUpdates) {

        if(parsedCLI.n || parsedCLI.m) {
            //compress everything relative to the shifted START
            def originalDiffToStart = event.updateTime - track.originalStartTime;
            event.updateTime = (long)(track.startTime + (originalDiffToStart * compressionMultiplier));
        }
        else {
            //compress everything relative to the shifted END
            def originalDiffToEnd = track.originalEndTime - event.updateTime;
            event.updateTime = (long)(track.endTime - (originalDiffToEnd * compressionMultiplier));
        }

        calendar.setTimeInMillis(event.updateTime);
        if((event.updateBody != null) && (event.updateBody["time"] != null)) {
            event.updateBody["time"] =
                javax.xml.bind.DatatypeConverter.printDateTime(calendar);
        }

        //finally drop each of the time corrected events into the event heap
        //to achieve a global sorting relative to all other track events
        eventHeap.offer(event);
    }
}




println "Starting REST Client... ";
client = new RESTClient( 'http://127.0.0.1:8080' );

println "Sending package events... ";
def event = eventHeap.poll();
while(event != null) {

    def now = System.currentTimeMillis();
    if(event.updateTime > now) {

        def sleepTime = event.updateTime - now;

        if(sleepTime > 300000) {
            def startDate = new Date(now);
            def endDate = new Date(event.updateTime);
            println "Sleeping "+TimeCategory.minus(endDate, startDate)+" until next event to be sent...";
        }

        Thread.sleep(sleepTime);
    }



    if(event.eventType == START) {

        event.track.uuid = UUID.randomUUID().toString();

        // start new track from the GPX data.. 
        def resp = client.get( path : "/tracknewpackage",
                               query: [ name:event.track.name, destinationLat:event.track.destinationLat, destinationLon:event.track.destinationLon, uuid:event.track.uuid ],
                               requestContentType : groovyx.net.http.ContentType.JSON );
        assert resp.status == 200;
        println "Package \""+event.track.name+"\" with destination lat:"+event.track.destinationLat+" lon:"+event.track.destinationLon+"  linked to tracking unit UUID: "+resp.data.ackUUID;
    }
    else {
        // existing track update
        def resp = client.post( path : "/packagetrackupdate/"+event.track.uuid,
                                body : event.updateBody,
                                requestContentType : groovyx.net.http.ContentType.JSON );
        assert resp.status == 200;

        if(event.eventType == DELIVERY) {
            println "Package "+event.track.name+":  DELIVERED";
        }
    }

    event = eventHeap.poll();
}






