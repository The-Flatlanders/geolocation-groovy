/**
 * This function executes an http get or post request with given information. <br>
 * The first parameter specifies whether the request should be a get or post ("GET" or "POST") <br>
 * The second specifies the path to make the request to, for example "http://localhost:8000/foo"<br>
 * The third specifies whether the request should be synchronized (true or false)<br>
 * The fourth specifies the function to execute on a server response
 */
function execHttpRequest(method, path, async, params, responseRecieved) {
	xmlhttp = new XMLHttpRequest();
	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
			responseRecieved(xmlhttp.responseText);
		}
	}
	xmlhttp.open(method, path, async);
	xmlhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
	xmlhttp.setRequestHeader("Content-length", params.length);
	xmlhttp.setRequestHeader("Connection", "close");
	xmlhttp.setRequestHeader("Access-Control-Allow-Origin", "http://localhost");

	xmlhttp.send(params);
}