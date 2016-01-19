// When the page is fully loaded...
$(document).ready(function() {
	
	// Add an event that triggers when ANY button
	// on the page is clicked...
    $("button").click(function(event) {
    	
    	// Get the button id, as we will pass it to the servlet
    	// using a GET request and it will be used to get different
    	// results (bands OR bands and albums).
    	var buttonID = event.target.id;
    	
    	// Basic JQuery Ajax GET request. We need to pass 3 arguments:
    	// 		1. The servlet url that we will make the request to.
    	//		2. The GET data (in our case just the button ID).
    	//		3. A function that will be triggered as soon as the request is successful.
    	// Optionally, you can also chain a method that will handle the possibility
    	// of a failed request.
    	$.get('locahost:8000/test', {"button-id": buttonID},
            function(resp) { // on sucess
    			// We need 2 methods here due to the different ways of 
    			// handling a JSON object.
    			if (buttonID === "bands")
    				printBands(resp);
    			else if (buttonID === "bands-albums")
    				printBandsAndAlbums(resp); 
            })
            .fail(function() { // on failure
                alert("Request failed.");
            });
    });  
});