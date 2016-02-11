@Grab(group='org.eclipse.jetty.aggregate', module='jetty-all', version='7.6.15.v20140411')
import org.eclipse.jetty.server.*
import org.eclipse.jetty.servlet.*
import groovy.json.*
import javax.servlet.http.*
import javax.servlet.*

/**
 * Creates a server that manages packages<br>
 * Functionality:<br>
 *  Add new packages to the database (doGet)<br>
 *	Update package information (doPost)<br>
 *	User login (doPost)<br>
 *	Return user's associated packages (doPost)<br>
 *	Associate package with user (doPost)<br>
 */
class SimpleGroovyServlet extends HttpServlet {
	/**
	 * Hashmap of all tracked packages and their information
	 */
	private HashMap<String, TrackablePackage> trackedIDs=new HashMap() 
	/**
	 * Hashmap of all usernames and password
	 */
	private HashMap<String, String> authorization = new HashMap<String, String>()
	private HashMap<String, String> adminAuthorization=new HashMap<String,String>() //TODO
	/**
	 * For associating packages to users
	 */
	private HashMap<String, HashSet<TrackablePackage>> userOpenedPackages = new HashMap<>() 
	
	/**
	 * Handles server doGet requests<br>
	 * Accepts path info types: /tracknewpackage , /logout, /help
	 */
	void doGet(HttpServletRequest req, HttpServletResponse resp){
		if(req.getPathInfo().equals("/tracknewpackage")) {
			trackNewPackage(req, resp);
		}
		if(req.getPathInfo().equals("/logout")){
			logout(req, resp);
		}
		if(req.getPathInfo().equals("/help")){
			help(req, resp);
		}
	}

	/**
	 * Records a new package and adds it to the package hashmap, using the packages UUID
	 * as a key and the object as the value	
	 * @param req The server request, contains new package information
	 * @param resp The server response
	 */
	private void trackNewPackage(HttpServletRequest req, HttpServletResponse resp){
		def uuids = req.getParameterMap().get("uuid")
		def responseString = "{ \"ackUUID\":\""+uuids+"\" }"
		double lat=Double.parseDouble(req.getParameterMap().get("destinationLat")[0])
		double lon=Double.parseDouble(req.getParameterMap().get("destinationLon")[0])
		println responseString;
		//Creates a new package
		trackedIDs.putAt(uuids[0],new TrackablePackage(uuids[0], new Coordinate(lat,lon)))
		resp.setContentType("application/json")
		def writer = resp.getWriter()
		writer.print(responseString)
		writer.flush()
	}

	/**
	 * Logs the user out by deleting all serverside information on the user
	 * @param req The server request, contains user cookie
	 * @param resp The server response, does not contain user cookie
	 */
	private void logout(HttpServletRequest req, HttpServletResponse resp){
		def info = req.getCookies()
		def cookie = info[0]
		cookie.setValue(null);
		cookie.setMaxAge(0);
		resp.addCookie(cookie);
	}
	
	/**
	 * Return the help document to display
	 * @param req The server request
	 * @param resp The server response, contains help document information
	 */
	private void help(HttpServletRequest req, HttpServletResponse resp){
		String helpText = returnText("Text/helpText");
		def writer = resp.getWriter()
		resp.setContentType("text/plain")
		writer.print(helpText)
	}

	/**
	 * Returns all of the text from a given file 
	 * @param The path of the file to return text from
	 * @return The text from a given file
	 */
	private String returnText(String path){
		def scanner = new Scanner(new File(path));
		String text = scanner.useDelimiter("\\A").next()
		scanner.close()
		return text
	}


	/**
	 * Handles server doPost request
	 * Accepts path info types: /login, /packages, /addPackage/.*, /packagetrackupdate/.*, /updateNotes/.*
	 */
	void doPost(HttpServletRequest req, HttpServletResponse resp) {
		if(req.getPathInfo().equals("/login")){
			userLogin(req,resp)
		}
		if(req.getPathInfo().equals("/packages")){
			getPackages(req,resp)
		}

		if(req.getPathInfo().startsWith("/addPackage")) {
			addPackage(req,resp)
		}

		if(req.getPathInfo().startsWith("/packagetrackupdate")) {
			packageTrackUpdate(req,resp)
		}
		
		if(req.getPathInfo().startsWith("/updateNotes")) {
			updateNotes(req,resp)
		}
	}

	/**
	 * Authorizes the user, and, if confirmed, adds a cookie of the user's username
	 * <br>Prints out to resp an error message in different cases:
	 * @param req The server request, contains username and password
	 * @param resp The server response, contains new cookie or a response that is either the string "mismatch" or the string "blankfield"
	 */
	private void userLogin(HttpServletRequest req, HttpServletResponse resp){
		//Gets the username and password from the request
		//Uses cookies to score username
		String username = req.getParameter("username")
		String password = req.getParameter("password")
		def writer = resp.getWriter()
		resp.setContentType("text/plain")
		//Checks for a mismatch
		if((authorization.containsKey(username) && authorization.get(username) != password)){
			writer.print("mismatch")
			return
		}
		//Checks for a null field
		else if(username == "" || password == ""){
			writer.print("blankfield")
		}
		else{
			authorization.put(username, password)
			
			Cookie user = new Cookie("username", username)
			resp.addCookie(user)
		}

	}

	/**
	 * Sends back the user's associated packages in the response
	 * @param req The server request, contains user cookie
	 * @param resp The server response, contains JSON of the packages or the string "noUser" when there is not a current username cookie
	 */
	private void getPackages(HttpServletRequest req, HttpServletResponse resp){
		def writer = resp.getWriter()
		resp.setContentType("application/json")
		def username
		try{
			def info = req.getCookies()
			username = info[0].getValue()
		}
		catch(ArrayIndexOutOfBoundsException e){
			writer.print("noUser");
			return;
		}

		//Creates list of packages either entered by the user previously or now
		HashSet<TrackablePackage> packageInfos
		if(userOpenedPackages.containsKey(username)){
			packageInfos = userOpenedPackages.get(username)
		}
		else{
			packageInfos = new HashSet<TrackablePackage>()
			userOpenedPackages.put(username, packageInfos)
		}
		if(username.equals("admin")){
			packageInfos = trackedIDs.values()
		}

		//Returns packages
		def toJson = JsonOutput.toJson(packageInfos)
		writer.print(toJson)
		writer.flush()
	}

	/**
	 * Associates packages with a user to be displayed later
	 * @param req The server request, contains new package UUIDs to add and user cookie
	 * @param resp The server response, contains all packages asociated with user cookie, including new one
	 */
	private void addPackage(HttpServletRequest req, HttpServletResponse resp){
		def info = req.getCookies()
		def username
		try{
			username=info[0].getValue()
		}
		catch(ArrayIndexOutOfBoundsException e){
			return;
		}
		HashSet<TrackablePackage> packageInfos
		if(userOpenedPackages.containsKey(username)){
			packageInfos = userOpenedPackages.get(username)
		}
		else{
			packageInfos=new HashSet<TrackablePackage>();
			userOpenedPackages.put(username,packageInfos);
		}
		def uuid = req.getParameter("uuid") //Not sure if this will work. If errors, look here
		if(trackedIDs.containsKey(uuid)){
			packageInfos.add(trackedIDs.get(uuid))
		}
	}
	
	/**
	 * Associates packages with a user to be displayed later
	 * @param req The server request, contains new package UUIDs to add and user cookie
	 * @param resp The server response, contains all packages asociated with user cookie, including new one
	 */
	private void updateNotes(HttpServletRequest req, HttpServletResponse resp){
		def uuid = req.getParameter("uuid")
		def notes = req.getParameter("notes")
		def currentPackage = trackedIDs.get(uuid);
		currentPackage.setNotes(notes);
	}

	/**
	 * Updates a specific package's location and delivery status. 
	 * @param req The server request, contains package update information
	 * @param resp The server response
	 */
	private void packageTrackUpdate(HttpServletRequest req, HttpServletResponse resp){
		try {
			BufferedReader reader = req.getReader()
			String line = null
			while ((line = reader.readLine()) != null) {
				def slurper=new JsonSlurper()
				def inf=slurper.parseText(line)
				def uuid = req.getPathInfo().replace("/packagetrackupdate/","")
				TrackablePackage currentPackage = trackedIDs.get(uuid)
				if(line.contains("delivered")) {
					//This code registers delivery events
					println uuid +" -> "+ line
					currentPackage.setDelivered(true)
				}
				else{
					//This code tracks all non delivery events
					currentPackage.update(new Coordinate(Double.parseDouble(inf.lat),Double.parseDouble(inf.lon),Double.parseDouble(inf.ele)),inf.time)					
				}
			}
		} catch (Exception e) { e.printStackTrace() /*report an error*/ }
	}


}

//Starts the server on port 8000
def server = new Server(8000)
ServletHandler handler = new ServletHandler()
server.setHandler(handler)
handler.addServletWithMapping(SimpleGroovyServlet.class, "/*")
println "Starting Jetty, press Ctrl+C to stop."
server.start()
server.join()
