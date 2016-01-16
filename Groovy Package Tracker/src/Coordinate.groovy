
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
		double l1 = Coord1.lat.toRadians();
		double l2 = Coord2.lat.toRadians();
		double dlat = (Coord2.lat-Coord1.lat).toRadians();
		double dlon = (Coord2.lon-Coord1.lon).toRadians();
		
		double a = Math.sin(dlat/2) * Math.sin(dlon/2) +
				Math.cos(l1) * Math.cos(l2) *
				Math.sin(dlon/2) * Math.sin(dlon/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		
		double d = R * c;
		return d;
	}
}

