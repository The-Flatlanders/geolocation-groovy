
class TrackablePackage {

	Coordinate location;
	Coordinate destination;
	int elevation;
	int speed;
	int time;
	String uuid;
	boolean delivered;
	public TrackablePackage(String uuid, Coordinate destination){
		this.uuid=uuid;
		this.destination=destination;
		delivered = false;
	}
	public Coordinate getLocation() {
		return location;
	}
	public void setLocation(Coordinate location) {
		this.location = location;
	}
	public int getElevation() {
		return elevation;
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
		return speed*getDistanceFromDestination()
	}
	public String getETA(){
		return get
	}
	public static String convertSecondsToTime(int nSeconds){
		double h,m,s,mil;
			mil = nSeconds % 1000;
			s = nSeconds/1000;
			m = s/60;
			h = m/60;
			s = s % 60;
			m = m % 60;
			h = h % 24;
			return ((int)h < 10 ? "0"+String.valueOf((int)h) : String.valueOf((int)h))+":"+((int)m < 10 ? "0"+String.valueOf((int)m) : String.valueOf((int)m))
					+":"+((int)s < 10 ? "0"+String.valueOf((int)s) : String.valueOf((int)s))
					+":"+((int)mil > 100 ? String.valueOf((int)mil) : (int)mil > 9 ? "0"+String.valueOf((int)mil) : "00"+String.valueOf((int)mil));
	}
	public void update(Coordinate updateLocation, int updateTime){
		def distanceTravelled=Coordinate.getDistance(location, updateLocation)
		def timeElapsed=updateTime-currentTime
		speed=distanceTraveled/timeElapsed
	}
	public String toString(){
		String delivered = delivered? "Package is delivered" : "Package is not delivered";
		return ("Package: "+uuid+"\nDestination: "+destination+"\nCurrent Location: "+location + "\n" + delivered+"\nETA"+getETA);
	}
}
