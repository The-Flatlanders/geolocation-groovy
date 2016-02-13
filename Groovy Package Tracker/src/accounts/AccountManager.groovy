package accounts
import java.util.HashMap
import groovy.json.*

/**
 *Manages {@link UserAccount} objects, saving them in case of a crash and manipulating them in normal usage.
 *See public methods for more information.
 *
 */
class AccountManager {

	/**
	 *{@link UserAccount}s persist as JSON on server crash in this file
	 */
	public static final String accountsBackupPath="User Accounts List.map"
	/**
	 * Array of all {@link UserAccount} objects currently tracked
	 * @return
	 */
	static HashMap<String,UserAccount> accounts


	/**
	 * Run this method to add admin accounts or when restarting the server from a crash
	 */
	public static void main(String[] args){
		initAccounts()
		addUserAccountFromConsole()
		backUpAccountList()
	}

	/**
	 * Initializes the accounts stored in this object by resetting the accounts object and getting all accounts from the text file
	 */
	public static void initAccounts(){
		accounts=new HashMap<String,UserAccount>()
		restoreAccountsFromFile()
	}

	/**
	 * Writes out all current accounts to the accounts text file
	 */
	public static void backUpAccountList(){
		PrintWriter out = new PrintWriter(accountsBackupPath)
		out.println(JsonOutput.toJson(accounts))
		out.close()
	}

	/**
	 * Restores the accounts stored in this object
	 */
	public static void restoreAccountsFromFile(){
		Scanner scanner = new Scanner( new File(accountsBackupPath) )
		String text = scanner.useDelimiter("\\A").next()
		scanner.close()
		def jsonSlurper = new JsonSlurper()
		def oldAccounts = jsonSlurper.parseText(text)
		for(Object user:oldAccounts.values()){
			addUserAccount(user.username,user.password,user.admin)
		}
	}

	/**
	 * Makes a new user account by prompting the user in the console
	 */
	private static void addUserAccountFromConsole(){
		try{
			Scanner input=new Scanner(System.in)
			println "Create a new account:\n username:"
			String username=input.nextLine()
			println "password:"
			String password=input.nextLine()
			println "Admin account? (1=yes,0=no)"
			boolean isAdmin=input.nextInt()==1
			UserAccount acc=addUserAccount(username,password,isAdmin)
			if(acc==null){
				println "Failed to create account. Username "+username+" already exists."
			}else{
				println "User Account successfully created:\n"+acc
			}
			println "Create another account? (1=yes,0=no)"
			if(input.nextInt()==1){
				addUserAccountFromConsole()
			}
		}
		catch(Exception e){
			println("Procces aborted")
			//Input was bad
		}
	}

	/**
	 * Makes a new account and adds it to the user accounts file
	 * @param username Username of new account
	 * @param password Password of new account
	 * @param isAdmin Whether the new user is an admin or not
	 * @return
	 */
	public static UserAccount addUserAccount(String username,String password,boolean isAdmin){
		if(!accounts.containsKey(username)){
			UserAccount acc=new UserAccount(username,password,isAdmin)
			accounts.put(username,acc)
			return acc
		}else{
			return null
		}
	}

	
	/**
	 * Use this to get all user accounts
	 */
	public static HashMap<String,UserAccount> getAccounts(){
		return accounts
	}

}
