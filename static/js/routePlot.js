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
if (getInactive)
	params['getInactive'] = 'true';

params['runways'] = 'true';
params['runway'] = f.runway.options[f.runway.selectedIndex].value;
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
var xmlreq = getXMLHttpRequest();
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
	var reCenter = (!f.noRecenter.checked);
	var mps = xdoc.getElementsByTagName("midpoint");
	var mpp = mps[0];
	if (mpp && reCenter) {
		var mp = new GLatLng(parseFloat(mpp.getAttribute("lat")), parseFloat(mpp.getAttribute("lng")));
		map.setCenter(mp, getDefaultZoom(parseInt(mpp.getAttribute("distance"))));
	}

	// Load the runways
	var rws = xdoc.getElementsByTagName("runway");
	updateRoutes(f.runway, rws);
	showObject(getElement("runways"), (f.runway.options.length > 1));

	// Load the SID/STAR list
	updateRoutes(f.sid, xdoc.getElementsByTagName("sid"));
	displayObject(getElement("sids"), (f.sid.options.length > 1));
	updateRoutes(f.star, xdoc.getElementsByTagName("star"));
	displayObject(getElement("stars"), (f.star.options.length > 1));
	return true;
}

if (myParams == null)
	myParams = getAJAXParams();

xmlreq.send(formatAJAXParams(myParams, '&'));
gaEvent('Route Plotter', 'Plot', formatAJAXParams(myParams, ' '));
return true;
}

function searchRoutes()
{
disableButton('SearchButton');
var f = document.forms[0];
var aD = f.airportD.options[f.airportD.selectedIndex].value;
var aA = f.airportA.options[f.airportA.selectedIndex].value;
var rwy = f.runway.options[f.runway.selectedIndex].value;
var ext = true;
if (f.external)
	ext = f.external.checked;

// Generate an XMLHTTP request
var xmlreq = getXMLHttpRequest();
xmlreq.open("GET", "dsproutes.ws?airportD=" + aD + "&airportA=" + aA + "&external=" + ext + "&runway=" + rwy, true);

//Build the update handler	
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	enableElement('SearchButton', true);
	if (xmlreq.status != 200) {
		alert(xmlreq.statusText);
		return false;
	}
	
	// Load the SID/STAR list
	var xdoc = xmlreq.responseXML.documentElement;
	var cbo = document.forms[0].routes;
	enableObject(cbo, true);
	var rts = xdoc.getElementsByTagName("route");
	cbo.options.length = rts.length + 1;
	cbo.options[0] = new Option("-");
	for (var x = 0; x < rts.length; x++) {
		var rt = rts[x];
		var rtw = rt.getElementsByTagName("waypoints");
		var rtn = rt.getElementsByTagName("name");
		var oLabel = rtn[0].firstChild.data;
		var oValue = rtw[0].firstChild.data;
		var opt = new Option(oLabel, oValue);
		opt.routeID = rt.getAttribute("id");
		opt.SID = rt.getAttribute("sid");
		opt.STAR = rt.getAttribute("star");
		opt.altitude = rt.getAttribute("altitude");
		opt.isExternal = rt.getAttribute("external");
		cbo.options[x + 1] = opt;
		var rtc = rt.getElementsByTagName("comments");
		var c = rtc[0].firstChild;
		if ((c != null) && (c.data != null))
			opt.comments = c.data;
	}

	return true;
}

xmlreq.send(null);
gaEvent('Route Plotter', 'Route Search', aD + '-' + aA, ext ? 1 : 0);
return true;
}

function setRoute(combo)
{
var f = document.forms[0];
if (combo.selectedIndex < 1) {
	f.cruiseAlt.value = '';
	f.route.value = '';
	f.comments.value = ''
	f.sid.selectedIndex = 0;
	f.star.selectedIndex = 0;
	if (f.routeID)
		f.routeID.value = '0';

	plotMap();
	return true;
}

// Update the route
try {
	var opt = combo.options[combo.selectedIndex];
	f.cruiseAlt.value = opt.altitude;
	f.route.value = opt.value;
	f.comments.value = opt.comments;
	setCombo(f.sid, opt.SID);
	setCombo(f.star, opt.STAR);
	if (f.routeID)
		f.routeID.value = opt.routeID;
} catch (err) {
	alert('Error setting route - ' + err.description);
}

enableElement('RouteSaveButton', true);
plotMap();
gaEvent('Route Plotter', 'Set Route');
return true;
}

function updateRoute(airportsChanged, rwyChanged)
{
var f = document.forms[0];
routeUpdated = true;
if (rwyChanged) {
	f.runway.selectedIndex = 0;
	f.runway.options.length = 1;
}

if (airportsChanged) {
	f.routes.options.length = 1;
	f.routes.options[0] = new Option("No Routes Loaded", "");
	f.routes.selectedIndex = 0;
	f.route.value = '';
	showObject(getElement('routeList'), false);
	setRoute(f.routes);
}

enableElement('SearchButton', (f.airportD.selectedIndex > 0) && (f.airportA.selectedIndex > 0));
enableElement('RouteSaveButton', (f.route.value.length > 2));
return true;
}
