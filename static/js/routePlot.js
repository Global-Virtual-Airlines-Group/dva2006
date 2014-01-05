function getAJAXParams()
{
var f = document.forms[0];
var params = [];
if (comboSet(f.airportD)) {
	params['airportD'] = getValue(f.airportD);
	f.airportDCode.value = getValue(f.airportD);
}
if (comboSet(f.airportA)) {
	params['airportA'] = getValue(f.airportA);
	f.airportACode.value = getValue(f.airportA);
}
if (comboSet(f.airportL)) {
	params['airportL'] = getValue(f.airportL);
	f.airportLCode.value = getValue(f.airportL);
}

if (comboSet(f.gateD))
	params['gateD'] = getValue(f.gateD);
if (comboSet(f.gateA))
	params['gateA'] = getValue(f.gateA);
if (comboSet(f.eqType))
	params['eqType'] = getValue(f.eqType);
if (comboSet(f.sid))
	params['sid'] = getValue(f.sid);
if (comboSet(f.star))
	params['star'] = getValue(f.star);
if ((f.route) && (f.route.value.length > 0))
	params['route'] = f.route.value;
if (getInactive)
	params['getInactive'] = 'true';
for (var j = 0; ((f.simVersion) && (j < f.simVersion.length)); j++) {
	if (f.simVersion[j].checked)
		params['simVersion'] = f.simVersion[j].value;
}

params['runways'] = 'true';
params['runway'] = getValue(f.runway);
return params;
}

function formatAJAXParams(params, sep)
{
var results = [];
for (k in params) {
	var v = params[k]; 
	if (Object.prototype.toString.call(v) != '[object Function]')
		results.push(k + '=' + escape(v));
}
	
return results.join(sep);
}

function updateRoutes(combo, elements)
{
// Save the old value
if (!combo) return false;
var oldCode = getValue(combo);

// Update the combobox choices
combo.options.length = elements.length + 1;
combo.options[0] = new Option('-', '');
for (var i = 0; i < elements.length; i++) {
	var e = elements[i];
	var rLabel = e.getAttribute('label');
	var rCode = e.getAttribute('code');
	combo.options[i+1] = new Option(rLabel, rCode);
	if ((oldCode == rCode) || (oldCode == rLabel))
		combo.selectedIndex = (i+1);
}

golgotha.event.beacon('Route Plotter', 'Update Routes');
return true;
}

function updateGates(combo, elements)
{
// Save the old value
if (!combo) return false;
var oldCode = getValue(combo);

// Update the combobox choices
combo.options.length = elements.length + 1;
combo.options[0] = new Option('-', '');
for (var i = 0; i < elements.length; i++) {
	var e = elements[i];
	var gCode = e.getAttribute('name');
	var o = new Option(gCode);
	o.ll = new google.maps.LatLng(parseFloat(e.getAttribute('lat')), parseFloat(e.getAttribute('lng')));
	combo.options[i+1] = o; 
	if (oldCode == gCode)
		combo.selectedIndex = (i+1);
}

return true;
}

function plotMap(myParams)
{
// Generate an XMLHTTP request
var xmlreq = getXMLHttpRequest();
xmlreq.open('post', 'routeplot.ws', true);
xmlreq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	map.clearOverlays();
	
	// Draw the markers and load the codes
	var positions = [];
	var codes = [];
	var xdoc = xmlreq.responseXML.documentElement;
	var waypoints = xdoc.getElementsByTagName('pos');
	for (var i = 0; i < waypoints.length; i++) {
		var wp = waypoints[i];
		var label = wp.firstChild;
		var p = new google.maps.LatLng(parseFloat(wp.getAttribute('lat')), parseFloat(wp.getAttribute('lng')));
		positions.push(p);
		codes.push(wp.getAttribute('code'));
		var mrk = null;
		if (wp.getAttribute('pal'))
			mrk = new golgotha.maps.IconMarker({pal:wp.getAttribute('pal'), icon:wp.getAttribute('icon'), info:label.data, map:map}, p);
		else
			mrk = new golgotha.maps.Marker({color:wp.getAttribute('color'), info:label.data, map:map}, p);
	}
	
	// Draw the route
	var rt = new google.maps.Polyline({path:positions, strokeColor:'#4080af', strokeWeight:2, strokeOpacity:0.8, geodesic:true, zIndex:golgotha.maps.z.POLYLINE});
	rt.setMap(map);

	// Save the codes
	var f = document.forms[0];
	if (f.routeCodes)
		f.routeCodes.value = codes.join(' ');

	// Get the midpoint and center the map
	var reCenter = (!f.noRecenter.checked);
	var mps = xdoc.getElementsByTagName('midpoint');
	var dst = xdoc.getAttribute('distance');
	var mpp = mps[0];
	if (mpp && dst && reCenter) {
		var mp = new google.maps.LatLng(parseFloat(mpp.getAttribute('lat')), parseFloat(mpp.getAttribute('lng')));
		map.setCenter(mp);
		map.setZoom(getDefaultZoom(parseInt(dst)));
	}
	
	// Set departure location
	var ade = golgotha.getChild(xdoc, 'airportD');
	if (ade != null) {
		var adp = new google.maps.LatLng(parseFloat(ade.getAttribute('lat')), parseFloat(ade.getAttribute('lng')));
		dGates.mapCenter = adp;
	}

	// Set the distance
	var dstE = document.getElementById('rtDistance');
	if (dst) {
		if(dst > 0)
			dstE.innerHTML = ' - ' + dst + ' miles';
		else
			dstE.innerHTML = '';
	}

	// Load the runways
	var rws = xdoc.getElementsByTagName('runway');
	updateRoutes(f.runway, rws);
	showObject(document.getElementById('runways'), (f.runway.options.length > 1));

	// Load the SID/STAR list
	updateRoutes(f.sid, xdoc.getElementsByTagName('sid'));
	displayObject(document.getElementById('sids'), (f.sid.options.length > 1));
	updateRoutes(f.star, xdoc.getElementsByTagName('star'));
	displayObject(document.getElementById('stars'), (f.star.options.length > 1));

	// Check for ETOPS warning
	var ete = golgotha.getChild(xdoc, 'etops');
	if (ete != null) {
		var etopsWarn = ete.getAttribute('warning');
		if (etopsWarn == 'true') {
			var wpt = golgotha.getChild(ete, 'warnPoint');
			var wll = new google.maps.LatLng(parseFloat(wpt.getAttribute('lat')), parseFloat(wpt.getAttribute('lng')));
			var wmrk = new golgotha.maps.IconMarker({pal:wpt.getAttribute('pal'), icon:wpt.getAttribute('icon'), info:wpt.firstChild.data, map:map}, wll);
			
			var pts = ete.getElementsByTagName('airport');
			for (var x = 0; x < pts.length; x++) {
				var cll = new google.maps.LatLng(parseFloat(pts[x].getAttribute('lat')), parseFloat(pts[x].getAttribute('lng')));
				var apmrk = new golgotha.maps.IconMarker({pal:pts[x].getAttribute('pal'), icon:pts[x].getAttribute('icon'), info:pts[x].firstChild.data, map:map}, cll);
				
				// Draw the circle and line
				var crng = golgotha.maps.miles2Meter(parseInt(ete.getAttribute('range')));
				var c = new google.maps.Circle({center:cll,radius:crng,fillColor:'#601010',fillOpacity:0.15,strokeColor:'darkred',strokeOpacity:0.4,strokeWeight:1,zIndex:golgotha.maps.z.POLYLINE});
				c.setMap(map);
				var wl = new google.maps.Polyline({path:[cll,wll],strokeColor:'red',strokeOpacity:0.55,strokeWeight:1.15,zIndex:golgotha.maps.z.POLYLINE+1})
				wl.setMap(map);
			}
		}
	}

	// Load the alternate list
	var alts = xdoc.getElementsByTagName('alt');
	if (alts.length > 0) {
		displayObject(document.getElementById('airportL'), true);
		var apList = [];
		for (var x = 0; x < alts.length; x++) {
			var aE = alts[x];
			var ap = {};
			ap.iata = aE.getAttribute('iata');
			ap.icao = aE.getAttribute('icao');
			ap.name = aE.getAttribute('name');
			apList.push(ap);
		}
		
		var oldAL = getValue(f.airportL);
		golgotha.airportLoad.setOptions(f.airportL, apList, golgotha.airportLoad.config);
		if (!f.airportL.setAirport(oldAL))
			f.airportLCode.value = '';
	} else
		displayObject(document.getElementById('airportL'), false);

	// Load the gates
	var dGts = xdoc.getElementsByTagName('gateD');
	var aGts = xdoc.getElementsByTagName('gateA');
	displayObject(document.getElementById('gatesD'), (dGts.length > 0));
	displayObject(document.getElementById('gatesA'), (aGts.length > 0));
	updateGates(f.gateD, dGts);
	updateGates(f.gateA, aGts);
	dGates.clearMarkers();
	dGates.hide();
	for (var i = 0; i < dGts.length; i++) {
		var gt = dGts[i];
		var p = new google.maps.LatLng(parseFloat(gt.getAttribute('lat')), parseFloat(gt.getAttribute('lng')));
		var gmrk = new golgotha.maps.IconMarker({pal:2, icon:56}, p);
		gmrk.gate = gt.getAttribute('name');
		google.maps.event.addListener(gmrk, 'dblclick', function(e) { setCombo(f.gateD, this.gate); alert('Departure Gate set to ' + this.gate); plotMap(); });
		dGates.addMarker(gmrk, 10);
	}

	// Get weather
	displayObject(document.getElementById('wxDr'), false);
	displayObject(document.getElementById('wxAr'), false);
	var wxs = xdoc.getElementsByTagName('wx');
	for (var i = 0; i < wxs.length; i++) {
		var wx = wxs[i];

		// Figure out which row to put it in
		var isTAF = (wx.getAttribute('type') == 'taf');
		var isDst = (wx.getAttribute('dst') == 'true');
		if (!isTAF) {
			displayObject(document.getElementById(isDst ? 'wxAr' : 'wxDr'), true);
			var metarSpan = document.getElementById(isDst ? 'wxAmetar' : 'wxDmetar');
			displayObject(metarSpan, true);
			if (metarSpan)
				metarSpan.innerHTML = golgotha.getCDATA(wx).data;
		} else {
			displayObject(document.getElementById(isDst ? 'wxAr' : 'wxDr'), true);
			var tafSpan = document.getElementById('wxAtaf');
			displayObject(tafSpan, true);
			if (tafSpan)
				tafSpan.innerHTML = golgotha.getCDATA(wx).data;
		}
	}
	
	// Show departure gates if required
	if ((f.showGaes) && f.showGates.checked)
		toggleGates(dGates);

	return true;
}

if (myParams == null) myParams = getAJAXParams();
xmlreq.send(formatAJAXParams(myParams, '&'));
golgotha.event.beacon('Route Plotter', 'Plot', formatAJAXParams(myParams, ' '));
return true;
}

function searchRoutes()
{
disableButton('SearchButton');
var f = document.forms[0];
var aD = f.airportD.options[f.airportD.selectedIndex].value;
var aA = f.airportA.options[f.airportA.selectedIndex].value;
var rwy = f.runway.options[f.runway.selectedIndex].value;
var ext = (f.external) ? f.external.checked : false;
var faReload = (f.forceFAReload) ? f.forceFAReload.checked : false;

// Generate an XMLHTTP request
var xmlreq = getXMLHttpRequest();
xmlreq.open('get', 'dsproutes.ws?airportD=' + aD + '&airportA=' + aA + '&external=' + ext + '&runway=' + rwy + '&faReload=' + faReload, true);
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
	var rts = xdoc.getElementsByTagName('route');
	cbo.options.length = rts.length + 1;
	cbo.options[0] = new Option('-');
	for (var x = 0; x < rts.length; x++) {
		var rt = rts[x];
		var rtw = rt.getElementsByTagName('waypoints');
		var rtn = rt.getElementsByTagName('name');
		var oLabel = rtn[0].firstChild.data;
		var oValue = rtw[0].firstChild.data;
		var opt = new Option(oLabel, oValue);
		opt.routeID = rt.getAttribute('id');
		opt.SID = rt.getAttribute('sid');
		opt.STAR = rt.getAttribute('star');
		opt.altitude = rt.getAttribute('altitude');
		opt.isExternal = rt.getAttribute('external');
		cbo.options[x + 1] = opt;
		var rtc = rt.getElementsByTagName('comments');
		var c = rtc[0].firstChild;
		if ((c != null) && (c.data != null))
			opt.comments = c.data;
	}

	return true;
}

xmlreq.send(null);
if (faReload) f.forceFAReload.checked = false;
golgotha.event.beacon('Route Plotter', 'Route Search', aD + '-' + aA, ext ? 1 : 0);
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
	f.comments.value = opt.comments ? opt.comments : '';
	setCombo(f.sid, opt.SID);
	setCombo(f.star, opt.STAR);
	if (f.routeID)
		f.routeID.value = opt.routeID;
} catch (err) {
	alert('Error setting route - ' + err.description);
}

enableElement('RouteSaveButton', true);
plotMap();
golgotha.event.beacon('Route Plotter', 'Set Route');
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
	f.routes.options[0] = new Option('No Routes Loaded', '');
	f.routes.selectedIndex = 0;
	f.route.value = '';
	showObject(document.getElementById('routeList'), false);
	setRoute(f.routes);
}

enableElement('SearchButton', (f.airportD.selectedIndex > 0) && (f.airportA.selectedIndex > 0));
enableElement('RouteSaveButton', (f.route.value.length > 2));
return true;
}

function toggleGates(gts)
{
gts.toggle();
if (gts.visible() && map.getZoom() < 14)
	map.setZoom(14);

if (gts.mapCenter)
	map.setCenter(gts.mapCenter);
	
return true;
}
