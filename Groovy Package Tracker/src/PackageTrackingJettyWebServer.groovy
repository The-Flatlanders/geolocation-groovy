@Grab(group='org.eclipse.jetty.aggregate', module='jetty-all', version='7.6.15.v20140411')
import org.eclipse.jetty.server.*
import org.eclipse.jetty.servlet.*
import groovy.json.JsonSlurper
import javax.servlet.http.*
import javax.servlet.*

class SimpleGroovyServlet extends HttpServlet {
	final String KEY="AIzaSyCh8IK9eDqqGB8Wx2k0Vr_pcisZD1qw74A"
	HashMap trackedIDs=new HashMap();
	long updateCount = 0;
	long lastPrintCount = 0; //Is this stuff thread safe?
	void doGet(HttpServletRequest req, HttpServletResponse resp) {
		//println "GET  "+req.getRequestURL()+"   query string:"+req.getQueryString();
		def uuids = req.getParameterMap().get("uuid")

		//If url is navigated to directly, only shows page if already logged in
		if(req.getPathInfo().equals("/trackPackages")){
			if(req.getCookies().size() != 0){
				doPost(req, resp);
			}
		}
		if(req.getPathInfo().equals("/test")){
			def writer=resp.getWriter();
			resp.setContentType("text/plain")
			writer.print("50.1")
			println req.getParameterMap().get("uuid");
		}
		if(req.getPathInfo().equals("/tracknewpackage")) {
			def responseString = "{ \"ackUUID\":\""+uuids+"\" }"
			double lat=Double.parseDouble(req.getParameterMap().get("destinationLat")[0])
			double lon=Double.parseDouble(req.getParameterMap().get("destinationLon")[00])
			trackedIDs.putAt(uuids[0],new TrackablePackage(uuids[0], new Coordinate(lat,lon)));
			resp.setContentType("application/json");
			def writer = resp.getWriter();
			writer.print(responseString);
			writer.flush();
			println "\t\t  "+responseString;
		}
		if(req.getPathInfo().equals("/logout")){

			//Removes all cookies
			for(Cookie c : req.getCookies()){
				c.setValue(null);
				c.setMaxAge(0);
				resp.addCookie(c);
			}
			//Redirects to the login screen
			resp.sendRedirect("http://localhost:8000/login");
		}
		if(req.getPathInfo().equals("/")){
			resp.sendRedirect("http://localhost:8000/login");
		}
		if(req.getPathInfo().equals("/login")){
			resp.setContentType("text/html")
			def writer = resp.getWriter()
			writer.print(returnText("HTML/login.HTML"))
			writer.flush()
		}

	}

	private String returnText(String path){
		def scanner = new Scanner( new File(path));
		String text = scanner.useDelimiter("\\A").next();
		scanner.close()
		return text
	}

	HashMap<String, String> authorization = new HashMap<String, String>(); //For checking username to password
	HashMap<String, HashSet<TrackablePackage>> userOpenedPackages = new HashMap<>(); //For 
	void doPost(HttpServletRequest req, HttpServletResponse resp) {

		//Prints out package information to the webpage and prompts the user to enter more packages
		if(req.getPathInfo().equals("/trackPackages")){

			//Gets the username and password from the current session or the last page if it was login
			//Uses cookies to score and get pwd and username
			String username = req.getParameter("username")
			String password = req.getParameter("password")
			if(username != null && password != null){
				if(authorization.containsKey(username) && authorization.get(username) != password){
					//Mismatch
					resp.sendRedirect("http://localhost:8000/logout");
					return;
				}
				else{
					authorization.put(username, password);
				}
				Cookie user = new Cookie("username", username)
				Cookie pwd = new Cookie("password", password)
				resp.addCookie(user);
				resp.addCookie(pwd);
			}
			else{
				def info = req.getCookies()
				username = info[0].getValue()
				password = info[1].getValue()
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
			for(Cookie c : req.getCookies()){
				if(c.getName() == "UUID"){
					String id = c.getValue();
					if(trackedIDs.containsKey(id)){
						packageInfos.add((trackedIDs.get(id,null)))
					}
				}
			}
			def uuids = req.getParameterMap().get("uuid")
			for(String id:uuids){
				if(trackedIDs.containsKey(id)){
					packageInfos.add((trackedIDs.get(id,null)))
				}
			}

			//Prints out packages to window
			def writer = resp.getWriter();
			resp.setContentType("text/html")
			for(TrackablePackage p : packageInfos){
				String front="<iframe width=\"600\" height=\"450\" frameborder=\"0\" style=\"border:0\" src=\"https://www.google.com/maps/embed/v1/directions?";
				String rear="&amp;key=AIzaSyCh8IK9eDqqGB8Wx2k0Vr_pcisZD1qw74A\" allowfullscreen=\"\"></iframe>"
				Coordinate c=p.getLocation()
				Coordinate d=p.getDestination()
				writer.print(front+"&origin="+c.lat+"%20"+c.lon+"&destination="+d.lat+"%20"+d.lon+rear);
				writer.print("<h4>"+(int)(p.getDistanceFromDestination()/1000)+" km from destination</h4>")
				writer.print("<h4>"+(int)p.getETA()+" hours</h4>")
			}
			writer.print(returnText("HTML/TrackNewPackageForm.HTML"));
			writer.print(returnText("HTML/Logout.HTML"))
			writer.flush();
		}


		if(req.getPathInfo().startsWith("/packagetrackupdate/")) {

			try {
				BufferedReader reader = req.getReader();
				String line = null;
				while ((line = reader.readLine()) != null) {
					def slurper=new JsonSlurper()
					def inf=slurper.parseText(line);
					def uuid = req.getPathInfo().replace("/packagetrackupdate/","");
					TrackablePackage currentPackage = trackedIDs.get(uuid);
					if(line.contains("delivered")) {
						//This code registers delivery events
						println uuid +" -> "+ line;
						currentPackage.setDelivered(true);
					}
					else{
						//This code tracks all non delivery events
						currentPackage.update(new Coordinate(Double.parseDouble(inf.lat),Double.parseDouble(inf.lon),Double.parseDouble(inf.ele)),inf.time);
						println "eta: "+currentPackage.getETA()+" hours";
						println currentPackage.getSpeed()+" meters per second";
						//println req.getPathInfo()+" -> "+line; //Comment out if you only want to print the delivered updates
					}
				}

				updateCount++;
				if((updateCount - lastPrintCount) >= 1000) {
					println "packagetrackupdate count: "+updateCount;
					lastPrintCount = updateCount;
				}

			} catch (Exception e) { e.printStackTrace(); /*report an error*/ }
		}
	}

}

def server = new Server(8000);
ServletHandler handler = new ServletHandler();
server.setHandler(handler);
handler.addServletWithMapping(SimpleGroovyServlet.class, "/*");
println "Starting Jetty, press Ctrl+C to stop."
server.start()
server.join();