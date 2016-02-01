@Grab(group='org.eclipse.jetty.aggregate', module='jetty-all', version='7.6.15.v20140411')
import org.eclipse.jetty.server.*
import org.eclipse.jetty.servlet.*
import groovy.json.*
import javax.servlet.http.*
import javax.servlet.*

class SimpleGroovyServlet extends HttpServlet {
	HashMap trackedIDs=new HashMap()
	HashMap<String, String> authorization = new HashMap<String, String>() //For checking username to password
	HashMap<String, String> adminAuthorization=new HashMap<String,String>()
	HashMap<String, HashSet<TrackablePackage>> userOpenedPackages = new HashMap<>() //For
	long updateCount = 0
	long lastPrintCount = 0 //Is this stuff thread safe?

	void doGet(HttpServletRequest req, HttpServletResponse resp){
		if(req.getPathInfo().equals("/tracknewpackage")) {
			def responseString = "{ \"ackUUID\":\""+uuids+"\" }"
			double lat=Double.parseDouble(req.getParameterMap().get("destinationLat")[0])
			double lon=Double.parseDouble(req.getParameterMap().get("destinationLon")[0])
			trackedIDs.putAt(uuids[0],new TrackablePackage(uuids[0], new Coordinate(lat,lon)))
			resp.setContentType("application/json")
			def writer = resp.getWriter()
			writer.print(responseString)
			writer.flush()
			println uuids
		}
	}
	
	private String returnText(String path){
		def scanner = new Scanner( new File(path))
		String text = scanner.useDelimiter("\\A").next()
		scanner.close()
		return text
	}

	void doPost(HttpServletRequest req, HttpServletResponse resp) {
		//Prints out package information to the webpage and prompts the user to enter more packages
		if(req.getPathInfo().equals("/login")){
			userLogin(req,resp);
		}
		if(req.getPathInfo().equals("/packages")){
			getPackages(req,resp)
		}

		if(req.getPathInfo().startsWith("/addPackage/")) {
			addPackage(req,resp);
		}

		if(req.getPathInfo().startsWith("/packagetrackupdate/")) {
			packageTrackUpdate(req,resp);
		}
	}
	
	void getPackages(HttpServletRequest req, HttpServletResponse resp){
		def info = req.getCookies()
		def username = info[0].getValue()

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

		//Returns packages to test.html
		def writer = resp.getWriter()
		resp.setContentType("application/json")
		def toJson = JsonOutput.toJson(packageInfos)
		writer.print(toJson)
		writer.flush()
	}
	
	void userLogin(HttpServletRequest req, HttpServletResponse resp){
		//Gets the username and password from the current session or the last page if it was login
		//Uses cookies to score and get pwd and username
		String username = req.getParameter("username")
		String password = req.getParameter("password")

		//User just came from the login page
		if(username != null && password != null){
			if(authorization.containsKey(username) && authorization.get(username) != password){
				//Mismatch
				//resp.sendRedirect("http://localhost:8000/logout")
				return
			}
			else{
				authorization.put(username, password)
				Cookie user = new Cookie("username", username)
				resp.addCookie(user)
			}

		}
		else{
		}
	}

	void addPackage(HttpServletRequest req, HttpServletResponse resp){
		def info = req.getCookies()
		def username = info[0].getValue()
		def packageInfos = userOpenedPackages.get(username);

		def uuids = req.getParameterMap().get("uuid")
		for(String id:uuids){
			if(trackedIDs.containsKey(id)){
				packageInfos.add((trackedIDs.get(id,null)))
			}
		}
		//Now you have to recall a dopost perhaps?
	}

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
				println "packagetrackupdate count: "+updateCount
				lastPrintCount = updateCount
			}
		} catch (Exception e) { e.printStackTrace() /*report an error*/ }
	}

}
def server = new Server(8000)
ServletHandler handler = new ServletHandler()
server.setHandler(handler)
handler.addServletWithMapping(SimpleGroovyServlet.class, "/*")
println "Starting Jetty, press Ctrl+C to stop."
server.start()
server.join();