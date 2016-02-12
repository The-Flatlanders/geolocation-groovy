
class UserAccount {
	public static final String accountsBackupPath="User Accounts List.map";
	static HashMap<String,UserAccount> accounts;
	private String username;
	private String password;
	private HashSet<TrackablePackage> myTrackedPackages;
	private boolean isAdmin;
	public UserAccount(String username,String password,boolean isAdmin){
		this.username=username;
		this.password=password;
		this.isAdmin=isAdmin;
		myTrackedPackages=new HashSet<TrackablePackage>();
		if(accounts==null)accounts=new HashMap<String,UserAccount>();
		accounts.put(username,this);
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
	private void resetMyTrackedPackages(){
		myTrackedPackages=new HashSet<TrackablePackage>();
	}
	public String toString(){
		return "\tUsername:"+username+"\n\tPassword: "+password+"\n\tAdmin rights:"+(isAdmin?"yes":"no")+"\n\tCurrently Tracked Packages: "+myTrackedPackages
	}
	public static void backUpAccountList(){
		FileOutputStream fout = new FileOutputStream(accountsBackupPath);
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(accounts);
	}
	public static void restoreAccountsFromFile(){
		try{
			FileInputStream fin = new FileInputStream(accountsBackupPath);
			ObjectInputStream ois = new ObjectInputStream(fin);
			accounts=new HashMap<String,UserAccount>();
			accounts = (HashMap) ois.readObject();
			ois.close();
			println(accounts);
		}catch(Exception ex){
		}
	}
	public static  void resetUserTrackedPackages(){
		for(UserAccount user:accounts){
			user.resetMyTrackedPackages();
		}
	}
	public static void addUserAccountFromConsole(){
		Scanner input=new Scanner(System.in);
		println "Create a new account:\n username:"
		String username=input.nextLine();
		println "password:"
		String password=input.nextLine();
		println "Admin account? (1=yes,0=no)"
		boolean isAdmin=input.nextInt()==1;
		UserAccount acc=addUserAccount(username,password,isAdmin);
		if(acc==null){
			println "Failed to create account. Username "+username+" already exists."
		}else{
			println "User Account successfully created:\n"+acc;
		}
		println "Create another account? (1=yes,0=no)"
		if(input.nextInt()==1){
			addUserAccountFromConsole();
		}
	}
	public static void main(String[] args){
		restoreAccountsFromFile();
		resetUserTrackedPackages();
		UserAccount.addUserAccountFromConsole()
		backUpAccountList();
	}
	public static UserAccount addUserAccount(String username,String password,boolean isAdmin){
		println username
		if(!accounts.containsKey(username)){
			UserAccount acc=new UserAccount(username,password,isAdmin);
			accounts.put(username,acc);
			return acc;
		}else{
			return null
		}
	}
}
