
@Grab(group='org.eclipse.jetty.aggregate', module='jetty-all', version='7.6.15.v20140411')

import org.eclipse.jetty.server.*
import org.eclipse.jetty.servlet.*
import javax.servlet.http.*
import javax.servlet.*
 

class SimpleGroovyServlet extends HttpServlet {

	long updateCount = 0;
	long lastPrintCount = 0;


	HashSet<String> keysUsed = new HashSet<String>();
	void doGet(HttpServletRequest req, HttpServletResponse resp) {
		println "GET  "+req.getRequestURL()+"   query string:"+req.getQueryString();
		if(req.getPathInfo().equals("/tracknewpackage")) {
			def responseString = "{ \"ackUUID\":\""+req.getParameterMap().get("uuid")+"\" }"
			resp.setContentType("application/json");
			def writer = resp.getWriter();
			int size = keysUsed.size();
			System.out.println(size);
			keysUsed.add(req.getQueryString());
			System.out.println(keysUsed.size());
			if(size == keysUsed.size()){
				writer.print("Used before");
			}
			writer.print(responseString);
			writer.flush();
			println "\t\t  "+responseString;
			
		}
	}


	void doPost(HttpServletRequest req, HttpServletResponse resp) {
        println "POST: "+req.getRequestURL();

		if(req.getPathInfo().startsWith("/packagetrackupdate/")) {


			try {
				BufferedReader reader = req.getReader();
				String line = null;
				while ((line = reader.readLine()) != null) {

					if(line.contains("delivered")) {
						println req.getPathInfo()+" -> "+line;
					}
					else {
						//comment out if you only want to print the delivered
						//events
						println req.getPathInfo()+" -> "+line;
					}
				}

				updateCount++;
				if((updateCount - lastPrintCount) >= 1000) {
					println "packagetrackupdate count: "+updateCount;
					lastPrintCount = updateCount;
				}


				//
				//
				//
				//TODO: send the package track update to the update received
				//logic here?
				//
				//
				//

			} catch (Exception e) { e.printStackTrace(); /*report an error*/ }
		}
	}

}


def server = new Server(8080);
ServletHandler handler = new ServletHandler();
server.setHandler(handler);
handler.addServletWithMapping(SimpleGroovyServlet.class, "/*");
println "Starting Jetty, press Ctrl+C to stop."
server.start()
server.join();
