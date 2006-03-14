function generateXMLRequest(imgPath)
{
// Build the XML Requester
var d = new Date();
var xmlreq = GXmlHttp.create();
xmlreq.open("GET", "acars_map.ws?time=" + d.getTime(), true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	var isLoading = getElement('isLoading');
	isLoading.innerHTML = ' - REDRAWING...';
	
	// Parse the XML
	var xmlDoc = xmlreq.responseXML;
	var ac = xmlDoc.documentElement.getElementsByTagName("aircraft");
	map.clearOverlays();
	for (var i = 0; i < ac.length; i++) {
		var a = ac[i];
		var label = a.firstChild;
		var p = new GPoint(parseFloat(a.getAttribute("lng")), parseFloat(a.getAttribute("lat")));
		var mrk = googleMarker(imgPath, a.getAttribute("color"), p, null);
		GEvent.addListener(mrk, 'infowindowclose', function() { document.pauseRefresh = false; map.removeOverlay(routeData); map.removeOverlay(routeWaypoints); });
		mrk.flight_id = a.getAttribute("flight_id");
		mrk.infoLabel = label.data;
		mrk.infoShow = clickIcon;
		
		// Set the the click handler
		GEvent.bind(mrk, 'click', mrk, mrk.infoShow);
		map.addOverlay(mrk);
	} // for
	
	// Focus on the map
	isLoading.innerHTML = '';
	return true;
} // function

return xmlreq;
}

function clickIcon()
{
// Check what info we display
var f = document.forms[0];
var isProgress = f.showProgress.checked;
var isRoute = f.showRoute.checked;
var isInfo = f.showInfo.checked;

// Display the info
if (isInfo) this.openInfoWindowHtml(this.infoLabel);
if (isProgress || isRoute) {
	map.removeOverlay(routeData);
	map.removeOverlay(routeWaypoints);
	showFlightProgress(this, isProgress, isRoute);
}

document.pauseRefresh = true;
return true;
}

function showFlightProgress(marker, doProgress, doRoute)
{
// Build the XML Requester
var d = new Date();
var xreq = GXmlHttp.create();
xreq.open("GET", "acars_progress.ws?id=" + marker.flight_id + "&time=" + d.getTime(), true);
xreq.onreadystatechange = function() {
	if (xreq.readyState != 4) return false;

	// Load the XML
	var xdoc = xreq.responseXML;
	var wsdata = xdoc.documentElement;
	
	// Draw the flight route
	if (doRoute) {
		var wps = wsdata.getElementsByTagName("route");
		var waypoints = new Array();
		for (var i = 0; i < wps.length; i++) {
			var wp = wps[i];
			var p = new GPoint(parseFloat(wp.getAttribute("lng")), parseFloat(wp.getAttribute("lat")));
			waypoints.push(p);
		} // for
	
		routeWaypoints = new GPolyline(waypoints, '#AF8040', 2, 0.7);
		map.addOverlay(routeWaypoints);
	}
	
	// Draw the flight progress
	if (doProgress) {
		var pos = wsdata.getElementsByTagName("pos");
		var positions = new Array();
		for (var i = 0; i < pos.length; i++) {
			var pe = pos[i];
			var p = new GPoint(parseFloat(pe.getAttribute("lng")), parseFloat(pe.getAttribute("lat")));
			positions.push(p);
		} // for
		
		// Draw the line
		routeData = new GPolyline(positions, '#4080AF', 2, 0.8);
		map.addOverlay(routeData);
	}
	
	// Check if we cross the IDL on either the route or the progress
	if (wsdata.getAttribute("crossIDL") == "true")
		updateOverlays();
	
	return true;
} // function

xreq.send(null);
return true;
}

function mapZoom(lat, lng, size)
{
map.centerAndZoom(new GPoint(lng, lat), size);
return true;
}
