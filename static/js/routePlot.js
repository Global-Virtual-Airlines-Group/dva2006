function getAJAXParams()
{
var f = document.forms[0];
var params = [];
if (f.airportD.selectedIndex > 0) {
	params['airportD'] = f.airportD.options[f.airportD.selectedIndex].value;
	f.airportDCode.value = f.airportD.options[f.airportD.selectedIndex].value;
}
if (f.airportA.selectedIndex > 0) {
	params['airportA'] = f.airportA.options[f.airportA.selectedIndex].value;
	f.airportACode.value = f.airportA.options[f.airportA.selectedIndex].value;
}
if ((f.airportL) && (f.airportL.selectedIndex > 0)) {
	params['airportL'] = f.airportL.options[f.airportL.selectedIndex].value;
	f.airportLCode.value = f.airportL.options[f.airportL.selectedIndex].value;
}
	
if ((f.sid) && (f.sid.selectedIndex > 0))
	params['sid'] = f.sid.options[f.sid.selectedIndex].value;
if ((f.star) && (f.star.selectedIndex > 0))
	params['star'] = f.star.options[f.star.selectedIndex].value;
if ((f.route) && (f.route.value.length > 0))
	params['route'] = f.route.value;
if (doRunways) {
	params['runways'] = 'true';
	params['runway'] = f.runway.options[f.runway.selectedIndex].value;
}

return params;
}

function formatAJAXParams(params, sep)
{
var results = [];
for (k in params)
	results.push(k + '=' + params[k]);
	
return results.join(sep);
}

function updateRoutes(combo, elements)
{
// Save the old value
if (!combo) return false;
var oldCode = (combo.selectedIndex == -1) ? '' : combo.options[combo.selectedIndex].value;

// Update the combobox choices
combo.options.length = elements.length + 1;
combo.options[0] = new Option("-", "");
for (var i = 0; i < elements.length; i++) {
	var e = elements[i];
	var rLabel = e.getAttribute("label");
	var rCode = e.getAttribute("code");
	combo.options[i+1] = new Option(rLabel, rCode);
	if ((oldCode == rCode) || (oldCode == rLabel))
		combo.selectedIndex = (i+1);
} // for

gaEvent('Route Plotter', 'Update Routes');
return true;
}

function plotMap(myParams)
{
// Generate an XMLHTTP request
var xmlreq = GXmlHttp.create();
xmlreq.open("POST", "routeplot.ws", true);
xmlreq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');

// Build the update handler	
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	map.clearOverlays();
	
	// Draw the markers and load the codes
	var positions = [];
	var codes = [];
	var xdoc = xmlreq.responseXML.documentElement;
	var waypoints = xdoc.getElementsByTagName("pos");
	for (var i = 0; i < waypoints.length; i++) {
		var wp = waypoints[i];
		var label = wp.firstChild;
		var p = new GLatLng(parseFloat(wp.getAttribute("lat")), parseFloat(wp.getAttribute("lng")));
		positions.push(p);
		codes.push(wp.getAttribute("code"));
		if (wp.getAttribute("pal"))
			map.addOverlay(googleIconMarker(wp.getAttribute("pal"), wp.getAttribute("icon"), p, label.data));
		else
			map.addOverlay(googleMarker(document.imgPath, wp.getAttribute("color"), p, label.data));
	} // for
	
	// Draw the route
	map.addOverlay(new GPolyline(positions, '#4080AF', 2, 0.8));
	
	// Save the codes
	var f = document.forms[0];
	if (f.routeCodes)
		f.routeCodes.value = codes.join(' ');

	// Get the midpoint and center the map
	var mps = xdoc.getElementsByTagName("midpoint");
	var mpp = mps[0];
	if (mpp) {
		var mp = new GLatLng(parseFloat(mpp.getAttribute("lat")), parseFloat(mpp.getAttribute("lng")));
		map.setCenter(mp, getDefaultZoom(parseInt(mpp.getAttribute("distance"))));
	}

	// Load the runways
	var rws = xdoc.getElementsByTagName("runway");
	updateRoutes(f.runway, rws);
	showObject(getElement('runways'), true);

	// Load the SID/STAR list
	updateRoutes(f.sid, xdoc.getElementsByTagName("sid"));
	updateRoutes(f.star, xdoc.getElementsByTagName("star"));
	return true;
}

if (myParams == null)
	myParams = getAJAXParams();

xmlreq.send(formatAJAXParams(myParams, '&'));
gaEvent('Route Plotter', 'Plot', formatAJAXParams(myParams, ' '));
return true;
}
