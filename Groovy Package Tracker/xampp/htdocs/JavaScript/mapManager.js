/**
 * Contains various map and package display functions, accessed from mainPage.html
 */

/**
 * The map that contains all markers and other graphic information
 */
var map;
/**
 * True if no packages have been displayed on the map yet
 */
var noPackagesYet = true;
/**
 * Map items (markers, or polylines) to delete on a reclick of a different marker than the one currently selected
 */
var deleteOnReclick = [];
/**
 * Array of all of the current location markers currently on the map, NOT start and end markers
 */
var allMarkers = [];
/**
 * The package currently clicked on, if any. If no package is clicked on, this field is null
 */
var activePackage;

/**
 * Initializes a google map, stored in the map variable
 * <br>Set at first to:
 * <br>Zoom 2 (the entire world)
 * <br>Center at lat = 0, lon = 0
 */
function initMap() {
	map = new google.maps.Map(document.getElementById('map'), {
		center : {
			lat : 0,
			lng : 0
		},
		zoom : 2
	});
}

/**
 * Resizes the map around an array of markers passed as a parameter
 * @param markersToUse The markers to use when sizing the map.
 * Each one of these markers are assured to be in the bounds of the map at the end of the function
 */
function sizeMap(markersToUse) {
	var bounds = new google.maps.LatLngBounds();
	for (i = 0; i < markersToUse.length; i++) {
		bounds.extend(markersToUse[i].getPosition());
	}
	map.setCenter(bounds.getCenter());
	map.fitBounds(bounds);
	map.setZoom(map.getZoom());
	if (map.getZoom() > 10) {
		map.setZoom(10);
	}
}

/**
 * Removes from the map all map objects currently stored in the deleteOnReclick array
 */
function deleteMapObjects() {
	for (var i = 0; i < deleteOnReclick.length; i++) {
		deleteOnReclick[i].setMap(null);
	}
	deleteOnReclick=[];
}
/**
 * If a package has been clicked on, returns that package. Otherwise return null.
 * @returns
 */
function getActivePackage() {
	return activePackage;
}

/**
 * Accepts a package object and shows it as a marker on the map. Calls inspectPackage(myPackage) on a marker click.
 * @param myPackage The package to be shown on the map
 */
function showPackage(myPackage) {
	var myLatLng = {
			lat : myPackage.location.lat,
			lng : myPackage.location.lon
	};
	var marker = new google.maps.Marker({
		position : myLatLng,
		map : map,
		title : myPackage.uuid,
	});
	allMarkers.push(marker);
	sizeMap(allMarkers);
	addGeocode(myPackage);
	marker.addListener('click', function(){
		inspectPackage(myPackage);
	});

}

/**
 * Calls various methods to:<br>
 * Show the package path
 * Show package start and end
 * Zoom to just package start, current location, and end
 * Show package details in tab on right
 * @param myPackage The package that has been clicked on
 */
function inspectPackage(myPackage) {
	activePackage=myPackage;
	showPackagePath(myPackage);
	window.location = "#DetailTab";
	showPackageDetails(myPackage);
}

/**
 * Creates a line that goes through all previous locations of the package and displays it on the map
 * @param myPackage Contains all previous locations
 */
function showPackagePath(myPackage) {
	deleteMapObjects();
	var pastCords = myPackage.pastCords;
	for (var i = 0; i < pastCords.length - 1; i++) {
		var cord1 = pastCords[i];
		var cord2 = pastCords[i + 1];
		var latlong1 = {
				lat : cord1.lat,
				lng : cord1.lon
		};
		var latlong2 = {
				lat : cord2.lat,
				lng : cord2.lon
		};

		var polyline = new google.maps.Polyline({
			path : [ latlong1, latlong2 ],
			strokeColor : '#036564',
			strokeOpacity : 1.0,
			strokeWeight : 4,
			map : map
		});
		deleteOnReclick.push(polyline);
	}

	var blue = './Resources/blue-dot.png';
	var start = new google.maps.Marker({
		position : {
			lat : myPackage.startingLocation.lat,
			lng : myPackage.startingLocation.lon
		},
		map : map,
		icon:blue
	});

	var green = './Resources/green-dot.png';
	var end = new google.maps.Marker({
		position : {
			lat : myPackage.destination.lat,
			lng : myPackage.destination.lon
		},
		map : map,
		icon: green
	});
	var current = new google.maps.Marker({
		position : {
			lat : myPackage.location.lat,
			lng : myPackage.location.lon
		},
	});

	deleteOnReclick.push(current);
	deleteOnReclick.push(start);
	deleteOnReclick.push(end);


	var markersToInclude = [];
	markersToInclude.push(start);
	markersToInclude.push(end);
	markersToInclude.push(current);

	sizeMap(markersToInclude);
}

/**
 * Shows the city location information of the package in the left tab
 * @param myPackage Contains location information
 */
function addGeocode(myPackage){
	var location;
	var myLatLng = {
			lat : myPackage.location.lat,
			lng : myPackage.location.lon
	};
	var destLatLng = {
			lat : myPackage.destination.lat,
			lng : myPackage.destination.lon
	};
	var geocoder = new google.maps.Geocoder();
	geocoder.geocode({'location' : destLatLng}, function(results, status) {
		if (status === google.maps.GeocoderStatus.OK) {
			if (results[1]) {
				location= (results[1].formatted_address);
			} else {
				console.log('No results found');
			}
		} else {
			location = "Unknown or unpopulated area"
				console.log('Geocoder failed due to: ' + status);
		}
		myPackage.packageDestination = location;
	});
	geocoder.geocode({'location' : myLatLng}, function(results, status) {
		if (status === google.maps.GeocoderStatus.OK) {
			if (results[1]) {
				location= (results[1].formatted_address);
			} else {
				console.log('No results found');
			}
		} else {
			location = "Unknown or unpopulated area"
				console.log('Geocoder failed due to: ' + status);
		}
		myPackage.packageLocation = location;
	});
}

/**
 * Shows all relevant information about the package in the details tab on the left
 * @param myPackage Contains all pertinent information
 */
function showPackageDetails(myPackage) {
	var details;
	document.getElementById('notes').style.visibility = "visible";
	var notes = myPackage.notes;
	if (notes != null){
		notes = "<br><br><br>Notes: " + notes;
	} else {
		notes = "";
	}
	if (myPackage.delivered) {
		details = "UUID: " + myPackage.uuid
		+ "<br><br> Current Location: " + myPackage.packageLocation
		+ "<br><br> Delivered"
		+ "<br>";
	} else {
		details = "UUID: " + myPackage.uuid 
		+ "<br><br>Destination: " + myPackage.packageDestination
		+ "<br><br>ETA: "
		+ Math.round(myPackage.eta * 100) / 100 + " hours" + "<br><br>"
		+ "Distance: "
		+ (Math.round(myPackage.distanceFromDestination / 10) / 100)
		+ " Km" + "<br><br>" + "Current Location: " + myPackage.packageLocation
		+ "<br><br> Not delivered"
		+ notes
		+ "<br>";
	}
	document.getElementById('details').innerHTML = details;
}
