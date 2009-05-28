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
		
	// Clean up the map - don't strip out the weather layer
	removeMarkers(map, 'routeData');
	removeMarkers(map, 'routeWaypoints');
	removeMarkers(map, 'acPositions');
	removeMarkers(map, 'dcPositions');
	acPositions.length = 0;
	dcPositions.length = 0;

	// Parse the XML
	var xml = xmlreq.responseXML;
	if (!xml) return false;
	var xe = xml.documentElement;
	var ac = xe.getElementsByTagName("aircraft");
	if (ac.length > 0)
		gaEvent('ACARS', 'Aircraft Positions');
	for (var i = 0; i < ac.length; i++) {
		var a = ac[i]; var mrk = null;
		var p = new GLatLng(parseFloat(a.getAttribute("lat")), parseFloat(a.getAttribute("lng")));
		if (a.getAttribute("pal"))
			mrk = googleIconMarker(a.getAttribute("pal"), a.getAttribute("icon"), p, null);
		else if (a.getAttribute("color"))
			mrk = googleMarker(document.imgPath, a.getAttribute("color"), p, null);

		mrk.flight_id = a.getAttribute("flight_id");
		mrk.isBusy = (a.getAttribute("busy") == 'true');
		var tabs = parseInt(a.getAttribute("tabs"));
		GEvent.addListener(mrk, 'infowindowclose', function() { document.pauseRefresh = false; removeMarkers(map, 'routeData'); removeMarkers(map, 'routeWaypoints'); });
		if (tabs == 0) {
			var label = a.firstChild;
			mrk.infoLabel = label.data;
		} else {
			mrk.tabs = [];
			var tbs = a.getElementsByTagName("tab");
			for (var x = 0; x < tbs.length; x++) {
				var tab = tbs[x];
				var label = tab.firstChild;
				mrk.tabs.push(new GInfoWindowTab(tab.getAttribute("name"), label.data));
			}
		}

		// Set the the click handler
		GEvent.bind(mrk, 'click', mrk, clickAircraft);
		acPositions.push(mrk);
		map.addOverlay(mrk);
	} // for

	var dc = xe.getElementsByTagName("dispatch");
	if (dc.length > 0)
		gaEvent('ACARS', 'Dispatch Positions');
	for (var i = 0; i < dc.length; i++) {
		var d = dc[i]; var mrk = null;
		var p = new GLatLng(parseFloat(d.getAttribute("lat")), parseFloat(d.getAttribute("lng")));
		if (d.getAttribute("pal"))
			mrk = googleIconMarker(d.getAttribute("pal"), d.getAttribute("icon"), p, null);
		else if (d.getAttribute("color"))
			mrk = googleMarker(document.imgPath, d.getAttribute("color"), p, null);

		var tabs = parseInt(d.getAttribute("tabs"));
		mrk.range = parseInt(d.getAttribute("range"));
		mrk.isBusy = (d.getAttribute("busy") == 'true');
		GEvent.bind(mrk, 'infowindowclose', mrk, function() { document.pauseRefresh = false; if (this.rangeCircle) this.rangeCircle.hide(); });
		if (tabs == 0) {
			var label = d.firstChild;
			mrk.infoLabel = label.data;
		} else {
			mrk.tabs = [];
			var tbs = d.getElementsByTagName("tab");
			for (var x = 0; x < tbs.length; x++) {
				var tab = tbs[x];
				var label = tab.firstChild;
				mrk.tabs.push(new GInfoWindowTab(tab.getAttribute("name"), label.data));
			}
		}

		// Set the the click handler
		GEvent.bind(mrk, 'click', mrk, clickDispatch);
		dcPositions.push(mrk);
		map.addOverlay(mrk);
	} // for

	// Enable the Google Earth button depending on if we have any aircraft
	enableElement('EarthButton', (ac.length > 0));

	// Display dispatch status
	var de = getElement('dispatchStatus');
	if ((de) && (dc.length > 0)) {
		de.className = 'ter bld caps';
		de.innerHTML = 'Dispatcher Currently Online';
	} else if (de) {
		de.className = 'bld caps';	
		de.innerHTML = 'Dispatcher Currently Offline';
	}

	// Focus on the map
	if (isLoading)
		isLoading.innerHTML = '';

	return true;
} // function

return xmlreq;
}

function clickAircraft()
{
// Check what info we display
var f = document.forms[0];
var isProgress = f.showProgress.checked;
var isRoute = f.showRoute.checked;
var isInfo = f.showInfo.checked;
gaEvent('ACARS', 'Flight Info', this.flight_id);

// Display the info
if (isInfo && (this.tabs))
	this.openInfoWindowTabsHtml(this.tabs)
else if (isInfo)
	this.openInfoWindowHtml(this.infoLabel);

// Display flight progress / route
if (isProgress || isRoute) {
	removeMarkers(map, 'routeData');
	removeMarkers(map, 'routeWaypoints');
	showFlightProgress(this, isProgress, isRoute);
}

document.pauseRefresh = true;
return true;
}

function clickDispatch()
{
// Check what info we display
var f = document.forms[0];
var isInfo = f.showInfo.checked;
gaEvent('ACARS', 'Dispatch Info');

//Display the info
if (isInfo && (this.tabs))
	this.openInfoWindowTabsHtml(this.tabs)
else if (isInfo)
	this.openInfoWindowHtml(this.infoLabel);

//Display flight progress / route
if (!this.rangeCircle) {
	this.rangeCircle = getServiceRange(this, this.range);
	if (this.rangeCircle) {
		gaEvent('ACARS', 'Dispatch Service Range');
		map.addOverlay(this.rangeCircle);
	}
} else
	this.rangeCircle.show();

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
		var waypoints = [];
		for (var i = 0; i < wps.length; i++) {
			var wp = wps[i];
			var p = new GLatLng(parseFloat(wp.getAttribute("lat")), parseFloat(wp.getAttribute("lng")));
			waypoints.push(p);
		} // for

		gaEvent('ACARS', 'Flight Route Info', marker.flight_id);
		routeWaypoints = new GPolyline(waypoints, '#AF8040', 2, 0.7);
		map.addOverlay(routeWaypoints);
	}

	// Draw the flight progress
	if (doProgress) {
		var pos = wsdata.getElementsByTagName("pos");
		var positions = [];
		for (var i = 0; i < pos.length; i++) {
			var pe = pos[i];
			var p = new GLatLng(parseFloat(pe.getAttribute("lat")), parseFloat(pe.getAttribute("lng")));
			positions.push(p);
		} // for

		// Draw the line
		gaEvent('ACARS', 'Flight Progress Info', marker.flight_id);
		routeData = new GPolyline(positions, '#4080AF', 2, 0.8);
		map.addOverlay(routeData);
	}

	return true;
} // function

xreq.send(null);
return true;
}

function getServiceRange(marker, range)
{
if (range == 0) return null;
var p = map.getCurrentMapType().getProjection();
var l2 = new GLatLng(marker.getLatLng().lat() + (range / 69.16), marker.getLatLng().lng());
var centerPt = p.fromLatLngToPixel(marker.getLatLng(), map.getZoom()); 
var radiusPt = p.fromLatLngToPixel(l2, map.getZoom());

// Build the circle
var pts = [];
var radius = Math.floor(Math.sqrt(Math.pow((centerPt.x-radiusPt.x),2) + Math.pow((centerPt.y-radiusPt.y),2))); 
for (var a = 0 ; a < 361 ; a+=5 ) {
    var aRad = (Math.PI / 180) * a;
    var y = centerPt.y + radius * Math.sin(aRad);
    var x = centerPt.x + radius * Math.cos(aRad);
    pts.push(p.fromPixelToLatLng(new GPoint(x,y), map.getZoom()));
} 

// Set border/fill colors
var bColor = marker.isBusy ? '#C02020' : '#20C060';
var fColor = marker.isBusy ? '#802020' : '#208040';
return new GPolygon(pts, bColor, 1, 0.65, fColor, marker.isBusy ? 0.1 : 0.2); 
}
