golgotha.tour = golgotha.tour || {};

golgotha.tour.deleteRows = function() {
	const t = document.getElementById('searchTable');
	const rows = golgotha.util.getElementsByClass('searchResultEntry', 'tr');
	rows.forEach(function(r) { t.deleteRow(r.rowIndex); });
	return true;
};

golgotha.tour.deleteLeg = function(idx) {
	const f = document.forms[0];
	const t = document.getElementById('baseTable');
	const r = document.getElementById('legRow-' + idx);
	if (r) t.deleteRow(r.rowIndex);
	f.legCodes.value = golgotha.tour.buildLegCodes();
	golgotha.util.display('tourEmpty', (golgotha.tour.legs() < 1));
	return true;
};

golgotha.tour.legs = function() {
	const lc = document.forms[0].legCodes;
	if ((!lc) || (lc.value.length < 3)) return 0;
	const codes = lc.value.split(',');
	return codes.length;
};

golgotha.tour.buildLegCodes = function() {
	const rows = golgotha.util.getElementsByClass('legRow', 'tr', document.getElementById('baseTable'));
	const legInfo = [];
	rows.forEach(function(r) { legInfo.push(r.flight); });
	return JSON.stringify(legInfo);
};

golgotha.tour.code = function(a) { return golgotha.airportLoad.config.doICAO ? a.icao : a.iata; };
golgotha.tour.renderRoute = function(se) { return se.airportD.name + ' (' + golgotha.tour.code(se.airportD) + ') - ' + se.airportA.name + ' (' + golgotha.tour.code(se.airportA) + ')'; };
golgotha.tour.renderDuration = function(se) {
	const dH = Math.floor(se.duration / 3600000); const dM = (se.duration % 3600000) / 60000;
	return '(' + dH + 'h ' + dM + 'm)';
};

golgotha.tour.renderTime = function(se) { return se.timeD.text + ' - ' + se.timeA.text + ' ' + golgotha.tour.renderDuration(se);; };
golgotha.tour.clearCustomLeg = function() { delete golgotha.tour.customLog; golgotha.util.display('customLeg', false); return true; };
golgotha.tour.showCustomFields = function(doShow) {
	const srn = golgotha.util.getElementsByClass('searchResultNone', 'tr');
	srn.forEach(function(e) { golgotha.util.display(e, doShow); });
	golgotha.util.display('legWarnRow', false);
	return true;
};

golgotha.tour.clearCustomFields = function() {
	const f = document.forms[0];
	f.airline.selectedIndex = 0;
	f.eq.selectedIndex = 0;
	f.flightNumber.value = '';
	f.flightLeg.value = '';
	f.flightTimeD.value = '';
	f.flightTimeA.value = '';
	f.airportD.selectedIndex = 0;
	f.airportA.selectedIndex = 0;
	f.airportDCode.value = '';
	f.airportACode.value = '';
	return true;
};

golgotha.tour.validateTime = function(opts) {
	const v = opts.f.value; const pos = v.indexOf(':');
	if (v.length < 4) throw new golgotha.event.ValidationError('Please provide the ' + opts.t, opts.f);
	if (pos < 1) throw new golgotha.event.ValidationError('Please provide a valid ' + opts.t, opts.f);
	const h = parseInt(v.substring(0, pos)); const m = parseInt(v.substring(pos + 1));
	if (isNaN(h) || isNaN(m)) throw new golgotha.event.ValidationError('Unparseable ' + opts.t + ' - ' + v, opts.f);
	return true;
};

golgotha.tour.addLeg = function(se) {
	golgotha.util.display('tourEmpty', false);
	const code = se.airline + se.flight + ' Leg ' + se.leg; 
	const t = document.getElementById('baseTable');
	const r = t.insertRow(-1); r.className = 'legRow mid'; let idx = r.rowIndex; r.flight = se; r.id = 'legRow-' + idx;
	let c = r.insertCell(0);
	const btn = document.createElement('button'); btn.className = 'button';
	btn.innerHTML = 'DELETE'; btn.onclick = function() { golgotha.tour.deleteLeg(idx); };
	c.appendChild(btn);
	c = r.insertCell(1); c.className = 'pri bld'; c.innerHTML = code;
	c = r.insertCell(2); c.className = 'sec bld'; c.innerHTML = se.eqType;
	c = r.insertCell(3); c.className = 'small'; c.innerHTML = golgotha.tour.renderRoute(se);
	c = r.insertCell(4); c.className = 'bld';  c.innerHTML = golgotha.tour.renderTime(se);
	document.forms[0].legCodes.value = golgotha.tour.buildLegCodes();
	golgotha.tour.clearCustomFields();
	return true;
};

golgotha.tour.validateCustom = function(f) {
	if (!golgotha.form.comboSet(f.airportD) && !golgotha.form.comboSet(f.airportA)) return false;
	golgotha.tour.validateTime({f:f.flightTimeD, t:'Departure Time'});
	golgotha.tour.validateTime({f:f.flightTimeA, t:'Departure Time'});
	golgotha.form.validate({f:f.airline, t:'Airline'});
	golgotha.form.validate({f:f.flightNumber, min:1, t:'Flight Number'});
	golgotha.form.validate({f:f.flightLeg, min:1, t:'Flight Leg'});
	golgotha.form.validate({f:f.eq, t:'Equipment Type'});
	golgotha.form.validate({f:f.airportD, t:'Departure Airport'});
	golgotha.form.validate({f:f.airportA, t:'Arrival Airport'});
	return true;
};

golgotha.tour.searchCustom = function() {
	const f = document.forms[0];
	const isOK = golgotha.form.wrap(golgotha.tour.validateCustom, f);
	if (!isOK) return false;

	// Build the request
	const params = [];
	params.push('airportD=' + golgotha.form.getCombo(f.airportD));
	params.push('airportA=' + golgotha.form.getCombo(f.airportA));
	params.push('eqType=' + golgotha.form.getCombo(f.eq));
	params.push('airline=' + golgotha.form.getCombo(f.airline));
	params.push('flight=' + f.flightNumber.value);
	params.push('leg=' + f.flightLeg.value);
	params.push('timeD=' + f.flightTimeD.value);
	params.push('timeA=' + f.flightTimeA.value);

	const xmlreq = new XMLHttpRequest();
	xmlreq.open('post', 'tourleg.ws', true);
	xmlreq.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
	xmlreq.onreadystatechange = function() {
		if (xmlreq.readyState != 4) return false;
		if (xmlreq.status != 200) {
			golgotha.util.setHTML('customResultMsg', ' - ERROR ' + xmlreq.status);
			golgotha.form.clear(f);
			return false;
		}

		const js = JSON.parse(xmlreq.responseText);
		golgotha.tour.customLeg = js;
		golgotha.util.display('customLeg', true);
		golgotha.util.display('rangeWarn', js.rangeWarn);
		golgotha.util.display('trWarn', js.tRunwayWarn);
		golgotha.util.display('lrWarn', js.lRunwayWarn);
		golgotha.util.display('etopsWarn', js.etopsWarn);
		golgotha.util.display('legWarnRow', (js.rangeWarn || js.tRunwayWarn || js.lRunwayWarn || js.etopsWarn));
		golgotha.util.setHTML('customLegInfo', golgotha.tour.renderDuration(js));
		golgotha.form.clear(f);
		return true;
	};

	golgotha.util.display('customLeg', false);
	golgotha.form.submit(f);
	xmlreq.send(params.join('&'));
	return true;
};

golgotha.tour.addCustomLeg = function() {
	if (!golgotha.tour.customLeg) return false;
	golgotha.tour.addLeg(golgotha.tour.customLeg);
	golgotha.tour.clearCustomLeg();
	golgotha.tour.showCustomFields(false);
	return true;
};

golgotha.tour.search = function() {
	const f = document.forms[0];
	const xmlreq = new XMLHttpRequest();
	if (!golgotha.form.comboSet(f.airportD) && !golgotha.form.comboSet(f.airportA)) return false;
	xmlreq.open('get', 'search.ws?airportD=' + golgotha.form.getCombo(f.airportD) + '&airportA=' + golgotha.form.getCombo(f.airportA), true);
	xmlreq.onreadystatechange = function() {
		if (xmlreq.readyState != 4) return false;
		if (xmlreq.status != 200) {
			golgotha.util.setHTML('isLoading', ' - ERROR ' + xmlreq.status);
			golgotha.form.clear(f);
			return false;
		}

		// Parse the JSON
		golgotha.tour.deleteRows();
		golgotha.form.clear(f);
		const js = JSON.parse(xmlreq.responseText);
		if (js.results.length == 0) {
			golgotha.tour.showCustomFields(true);
			return true;
		}

		// Render the results
		let idx = 1; golgotha.util.display('searchResultHdr', true);
		const hdr = document.getElementById('searchResultHdr'); const hdrIdx = hdr.rowIndex;
		const t = document.getElementById('searchTable');
		js.results.forEach(function(se) {
			const r = t.insertRow(hdrIdx + idx); r.className = 'mid searchResultEntry'; r.flight = se;
			let c = r.insertCell(0); 
			let btn = document.createElement('button'); btn.className = 'button';
			btn.innerHTML = 'ADD LEG'; btn.onclick = function() { golgotha.tour.addLeg(r.flight, hdrIdx + idx); return false; };
			c.appendChild(btn);
			let s = document.createElement('span'); s.className = 'pri bld';			
			s.innerText = se.airline + se.flight + ' Leg ' + se.leg;
			c = r.insertCell(1); c.appendChild(s);
			c.appendChild(document.createTextNode(' '));
			s = document.createElement('span'); s.className = 'small';
			s.innerText = golgotha.tour.renderRoute(se); c.appendChild(s); 
			c = r.insertCell(2); c.className = 'sec bld';
			c.innerText = se.eqType;
			c = r.insertCell(3); c.className = 'bld';
			c.innerText = golgotha.tour.renderTime(se);
			idx++;
		});

		golgotha.util.setHTML('isloading', js.results.length + ' RESULTS');
		return true;	
	};

golgotha.form.submit(f);
golgotha.tour.clearCustomLeg();
golgotha.util.display('searchResultHdr', false);
golgotha.tour.showCustomFields(false);
xmlreq.send(null);
return true;
};
