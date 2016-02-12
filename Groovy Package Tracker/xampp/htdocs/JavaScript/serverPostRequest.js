		xmlhttp = new XMLHttpRequest();

		function execHttpRequest(method, path, async, params, responseRecieved) {
		    xmlhttp.onreadystatechange =
		        function() {
		            if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
		                responseRecieved(xmlhttp.responseText);
		            }
		        }
		    xmlhttp.open(method, path, async);
		    xmlhttp.send(params);
		}