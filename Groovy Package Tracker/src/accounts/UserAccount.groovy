package accounts
import TrackablePackage;
import groovy.json.*
import geo.*;
import server.*; 
class UserAccount{
	protected String username;
	protected String password;
	protected HashSet<TrackablePackage> myTrackedPackages;
	protected boolean isAdmin;
	public UserAccount(String username,String password,boolean isAdmin){
		this.username=username;
		this.password=password;
		this.isAdmin=isAdmin;
		myTrackedPackages=new HashSet<TrackablePackage>();
	}
	public void addPackage(TrackablePackage myPackage){
		myTrackedPackages.add(myPackage);
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

