function generateXMLRequest()
{
// Build the XML Requester
var d = new Date();
var xmlreq = GXmlHttp.create();
xmlreq.open("GET", "acars_map.ws?time=" + d.getTime(), true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	var isLoading = getElement('isLoading');
	if (isLoading)
		isLoading.innerHTML = ' - REDRAWING...';

	// Parse the XML
	var xmlDoc = xmlreq.responseXML;
	if (!xmlDoc) return false;
	var ac = xmlDoc.documentElement.getElementsByTagName("aircraft");
	map.clearOverlays();
	for (var i = 0; i < ac.length; i++) {
		var a = ac[i];
		var p = new GLatLng(parseFloat(a.getAttribute("lat")), parseFloat(a.getAttribute("lng")));
		var mrk = googleMarker(document.imgPath, a.getAttribute("color"), p, null);
		mrk.flight_id = a.getAttribute("flight_id");
		var tabs = parseInt(a.getAttribute("tabs"));
		mrk.infoShow = clickIcon;
		GEvent.addListener(mrk, 'infowindowclose', function() { document.pauseRefresh = false; map.removeOverlay(routeData); map.removeOverlay(routeWaypoints); });
		if (tabs == 0) {
			var label = a.firstChild;
			mrk.infoLabel = label.data;
		} else {
			mrk.tabs = new Array();
			var tbs = a.getElementsByTagName("tab");
			for (var x = 0; x < tbs.length; x++) {
				var tab = tbs[x];
				var label = tab.firstChild;
				mrk.tabs.push(new GInfoWindowTab(tab.getAttribute("name"), label.data));
			}
		}
		
		// Set the the click handler
		GEvent.bind(mrk, 'click', mrk, mrk.infoShow);
		map.addOverlay(mrk);
	} // for
	
	// Enable the Google Earth button depending on if we have any aircraft
	enableElement('EarthButton', (ac.length > 0));
	
	// Focus on the map
	if (isLoading)
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
if (isInfo && (this.tabs)) {
	this.openInfoWindowTabsHtml(this.tabs)
} else if (isInfo) {
	this.openInfoWindowHtml(this.infoLabel);
}

// Display flight progress / route
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
xreq.open("GET", "acars_progress.ws?id=" + marker.flight_id + "&time=" + d.getTime() + "&route=" + doRoute, true);
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
			var p = new GLatLng(parseFloat(wp.getAttribute("lat")), parseFloat(wp.getAttribute("lng")));
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
			var p = new GLatLng(parseFloat(pe.getAttribute("lat")), parseFloat(pe.getAttribute("lng")));
			positions.push(p);
		} // for
		
		// Draw the line
		routeData = new GPolyline(positions, '#4080AF', 2, 0.8);
		map.addOverlay(routeData);
	}
	
	return true;
} // function

xreq.send(null);
return true;
}

function renderBlowup(lat, lng, color, zoom)
{
// Create the map
var bmap = new GMap2(getElement("mapBlowupBox"), G_SATELLITE_TYPE);
bmap.setCenter(new GLatLng(lat, lng), zoom);
bmap.setMapType(G_SATELLITE_TYPE);

// Create the marker
var mrk = googleMarker(document.imgPath, color, p, null);
bmap.addOverlay(mrk);
return true;
}
