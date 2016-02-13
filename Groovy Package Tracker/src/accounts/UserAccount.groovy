package accounts
import TrackablePackage;
import groovy.json.*
import geo.*;
import server.*; 

/**
 * Represents a single user account. This includes username, password, and whether the user is an admin
 */
class UserAccount{
	protected final String username;
	protected String password; //Password could conceivably be changed at some point
	protected HashSet<TrackablePackage> myTrackedPackages;
	protected final boolean isAdmin;
	
	/**
	 * Makes a new user account
	 * @param username Username of the account
	 * @param password Password of the account
	 * @param isAdmin Whether the user is an admin
	 */
	protected UserAccount(String username,String password,boolean isAdmin){
		this.username=username;
		this.password=password;
		this.isAdmin=isAdmin;
		myTrackedPackages=new HashSet<TrackablePackage>();
	}
	
	/**
	 * Makes this package visible to the user
	 */
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

