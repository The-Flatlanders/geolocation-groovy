package accounts
import java.util.HashMap;
import groovy.json.*;
class AccountManager {
	public static final String accountsBackupPath="User Accounts List.map";
	static HashMap<String,UserAccount> accounts;
	public static HashMap<String,UserAccount> getAccounts(){
		return accounts;
	}
	public static void backUpAccountList(){
		PrintWriter out = new PrintWriter(accountsBackupPath);
		out.println(JsonOutput.toJson(accounts));
		out.close()
	}
	public static void restoreAccountsFromFile(){
		Scanner scanner = new Scanner( new File(accountsBackupPath) );
		String text = scanner.useDelimiter("\\A").next();
		scanner.close();
		def jsonSlurper = new JsonSlurper()
		def oldAccounts = jsonSlurper.parseText(text);
		for(Object user:oldAccounts.values()){
			println user;
			addUserAccount(user.username,user.password,user.admin)
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
		initAccounts();
		addUserAccountFromConsole()
		backUpAccountList();
	}
	public static void initAccounts(){
		accounts=new HashMap<String,UserAccount>();
		restoreAccountsFromFile();
	}
	public static UserAccount addUserAccount(String username,String password,boolean isAdmin){
		if(!accounts.containsKey(username)){
			UserAccount acc=new UserAccount(username,password,isAdmin);
			accounts.put(username,acc);
			return acc;
		}else{
			return null
		}
	}
}
