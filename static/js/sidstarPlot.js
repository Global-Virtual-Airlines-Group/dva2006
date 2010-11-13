function getRoutes(combo, useCache)
{
var icao = combo.options[combo.selectedIndex].value;

// Build the URL
var url = 'apsidstar.ws?airport=' + icao;
if (!useCache) {
	var d = new Date();
	url += '&time=' + d.getTime();
}

// Build the XML Requester
var xmlreq = getXMLHttpRequest();
xmlreq.open('get', url, true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	mm.clearMarkers();
	removeMarkers('apMarker');
	removeMarkers('routeTrack');
	var cbo = document.forms[0].tRoutes;
	cbo.selectedIndex = 0;
	cbo.options.length = 1;
	routes.length = 0;

	// Parse the XML
	var xml = xmlreq.responseXML;
	var xe = xml.documentElement;

	// Get the Airport and center on it
	var icao = xe.getAttribute('icao');
	if (airports[icao] == null) {
		var ap = xe.getElementsByTagName('airport')[0];
		var apLoc = new google.maps.LatLng(parseFloat(ap.getAttribute('lat')), parseFloat(ap.getAttribute('lng')));
		if (ap.getAttribute('pal'))
			map.apMarker = googleIconMarker(ap.getAttribute('pal'), ap.getAttribute('icon'), apLoc, ap.firstChild.data);
		else
			map.apMarker = googleMarker(ap.getAttribute('color'), apLoc, ap.firstChild.data);

		map.setCenter(apLoc);
		airports[icao] = map.apMarker;
		map.currentAirport = icao;
		map.apMarker.setMap(map);
	} else {
		map.apMarker.setMap(null);
		map.apMarker = airports[icao];
		map.currentAirport = icao;
		map.apMarker.setMap(map);
	}

	// Get the routes
	var rt = xe.getElementsByTagName('route');
	for (var i = 0; i < rt.length; i++) {
		var tr = rt[i];
		var id = tr.getAttribute('id');
		var label = id + ' (' + tr.getAttribute('type') + ')';
		cbo.options[cbo.options.length] = new Option(label, id);
		tr.SID = (tr.getAttribute('type') == 'SID');

		// Add waypoints
		tr.waypoints = [];
		var wps = tr.getElementsByTagName('waypoint');
		for (var j = 0; j < wps.length; j++) {
			var wp = wps[j];
			var code = wp.getAttribute('code');
			if (waypoints[code] == null) {
				var mrk = null;
				var p = new google.maps.LatLng(parseFloat(wp.getAttribute('lat')), parseFloat(wp.getAttribute('lng')));
				if (wp.getAttribute('pal'))
					mrk = googleIconMarker(wp.getAttribute('pal'), wp.getAttribute('icon'), p, wp.firstChild.data);
				else
					mrk = googleMarker(wp.getAttribute('color'), p, wp.firstChild.data);

				mrk.code = code;
				google.maps.event.addListener(mrk, 'dblclick', toggleMarker);
				waypoints[code] = mrk;

				// Calculate the min zoom
				mrk.minZoom = 5;
				var type = wp.getAttribute('type');
				if (type == 'NDB')
					mrk.minZoom = 8;
				else if (type == 'Intersection')
					mrk.minZoom = 11;
			}

			tr.waypoints.push(waypoints[code]);
		}
		
		// Add the route
		routes[id] = tr;
	}

	return true;
} // function

xmlreq.send(null);
return true;
}

function loadWaypoints()
{
// Get the lat/long
var lat = map.getCenter().lat();
var lng = map.getCenter().lng();
var range = (map.getBounds().getNorthEast().lat() - map.getBounds().getSouthWest().lat()) * 69.16;

// Build the XML Requester
var xmlreq = getXMLHttpRequest();
xmlreq.open('get', 'navaidsearch.ws?lat=' + lat + '&lng=' + lng + '&range=' + Math.round(range), true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;

	// Parse the XML
	var xml = xmlreq.responseXML;
	var xe = xml.documentElement;

	// Get the waypoints
	var wps = xe.getElementsByTagName('waypoint');
	for (var i = 0; i < wps.length; i++) {
		var wp = wps[i];
		var code = wp.getAttribute('code');
		if (waypoints[code] == null) {
			var mrk = null;
			var p = new google.maps.LatLng(parseFloat(wp.getAttribute('lat')), parseFloat(wp.getAttribute('lng')));
			if (wp.getAttribute('pal'))
				mrk = googleIconMarker(wp.getAttribute('pal'), wp.getAttribute('icon'), p, wp.firstChild.data);
			else
				mrk = googleMarker(wp.getAttribute('color'), p, wp.firstChild.data);

			mrk.code = code;
			google.maps.event.addListener(mrk, 'dblclick', toggleMarker);
			waypoints[code] = mrk;

			// Calculate the min zoom
			mrk.minZoom = 5;
			var type = wp.getAttribute('type');
			if (type == 'NDB')
				mrk.minZoom = 8;
			else if (type == 'Intersection')
				mrk.minZoom = 11;
		}
	}

	return true;
} // function

xmlreq.send(null);
return true;
}

function plotRoute(combo)
{
if (combo.selectedIndex == 0) {
	mm.clearMarkers();
	removeMarkers('routeTrack');
	return;
}

// Check the route ID
var id = combo.options[combo.selectedIndex].value;
var tr = routes[id];
if (tr == null)	return;

// Clear the marker manager
mm.clearMarkers();
removeMarkers('routeTrack');

// Plot the markers
var track = [];
if (tr.SID)
	track.push(map.apMarker.getPosition());
	
for (var i = 0; i < tr.waypoints.length; i++) {
	var mrk = tr.waypoints[i];
	track.push(mrk.getPosition());
	mm.addMarker(mrk, mrk.minZoom);
}

if (!tr.SID)
	track.push(map.apMarker.getPosition());

// Display the route and the markers
mm.refresh();
routeTrack = new google.maps.Polyline({path:track, strokeColor:map.getCurrentMapType().getTextColor(), strokeWeight:2, strokeOpacity:0.8});
routeTrack.setMap(map);
return true;
}

function toggleRows(show)
{
mm.clearMarkers();
removeMarkers('routeTrack');
var rows = getElementsByClass('doPlot');
for (var x = 0; x < rows.length; x++)
	showObject(rows[x], show);

return true;
}

function findMarker(code)
{
if (code.length < 1) return false;
var mrk = waypoints[code.toUpperCase()];
if (mrk != null) {
	mm.addMarker(mrk, 2);
	mm.refresh();
	google.maps.event.trigger(mrk, 'click');
} else
	alert('Cannot find ' + code);

return true;
}

function toggleMarker()
{
var pnts = [];
var route = document.forms[0].route;

var isRemoved = false;
var wps = route.value.split(' ');
alert(wps.length);
for (var x = 0; x < wps.length; x++) {
	var wp = wps[x];
	alert(wp);
	if (wp == this.code) {
		wps[x] = null;
		route.value = wps.join(' ');
		isRemoved = true;
	} else
		pnts.push(waypoints[x].getLatLng());
}

if (!isRemoved) {
	wps.push(this.code);
	route.value = wps.join(' ');
}

// Write new route line
return true;
}
