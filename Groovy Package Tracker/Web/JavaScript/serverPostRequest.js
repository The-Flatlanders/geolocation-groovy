		xmlhttp = new XMLHttpRequest();

		function execHttpRequest(method, path, async, params, responseRecieved) {
		    xmlhttp.onreadystatechange =
		        function() {
		            if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
		                responseRecieved(xmlhttp.responseText);
		            }
		        }
		    xmlhttp.open(method, path, async);
		    xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		    xmlhttp.setRequestHeader("Content-length", params.length);
		    xmlhttp.setRequestHeader("Connection", "close");
		    xmlhttp.setRequestHeader("Access-Control-Allow-Origin", "*");
		    xmlhttp.send(params);
		}