function getACARSData(pirepID, imgPath)
{
// Disable checkboxes
var f = document.forms[0];
f.showFDR.disabled = true;
f.showRoute.disabled = true;

// Build the XML Requester
var xmlreq = GXmlHttp.create();
xmlreq.open('GET', 'acars_pirep.ws?id=' + pirepID, true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	var xmlDoc = xmlreq.responseXML;
	var ac = xmlDoc.documentElement.getElementsByTagName("pos");
	for (var i = 0; i < ac.length; i++) {
		var a = ac[i];
		var label = a.firstChild;
		var p = new GLatLng(parseFloat(a.getAttribute("lat")), parseFloat(a.getAttribute("lng")));
		if (a.getAttribute("color")) {
			var mrk = googleMarker(imgPath, a.getAttribute("color"), p, label.data);
			routeMarkers.push(mrk);
		}
		
		routePoints.push(p);
	} // for
	
	// Create line
	gRoute = new GPolyline(routePoints,'#4080AF',3,0.85)
	
	// Enable checkboxes
	f.showFDR.disabled = false;
	f.showRoute.disabled = false;
	return true;
} // function

xmlreq.send(null);
return true;
}
