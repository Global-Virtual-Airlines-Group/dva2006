golgotha.maps.oceanic = golgotha.maps.oceanic || {};

golgotha.maps.oceanic.showTrackInfo = function(marker) {
	golgotha.util.setHTML('trackLabel', 'Track ' + marker.title);
	golgotha.util.setHTML('trackData', marker.trackPoints);
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
golgotha.util.setHTML('trackLabel', 'Track Data');
golgotha.util.setHTML('trackData', 'N/A');
return true;
};

// Toggle the points and tracks
golgotha.maps.oceanic.updateTracks = function(checkbox) {
	var xtracks = golgotha.maps.oceanic.tracks[checkbox.value];
	var trackPoints = golgotha.maps.oceanic.points[checkbox.value];
	var markerMap = checkbox.checked ? map : null;
	trackPoints.forEach(function(pt) { pt.setMap(markerMap); });
	xtracks.forEach(function(pt) { pt.setMap(markerMap); });
	return true;
};

golgotha.maps.oceanic.loadTracks = function(type)
{
// Check the combobox
var f = document.forms[0];
var dt = f.date.options[f.date.selectedIndex];
if (f.date.selectedIndex < 1) return;

// Generate an XMLHTTP request
var xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'otrackinfo.ws?type=' + type + '&date=' + dt.text, true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	if (xmlreq.status != 200) {
		golgotha.util.setHTML('isLoading', ' - ERROR ' + xmlreq.statusText);		
		return false;
	}

	map.clearOverlays();
	golgotha.maps.oceanic.resetTracks();

	// Get the JSON document
	var jsData = JSON.parse(xmlreq.responseText);
	jsData.tracks.forEach(function(t) {
		var trackPos = [];
		t.waypoints.forEach(function(wp) {
			trackPos.push(wp.ll);

			// Create the map marker
			var mrk = new golgotha.maps.Marker({map:map, color:wp.color, info:t.info, label:wp.code, opacity:0.75}, wp.ll);
			mrk.title = t.code; mrk.trackPoints = t.track; 
			google.maps.event.addListener(mrk, 'click', function() { golgotha.maps.oceanic.showTrackInfo(this); });
			golgotha.maps.oceanic.points[t.type].push(mrk);
		});

		// Draw the route
		var trackLine = new google.maps.Polyline({map:map, path:trackPos, strokeColor:t.color, strokeWeight:2, strokeOpacity:0.7, geodesic:true, zIndex:golgotha.maps.z.POLYLINE});
		golgotha.maps.oceanic.tracks[t.type].push(trackLine);
	});

	golgotha.util.setHTML('isLoading', '');
	return true;
};

golgotha.util.setHTML('isLoading', ' - LOADING...');
xmlreq.send(null);
return true;
};