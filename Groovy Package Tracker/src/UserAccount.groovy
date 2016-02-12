
class UserAccount {
	static HashMap<String,UserAccount> accounts;
	private String username;
	private String password;
	private HashSet<TrackablePackage> myTrackedPackages;
	private boolean isAdmin;
	public UserAccount(String username,String password){
		this.username=username;
		this.password=password;
		isAdmin=false;
		myTrackedPackages=new HashSet<TrackablePackage>();
		if(accounts==null)accounts=new HashMap<String,UserAccount>();
		accounts.put(username,this);
	}
	public UserAccount(String username,String password,boolean isAdmin){
		this(username,password);
		this.isAdmin=isAdmin;
	}
	public static HashMap<String,UserAccount> getAccounts(){
		return accounts;
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
}
