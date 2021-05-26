golgotha.gate = golgotha.gate || {ids:[],gates:[],ourGates:[],isRoute:false,isEdit:false,isDirty:[],data:[]};
golgotha.gate.icons = {ours:{pal:2,icon:56,tx:1},intl:{pal:2,icon:48,tx:1},pop:{pal:3,icon:52,tx:0.75},other:{pal:3,icon:60,tx:0.65},uspfi:{pal:2,icon:16,tx:0.75}};
golgotha.gate.markDirty = function(id) { if (!golgotha.gate.isDirty.contains(id)) golgotha.gate.isDirty.push(id); };
golgotha.gate.clearListeners = function(m) { google.maps.event.clearInstanceListeners(m); };
golgotha.gate.toggleAirline = function(al) {
	const wasOurs = this.airlines.remove(al); 
	if (!wasOurs)  this.airlines.push(al); 
	return !wasOurs;
};

golgotha.gate.load = function(opts)
{
opts.cf = opts.cf || golgotha.gate.display;
let url = 'gates.ws?id=' + opts.id;
if (opts.airportA)
	url += '&aa=' + opts.airportA;
if (opts.forceReload)
	url += '&time=' + golgotha.util.getTimestamp(3000);

const xreq = new XMLHttpRequest();
xreq.open('get', url, true);
xreq.onreadystatechange = function() {
	if ((xreq.readyState != 4) || (xreq.status != 200)) return false;
	const jsData = JSON.parse(xreq.responseText);
	golgotha.gate.zones = jsData.zones;
	golgotha.gate.airportD = jsData.airportD; 
	golgotha.gate.ids = [];
	golgotha.gate.maxUse = jsData.maxUse;
	jsData.gates.forEach(function(g) { golgotha.gate.data[g.id] = g; golgotha.gate.ids.push(g.id); });
	opts.cf();
	return true;
};

xreq.send(null);
return true;
};

golgotha.gate.display = function(al, ff) {
	ff = ff || function() { return true; };
	golgotha.gate.gates.forEach(golgotha.gate.clearListeners);
	golgotha.gate.ourGates.forEach(golgotha.gate.clearListeners);
	map.removeMarkers(golgotha.gate.gates);
	map.removeMarkers(golgotha.gate.ourGates);
	golgotha.gate.gates = []; golgotha.gate.ourGates = [];
	const llb = new google.maps.LatLngBounds();
	golgotha.gate.ids.forEach(function(id) {
		const g = golgotha.gate.data[id]; let opts = golgotha.gate.icons.other;
		g.isOurs = (((al == null) && (g.airlines.length > 0)) || g.airlines.contains(al));
		if (!ff(g)) return false;

		if (g.isOurs && (g.zone == 2))
			opts = golgotha.gate.icons.uspfi;
		else if (g.isOurs && (g.zone == 1))
			opts = golgotha.gate.icons.intl;
		else if (g.isOurs)
			opts = golgotha.gate.icons.ours;
		else if (g.useCount > 0)
			opts = golgotha.gate.icons.pop;

		llb.extend(g.ll);
		const mrk = new golgotha.maps.IconMarker({pal:opts.pal,icon:opts.icon,opacity:opts.tx,info:g.info}, g.ll);
		mrk.gateID = id; g.toggleAirline = golgotha.gate.toggleAirline;
		const dst = g.isOurs ? golgotha.gate.ourGates : golgotha.gate.gates;
		if (golgotha.gate.isEdit) {
			google.maps.event.addListener(mrk, 'dblclick', golgotha.gate.toggleOurs);
			if (g.isOurs)
				google.maps.event.addListener(mrk, 'rightclick', golgotha.gate.toggleZone);
		}

		dst.push(mrk);
	});

	map.addMarkers(golgotha.gate.gates);
	map.addMarkers(golgotha.gate.ourGates);
	map.fitBounds(llb);
	return true;
};

golgotha.gate.edit = function() {
	golgotha.gate.isEdit = true;
	const f = document.forms[0];
	f.airline.selectedIndex = 0;
	f.airportA.selectedIndex = 0;
	golgotha.util.display('editLink', false);
	golgotha.util.display('viewLink', true);
	golgotha.util.display('airlineCombo', true);
	golgotha.util.display('airportARow', false);
	golgotha.gate.load({id:golgotha.gate.airportD.icao,cf:golgotha.gate.display});
	map.setCenter(golgotha.gate.airportD);
	return true;
};

golgotha.gate.view = function() {
	golgotha.gate.isEdit = false;
	golgotha.util.display('airlineCombo', false);
	golgotha.util.display('saveLink', false);
	golgotha.util.display('helpText', false);
	golgotha.util.display('viewLink', false);
	golgotha.util.display('editLink', true);
	golgotha.util.display('airportARow', true);
	return true;
};

golgotha.gate.save = function() {
	const xreq = new XMLHttpRequest();
	xreq.open('post', 'gateupdate.ws?id=' + golgotha.gate.airportD.icao, true);
	xreq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=utf-8');
	xreq.onreadystatechange = function() {
		if (xreq.readyState != 4) return false;
		if (xreq.status != 200) {
			alert('Error updating Gate Data!');
			return false;
		}

		golgotha.util.display('saveLink', false);
		golgotha.util.display('helpText', false);
		golgotha.gate.isDirty = [];
		return true;
	};

	// Convert associative array to object
	let data = [];
	golgotha.gate.isDirty.forEach(function(k) { const gd = golgotha.gate.data[k]; data.push({id:k,airlines:gd.airlines,zone:gd.zone}); });
	xreq.send('data=' + JSON.stringify(data));
	return true;
};

golgotha.gate.updateAirline = function(combo) {
	if (golgotha.gate.isEdit) return false;
	golgotha.util.display('helpText', (combo.selectedIndex > 0));
	if (combo.selectedIndex < 1) {
		delete golgotha.gate.airline;
		golgotha.gate.display();
		return false;
	}

	let alName = combo.options[combo.selectedIndex].text;
	if (alName.indexOf('- ') > 0)
		alName = alName.substring(alName.indexOf('- ') + 2);

	const name = document.getElementById('airlineName');
	name.innerHTML = alName;
	golgotha.gate.airline = golgotha.form.getCombo(combo);
	golgotha.gate.display(golgotha.gate.airline);
	return true;
};

golgotha.gate.filter = function(g, opts) {
	const minUse = Math.round((golgotha.gate.maxUse * opts.minPct / 100) + 0.5);
	return (!opts.oursOnly || g.isOurs) && (g.useCount > minUse);
};

golgotha.gate.toggleOurs = function() {
	if (!golgotha.gate.airline) return false;
	const g = golgotha.gate.data[this.gateID];
	const isOurs = g.toggleAirline(golgotha.gate.airline);
	if (!isOurs && (g.zone > 1) && (g.airlines.length == 0))
		g.zone = 0;

	golgotha.gate.markDirty(this.gateID);
	golgotha.util.display('saveLink', true);
	golgotha.gate.display(golgotha.gate.airline);
	return true;
};

golgotha.gate.toggleZone = function() {
	if (!golgotha.gate.airline) return false;
	const g = golgotha.gate.data[this.gateID];
	const isOurs = g.airlines.contains(golgotha.gate.airline);
	if (!isOurs)
		g.airlines.push(golgotha.gate.airline);
	
	const z = g.zone + 1;
	g.zone = (z > (golgotha.gate.zones.length - 1)) ? 0 : z;
	console.log('Gate ' + g.id + ' set to ' + golgotha.gate.zones[g.zone].description);
	golgotha.gate.markDirty(this.gateID);
	golgotha.util.display('saveLink', true);
	golgotha.gate.display(golgotha.gate.airline);
	return true;
};

golgotha.local.updateAirportA = function(combo, departureICAO) {
	if (!golgotha.form.comboSet(combo)) {
		golgotha.gate.load({id:departureICAO,cf:golgotha.gate.display});
		return true;
	}

	const showFunc = function() { golgotha.gate.display(null, function(g) { return golgotha.gate.filter(g, {oursOnly:true,minPct:15}); }); }; 
	golgotha.gate.load({id:departureICAO, forceReload:true, airportA:golgotha.form.getCombo(combo), cf:showFunc });
	return true;
};