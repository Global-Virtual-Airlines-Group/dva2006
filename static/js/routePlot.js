golgotha.routePlot = golgotha.routePlot || {routeUpdated:false, getInactive:false, etopsCheck:true, rsts:[], hasBlob:false};
golgotha.routePlot.gateIcons = {ours:{pal:2,icon:56},intl:{pal:2,icon:48},pop:{pal:3,icon:52},other:{pal:3,icon:60}};
golgotha.routePlot.gatesVisible = function () { return (this.dGates.visible() || this.aGates.visible()); };
golgotha.routePlot.airspaceColors = {'P':{c:'#ee1010',tx:0.4}, 'R':{c:'#adad10',tx:0.2}, 'B':{c:'#10e0e0',tx:0.1}, 'C':{c:'#ffa018', tx:0.125}, 'D':{c:'#608040', tx:0.175}};
golgotha.routePlot.getAJAXParams = function()
{
var f = document.forms[0]; var o = {runways:true};
o.airportD = golgotha.form.getCombo(f.airportD);
if (golgotha.form.comboSet(f.airportA)) o.airportA = golgotha.form.getCombo(f.airportA);
if (golgotha.form.comboSet(f.airportL)) o.airportL = golgotha.form.getCombo(f.airportL);
if (golgotha.form.comboSet(f.airline)) o.airline = golgotha.form.getCombo(f.airline);
if (golgotha.form.comboSet(f.gateD)) o.gateD = golgotha.form.getCombo(f.gateD);
if (golgotha.form.comboSet(f.gateA)) o.gateA = golgotha.form.getCombo(f.gateA);
if (golgotha.form.comboSet(f.eqType)) o.eqType = golgotha.form.getCombo(f.eqType);
if (golgotha.form.comboSet(f.sid)) o.sid = golgotha.form.getCombo(f.sid);
if (golgotha.form.comboSet(f.star)) o.star = golgotha.form.getCombo(f.star);
if ((f.route) && (f.route.value.length > 0)) o.route = f.route.value;
o.saveDraft = ((f.saveDraft) && f.saveDraft.checked);
o.getInactive = golgotha.routePlot.getInactive;
o.etopsCheck = golgotha.routePlot.etopsCheck;
for (var j = 0; ((f.simVersion) && (j < f.simVersion.length)); j++) {
	if (f.simVersion[j].checked)
		o.simVersion = f.simVersion[j].value;
}

o.runway = golgotha.form.getCombo(f.runway);
return o;
};

golgotha.routePlot.generateGate = function(g, al) {
	var isOurs = (al) && g.airlines.contains(al.code);
	var opts = golgotha.routePlot.gateIcons.other;
	if (isOurs && g.isIntl)
		opts = golgotha.routePlot.gateIcons.intl;
	else if (isOurs)
		opts = golgotha.routePlot.gateIcons.ours;
	else if (g.useCount > 0)
		opts = golgotha.routePlot.gateIcons.pop;

	var gmrk = new golgotha.maps.IconMarker(opts, g.ll);
	gmrk.gate = g.name;
	return gmrk;
};

golgotha.routePlot.updateRoutes = function(combo, data)
{
// Save the old value
if (!combo) return false;
var oldCode = golgotha.form.getCombo(combo);

// Update the combobox choices
combo.options.length = data.length + 1;
combo.options[0] = new Option('-', '');
for (var i = 0; i < data.length; i++) {
	var e = data[i];
	combo.options[i+1] = new Option(e.label, e.code);
	if ((oldCode == e.code) || (oldCode == e.label)) combo.selectedIndex = (i+1);
}

golgotha.event.beacon('Route Plotter', 'Update Routes');
return true;
};

golgotha.routePlot.updateGates = function(combo, data)
{
// Save the old value
if (!combo) return false;
var oldCode = golgotha.form.getCombo(combo);

// Update the combobox choices
combo.options.length = data.length + 1;
combo.options[0] = new Option('-', '');
for (var i = 0; i < data.length; i++) {
	var g = data[i];
	var o = new Option(g.name);
	o.ll = g.ll;
	combo.options[i+1] = o; 
	if (oldCode == g.name) combo.selectedIndex = (i+1);
}

return true;
};

golgotha.routePlot.plotMap = function(myParams)
{
if (!golgotha.form.check()) return false;	
var xmlreq = new XMLHttpRequest();
xmlreq.open('post', 'routeplot.ws', true);
xmlreq.setRequestHeader('Content-Type', 'application/json; charset=utf-8');
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false; 
	if (xmlreq.status != 200) {
		alert('Error ' + xmlreq.statusText + ' plotting route');
		golgotha.form.clear();
		return false;
	}
	
	// Draw the markers
	map.clearOverlays();
	var positions = [];
	var js = JSON.parse(xmlreq.responseText);
	js.positions.forEach(function(wp) {
		positions.push(wp.ll);
		if (wp.pal)
			var mrk = new golgotha.maps.IconMarker({pal:wp.pal, icon:wp.icon, info:wp.info, map:map}, wp.ll);
		else
			var mrk = new golgotha.maps.Marker({color:wp.color, info:wp.info, map:map}, wp.ll);
	});
	
	// Draw the route
	var rt = new google.maps.Polyline({map:map, path:positions, strokeColor:'#4080af', strokeWeight:2, strokeOpacity:0.8, geodesic:true, zIndex:golgotha.maps.z.POLYLINE});

	// Get the midpoint and center the map
	var f = document.forms[0];
	if ((js.midPoint) && (js.distance) && (!f.noRecenter.checked)) {
		map.setCenter(js.midPoint.ll);
		map.setZoom(golgotha.maps.util.getDefaultZoom(js.distance));
	}

	// Set departure/arrival gate locations
	if (js.airportD) golgotha.routePlot.dGates.mapCenter = js.airportD;
	if (js.airportA) golgotha.routePlot.aGates.mapCenter = js.airportA;

	// Set the distance
	var dstE = document.getElementById('rtDistance');
	if ((dstE) && (js.distance > 0))
		dstE.innerHTML = ' - ' + js.distance + ' miles';
	else if (dstE)
		dstE.innerHTML = '';

	// Load the runways
	golgotha.routePlot.updateRoutes(f.runway, js.runways);
	golgotha.util.show('runways', (js.runways.length > 0));

	// Load the SID/STAR list
	golgotha.routePlot.updateRoutes(f.sid, js.sid);
	golgotha.util.display('sids', (js.sid.length > 0));
	golgotha.routePlot.updateRoutes(f.star, js.star);
	golgotha.util.display('stars', (js.star.length > 0));

	// Display ETOPS rating
	var etopsSpan = document.getElementById('rtETOPS');
	if (js.etops.warning || (js.etops.time > 75))
		etopsSpan.innerHTML = ' - ' + js.etops.rating;

	// Check for ETOPS warning
	if (js.etops.warning) {
		etopsSpan.innerHTML += ', AICRAFT IS RATED ' + js.etops.aircraftRating;
		var wmrk = new golgotha.maps.IconMarker({pal:js.etops.warnPoint.pal, icon:js.etops.warnPoint.icon, info:js.etops.warnPoint.info, map:map}, js.etops.warnPoint.ll);
		var crng = golgotha.maps.miles2Meter(js.etops.range);

		// Draw the circle and line
		js.etops.airports.forEach(function(a) {
			var apmrk = new golgotha.maps.IconMarker({pal:a.pal, icon:a.icon, info:a.info, map:map}, a.ll);
			var c = new google.maps.Circle({map:map, center:a.ll,radius:crng,fillColor:'#601010',fillOpacity:0.15,strokeColor:'darkred',strokeOpacity:0.4,strokeWeight:1,zIndex:golgotha.maps.z.POLYLINE});
			var wl = new google.maps.Polyline({map:map, path:[a.ll,js.etops.warnPoint.ll],strokeColor:'red',strokeOpacity:0.55,strokeWeight:1.15,zIndex:golgotha.maps.z.POLYLINE+1})
		});
	}
	
	// Check for restricted airspace
	golgotha.routePlot.rsts = []; var asIDs = [];
	js.airspace.forEach(function(as) {
		var c = golgotha.routePlot.airspaceColors[as.type];
		var p = new google.maps.Polygon({map:map, paths:[as.border], strokeColor:c.c, strokeWeight:1, strokeOpacity:c.tx, fillColor:'#802020', fillOpacity:0.2, zIndex:golgotha.maps.z.POLYGON});
		p.info = as.info; p.ll = as.ll;
		google.maps.event.addListener(p, 'click', function() { map.infoWindow.setContent(this.info); map.infoWindow.open(map); map.infoWindow.setPosition(this.ll); });
		golgotha.routePlot.rsts.push(p);
		asIDs.push(as.id);
	});

	// Display restricted airspace list
	golgotha.util.setHTML('aspaceList', asIDs.join(', '));
	golgotha.util.display('asWarnRow', (asIDs.length > 0));

	// Load the alternate list
	golgotha.util.display('airportL', (js.alternates.length > 0));
	if (js.alternates.length > 0) {
		var oldAL = golgotha.form.getCombo(f.airportL);
		golgotha.airportLoad.setOptions(f.airportL, js.alternates, golgotha.airportLoad.config);
		if (!f.airportL.setAirport(oldAL))
			f.airportLCode.value = '';
	}

	// Load the gates
	golgotha.util.display('gatesD', (js.departureGates.length > 0));
	golgotha.util.display('gatesA', (js.arrivalGates.length > 0));
	golgotha.routePlot.updateGates(f.gateD, js.departureGates);
	golgotha.routePlot.updateGates(f.gateA, js.arrivalGates);
	golgotha.routePlot.dGates.clearMarkers(); golgotha.routePlot.aGates.clearMarkers();
	golgotha.routePlot.dGates.hide(); golgotha.routePlot.aGates.hide();
	js.arrivalGates.forEach(function(gateData) { golgotha.routePlot.dGates.addMarker(golgotha.routePlot.generateGate(gateData, js.airline), 10); });
	js.departureGates.forEach(function(gateData) {
		var gmrk = golgotha.routePlot.generateGate(gateData, js.airline);
		google.maps.event.addListener(gmrk, 'dblclick', function(e) { golgotha.form.setCombo(f.gateD, this.gate); alert('Departure Gate set to ' + this.gate); golgotha.routePlot.plotMap(); });
		golgotha.routePlot.dGates.addMarker(gmrk, 10);
	});

	// Get weather
	golgotha.util.display('wxDr', false);
	golgotha.util.display('wxAr', false);
	js.wx.forEach(function(wx) {
		var isTAF = (wx.type == 'taf');
		if (!isTAF) {
			golgotha.util.display((wx.dst ? 'wxAr' : 'wxDr'), true);
			var metarSpan = document.getElementById(wx.dst ? 'wxAmetar' : 'wxDmetar');
			golgotha.util.display(metarSpan, true);
			if (metarSpan)
				metarSpan.innerHTML = wx.info;
		} else {
			golgotha.util.display((wx.dst ? 'wxAr' : 'wxDr'), true);
			var tafSpan = document.getElementById('wxAtaf');
			golgotha.util.display(tafSpan, true);
			if (tafSpan)
				tafSpan.innerHTML = wx.info;
		}
	});
	
	// Show departure gates if required
	if ((f.showGates) && f.showGates.checked) golgotha.routePlot.toggleGates(golgotha.routePlot.dGates);
	if ((f.showAGates) && f.showAGates.checked) golgotha.routePlot.toggleGates(golgotha.routePlot.aGates);
	golgotha.form.clear();
	return true;
};

delete golgotha.routePlot.keepRoute;
if (myParams == null) myParams = golgotha.routePlot.getAJAXParams();
golgotha.form.submit();
xmlreq.send(JSON.stringify(myParams));
golgotha.event.beacon('Route Plotter', 'Plot');
return true;
};

golgotha.routePlot.searchRoutes = function()
{
golgotha.util.disable('SearchButton');
var o = {};
var f = document.forms[0];
o.airportD = f.airportD.options[f.airportD.selectedIndex].value;
o.airportA = f.airportA.options[f.airportA.selectedIndex].value;
o.runway = f.runway.options[f.runway.selectedIndex].value;
o.external = (f.external) ? f.external.checked : false;
o.faReload = (f.forceFAReload) ? f.forceFAReload.checked : false;

// Generate an XMLHTTP request
var xmlreq = new XMLHttpRequest();
xmlreq.open('post', 'dsproutes.ws', true);
xmlreq.setRequestHeader('Content-Type', 'application/json; charset=utf-8');
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	golgotha.util.disable('SearchButton', false);
	if (xmlreq.status != 200) {
		alert('Error ' + xmlreq.statusText + ' fetching routes');
		golgotha.form.clear();
		return false;
	}

	// Load the SID/STAR list
	var js = JSON.parse(xmlreq.responseText);
	var cbo = f.routes;
	cbo.options.length = js.routes.length + 1;
	cbo.options[0] = new Option('-', '');
	for (var x = 0; x < js.routes.length; x++) {
		var rt = js.routes[x];
		var opt = new Option(rt.name, rt.waypoints);
		opt.routeID = rt.id;
		opt.SID = rt.sid;
		opt.STAR = rt.star;
		opt.altitude = rt.altitude;
		opt.isExternal = rt.external;
		opt.comments = rt.comments;
		cbo.options[x + 1] = opt;
	}

	cbo.disabled = false;
	golgotha.form.clear();
	return true;
};

golgotha.form.submit();
xmlreq.send(JSON.stringify(o));
if (o.faReload) f.forceFAReload.checked = false;
golgotha.event.beacon('Route Plotter', 'Route Search', o.airportD + '-' + o.airportA, o.external ? 1 : 0);
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

golgotha.routePlot.download = function() {
	if (!golgotha.form.wrap(golgotha.local.validate, document.forms[0])) return false;
	var xmlreq = new XMLHttpRequest();
	xmlreq.open('post', '/routeplan.ws', true);
	xmlreq.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
	xmlreq.responseType = 'blob';
	xmlreq.onreadystatechange = function() {
		if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
		var ct = xmlreq.getResponseHeader('Content-Type');
		var b = new Blob([xmlreq.response], {type: ct.substring(0, ct.indexOf(';')), endings:'native'});
		saveAs(b, xmlreq.getResponseHeader('X-Plan-Filename'));
		return true;
	};

	// Parse parameters
	var params = []; var o = golgotha.routePlot.getAJAXParams();
	for (p in o) {
		if (o.hasOwnProperty(p))
			params.push(p + '=' + o[p]);
	}

	xmlreq.send(params.join('&'));
	return true;
};
