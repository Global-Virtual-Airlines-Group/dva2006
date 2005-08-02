function generateXMLRequest()
{
// Build the XML Requester
var xmlreq = GXmlHttp.create();
xmlreq.open("GET", "acars_map.ws", true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;

	var points = new Array();
	var xmlDoc = xmlreq.responseXML;
	var ac = xmlDoc.documentElement.getElementsByTagName("aircraft");
	for (var i = 0; i < ac.length; i++) {
		var a = ac[i];
		var p = new GPoint(parseFloat(a.getAttribute("lng")), parseFloat(a.getAttribute("lat")));
		var m = googleMarker("${imgPath}",a.getAttribute("color"),p,a.Text);
		points.push(m);
	} // for
	
	// Remove positions from the map, and replace with the new positions
	map.clearOverlays();
	addMarkers(map, points);

	// Enable the buttons
	enableButton('ToggleButton');
	enableButton('RefreshButton');
	enableButton('SettingsButton');
	return true;
} // function

return xmlreq;
}

function toggleReload()
{
var btn = getElement('ToggleButton');
if (document.doRefresh) {
	document.doRefresh = false;
	btn.value = 'START AUTOMATIC REFRESH';
} else {
	document.doRefresh = true;
	btn.value = 'STOP AUTOMATIC REFRESH';
}

return true;
}
