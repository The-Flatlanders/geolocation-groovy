
class TrackablePackage {

	Coordinate location;
	Coordinate destination;
	int elevation;
	String time;
	String uuid;
	public TrackablePackage(String uuid, Coordinate destination){
		this.uuid=uuid;
		this.destination=destination;
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
	public void setTime(String time) {
		this.time = time;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public Coordinate getDestination() {
		return destination;
	}
	public String toString(){
		return ("Package "+uuid+" Destination: "+destination+" Current Location: "+location);
	}
}
