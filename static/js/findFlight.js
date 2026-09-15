golgotha.ff = golgotha.ff || {airlines:{}};
golgotha.ff.updateAirline = function(cb) {
	const f = document.forms[0];
	const cfg = golgotha.airportLoad.config.clone();
	cfg.airline = golgotha.form.getCombo(cb);
	const ao = golgotha.ff.airlines[cfg.airline];
	const hasAssoc = (ao) && (ao.associated.length > 0);
	golgotha.util.disable(f.allPrimary, !hasAssoc);
	golgotha.airportLoad.changeAirline([f.airportD], cfg);
	golgotha.util.show('historicOpts', !golgotha.form.comboSet(f.airline));
	window.setTimeout(function() {
		const cfg2 = cfg.clone();
    	cfg2.dst = true;
    	golgotha.airportLoad.changeAirline([f.airportA], cfg2);
	}, 250);
	return true;
};

golgotha.ff.updateFamily = function() { golgotha.form.setCombo(document.forms[0].eqType, '-'); };
golgotha.ff.updateEQ = function() { golgotha.form.setCombo(document.forms[0].family, '-'); };
golgotha.ff.updateSort = function(cb) { return golgotha.util.disable('sortDesc', !golgotha.form.comboSet(cb)); };
golgotha.ff.refreshAirports = function() { updateAirline(document.forms[0].airline); };
golgotha.ff.refreshNV = function(checkbox, cboName, isDest) {
	const f = checkbox.form;
	const srcA = golgotha.form.getCombo(f.airportD);
	const cfg = golgotha.airportLoad.config.clone();
	cfg.airline = golgotha.form.getCombo(f.airline); cfg.notVisited = checkbox.checked;
	if (isDest && (srcA != null) && (srcA != '')) {
		cfg.dst = true;	
		cfg.code = srcA;
	}

	const cbo = f[cboName];
	if (cbo) {
		cbo.notVisited = cfg.notVisited;
		cbo.loadAirports(cfg);
	}

	return true;
};

golgotha.ff.loadAirlines = function() {
	const p = fetch('airlines.ws', {signal:AbortSignal.timeout(5000)});
	p.then(function(rsp) {
		if (!rsp.ok) return false;
		rsp.json().then(function(js) {
			for (var x = 0; x < js.length; x++) {
				const ao = js[x];
				golgotha.ff.airlines[ao.code] = ao;
			}
		});
	});
};

golgotha.ff.getData = function() {
	const dd = localStorage.getItem('ff.settings');
	if (!dd) return false;
	try {
		const d = JSON.parse(dd);
		return d;
	} catch (err) {
		console.log('Cannot parse parameters - ' + err.message);
		golgotha.ff.clear();
		return null;
	}
};

golgotha.ff.load = function(f) {
	const d = golgotha.ff.getData();
	if (!d) return false;
	f.nVD.checked = d.nv.d;
	f.nVA.checked = d.nv.a;
	f.flightNumber.value = d.fn;
	f.distance.value = d.distance;
	f.maxResults.value = d.maxResults;
	f.maxFlights.value = d.maxFlights;
	golgotha.form.setCombo(f.airline, d.airline);
	f.airline.onchange();
	golgotha.form.setCombo(f.hourD, d.hourD);
	golgotha.form.setCombo(f.hourA, d.hourA);
	golgotha.form.setCombo(f.sortType, d.sortType);
	golgotha.form.setCombo(f.historicOnly, d.historicOnly);
	golgotha.form.setCombo(f.family, d.family);
	golgotha.form.setCombo(f.eqType, d.eqType);
	window.setTimeout(function() {
		f.airportD.setAirport(d.airportD, true);
		window.setTimeout(function() { f.airportA.setAirport(d.airportA, true); }, 450);
	}, 450);

	console.log(JSON.stringify(d));
	return true;
};

golgotha.ff.save = function(f) {
	const d = {};
	d.airline = golgotha.form.getCombo(f.airline);
	d.fn = f.flightNumber.value;
	d.distance = f.distance.value;
	d.family = golgotha.form.getCombo(f.family);
	d.eqType = golgotha.form.getCombo(f.eqType);
	d.airportD = golgotha.ff.getAirport(f.airportD);
	d.airportA = golgotha.ff.getAirport(f.airportA);
	d.hourD = golgotha.form.getCombo(f.hourD);
	d.hourA = golgotha.form.getCombo(f.hourA);
	d.sortType = golgotha.form.getCombo(f.sortType);
	d.historicOnly = golgotha.form.getCombo(f.historicOnly);
	d.maxResults = f.maxResults.value;
	d.maxFlights = f.maxFlights.value;
	d.nv = {a:f.nVA.checked, d:f.nVD.checked};
	localStorage.setItem('ff.settings', JSON.stringify(d));
	return true;
};

golgotha.ff.clear = function() {
	localStorage.removeItem('ff.settings');
	return true;
};

golgotha.ff.getAirport = function(combo) {
	if (combo.selectedIndex < 1) return null;
	const o = combo.options[combo.selectedIndex];
	return golgotha.airportLoad.config.getCode(o);
};

golgotha.ff.validate = function(f) {
	if (!golgotha.form.check()) return false;
	if (!golgotha.form.comboSet(f.eqType) && !golgotha.form.comboSet(f.airline) && !golgotha.form.comboSet(f.airportD) && !golgotha.form.comboSet(f.airportA))
		throw new golgotha.event.ValidationError('Please select at least an Airline, Aircraft Type or Departure/Arrival Airport.', f.airline);

	golgotha.form.submit(f);
	if (f.saveSearch.checked)
		golgotha.ff.save(f);
	else
		golgotha.ff.clear();

	return true;
};
