// This stores the raw KML in case the plugin hasn't loaded yet
var kml;
var acarsDataQueue;

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
	acarsDataQueue = Array.prototype.slice.call(ac);
	progressBar.start(Math.round(acarsDataQueue / 250));
	gaEvent('ACARS', 'Flight Data', pirepID);
	setTimeout("mrkLoad()", 10);

	// Enable checkboxes
	var isEarth = (map.getCurrentMapType() == G_SATELLITE_3D_MAP);
	f.showFDR.disabled = isEarth;
	f.showRoute.disabled = isEarth;
	return true;
} // function

xmlreq.send(null);
return true;
}

function mrkLoad()
{
var cnt = 0;
var a = queue.pop();
progressBar.updateLoader(2);
while ((cnt < 250) && (a != null)) {
	var label = a.firstChild;
	var p = new GLatLng(parseFloat(a.getAttribute("lat")), parseFloat(a.getAttribute("lng")));
	if (a.getAttribute("icon")) {
		var mrk = googleIconMarker(a.getAttribute("pal"), a.getAttribute("icon"), p, label.data);
		routeMarkers.push(mrk);
	} else if (a.getAttribute("color")) {
		var mrk = googleMarker(imgPath, a.getAttribute("color"), p, label.data);
		routeMarkers.push(mrk);
	}
	
	routePoints.push(p);
	cnt++;
	if (cnt < 250)
		a = queue.pop();
}

if (a != null)
	setTimeout("mrkLoad()", 2);
else {
	gRoute = new GPolyline(routePoints,'#4080AF',3,0.85)
	progressBar.remove();
}
	
return true;	
}

function displayKML()
{
if ((ge == null) || (kmlProgress != null))
	return false;

kmlProgress = ge.parseKml(kml);
ge.getFeatures().appendChild(kmlProgress);
return true;
}

function generateKMLRequest(pirepID, showRoute)
{
if (kml != null)
	return displayKML();

// Build the XML Requester

var xmlreq = GXmlHttp.create();
xmlreq.open("GET", "acars_earth.ws?id=" + pirepID + "&noCompress=true&showRoute=" + showRoute, true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	var xml = xmlreq.responseText;
	if (!xml) return false;
	kml = xml;
	gaEvent('ACARS', 'Flight Data KML', pirepID);
	displayKML();
	return true;
} // function

xmlreq.send(null);
return true;
}

function earthToggle()
{
var f = document.forms[0];
var isEarth = (map.getCurrentMapType() == G_SATELLITE_3D_MAP);
f.showFDR.disabled = isEarth;
f.showRoute.disabled = isEarth;
f.showFPlan.disabled = isEarth;
f.showFPMarkers.disabled = isEarth;
if (isEarth) {
	removeMarkers(map, 'gRoute');
	removeMarkers(map, 'routeMarkers');
	removeMarkers(map, 'gfRoute');
	removeMarkers(map, 'filedMarkers');
	displayKML();
} else {
	if (f.showRoute.checked) addMarkers(map, 'gRoute');
	if (f.showFDR.checked) addMarkers(map, 'routeMarkers');
	if (f.showFPlan.checked) addMarkers(map, 'gfRoute');
	if (f.showFPMarkers.checked) addMarkers(map, 'filedMarkers');
}

return true;
}
