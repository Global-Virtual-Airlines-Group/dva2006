golgotha.maps.oceanic = golgotha.maps.oceanic || {};
golgotha.maps.oceanic.showTrackInfo = function(marker)
{
var label = document.getElementById('trackLabel');
var data = document.getElementById('trackData');
if ((!label) || (!data)) return false;
label.innerHTML = "Track " + marker.title;
data.innerHTML = marker.trackPoints;
return true;
};

golgotha.maps.oceanic.resetTracks = function()
{
golgotha.maps.oceanic.tracks = {W:[], E:[], C:[]}; golgotha.maps.oceanic.points = {W:[], E:[], C:[]};

// Reset checkboxes
var f = document.forms[0];
for (var x = 0; x < f.showTracks.length; x++)
	f.showTracks[x].checked = true;

// Reset track data label
var label = document.getElementById('trackLabel');
var data = document.getElementById('trackData');
if ((!label) || (!data)) return false;
label.innerHTML = 'Track Data';
data.innerHTML = 'N/A';
return true;
};

golgotha.maps.oceanic.updateTracks = function(checkbox)
{
var xtracks = golgotha.maps.oceanic.tracks[checkbox.value];
var trackPoints = golgotha.maps.oceanic.points[checkbox.value];

//Toggle the points
var markerMap = checkbox.checked ? map : null;
for (var x = 0; x < trackPoints.length; x++)
	trackPoints[x].setMap(markerMap);

// Toggle the tracks
for (var x = 0; x < xtracks.length; x++)
	xtracks[x].setMap(markerMap);

return true;
};

golgotha.maps.oceanic.loadTracks = function(type)
{
// Check the combobox
var f = document.forms[0];
var dt = f.date.options[f.date.selectedIndex];
if (f.date.selectedIndex < 1) return;

// Set map as loading
var isLoading = document.getElementById('isLoading');
if (isLoading) isLoading.innerHTML = ' - LOADING...';

// Generate an XMLHTTP request
var xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'otrackinfo.ws?type=' + type + '&date=' + dt.text, true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	if (xmlreq.status != 200) {
		isLoading.innerHTML = ' - ERROR ' + xmlreq.statusText;		
		return false;
	}

	map.clearOverlays();
	golgotha.maps.oceanic.resetTracks();

	// Get the XML document
	var xdoc = xmlreq.responseXML.documentElement;
	var xtracks = xdoc.getElementsByTagName('track');
	for (var i = 0; i < xtracks.length; i++) {
		var trackPos = [];
		var track = xtracks[i];
		var trackType = track.getAttribute('type');
		var waypoints = track.getElementsByTagName('waypoint');
		for (var j = 0; j < waypoints.length; j++) {
			var wp = waypoints[j];
			var label = wp.firstChild;
			var p = {lat:parseFloat(wp.getAttribute('lat')), lng:parseFloat(wp.getAttribute('lng'))};
			trackPos.push(p);

			// Create the map marker
			var code = wp.getAttribute('code'); var cl = wp.getAttribute('color');
			var mrk = new golgotha.maps.Marker({map:map, color:cl, info:label.data, label:code}, p);
			mrk.title = track.getAttribute('code');
			mrk.trackPoints = track.getAttribute('track');
			google.maps.event.addListener(mrk, 'click', function() { golgotha.maps.oceanic.showTrackInfo(this); });
			golgotha.maps.oceanic.points[trackType].push(mrk);
		}

		// Draw the route
		var trackLine = new google.maps.Polyline({map:map, path:trackPos, strokeColor:track.getAttribute('color'), strokeWeight:2, strokeOpacity:0.7, geodesic:true, zIndex:golgotha.maps.z.POLYLINE});
		golgotha.maps.oceanic.tracks[trackType].push(trackLine);
	}

	// Focus on the map
	if (isLoading) isLoading.innerHTML = '';
	return true;
};

xmlreq.send(null);
return true;
};