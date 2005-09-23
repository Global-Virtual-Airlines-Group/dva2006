function generateXMLRequest(imgPath)
{
// Build the XML Requester
var xmlreq = GXmlHttp.create();
xmlreq.open("GET", "acars_map.ws", true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	enableButton('RefreshButton');
	
	// Parse the XML
	var xmlDoc = xmlreq.responseXML;
	var ac = xmlDoc.documentElement.getElementsByTagName("aircraft");
	map.clearOverlays();
	for (var i = 0; i < ac.length; i++) {
		var a = ac[i];
		var label = a.firstChild;
		var p = new GPoint(parseFloat(a.getAttribute("lng")), parseFloat(a.getAttribute("lat")));
		var mrk = googleMarker(imgPath, a.getAttribute("color"), p, null);
		GEvent.addListener(mrk, 'infowindowclose', function() { map.removeOverlay(routeData); });
		mrk.flight_id = a.getAttribute("flight_id");
		mrk.infoLabel = label.data;
		mrk.infoShow = clickIcon;
		
		// Set the the click handler
		GEvent.bind(mrk, 'click', mrk, mrk.infoShow);
		map.addOverlay(mrk);
	} // for
	
	return true;
} // function

return xmlreq;
}

function clickIcon()
{
// Check what info we display
var f = document.forms[0];
var isProgress = f.showProgress.checked;
var isInfo = f.showInfo.checked;

// Display the info
if (isInfo) this.openInfoWindowHtml(this.infoLabel);
if (isProgress) {
	map.removeOverlay(routeData);
	showFlightProgress(this);
}

return true;
}

function showFlightProgress(marker)
{
// Build the XML Requester
var xreq = GXmlHttp.create();
xreq.open("GET", "acars_progress.ws?id=" + marker.flight_id, true);
xreq.onreadystatechange = function() {
	if (xreq.readyState != 4) return false;

	var xdoc = xreq.responseXML;
	var pos = xdoc.documentElement.getElementsByTagName("pos");
	var positions = new Array();
	for (var i = 0; i < pos.length; i++) {
		var pe = pos[i];
		var p = new GPoint(parseFloat(pe.getAttribute("lng")), parseFloat(pe.getAttribute("lat")));
		positions.push(p);
	} // for
	
	routeData = new GPolyline(positions, '#4080AF', 2, 0.8);
	map.addOverlay(routeData);
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
