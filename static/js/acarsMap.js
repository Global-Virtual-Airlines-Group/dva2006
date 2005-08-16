function generateXMLRequest(imgPath)
{
// Build the XML Requester
var xmlreq = GXmlHttp.create();
xmlreq.open("GET", "acars_map.ws", true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	
	// Check if we display the route
	var f = document.forms[0];
	var showProgress = f.showProgress.checked;

	var xmlDoc = xmlreq.responseXML;
	var ac = xmlDoc.documentElement.getElementsByTagName("aircraft");
	map.clearOverlays();
	for (var i = 0; i < ac.length; i++) {
		var a = ac[i];
		var label = a.firstChild;
		var p = new GPoint(parseFloat(a.getAttribute("lng")), parseFloat(a.getAttribute("lat")));
		var mrk = googleMarker(imgPath, a.getAttribute("color"), p, label.data);
		mrk.ID = a.getAttribute("flight_id");
		mrk.showFlightProgress = getProgress;
		GEvent.addListener(mrk, 'infowindowclose', function() { map.removeOverlay(routeData); });
		if (showProgress)
			GEvent.addListener(mrk, 'infowindowopen', function() { mrk.showFlightProgress(); });

		map.addOverlay(mrk);
	} // for
	
	// Enable the buttons
	enableButton('RefreshButton');
	enableButton('SettingsButton');
	return true;
} // function

return xmlreq;
}

function getProgress()
{
// Build the XML Requester
var xreq = GXmlHttp.create();
xreq.open("GET", "acars_progress.ws?id=" + this.ID, true);
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
