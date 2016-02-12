import java.text.SimpleDateFormat
import groovy.json.*;
/**
 *Represents a single package and all of its information  
 *
 */
class TrackablePackage {
	// to hide a field from the JSON ouptut, make its visibility public instead of private.
	private String uuid;
	private String notes;
	private Coordinate location;
	private Coordinate destination;
	private double averageSpeed;
	private double eta;
	private Coordinate startingLocation;
	private int time;
	private int startTime;
	private boolean delivered;
	private double numOfUpdates;
	private double distanceTraveledSoFar;
	private ArrayList<Coordinate> pastCords;
		
	public synchronized ArrayList<Coordinate> getPastCords() {
		return pastCords;
	}
	public TrackablePackage(String uuid, Coordinate destination){
		time=0;
		this.uuid=uuid;
		this.destination=destination;
		location=new Coordinate(1,1);
		delivered = false;
		numOfUpdates = 0;
		distanceTraveledSoFar = 0;
		pastCords = new ArrayList<Coordinate>();
	}
	public synchronized Coordinate getLocation() {
		return location;
	}
	public synchronized void setLocation(Coordinate location) {
		this.location = location;
	}
	public synchronized String getTime() {
		return time;
	}
	public synchronized String getUuid() {
		return uuid;
	}
	public synchronized String getNotes() {
		return notes;
	}
	public synchronized void setNotes(String notes) {
		this.notes = notes;
	}
	public synchronized boolean getDelivered() {
		return delivered;
	}
	public synchronized void setDelivered(boolean delivered) {
		this.delivered = delivered;
	}
	public synchronized Coordinate getDestination() {
		return destination;
	}
	public synchronized double getDistanceFromDestination(){
		return Coordinate.getDistance(location, destination)
	}
	public synchronized int getETAInSeconds(){
		//println "distance from destination: "+getDistanceFromDestination();
		return (getDistanceFromDestination()/averageSpeed) * (getDistanceTraveledSoFar() / calculateLineDistance())
	}
	public synchronized double geteta(){
		int ns= getETAInSeconds()
		return ns/3600;
	}
	public synchronized void update(Coordinate updateLocation, String updateTime){
		pastCords.add(updateLocation);
		if(!delivered){
			numOfUpdates++;
			updateTime=updateTime.replace("-06:00","")
			SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
			Date date=format.parse(updateTime);
			int newTime=date.getTime();
			
			if(startingLocation==null){
				startingLocation=updateLocation
				location=updateLocation
				startTime=newTime;
			}else{
				distanceTraveledSoFar += Coordinate.getDistance(location, updateLocation);
				averageSpeed=calculateAverageSpeed(newTime-startTime,Coordinate.getDistance(startingLocation, updateLocation))
				time=newTime;
				location=updateLocation
				eta=geteta();
			}
		}
	}
	public synchronized double calculateInstantaneousSpeed(double t1,double t2,double distance){
		double currentSpeed;
		double dt=t2-t1
		if(dt>0){
			currentSpeed= (1000*distance)/dt
		}else{
			currentSpeed= 0
		}
		println "moved "+distance+" meters in "+dt+" seconds. Speed="+currentSpeed;
	}
	public synchronized double calculateAverageSpeed(double time,double distance){
		double averageSpeed;
		if(time>0){
			averageSpeed=(1000)*distance/time
		}else{
			averageSpeed=0;
		}
		//println "moved "+distance+" meters in "+time+" milliseconds. Speed="+averageSpeed+"m/s"
		return averageSpeed;

	}
	public synchronized double getSpeed(){
		return averageSpeed;
	}
	public synchronized int getNumOfUpdates(){
		return numOfUpdates;
	}
	public synchronized String toString(){
		String delivered = delivered? "Package is delivered" : "Package is not delivered";
		return ("Package: "+uuid+"\nDestination: "+destination+"\nCurrent Location: "+location + "\n" + delivered+"\nETA"+geteta()+"\nNumber of updates: "+numOfUpdates);
	}
	public synchronized double getDistanceTraveledSoFar(){
		return distanceTraveledSoFar;
	}
	public synchronized double calculateLineDistance(){
		return Coordinate.getDistance(startingLocation, location);
	}
	public synchronized Coordinate getStartingLocation(){
		return startingLocation;
	}

}
