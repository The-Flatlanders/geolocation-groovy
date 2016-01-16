
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
	public static double getDistance(Coordinate Coord1,Coordinate Coord2){
		int R = 6371000; // radius of the earth (in meters)
		double l1 = Math.toRadians(Coord1.lat);
		double l2 = Math.toRadians(Coord2.lat);
		double dlat = Math.toRadians(Coord2.lat-Coord1.lat);
		double dlon = Math.toRadians(Coord2.lon-Coord1.lon);
		
		double a = Math.sin(dlat/2) * Math.sin(dlon/2) +
				Math.cos(l1) * Math.cos(l2) *
				Math.sin(dlon/2) * Math.sin(dlon/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		
		double d = R * c;
		return d;
	}
}

