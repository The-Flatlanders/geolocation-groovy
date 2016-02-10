var map;
var noPackagesYet = true;
var deleteOnReclick = [];
var allMarkers = [];
var activePackage;
function initMap() {
	map = new google.maps.Map(document.getElementById('map'), {
		center : {
			lat : 0,
			lng : 0
		},
		zoom : 2
	});
}
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
function deleteMapObjects() {
	for (var i = 0; i < deleteOnReclick.length; i++) {
		for (var j = 0; j < allMarkers.length; j++) {
			if (allMarkers[j] == deleteOnReclick[i]) {
				allMarkers.splice(j, 1);
			}
		}
		deleteOnReclick[i].setMap(null);
	}
	deleteOnReclick.length = 0;
}
function getActivePackage() {
	return activePackage;
}
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
function inspectPackage(myPackage) {
	activePackage=myPackage;
	showPackagePath(myPackage);
	window.location = "#DetailTab";
	showPackageDetails(myPackage);
}
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
function showPackageDetails(myPackage) {
	var details;
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

	var blue = '../Web/Resources/blue-dot.png';
	var start = new google.maps.Marker({
		position : {
			lat : myPackage.startingLocation.lat,
			lng : myPackage.startingLocation.lon
		},
		map : map,
		icon:blue
	});

	var green = '../Web/Resources/green-dot.png';
	var end = new google.maps.Marker({
		position : {
			lat : myPackage.destination.lat,
			lng : myPackage.destination.lon
		},
		map : map,
		icon: green
	});

	deleteOnReclick.push(start);
	deleteOnReclick.push(end);
	
	var markersToInclude = [];
	markersToInclude.push(start);
	markersToInclude.push(end);
	//markersToInclude.push();
	
	
	sizeMap(markersToInclude);
}
