@Grab(group='org.eclipse.jetty.aggregate', module='jetty-all', version='7.6.15.v20140411')
import org.eclipse.jetty.server.*
import org.eclipse.jetty.servlet.*
import groovy.json.JsonSlurper
import javax.servlet.http.*
import javax.servlet.*


class SimpleGroovyServlet extends HttpServlet {
	HashMap trackedIDs=new HashMap();
	long updateCount = 0;
	long lastPrintCount = 0;
	void doGet(HttpServletRequest req, HttpServletResponse resp) {
		// println "GET  "+req.getRequestURL()+"   query string:"+req.getQueryString();
		def uuids = req.getParameterMap().get("uuid")
		
		if(req.getPathInfo().equals("/tracknewpackage")) {
			def responseString = "{ \"ackUUID\":\""+uuids+"\" }"
			double lat=Double.parseDouble(req.getParameterMap().get("destinationLat")[0])
			double lon=Double.parseDouble(req.getParameterMap().get("destinationLon")[0])
			trackedIDs.putAt(uuids[0],new TrackablePackage(uuids[0], new Coordinate(lat,lon)));
			resp.setContentType("application/json");
			def writer = resp.getWriter();
			writer.print(responseString);
			writer.flush();
			println "\t\t  "+responseString;
		}
		
		def packageInfos=new ArrayList()
		if(req.getPathInfo().equals("/trackPackages")){
			for(String id:uuids){
				if(trackedIDs.containsKey(id)){
					packageInfos.add((trackedIDs.get(id,null)))
				}
			}
			def writer = resp.getWriter();
			for(int x=0;x<packageInfos.size();x++){
				writer.print(packageInfos.get(x))
				writer.print("\n\n");
			}
			writer.flush();
		}

	}


	void doPost(HttpServletRequest req, HttpServletResponse resp) {
		//        println "POST: "+req.getRequestURL();

		if(req.getPathInfo().startsWith("/packagetrackupdate/")) {

			try {
				BufferedReader reader = req.getReader();
				String line = null;
				while ((line = reader.readLine()) != null) {
				
					def slurper=new JsonSlurper()
					def inf=slurper.parseText(line);
					def uuid = req.getPathInfo().replace("/packagetrackupdate/","");
					def currentPackage = trackedIDs.get(uuid);
					if(line.contains("delivered")) {
						//This code registers delivery events
						println uuid +" -> "+ line;
						currentPackage.setDelivered(true);
					}
					else{
						//This code tracks all non delivery events
						currentPackage.setLocation(new Coordinate(Double.parseDouble(inf.lat),Double.parseDouble(inf.lon)))
						println req.getPathInfo()+" -> "+line; //Comment out if you only want to print the delivered updates
					}
				}

				updateCount++;
				if((updateCount - lastPrintCount) >= 1000) {
					println "packagetrackupdate count: "+updateCount;
					lastPrintCount = updateCount;
				}

			} catch (Exception e) { e.printStackTrace(); /*report an error*/ }
		}
	}

}

def server = new Server(8080);
ServletHandler handler = new ServletHandler();
server.setHandler(handler);
handler.addServletWithMapping(SimpleGroovyServlet.class, "/*");
println "Starting Jetty, press Ctrl+C to stop."
server.start()
server.join();
