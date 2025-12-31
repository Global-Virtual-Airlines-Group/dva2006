golgotha.routePlot = golgotha.routePlot || {routeUpdated:false, getInactive:false, etopsCheck:true, rsts:[], aRwys:[], hasBlob:false, isDraft:false};
golgotha.routePlot.gateIcons = {ours:{pal:2,icon:56},intl:{pal:2,icon:48},pop:{pal:3,icon:52},other:{pal:3,icon:60},uspfi:{pal:2,icon:16},eu:{pal:2,icon:17}};
golgotha.routePlot.gatesVisible = function () { return (this.dGates.visible || this.aGates.visible); };
golgotha.routePlot.airspaceColors = {'P':{c:'#ee1010',tx:0.4}, 'R':{c:'#adad10',tx:0.2}, 'B':{c:'#10e0e0',tx:0.1}, 'C':{c:'#ffa018', tx:0.125}, 'D':{c:'#608040', tx:0.175}};
golgotha.routePlot.getAJAXParams = function()
{
const f = document.forms[0]; let o = {runways:true};
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
if (f.cruiseAlt) o.cruiseAlt = f.cruiseAlt.value;
o.saveDraft = ((f.saveDraft) && f.saveDraft.checked);
o.allSID = ((f.allSID) && f.allSID.checked);
o.allGates = ((f.allGates) && f.allGates.checked);
o.precalcPax = ((f.precalcPax) && !f.precalcPax.disabled && f.precalcPax.checked);
o.noDL = ((f.noDL) && f.noDL.checked);
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
	const isOurs = (al) && g.airlines.contains(al.code);
	let opts = golgotha.routePlot.gateIcons.other;
	if (isOurs && (g.zoneCode == 1))
		opts = golgotha.routePlot.gateIcons.intl;
	else if (isOurs && (g.zoneCode == 2))
		opts = golgotha.routePlot.gateIcons.uspfi;
	else if (isOurs && (g.zoneCode == 3))
		opts = golgotha.routePlot.gateIcons.eu;
	else if (isOurs)
		opts = golgotha.routePlot.gateIcons.ours;
	else if (g.useCount > 0)
		opts = golgotha.routePlot.gateIcons.pop;

	if (g.info) opts.info = g.info;
	opts.pt = g.ll;
	const gmrk = new golgotha.maps.IconMarker(opts);
	gmrk.gate = g.name;
	return gmrk;
};

golgotha.routePlot.updateRoutes = function(combo, data)
{
// Save the old value
if (!combo) return false;
const oldCode = golgotha.form.getCombo(combo);

// Update the combobox choices
combo.options.length = data.length + 1;
combo.options[0] = new Option('-', '');
for (var i = 0; i < data.length; i++) {
	const e = data[i];
	combo.options[i+1] = new Option(e.label, e.code);
	if ((oldCode == e.code) || (oldCode == e.label) || e.isSelected) combo.selectedIndex = (i+1);
}

return true;
};

golgotha.routePlot.updateGates = function(combo, data)
{
// Save the old value
if (!combo) return false;
const oldCode = golgotha.form.getCombo(combo);

// Update the combobox choices
combo.options.length = data.length + 1;
combo.options[0] = new Option('-', '');
for (var i = 0; i < data.length; i++) {
	const g = data[i];
	const o = new Option(g.name + ' [' + g.zone + '] (' + g.useCount + ' flights)', g.name);
	o.ll = g.ll;
	combo.options[i+1] = o; 
	if (oldCode == g.name) combo.selectedIndex = (i+1);
}

return true;
};

golgotha.routePlot.plotMap = function(myParams)
{
if (!golgotha.form.check()) return false;	
const xmlreq = new XMLHttpRequest();
xmlreq.timeout = 7500;
xmlreq.open('post', 'routeplot.ws', true);
xmlreq.setRequestHeader('Content-Type', 'application/json; charset=utf-8');
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false; 
	if (xmlreq.status != 200) {
		golgotha.form.clear();
		golgotha.form.showDialogMessage('Error ' + xmlreq.statusText + ' plotting route');
		return false;
	}
	
	// Draw the markers
	map.removeMarkers(golgotha.maps.displayedMarkers);
	if (map.hasLayer('FlightRoute')) map.removeLine('FlightRoute');
	const js = JSON.parse(xmlreq.responseText);
	js.positions.forEach(function(wp) {
		const mrk = (wp.pal) ? new golgotha.maps.IconMarker({pal:wp.pal,icon:wp.icon,info:wp.info,pt:wp.ll,label:wp.code}) : new golgotha.maps.Marker({color:wp.color,info:wp.info,pt:wp.ll,label:wp.code});
		mrk.setMap(map);
	});
	
	// Draw the route
	if (js.track.length > 1) {
		const rt = new golgotha.maps.Line('FlightRoute', {color:'#4080af', width:2, opacity:0.8}, js.track);
		map.addLine(rt);
	}

	// Get the midpoint and center the map
	const f = document.forms[0];
	if ((js.midPoint) && (js.distance) && (!f.noRecenter.checked)) {
		map.setCenter(js.midPoint.ll);
		map.setZoom(golgotha.maps.util.getDefaultZoom(js.distance));
	}

	// Set the distance
	const distUnit = js.distanceUnit || {id:'SM',name:'Statute Mile',factor:1.0};
	const dstE = document.getElementById('rtDistance');
	if ((dstE) && (js.distance > 0)) {
		let distDelta = (js.distance - js.gcDistance) * 1.0 / js.distance;
		distDelta = Math.round(distDelta * 10000) / 100;
		dstE.innerText = ' - ' + Math.round(js.distance * distUnit.factor) + ' ' + distUnit.name.toLowerCase() + 's';
		if (distDelta > 1)
			dstE.innerText += ' (' + distDelta + '% difference)';
	} else if (dstE)
		dstE.innerText = '';

	// Load the runways
	golgotha.routePlot.updateRoutes(f.runway, js.runways);
	golgotha.util.show('runways', (js.runways.length > 0));

	// Load the SID/STAR list
	golgotha.routePlot.updateRoutes(f.sid, js.sid);
	golgotha.util.display('sids', (js.sid.length > 0));
	golgotha.routePlot.updateRoutes(f.star, js.star);
	golgotha.util.display('stars', (js.star.length > 0));
	golgotha.event.beacon('Route Plotter', 'Update Routes');

	// Display ETOPS rating
	const etopsSpan = document.getElementById('rtETOPS');
	if (js.etops.warning || (js.etops.time > 75))
		etopsSpan.innerText = ' - ' + js.etops.rating;

	// Check for ETOPS warning
	if (js.etops.warning) {
		etopsSpan.innerText += ', AICRAFT IS RATED ' + js.etops.aircraftRating;
		const wmrk = new golgotha.maps.IconMarker({pal:js.etops.warnPoint.pal, icon:js.etops.warnPoint.icon, info:js.etops.warnPoint.info, pt:js.etops.warnPoint.ll});
		wmrk.setMap(map);

		// Draw the circle and line
		js.etops.airports.forEach(function(a) {
			const apmrk = new golgotha.maps.IconMarker({pal:a.pal,icon:a.icon,info:a.info,pt:a.ll});
			apmrk.setMap(map);
			const c = new golgotha.maps.Polygon('ETOPS-Range-' + a.icao, {color:'#801010',opacity:0.4,fillColor:'#601010',fillOpacity:0.15,width:1}, golgotha.maps.util.generateCircle(map, a.ll, js.etops.range));
			map.addLine(c);
			const wl = new golgotha.maps.Line('ETOPS-Warn-' + a.icao, {color:'red',width:1.25,opacity:0.55}, [a.ll,js.etops.warnPoint.ll]);
			map.addLine(wl);
		});
	}
	
	// Check for restricted airspace
	golgotha.routePlot.rsts = []; const asIDs = [];
	js.airspace.forEach(function(as) {
		const c = golgotha.routePlot.airspaceColors[as.type];
		const p = new golgotha.maps.Polygon('AS-' + as.id, {color:c.c,width:1,opacity:c.tx,fillColor:'#802020',fillOpacity:0.2}, as.border);
		p.info = as.info; p.ll = as.ll;
		map.addLine(p);
		golgotha.routePlot.rsts.push(p);
		asIDs.push(as.id);
	});

	// Display restricted airspace list
	golgotha.util.setHTML('aspaceList', asIDs.join(', '));
	golgotha.util.display('asWarnRow', (asIDs.length > 0));

	// Load the alternate list
	golgotha.util.display('airportL', (js.alternates.length > 0));
	if (js.alternates.length > 0) {
		const oldAL = golgotha.form.getCombo(f.airportL);
		golgotha.airportLoad.setOptions(f.airportL, js.alternates, golgotha.airportLoad.config);
		if (!f.airportL.setAirport(oldAL))
			f.airportLCode.value = '';
	}

	// Load the gates
	golgotha.routePlot.dGates.clear(); golgotha.routePlot.aGates.clear();
	golgotha.util.display('gatesD', (js.departureGates.length > 0));
	golgotha.util.display('gatesA', (js.arrivalGates.length > 0));
	golgotha.routePlot.updateGates(f.gateD, js.departureGates);
	golgotha.routePlot.updateGates(f.gateA, js.arrivalGates);
	js.arrivalGates.forEach(function(gateData) {
		const gmrk = golgotha.routePlot.generateGate(gateData, js.airline);
		golgotha.routePlot.aGates.add(gmrk);
		gmrk.getElement().addEventListener('dblclick', function(_e) {
			const g = this.marker.gate;
			golgotha.form.setCombo(f.gateA, g);
			window.setTimeout(function() { 
				golgotha.form.showDialogMessage('Arrival Gate set to ' + g, {className:'pri bld',timeout:2750});
				window.setTimeout(golgotha.routePlot.plotMap, 3250);
			}, 250);
		});
	});

	js.departureGates.forEach(function(gateData) {
		const gmrk = golgotha.routePlot.generateGate(gateData, js.airline);
		golgotha.routePlot.dGates.add(gmrk);
		gmrk.getElement().addEventListener('dblclick', function(_e) {
			const g = this.marker.gate;
			golgotha.form.setCombo(f.gateD, g);
			window.setTimeout(function() {
				golgotha.form.showDialogMessage('Departure Gate set to ' + g, {className:'pri bld',timeout:2750});	
				window.setTimeout(golgotha.routePlot.plotMap, 3250);	
			}, 250);
		});
	});

	// Get weather
	golgotha.util.display('wxDr', false);
	golgotha.util.display('wxAr', false);
	js.wx.forEach(function(wx) {
		const isTAF = (wx.type == 'taf');
		if (!isTAF) {
			golgotha.util.display((wx.dst ? 'wxAr' : 'wxDr'), true);
			const metarSpan = document.getElementById(wx.dst ? 'wxAmetar' : 'wxDmetar');
			golgotha.util.display(metarSpan, true);
			if (metarSpan)
				metarSpan.innerText = wx.info;
		} else {
			golgotha.util.display((wx.dst ? 'wxAr' : 'wxDr'), true);
			const tafSpan = document.getElementById('wxAtaf');
			golgotha.util.display(tafSpan, true);
			if (tafSpan)
				tafSpan.innerText = wx.info;
		}
	});
	
	// Show departure gates if required
	golgotha.routePlot.dGates.check(map.getZoom());
	golgotha.routePlot.aGates.check(map.getZoom());
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
let o = {};
const f = document.forms[0];
o.airportD = f.airportD.options[f.airportD.selectedIndex].value;
o.airportA = f.airportA.options[f.airportA.selectedIndex].value;
o.runway = f.runway.options[f.runway.selectedIndex].value;
o.external = (f.external) ? f.external.checked : false;
o.faReload = (f.forceFAReload) ? f.forceFAReload.checked : false;

// Generate an XMLHTTP request
const xmlreq = new XMLHttpRequest();
xmlreq.timeout = 2500;
xmlreq.open('post', 'dsproutes.ws', true);
xmlreq.setRequestHeader('Content-Type', 'application/json; charset=utf-8');
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	golgotha.util.disable('SearchButton', false);
	if (xmlreq.status != 200) {
		golgotha.form.showDialogMessage('Error ' + xmlreq.statusText + ' fetching routes');
		golgotha.form.clear();
		return false;
	}

	// Load the SID/STAR list
	const js = JSON.parse(xmlreq.responseText);
	const cbo = f.routes;
	cbo.options.length = js.routes.length + 1;
	cbo.options[0] = new Option('-', '');
	for (var x = 0; x < js.routes.length; x++) {
		const rt = js.routes[x];
		let opt = new Option(rt.name, rt.waypoints);
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
	const f = document.forms[0];
	if (combo.selectedIndex < 1) {
		if (!golgotha.routePlot.isDraft)
			f.cruiseAlt.value = '';
		if (!golgotha.routePlot.keepRoute) {
			f.route.value = '';
			f.sid.selectedIndex = 0;
			f.star.selectedIndex = 0;
		}

		f.comments.value = ''
		if (f.routeID) f.routeID.value = '0';
		golgotha.routePlot.plotMap();
		return true;
	}

// Update the route
try {
	const opt = combo.options[combo.selectedIndex];
	f.cruiseAlt.value = opt.altitude;
	f.route.value = opt.value;
	f.comments.value = opt.comments || '';
	golgotha.form.setCombo(f.sid, opt.SID);
	golgotha.routePlot.setTR(f.star, opt.STAR);
	golgotha.routePlot.isDraft = false;
	if (f.routeID)
		f.routeID.value = opt.routeID;
} catch (err) {
	console.log(err);
	golgotha.form.showDialogMessage('Error setting route - ' + err.description);
}

golgotha.util.disable('RouteSaveButton', false);
golgotha.routePlot.plotMap();
golgotha.event.beacon('Route Plotter', 'Set Route');
return true;
};

golgotha.routePlot.setTR = function(cb, v) {
	if ((!cb) || (!v)) return false;
	let rwyIdx = 999;
	console.log('Input is ' + v);
	const hasRwys = (golgotha.routePlot.aRwys.length > 0);
	if (v.indexOf('.') < v.lastIndexOf('.')) {
		v = v.substring(0, v.lastIndexOf('.'));
		console.log('Adjusted to ' + v);
	}

	cb.selectedIndex = -1;
	for (var x = 0; x < cb.options.length; x++) {
		const ov = cb.options[x].value;
		if (!ov.startsWith(v)) continue;
		const rw = ov.substring(ov.lastIndexOf('.') + 3);
		const idx = golgotha.routePlot.aRwys.indexOf(rw);

		// Get index of rwy name in aRwys, and save one with highest index
		if ((idx > -1) && (idx < rwyIdx)) {
			rwyIdx = idx;
			cb.selectedIndex = x;
		} else if (!hasRwys) { // if no arrival runways, ¯\_(ツ)_/¯
			cb.selectedIndex = x;
			return true;
		}
	}
	
	return false;
};

golgotha.routePlot.updateRoute = function(airportsChanged, rwyChanged)
{
const f = document.forms[0];
golgotha.routePlot.routeUpdated = true;
if (rwyChanged) {
	f.runway.selectedIndex = 0;
	f.runway.options.length = 1;
}

if (airportsChanged) {
	f.routes.options.length = 1;
	f.routes.options[0] = new Option('No Routes Loaded', '');
	f.routes.selectedIndex = 0;
	golgotha.routePlot.keepRoute = golgotha.routePlot.keepRoute || golgotha.form.comboSet(f.sid) || (f.route.value.length > 2); 
	golgotha.util.show('routeList', false);
	golgotha.routePlot.setRoute(f.routes);
}

golgotha.util.disable('SearchButton', (f.airportD.selectedIndex == 0) || (f.airportA.selectedIndex == 0));
golgotha.util.disable('RouteSaveButton', (f.route.value.length <= 2));
return true;
};

golgotha.routePlot.checkZoom = function(e) {
	const zl = e.target.getZoom();
	golgotha.routePlot.dGates.check(zl);
	golgotha.routePlot.aGates.check(zl);
};

golgotha.routePlot.GateManager = function(minZoom) { this._minZoom = Math.max(1,minZoom); this.shown = false; this.visible = false; this._gates = []; };
golgotha.routePlot.GateManager.prototype.add = function(mrk) { this._gates.push(mrk); };
golgotha.routePlot.GateManager.prototype.clear = function() { this.hide(); this._gates = []; };
golgotha.routePlot.GateManager.prototype.toggle = function() { return (this.visible == true) ? this.hide() : this.display(); };
golgotha.routePlot.GateManager.prototype.display = function() {
	if (this.visible) return false;
	map.addMarkers(this._gates);
	this.visible = true;
	return true;
};

golgotha.routePlot.GateManager.prototype.hide = function() {
	if (!this.visible) return false;
	map.removeMarkers(this._gates);
	this.visible = false;
	return true;
};

golgotha.routePlot.GateManager.prototype.check = function(zoom) {
	if (!this.shown && this.visible)
		this.hide();
	else if ((zoom < this._minZoom) && this.visible)
		this.hide();
	else if ((zoom > this._minZoom) && !this.visible)
		this.display();
};

golgotha.routePlot.GateManager.prototype.center = function() {
	let tLat = 0; let tLng = 0;
	this._gates.forEach(function(gt) { const ll = gt.getLngLat(); tLat += ll.lat; tLng += ll.lng; });
	return [(tLng / this._gates.length), (tLat / this._gates.length)];
}

golgotha.routePlot.toggleGates = function(gts, doPan) {
	gts.shown = doPan;
	if (doPan == true) {
		map.jumpTo({center:gts.center(), zoom:Math.max(14, map.getZoom())}); // this will make them show up
		gts.check(map.getZoom());
	} else
		gts.toggle();

	golgotha.util.display('gateLegendRow', golgotha.routePlot.gatesVisible());
	return true;
};

golgotha.routePlot.validateBlob = function(f) {
	try {
		golgotha.routePlot.hasBlob = !!new Blob;
	} catch (e) {}

	// Reset form links if blob download supported
	if (golgotha.routePlot.hasBlob) {
		f.onsubmit = function() { return false; };
		const btn = document.getElementById('SaveButton');
		btn.onclick = golgotha.routePlot.download;
	}

	return true;
};

golgotha.routePlot.togglePax = function() {
	const f = document.forms[0];
	const isOK = golgotha.form.comboSet(f.eqType) && golgotha.form.comboSet(f.airportD) && golgotha.form.comboSet(f.airportA);
	golgotha.util.disable(f.precalcPax, !isOK || !f.saveDraft.checked);
	return isOK && f.saveDraft.checked;
};

golgotha.routePlot.updateSave = function(isSave) {
	const btn = document.getElementById('SaveButton');
	const msg = (isSave ? 'SAVE' : 'DOWNLOAD') + ' FLIGHT PLAN';
	btn.textContent = msg; btn.value = msg; 
	return true;
};

golgotha.routePlot.download = function() {
	if (!golgotha.form.wrap(golgotha.local.validate, document.forms[0])) return false;
	const btn = document.getElementById('SaveButton');
	const xmlreq = new XMLHttpRequest();
	xmlreq.timeout = 4500;
	xmlreq.open('post', '/routeplan.ws', true);
	xmlreq.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
	xmlreq.responseType = 'blob';
	xmlreq.onreadystatechange = function() {
		if (xmlreq.readyState != 4) return false;
		if (xmlreq.status != 200) {
			golgotha.form.showDialogMessage('Error ' + xmlreq.statusText + ' generating flight plan');
			btn.disabled = false;
			return false;
		}

		const noRecalc = (xmlreq.getResponseHeader('X-Plan-No-Recalc') == 1);
		golgotha.util.disable(f.precalcPax, noRecalc);
		const noFP = (xmlreq.getResponseHeader('X-Plan-Empty') == 1);
		if (noFP) {
			golgotha.form.showDialogMessage('Draft Flight Report Updated', {className:'pri bld'});
			btn.disabled = false;
			return true;
		}

		const ct = xmlreq.getResponseHeader('Content-Type');
		const b = new Blob([xmlreq.response], {type:ct.substring(0, ct.indexOf(';')), endings:'native'});
		saveAs(b, xmlreq.getResponseHeader('X-Plan-Filename'));
		btn.disabled = false;
		return true;
	};

	// Generate parameters
	btn.disabled = true;
	const params = golgotha.util.createURLParams(golgotha.routePlot.getAJAXParams());
	xmlreq.send(params);
	return true;
};
