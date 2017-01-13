golgotha.routePlot = golgotha.routePlot || {routeUpdated:false, getInactive:false};
golgotha.routePlot.gateIcons = {ours:{pal:2,icon:56},intl:{pal:2,icon:48},pop:{pal:3,icon:52},other:{pal:3,icon:60}};
golgotha.routePlot.gatesVisible = function () { return (this.dGates.visible() || this.aGates.visible()); };
golgotha.routePlot.getAJAXParams = function()
{
var f = document.forms[0];
var params = [];
if (golgotha.form.comboSet(f.airportD))	params['airportD'] = golgotha.form.getCombo(f.airportD);
if (golgotha.form.comboSet(f.airportA))	params['airportA'] = golgotha.form.getCombo(f.airportA);
if (golgotha.form.comboSet(f.airportL)) params['airportL'] = golgotha.form.getCombo(f.airportL);
if (golgotha.form.comboSet(f.airline)) params['airline'] = golgotha.form.getCombo(f.airline);
if (golgotha.form.comboSet(f.gateD)) params['gateD'] = golgotha.form.getCombo(f.gateD);
if (golgotha.form.comboSet(f.gateA)) params['gateA'] = golgotha.form.getCombo(f.gateA);
if (golgotha.form.comboSet(f.eqType)) params['eqType'] = golgotha.form.getCombo(f.eqType);
if (golgotha.form.comboSet(f.sid)) params['sid'] = golgotha.form.getCombo(f.sid);
if (golgotha.form.comboSet(f.star)) params['star'] = golgotha.form.getCombo(f.star);
if ((f.route) && (f.route.value.length > 0)) params['route'] = f.route.value;
if (golgotha.routePlot.getInactive) params['getInactive'] = 'true';
for (var j = 0; ((f.simVersion) && (j < f.simVersion.length)); j++) {
	if (f.simVersion[j].checked)
		params['simVersion'] = f.simVersion[j].value;
}

params['runways'] = 'true';
params['runway'] = golgotha.form.getCombo(f.runway);
return params;
};

golgotha.routePlot.formatAJAXParams = function(params, sep)
{
var results = [];
for (k in params) {
	var v = params[k]; 
	if (!golgotha.util.isFunction(v))
		results.push(k + '=' + escape(v));
}

return results.join(sep);
};

golgotha.routePlot.updateRoutes = function(combo, elements)
{
// Save the old value
if (!combo) return false;
var oldCode = golgotha.form.getCombo(combo);

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
};

golgotha.routePlot.updateGates = function(combo, elements)
{
// Save the old value
if (!combo) return false;
var oldCode = golgotha.form.getCombo(combo);

// Update the combobox choices
combo.options.length = elements.length + 1;
combo.options[0] = new Option('-', '');
for (var i = 0; i < elements.length; i++) {
	var e = elements[i];
	var gCode = e.getAttribute('name');
	var o = new Option(gCode);
	o.ll = {lat:parseFloat(e.getAttribute('lat')), lng:parseFloat(e.getAttribute('lng'))};
	combo.options[i+1] = o; 
	if (oldCode == gCode) combo.selectedIndex = (i+1);
}

return true;
};

golgotha.routePlot.plotMap = function(myParams)
{
if (!golgotha.form.check()) return false;	
var xmlreq = new XMLHttpRequest();
xmlreq.open('post', 'routeplot.ws', true);
xmlreq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) {
		golgotha.form.clear();
		return false;
	}
	
	// Draw the markers and load the codes
	map.clearOverlays();
	var positions = [];
	var codes = [];
	var xdoc = xmlreq.responseXML.documentElement;
	var waypoints = xdoc.getElementsByTagName('pos');
	for (var i = 0; i < waypoints.length; i++) {
		var wp = waypoints[i];
		var label = wp.firstChild; var c = wp.getAttribute('code'); 
		var p = new google.maps.LatLng(parseFloat(wp.getAttribute('lat')), parseFloat(wp.getAttribute('lng')));
		positions.push(p);
		if (!codes.contains(c)) codes.push(c);
		var mrk = null;
		if (wp.getAttribute('pal'))
			mrk = new golgotha.maps.IconMarker({pal:wp.getAttribute('pal'), icon:wp.getAttribute('icon'), info:label.data, map:map}, p);
		else
			mrk = new golgotha.maps.Marker({color:wp.getAttribute('color'), info:label.data, map:map}, p);
	}
	
	// Draw the route
	var rt = new google.maps.Polyline({map:map, path:positions, strokeColor:'#4080af', strokeWeight:2, strokeOpacity:0.8, geodesic:true, zIndex:golgotha.maps.z.POLYLINE});

	// Save the codes
	var f = document.forms[0];
	if (f.routeCodes) f.routeCodes.value = codes.join(' ');

	// Get the midpoint and center the map
	var reCenter = (!f.noRecenter.checked);
	var mps = xdoc.getElementsByTagName('midpoint');
	var dst = xdoc.getAttribute('distance');
	var mpp = mps[0];
	if (mpp && dst && reCenter) {
		map.setCenter({lat:parseFloat(mpp.getAttribute('lat')), lng:parseFloat(mpp.getAttribute('lng'))});
		map.setZoom(golgotha.maps.util.getDefaultZoom(parseInt(dst)));
	}

	// Get airline code
	var ale = golgotha.getChild(xdoc, 'airline');
	var airlineCode = (ale != null) ? ale.getAttribute('code') : null; 

	// Set departure/arrival gate locations
	var ade = golgotha.getChild(xdoc, 'airportD');
	if (ade != null)
		golgotha.routePlot.dGates.mapCenter = {lat:parseFloat(ade.getAttribute('lat')), lng:parseFloat(ade.getAttribute('lng'))};
	var aae = golgotha.getChild(xdoc, 'airportA');
	if (aae != null)
		golgotha.routePlot.aGates.mapCenter = {lat:parseFloat(ade.getAttribute('lat')), lng:parseFloat(ade.getAttribute('lng'))};

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
	golgotha.routePlot.updateRoutes(f.runway, rws);
	golgotha.util.show('runways', (f.runway.options.length > 1));

	// Load the SID/STAR list
	golgotha.routePlot.updateRoutes(f.sid, xdoc.getElementsByTagName('sid'));
	golgotha.util.display('sids', (f.sid.options.length > 1));
	golgotha.routePlot.updateRoutes(f.star, xdoc.getElementsByTagName('star'));
	golgotha.util.display('stars', (f.star.options.length > 1));

	// Check for ETOPS warning
	var ete = golgotha.getChild(xdoc, 'etops');
	if (ete != null) {
		var etopsWarn = ete.getAttribute('warning');
		if (etopsWarn == 'true') {
			var wpt = golgotha.getChild(ete, 'warnPoint');
			var wll = {lat:parseFloat(wpt.getAttribute('lat')), lng:parseFloat(wpt.getAttribute('lng'))};
			var wmrk = new golgotha.maps.IconMarker({pal:wpt.getAttribute('pal'), icon:wpt.getAttribute('icon'), info:wpt.firstChild.data, map:map}, wll);
			var pts = ete.getElementsByTagName('airport');
			for (var x = 0; x < pts.length; x++) {
				var cll = {lat:parseFloat(pts[x].getAttribute('lat')), lng:parseFloat(pts[x].getAttribute('lng'))};
				var apmrk = new golgotha.maps.IconMarker({pal:pts[x].getAttribute('pal'), icon:pts[x].getAttribute('icon'), info:pts[x].firstChild.data, map:map}, cll);
				
				// Draw the circle and line
				var crng = golgotha.maps.miles2Meter(parseInt(ete.getAttribute('range')));
				var c = new google.maps.Circle({map:map, center:cll,radius:crng,fillColor:'#601010',fillOpacity:0.15,strokeColor:'darkred',strokeOpacity:0.4,strokeWeight:1,zIndex:golgotha.maps.z.POLYLINE});
				var wl = new google.maps.Polyline({map:map, path:[cll,wll],strokeColor:'red',strokeOpacity:0.55,strokeWeight:1.15,zIndex:golgotha.maps.z.POLYLINE+1})
			}
		}
	}

	// Load the alternate list
	var alts = xdoc.getElementsByTagName('alt');
	golgotha.util.display('airportL', (alts.length > 0));
	if (alts.length > 0) {
		var apList = [];
		for (var x = 0; x < alts.length; x++) {
			var aE = alts[x];
			var ap = {iata:aE.getAttribute('iata'), icao:aE.getAttribute('icao'), name:aE.getAttribute('name')};
			apList.push(ap);
		}
		
		var oldAL = golgotha.form.getCombo(f.airportL);
		golgotha.airportLoad.setOptions(f.airportL, apList, golgotha.airportLoad.config);
		if (!f.airportL.setAirport(oldAL))
			f.airportLCode.value = '';
	}

	// Load the gates
	var dGts = xdoc.getElementsByTagName('gateD');
	var aGts = xdoc.getElementsByTagName('gateA');
	golgotha.util.display('gatesD', (dGts.length > 0));
	golgotha.util.display('gatesA', (aGts.length > 0));
	golgotha.routePlot.updateGates(f.gateD, dGts);
	golgotha.routePlot.updateGates(f.gateA, aGts);
	golgotha.routePlot.dGates.clearMarkers(); golgotha.routePlot.aGates.clearMarkers();
	golgotha.routePlot.dGates.hide(); golgotha.routePlot.aGates.hide();
	for (var i = 0; i < dGts.length; i++) {
		var gt = dGts[i]; var alCodes = gt.getAttribute('airlines').split(','); var useCount = parseInt(gt.getAttribute('useCount'));
		var isOurs = alCodes.contains(airlineCode); var isIntl = (gt.getAttribute('isIntl') == 'true');
		var opts = golgotha.routePlot.gateIcons.other;
		if (isOurs && isIntl)
			opts = golgotha.routePlot.gateIcons.intl;
		else if (isOurs)
			opts = golgotha.routePlot.gateIcons.ours;
		else if (useCount > 0)
			opts = golgotha.routePlot.gateIcons.pop;

		var p = {lat:parseFloat(gt.getAttribute('lat')), lng:parseFloat(gt.getAttribute('lng'))};
		var gmrk = new golgotha.maps.IconMarker(opts, p);
		gmrk.gate = gt.getAttribute('name');
		google.maps.event.addListener(gmrk, 'dblclick', function(e) { golgotha.form.setCombo(f.gateD, this.gate); alert('Departure Gate set to ' + this.gate); plotMap(); });
		golgotha.routePlot.dGates.addMarker(gmrk, 10);
	}

	// Get weather
	golgotha.util.display('wxDr', false);
	golgotha.util.display('wxAr', false);
	var wxs = xdoc.getElementsByTagName('wx');
	for (var i = 0; i < wxs.length; i++) {
		var wx = wxs[i];

		// Figure out which row to put it in
		var isTAF = (wx.getAttribute('type') == 'taf');
		var isDst = (wx.getAttribute('dst') == 'true');
		if (!isTAF) {
			golgotha.util.display((isDst ? 'wxAr' : 'wxDr'), true);
			var metarSpan = document.getElementById(isDst ? 'wxAmetar' : 'wxDmetar');
			golgotha.util.display(metarSpan, true);
			if (metarSpan)
				metarSpan.innerHTML = golgotha.getCDATA(wx).data;
		} else {
			golgotha.util.display((isDst ? 'wxAr' : 'wxDr'), true);
			var tafSpan = document.getElementById('wxAtaf');
			golgotha.util.display(tafSpan, true);
			if (tafSpan)
				tafSpan.innerHTML = golgotha.getCDATA(wx).data;
		}
	}

	// Show departure gates if required
	if ((f.showGates) && f.showGates.checked) golgotha.routePlot.toggleGates(golgotha.routePlot.dGates);
	if ((f.showAGates) && f.showAGates.checked) golgotha.routePlot.toggleGates(golgotha.routePlot.aGates);
	golgotha.form.clear();
	return true;
};

delete golgotha.routePlot.keepRoute;
if (myParams == null) myParams = golgotha.routePlot.getAJAXParams();
golgotha.form.submit();
xmlreq.send(golgotha.routePlot.formatAJAXParams(myParams, '&'));
golgotha.event.beacon('Route Plotter', 'Plot', golgotha.routePlot.formatAJAXParams(myParams, ' '));
return true;
};

golgotha.routePlot.searchRoutes = function()
{
golgotha.util.disable('SearchButton');
var f = document.forms[0];
var aD = f.airportD.options[f.airportD.selectedIndex].value;
var aA = f.airportA.options[f.airportA.selectedIndex].value;
var rwy = f.runway.options[f.runway.selectedIndex].value;
var ext = (f.external) ? f.external.checked : false;
var faReload = (f.forceFAReload) ? f.forceFAReload.checked : false;

// Generate an XMLHTTP request
var xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'dsproutes.ws?airportD=' + aD + '&airportA=' + aA + '&external=' + ext + '&runway=' + rwy + '&faReload=' + faReload, true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	golgotha.util.disable('SearchButton', false);
	if (xmlreq.status != 200) {
		alert(xmlreq.statusText);
		return false;
	}
	
	// Load the SID/STAR list
	var xdoc = xmlreq.responseXML.documentElement;
	var cbo = document.forms[0].routes;
	cbo.disabled = false;
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

	golgotha.form.clear();
	return true;
};

golgotha.form.submit();
xmlreq.send(null);
if (faReload) f.forceFAReload.checked = false;
golgotha.event.beacon('Route Plotter', 'Route Search', aD + '-' + aA, ext ? 1 : 0);
return true;
};

golgotha.routePlot.setRoute = function(combo)
{
var f = document.forms[0];
if (combo.selectedIndex < 1) {
	f.cruiseAlt.value = '';
	if (!golgotha.routePlot.keepRoute) {
		f.route.value = '';
		f.sid.selectedIndex = 0;
		f.star.selectedIndex = 0;
	}

	f.comments.value = ''
	if (f.routeID)
		f.routeID.value = '0';

	golgotha.routePlot.plotMap();
	return true;
}

// Update the route
try {
	var opt = combo.options[combo.selectedIndex];
	f.cruiseAlt.value = opt.altitude;
	f.route.value = opt.value;
	f.comments.value = opt.comments ? opt.comments : '';
	golgotha.form.setCombo(f.sid, opt.SID);
	golgotha.form.setCombo(f.star, opt.STAR);
	if (f.routeID)
		f.routeID.value = opt.routeID;
} catch (err) {
	alert('Error setting route - ' + err.description);
}

golgotha.util.disable('RouteSaveButton', false);
golgotha.routePlot.plotMap();
golgotha.event.beacon('Route Plotter', 'Set Route');
return true;
};

golgotha.routePlot.updateRoute = function(airportsChanged, rwyChanged)
{
var f = document.forms[0];
golgotha.routePlot.routeUpdated = true;
if (rwyChanged) {
	f.runway.selectedIndex = 0;
	f.runway.options.length = 1;
}

if (airportsChanged) {
	f.routes.options.length = 1;
	f.routes.options[0] = new Option('No Routes Loaded', '');
	f.routes.selectedIndex = 0;
	if (!golgotha.routePlot.keepRoute)
		f.route.value = '';
	golgotha.util.show('routeList', false);
	golgotha.routePlot.setRoute(f.routes);
}

golgotha.util.disable('SearchButton', (f.airportD.selectedIndex == 0) || (f.airportA.selectedIndex == 0));
golgotha.util.disable('RouteSaveButton', (f.route.value.length <= 2));
return true;
};

golgotha.routePlot.toggleGates = function(gts) {
	gts.toggle();
	if (gts.visible() && map.getZoom() < 14) map.setZoom(14);
	if (gts.mapCenter) map.setCenter(gts.mapCenter);
	golgotha.util.display('gateLegendRow', golgotha.routePlot.gatesVisible());
	return true;
};
