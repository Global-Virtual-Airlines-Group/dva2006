golgotha.gate = golgotha.gate || {ids:[],gates:[],mrks:{},isDirty:[],data:[],zones:{}};
golgotha.gate.icons = {ours:{pal:2,icon:56,tx:0.8},intl:{pal:2,icon:48,tx:0.8},pop:{pal:3,icon:52,tx:0.675},other:{pal:3,icon:60,tx:0.425},uspfi:{pal:2,icon:16,tx:0.7},schengen:{pal:2,icon:17,tx:0.7}};
golgotha.gate.markDirty = function(id) { if (!golgotha.gate.isDirty.contains(id)) golgotha.gate.isDirty.push(id); };

golgotha.gate.load = function(opts) {
	let url = 'gates.ws?id=' + opts.id;
	if (opts.airportA) url += '&aa=' + opts.airportA;
	if (opts.forceReload) url += '&time=' + golgotha.util.getTimestamp(3000);
	const p = fetch(url, {signal:AbortSignal.timeout(2500)});
	p.then(function(rsp) {
		if (rsp.status != 200) return false;
		rsp.json().then(function(js) {
			js.zones.forEach(function(z) { golgotha.gate.zones[z.id] = z; });
			golgotha.gate.airportD = js.airportD; 
			golgotha.gate.ids = [];
			golgotha.gate.maxUse = js.maxUse;
			golgotha.gate.airlines = js.airlines;
			js.gates.forEach(function(g) { golgotha.gate.data[g.id] = g; golgotha.gate.ids.push(g.id); });
			golgotha.gate.display();	
		});
	});
};

golgotha.gate.getOptions = function(g) {
	let opts = golgotha.gate.icons.other;
	const isOurs = (g.airlines.length > 0);
	if (isOurs && (g.zone == 'USPFI'))
		opts = golgotha.gate.icons.uspfi;
	else if (isOurs && (g.zone == 'SCHENGEN'))
		opts = golgotha.gate.icons.schengen;
	else if (isOurs && (g.zone == 'INTERNATIONAL'))
		opts = golgotha.gate.icons.intl;
	else if (isOurs)
		opts = golgotha.gate.icons.ours;
	else if (g.useCount > (golgotha.gate.maxUse / 10))
		opts = golgotha.gate.icons.pop;

	return opts;
};

golgotha.gate.setValues = function(id) {
	const g = golgotha.gate.data[id]; 
	const f = document.forms[0];
	const zcb = f['zoneSelect-' + g.id]; zcb.selectedIndex = g.zone;
	const acb = f['gateSelect-' + g.id];
	acb.forEach(function(ch) { ch.checked = g.airlines.contains(ch.value); });
	return true;
};

golgotha.gate.display = function(al) {
	map.removeMarkers(golgotha.gate.gates);
	golgotha.gate.gates = []; golgotha.gate.mrks = {};
	const llb = new mapboxgl.LngLatBounds();
	golgotha.gate.ids.forEach(function(id) {
		const g = golgotha.gate.data[id]; const opts = golgotha.gate.getOptions(g);
		g.isOurs = (((al == null) && (g.airlines.length > 0)) || g.airlines.contains(al));
		if ((!al) || (g.isOurs && (al))) llb.extend(g.ll);
		const mrk = new golgotha.maps.IconMarker({pal:opts.pal,icon:opts.icon,opacity:opts.tx,info:g.info, pt:g.ll});
		golgotha.gate.mrks[id] = mrk; mrk.markerID = g.id;
		mrk.updateTab = golgotha.maps.util.updateTab; mrk.tabs = g.tabs;
		if (golgotha.gate.showTabs && (mrk.tabs)) {
			mrk.getElement().addEventListener('click', function(_e) {
				const mrk = this.marker;
				mrk.updateTab(1);
				//setTimeout('golgotha.gate.setValues("' + mrk.markerID + '")', 20);
				window.setTimeout(golgotha.gate.setValues, 20, mrk.markerID);
				golgotha.maps.selectedMarker = mrk;
			});
		}

		golgotha.gate.gates.push(mrk);
	});

	map.addMarkers(golgotha.gate.gates);
	if (!llb.isEmpty()) map.fitBounds(llb);
	return true;
};

golgotha.gate.save = function() {
	const xreq = new XMLHttpRequest();
	xreq.timeout = 3500;
	xreq.open('post', 'gateupdate.ws?id=' + golgotha.gate.airportD.icao, true);
	xreq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=utf-8');
	xreq.onreadystatechange = function() {
		if (xreq.readyState != 4) return false;
		if (xreq.status != 200) {
			alert('Error ' + xreq.status + ' updating Gate Data!');
			return false;
		}

		console.log(xreq.getResponseHeader('updatedGates') + ' gates updated');
		golgotha.util.display('buttonRow', false);
		golgotha.gate.isDirty = [];
		return true;
	};

	// Convert associative array to object
	const data = [];
	golgotha.gate.isDirty.forEach(function(k) { const gd = golgotha.gate.data[k]; data.push({id:gd.name,airlines:gd.airlines,zone:gd.zone}); });
	xreq.send('data=' + JSON.stringify(data));
	return true;
};

golgotha.gate.undo = function() {
	golgotha.gate.isDirty = [];
	golgotha.gate.load({id:golgotha.gate.airportD.icao});
	return true;
};

golgotha.gate.updateZone = function(cb) {
	const id = cb.name.substring(cb.name.indexOf('-') + 1);
	const g = golgotha.gate.data[id];
	g.zone = golgotha.form.getCombo(cb);

	const mrk = golgotha.gate.mrks[id]; const opts = golgotha.gate.getOptions(g);
	const mrk2 = new golgotha.maps.IconMarker({pal:opts.pal,icon:opts.icon,opacity:opts.tx,pt:mrk.getLngLat()});
	mrk.getElement().innerHTML = mrk2.getElement().innerHTML;
	
	console.log('Gate ' + g.name + ' set to ' + golgotha.gate.zones[g.zone].description);
	golgotha.gate.markDirty(id);
	golgotha.util.display('buttonRow', true);
	return true;
};

golgotha.gate.updateGateAirline = function(cb) {
	const id = cb.name.substring(cb.name.indexOf('-') + 1);
	const g = golgotha.gate.data[id];
	if (cb.checked && !g.airlines.contains(cb.value))
		g.airlines.push(cb.value);
	else if (!cb.checked)
		g.airlines.remove(cb.value);

	const mrk = golgotha.gate.mrks[id];  const opts = golgotha.gate.getOptions(g);
	const mrk2 = new golgotha.maps.IconMarker({pal:opts.pal,icon:opts.icon,opacity:opts.tx,pt:mrk.getLngLat()});
	mrk.getElement().innerHTML = mrk2.getElement().innerHTML;
	
	console.log('Gate ' + g.name + ' airlines set to ' + g.airlines);
	golgotha.gate.markDirty(id);
	golgotha.util.display('buttonRow', true);
	return true;
};
