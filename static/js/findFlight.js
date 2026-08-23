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
		return null;
	}
};

golgotha.ff.load = function(f) {
	const d = golgotha.ff.getData();
	if (!d) return false; 
	golgotha.form.setCombo(f.airline, d.airline);
	f.flightNumber.value = df.fn;
	f.distance.value = d.distance;
	golgotha.form.setCombo(f.family, d.family);
	golgotha.form.setCombo(f.eqType, d.eqType);
	golgotha.form.setCombo(f.airportD, d.airportD);
	golgotha.form.setCombo(f.airportA, d.airportA);
	golgotha.form.setCombo(f.hourD, d.hourD);
	golgotha.form.setCombo(f.hourA, d.hourA);
	golgotha.form.setCombo(f.sortType, d.sortType);
	f.maxresults.value = d.maxReuslts;
	f.maxFlights.value = d.maxFlights;
	console.log('Restored search parameters');
	return true;
};

golgotha.ff.save = function(f) {
	const d = {};
	d.airline = golgotha.form.getCombo(f.airline);
	d.fn = f.flightNumber.value;
	d.distance = f.distance.value;
	d.family = golgotha.form.getCombo(f.family);
	d.eqType = golgotha.form.getCombo(f.eqType);
	d.airportD = golgotha.form.getCombo(f.airportD);
	d.airportA = golgotha.form.getCombo(f.airportA);
	d.hourD = golgotha.form.getCombo(f.hourD);
	d.hourA = golgotha.form.getCombo(f.hourA);
	d.sortType = golgotha.form.getCombo(f.sortType);
	d.maxResults = f.maxResults.value;
	d.maxFlights = f.maxFlights.value;
	localStorage.setItem('ff.settings', JSON.stringify(d));
	console.log('Saved search parameters');
	return true;
};
