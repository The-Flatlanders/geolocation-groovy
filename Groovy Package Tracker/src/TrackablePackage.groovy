import java.text.SimpleDateFormat
import groovy.json.*;
/**
 *Represents a single package and all of its information.  
 *Contains synchronized methods because frequently the information from one package is accessed from many different threads. 
 *Many method return or use 
 */
class TrackablePackage {
	private final String uuid;
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

	/**
	 * Constructs a new package object with given UUID and destination
	 * @param uuid The UUID of the new package
	 * @param destination The destination of the new package, a {@link Coordinate} object
	 */
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

	/**
	 * Updates all of this packages fields with the new update's information
	 * @param updateLocation The {@link Coordinate} representing the packages current, updated position
	 * @param updateTime The time of the update
	 */
	public synchronized void update(Coordinate updateLocation, String updateTime){
		pastCords.add(updateLocation);
		
		if(!delivered){
			numOfUpdates++;
			updateTime = updateTime.replace("-06:00","")
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
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

	/**
	 * Calculates the average speed over an amount of time
	 * @param time The time interval
	 * @param distance The distance interval
	 * @return The distance
	 */
	public synchronized double calculateAverageSpeed(double time,double distance){
		double averageSpeed;
		if(time>0){
			averageSpeed=(1000)*distance/time
		}else{
			averageSpeed=0;
		}
		return averageSpeed;

	}

	/**
	 * Returns the distance traveled as exactly as possible by summing the distance between each GPS update
	 * @return A double representing the distance traveled
	 */
	public synchronized double getDistanceTraveledSoFar(){
		return distanceTraveledSoFar;
	}

	/**
	 * Returns the straight line distance to this packages starting location
	 * @return A double representing the distance in miles between this packages starting and current locations, traveled in a straight line. "As
	 * the crow flies," the saying goes.
	 */
	public synchronized double calculateLineDistance(){
		return Coordinate.getDistance(startingLocation, location);
	}

	/**
	 * Returns the estimated time of arrival in seconds. 
	 * It finds the distance to destination and divides by the average speed. Then, it multiplies by a ratio that is the 
	 * average deviation from the straight line path to the starting location.
	 * @return The ETA
	 */
	public synchronized int getETAInSeconds(){
		return (getDistanceFromDestination()/averageSpeed) * (getDistanceTraveledSoFar() / calculateLineDistance())
	}

	/**
	 * Returns the ETA in seconds
	 * @return The ETA
	 */
	public synchronized double geteta(){
		int ns= getETAInSeconds()
		return ns/3600;
	}

	public synchronized Coordinate getLocation() {
		return location;
	}
	
	public synchronized double getSpeed(){
		return averageSpeed;
	}

	public synchronized int getNumOfUpdates(){
		return numOfUpdates;
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

	public synchronized Coordinate getStartingLocation(){
		return startingLocation;
	}

	/**
	 * Returns the geographical path of the package
	 * @return A list of {@link Coordinate} objects representing the packages previous locations
	 */
	public synchronized ArrayList<Coordinate> getPastCords() {
		return pastCords;
	}

	
	@Override
	public synchronized String toString(){
		String delivered = delivered? "Package is delivered" : "Package is not delivered";
		return ("Package: "+uuid+"\nDestination: "+destination+"\nCurrent Location: "+location + "\n" + delivered+"\nETA"+geteta()+"\nNumber of updates: "+numOfUpdates);
	}
	
}
