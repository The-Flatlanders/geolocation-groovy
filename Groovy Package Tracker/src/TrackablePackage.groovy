import java.text.SimpleDateFormat


class TrackablePackage {
	int startTime;
	Coordinate startingLocation;
	Coordinate location;
	Coordinate destination;
	double averageSpeed;
	int time;
	String uuid;
	boolean delivered;

	public TrackablePackage(String uuid, Coordinate destination){
		time=0;
		this.uuid=uuid;
		this.destination=destination;
		location=new Coordinate(1,1);
		delivered = false;
	}
	public Coordinate getLocation() {
		return location;
	}
	public void setLocation(Coordinate location) {
		this.location = location;
	}
	public void setElevation(int elevation) {
		this.elevation = elevation;
	}
	public String getTime() {
		return time;
	}
	public String getUuid() {
		return uuid;
	}
	public boolean getDelivered() {
		return time;
	}
	public void setDelivered(boolean delivered) {
		this.delivered = delivered;
	}
	public Coordinate getDestination() {
		return destination;
	}
	public double getDistanceFromDestination(){
		return Coordinate.getDistance(location, destination)
	}
	public int getETAInSeconds(){
		//println "distance from destination: "+getDistanceFromDestination();
		return getDistanceFromDestination()/averageSpeed
	}
	public double getETA(){
		int ns= getETAInSeconds()
		return ns/3600;
	}
	public void update(Coordinate updateLocation, String updateTime){
		if(!delivered){
			updateTime=updateTime.replace("-06:00","")
			SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
			Date date=format.parse(updateTime);
			int newTime=date.getTime();
			if(startingLocation==null){
				startingLocation=updateLocation
				startTime=newTime;
			}else{
				averageSpeed=calculateAverageSpeed(newTime-startTime,Coordinate.getDistance(startingLocation, updateLocation))
				time=newTime;
				location=updateLocation
			}
		}
	}
	public double calculateInstantaneousSpeed(double t1,double t2,double distance){
		double currentSpeed;
		double dt=t2-t1
		if(dt>0){
			currentSpeed= (1000*distance)/dt
		}else{
			currentSpeed= 0
		}
		println "moved "+distance+" meters in "+dt+" seconds. Speed="+currentSpeed;
	}
	public double calculateAverageSpeed(double time,double distance){
		double averageSpeed;
		if(time>0){
			averageSpeed=(1000)*distance/time
		}else{
			averageSpeed=0;
		}
		//println "moved "+distance+" meters in "+time+" milliseconds. Speed="+averageSpeed+"m/s"
		return averageSpeed;

	}
	public double getSpeed(){
		return averageSpeed;
	}
	public String toString(){
		String delivered = delivered? "Package is delivered" : "Package is not delivered";
		return ("Package: "+uuid+"\nDestination: "+destination+"\nCurrent Location: "+location + "\n" + delivered+"\nETA"+getETA);
	}
}
