
public class Coordinate {
	public double lon;
	public double lat;
	public Coordinate(double lat, double lon){
		this.lat=lat;
		this.lon=lon;
	}
	public String toString(){
		return "{"+lat+","+lon+"}";
	}
}

