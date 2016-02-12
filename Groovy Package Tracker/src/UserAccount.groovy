import groovy.json.*

class UserAccount{
	private String username;
	private String password;
	private HashSet<TrackablePackage> myTrackedPackages;
	private boolean isAdmin;
	public UserAccount(String username,String password,boolean isAdmin){
		this.username=username;
		this.password=password;
		this.isAdmin=isAdmin;
		myTrackedPackages=new HashSet<TrackablePackage>();
	}
	public void addPackage(TrackablePackage myPackage){
		myTrackedPackages.put(myPackage);
	}
	public HashSet<TrackablePackage> getTrackedPackages(){
		return myTrackedPackages;
	}
	public String getPassword(){
		return password;
	}
	public String getUsername(){
		return username;
	}
	public void setPassword(String password){
		this.password=password;
	}
	public boolean isAdmin(){
		return isAdmin;
	}
	public String toString(){
		return "\tUsername:"+username+"\n\tPassword: "+password+"\n\tAdmin rights:"+(isAdmin?"yes":"no")+"\n\tCurrently Tracked Packages: "+myTrackedPackages
	}
	
}
