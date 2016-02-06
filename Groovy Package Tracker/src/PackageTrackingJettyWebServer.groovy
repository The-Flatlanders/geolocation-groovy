@Grab(group='org.eclipse.jetty.aggregate', module='jetty-all', version='7.6.15.v20140411')
import org.eclipse.jetty.server.*
import org.eclipse.jetty.servlet.*
import groovy.json.*
import javax.servlet.http.*
import javax.servlet.*

/**
 * Creates a server that manages packages<br>
 * Current functionality:<br>
 *  Add new packages to the database (doGet)<br>
 *	Update package information (doPost)<br>
 *	User login (doPost)<br>
 *	Return user's associated packages (doPost)<br>
 *	Associate package with user (doPost)<br>
 */
class SimpleGroovyServlet extends HttpServlet {
	HashMap trackedIDs=new HashMap()
	HashMap<String, String> authorization = new HashMap<String, String>() //For checking username to password
	HashMap<String, String> adminAuthorization=new HashMap<String,String>() //TODO
	HashMap<String, HashSet<TrackablePackage>> userOpenedPackages = new HashMap<>() //For associating packages to users
	long updateCount = 0
	long lastPrintCount = 0 //TODO: Is this stuff thread safe?


	/**
	 * Handles server doGet requests<br>
	 * Accepts path info types: /tracknewpackage 
	 */
	void doGet(HttpServletRequest req, HttpServletResponse resp){
		//TODO: Split this up like doPost
		if(req.getPathInfo().equals("/tracknewpackage")) {
			def uuids = req.getParameterMap().get("uuid")
			def responseString = "{ \"ackUUID\":\""+uuids+"\" }"
			println(responseString);
			double lat=Double.parseDouble(req.getParameterMap().get("destinationLat")[0])
			double lon=Double.parseDouble(req.getParameterMap().get("destinationLon")[0])
			trackedIDs.putAt(uuids[0],new TrackablePackage(uuids[0], new Coordinate(lat,lon)))
			resp.setContentType("application/json")
			def writer = resp.getWriter()
			writer.print(responseString)
			writer.flush()
		}
		if(req.getPathInfo().equals("/logout")){
			def info = req.getCookies()
			def cookie = info[0]
			cookie.setValue(null);
			cookie.setMaxAge(0);
			resp.addCookie(cookie);
		}
	}


	/**
	 * Returns all of the text from a given file 
	 * @param The path of the file to return text from
	 * @return The text from a given file
	 */
	private String returnText(String path){
		def scanner = new Scanner( new File(path))
		String text = scanner.useDelimiter("\\A").next()
		scanner.close()
		return text
	}


	/**
	 * Handles server doPost request<br>
	 * Accepts path info types: /login, /packages, /addPackage/.*, /packagetrackupdate/.*
	 */
	void doPost(HttpServletRequest req, HttpServletResponse resp) {
		//println(req.getPathInfo());
		if(req.getPathInfo().equals("/login")){
			userLogin(req,resp)
		}
		if(req.getPathInfo().equals("/packages")){
			getPackages(req,resp)
		}

		if(req.getPathInfo().startsWith("/addPackage")) {
			addPackage(req,resp)
		}

		if(req.getPathInfo().startsWith("/packagetrackupdate/")) {
			packageTrackUpdate(req,resp)
		}
	}


	/**
	 * Authorizes the user, and, if confirmed, adds a cookie of the user's username
	 * <br>Prints out to resp an error message in different cases:
	 * <br>"mismatch" without the quotation marks if the password is wrong
	 * <br>"blankfield" without the quotation marks if username or password is an empty string
	 * @param req The server request, contains username and password
	 * @param resp The server response, contains new cookie or response detailed above
	 */
	void userLogin(HttpServletRequest req, HttpServletResponse resp){
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
	 * @param resp The server response, contains JSON of the packages
	 */
	void getPackages(HttpServletRequest req, HttpServletResponse resp){
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
		
		//Removes packages with no updates yet
		ArrayList<TrackablePackage> packagesToRemove = new ArrayList<TrackablePackage>()
		for(TrackablePackage pack: packageInfos){
			if(pack.getNumOfUpdates() == 0){
				packagesToRemove.add(pack)
				println(pack)
			}
		}
		for(TrackablePackage pack : packagesToRemove){
			packageInfos.remove(pack);
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
	void addPackage(HttpServletRequest req, HttpServletResponse resp){

		def info = req.getCookies()
		def username
		try{
			info[0].getValue()
		}
		catch(ArrayIndexOutOfBoundsException e){
			return;
		}

		def packageInfos = userOpenedPackages.get(username)
		def uuid = req.getParameter("uuid") //Not sure if this will work. If errors, look here
		if(trackedIDs.containsKey(uuid)){
			packageInfos.add((trackedIDs.get(uuid)))
		}

		getPackages(req, resp) //Return all user packages
	}


	/**
	 * Updates a specific package's location and delivery status. 
	 * @param req The server request, contains package update information
	 * @param resp The server response
	 */
	void packageTrackUpdate(HttpServletRequest req, HttpServletResponse resp){
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
					//println "eta: "+currentPackage.getETA()+" hours"
					//println currentPackage.getSpeed()+" meters per second"
				}
			}
			updateCount++
			if((updateCount - lastPrintCount) >= 1000) {
				//println "packagetrackupdate count: "+updateCount
				lastPrintCount = updateCount
			}
		} catch (Exception e) { e.printStackTrace() /*report an error*/ }
	}


}

//Starts the server on port 8000
def server = new Server(8000)
ServletHandler handler = new ServletHandler()
server.setHandler(handler)
handler.addServletWithMapping(SimpleGroovyServlet.class, "/*") //TODO: Figure out what this line does
println "Starting Jetty, press Ctrl+C to stop."
server.start()
server.join()
