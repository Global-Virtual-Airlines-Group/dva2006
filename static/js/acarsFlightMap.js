// This stores the raw KML in case the plugin hasn't loaded yet
var kml;
var acarsDataQueue;

// ATC positions
var selectedFIRs = [];

function getACARSData(pirepID, imgPath)
{
// Disable checkboxes
var f = document.forms[0];
f.showFDR.disabled = true;
f.showRoute.disabled = true;

// Build the XML Requester
var xmlreq = GXmlHttp.create();
xmlreq.open('get', 'acars_pirep.ws?id=' + pirepID, true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var xmlDoc = xmlreq.responseXML;
	var ac = xmlDoc.documentElement.getElementsByTagName('pos');
	for (var x = 0; x < ac.length; x++) {
		var a = ac[x]; var mrk;
		var label = a.getCDATA ? a.getCDATA() : getCDATA(a);
		var p = new GLatLng(parseFloat(a.getAttribute('lat')), parseFloat(a.getAttribute('lng')));
		if (a.getAttribute('icon')) {
			mrk = googleIconMarker(a.getAttribute('pal'), a.getAttribute('icon'), p, label.data);
			routeMarkers.push(mrk);
		} else if (a.getAttribute('color')) {
			mrk = googleMarker(imgPath, a.getAttribute('color'), p, label.data);
			routeMarkers.push(mrk);
		}	

		// Add ATC data
		var ace = a.getChild ? a.getChild('atc') : getChild(a, 'atc');
		if ((ace != null) && (mrk != null)) {
			var type = ace.getAttribute('type');
			if ((type != 'CTR') && (type != 'FSS')) {
				var acp = new GLatLng(parseFloat(ace.getAttribute('lat')), parseFloat(ace.getAttribute('lng')));
				mrk.pts = circle(acp, parseInt(ace.getAttribute('range')));
				GEvent.addListener(mrk, 'click', function() { showAPP(this.pts); });
			} else {
				mrk.atcID = ace.getAttribute('id');
				GEvent.addListener(mrk, 'click', function() { showFIR(this.atcID); });
			}
		}

		routePoints.push(p);
	}

	gRoute = new GPolyline(routePoints,'#4080af', 3, 0.85);
	gaEvent('ACARS', 'Flight Data', pirepID);

	// Enable checkboxes
	var isEarth = (map.getCurrentMapType() == G_SATELLITE_3D_MAP);
	f.showFDR.disabled = isEarth;
	f.showRoute.disabled = isEarth;
	return true;
} // function

xmlreq.send(null);
return true;
}

function showAPP(cpts)
{
selectedFIRs.length = 0;
var c = new GPolygon(cpts, '#efefff', 1, 0.85, '#7f7f80', 0.25);
selectedFIRs.push(c);
map.addOverlay(c);
return true;
}

function showFIR(code)
{
var xmlreq = GXmlHttp.create();
xmlreq.open('get', 'fir.ws?id=' + code, true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var xdoc = xmlreq.responseXML;
	var re = xdoc.documentElement;
	
	// Loop through the FIRs
	selectedFIRs.length = 0;
	var fs = re.getElementsByTagName('fir');
	if (fs.length == 0) return true;
	for (var x = 0; x < fs.length; x++) {
		var fe = fs[x];
		var bPts = [];	

		// Display border
		var pts = fe.getElementsByTagName('pt');
		for (var i = 0; i < pts.length; i++) {
			var pt = pts[i];
			bPts.push(new GLatLng(parseFloat(pt.getAttribute('lat')), parseFloat(pt.getAttribute('lng'))));
		}

		if (bPts.length > 0) {
			bPts.push(bPts[0]);
			var rt = new GPolygon(bPts, '#efefff', 1, 0.85, '#7f7f80', 0.25);
			selectedFIRs.push(rt);
			map.addOverlay(rt);
		}
	}

	gaEvent('ACARS', 'Show FIR', code);
	return true;
} // function

xmlreq.send(null);
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
xmlreq.open('get', 'acars_earth.ws?id=' + pirepID + '&noCompress=true&showRoute=' + showRoute, true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
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
