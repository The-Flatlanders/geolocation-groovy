public class Coordinate {
	private double lon;
	private double lat;
	final public double ELE;
	public Coordinate(double lat, double lon, double ELE){
		this.lat=lat;
		this.lon=lon;
		this.ELE=ELE;
	}
	public Coordinate(double lat,double lon){
		this(lat,lon,0.0);
	}
	public double getlon(){
		return lon;
	}
	public double getlat(){
		return lat;
	}
	public String toString(){
		return "{"+lat+","+lon+","+ELE+"}";
	}
	public static double getDistance(Coordinate Coord1,Coordinate Coord2){
		final int R = 6371; // Radius of the earth
		double lat1=Coord1.lat;
		double lat2=Coord2.lat;
		double lon1=Coord1.lon;
		double lon2=Coord2.lon;
		double ELE1=Coord1.ELE;
		double ELE2=Coord2.ELE;
		double latDistance = Math.toRadians(lat2 - lat1);
		double lonDistance = Math.toRadians(lon2 - lon1);
		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))* Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = R * c * 1000; // convert to meters
		double height = ELE1 - ELE2;
		distance = Math.pow(distance, 2) + Math.pow(height, 2);
		return Math.sqrt(distance);
	}
	public static Coordinate midPoint(Coordinate Coord1,Coordinate Coord2){
		double lat1=Coord1.lat;
		double lat2=Coord2.lat;
		double lon1=Coord1.lon;
		double lon2=Coord2.lon;
		double dLon = Math.toRadians(lon2 - lon1);

		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		lon1 = Math.toRadians(lon1);

		double Bx = Math.cos(lat2) * Math.cos(dLon);
		double By = Math.cos(lat2) * Math.sin(dLon);
		double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
		double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);
		return new Coordinate(Math.toDegrees(lat3),Math.toDegrees(lon3))
	}
}

