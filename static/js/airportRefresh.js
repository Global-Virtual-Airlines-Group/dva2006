golgotha.airportLoad = golgotha.airportLoad || {config:{doICAO:false, notVisited:false, useSched:true, dst:false, myRated:false}};
golgotha.airportLoad.config.clone = function() {
	let o = {};
	for (p in this) {
		if (this.hasOwnProperty(p))
			o[p] = this[p];
	}

	return o;
};

golgotha.airportLoad.config.URLParams = function() {
	let params = [];
	for (p in this) {
		let v = this[p];
		if (this.hasOwnProperty(p) && !golgotha.util.isFunction(v) && (v != null))
			params.push(p + '=' + escape(v));
	}
	
	return params.join('&');
};

// Get Airport code out of select option
golgotha.airportLoad.config.getCode = function(opt) {
	if (!opt.airport) return opt.value.toUpperCase();
	return this.doICAO ? opt.airport.icao : opt.airport.iata;
};

// Helper functions to attach to airport/airline comboboxen
golgotha.airportLoad.setHelpers = function(combo, addSIDSTARHook) {
	if (combo == null) return false;
	combo.massageSelects = function() { golgotha.airportLoad.massageSelects(combo); };
	combo.loadAirports = golgotha.airportLoad.loadAirports;
	combo.updateAirportCode = golgotha.airportLoad.updateAirportCode;
	combo.setAirport = golgotha.airportLoad.setAirport;
	combo.massageSelects(); combo.notVisited = false;
	if (addSIDSTARHook) combo.loadSIDSTAR = golgotha.airportLoad.loadSIDSTAR;
	return true;
};

golgotha.airportLoad.updateAirlineCode = function() {
	let txt = this.form[this.name + 'Code'];
	if (!txt) return false;
	var o = this.options[this.selectedIndex];
	txt.value = o.value;
	return true;
};

golgotha.airportLoad.updateAirportCode = function() {
	let txt = this.form[this.name + 'Code'];
	if (!txt) return false;
	let o = this.options[this.selectedIndex];
	txt.value = golgotha.airportLoad.config.getCode(o);
	return true;
};

golgotha.airportLoad.updateOrigin = function(combo) {
	let f = document.forms[0];
	let cfg = golgotha.airportLoad.config.clone();
	cfg.useSched = true; cfg.notVisited = combo.notVisited; cfg.dst = true;
	cfg.airline = golgotha.form.getCombo(f.airline); 
	cfg.code = (combo.selectedIndex > 0) ? golgotha.form.getCombo(combo) : null;
	f.airportA.loadAirports(cfg);
	return true;
};

golgotha.airportLoad.setAirline = function(cb, sender, fireEvent) {
	let code = sender.value;
	if (code.length < 2) {
		var oldIdx = cb.selectedIndex; cb.selectedIndex = 0;
		if (fireEvent && cb.onchange && (oldIdx != 0)) cb.onchange();
		return true;
	}

	code = code.toUpperCase();
	for (var x = 0; x < cb.options.length; x++) {
		if (code == cb.options[x].value) {
			cb.selectedIndex = x;
			if (fireEvent && cb.onchange) cb.onchange();
			return true;
		}
	}

	sender.value = '';
	return false;
};

golgotha.airportLoad.setAirport = function(code, fireEvent, sender) {
	if (code == null) return false;
	if (code.length < 2) {
		let oldIdx = this.selectedIndex; this.selectedIndex = 0;
		if (fireEvent && this.onchange && (oldIdx != 0)) this.onchange();
		return true;
	}

	code = code.toUpperCase();
	for (var x = 0; x < this.options.length; x++) {
		let opt = this.options[x]; let ap = opt.airport || {};
		if ((code == opt.value) || (code == ap.icao) || (code == ap.iata)) {
			this.selectedIndex = x;
			if (fireEvent && this.onchange) this.onchange();
			return true;
		}
	}

	if (sender != null) sender.value = '';
	return false;
};

golgotha.airportLoad.setOptions = function(combo, data, opts) {
	combo.options.length = data.length + 1;
	combo.options[0] = new Option('-', '');
	let codeAttr = (opts.doICAO) ? 'icao' : 'iata';
	for (var i = 0; i < data.length; i++) {
		let a = data[i];
		let apCode = a[codeAttr];
		let opt = new Option(a.name + ' (' + apCode + ')', apCode);
		opt.airport = a;
		combo.options[i+1] = opt;
	}

	return true;
};

golgotha.airportLoad.loadAirports = function(opts)
{
let oldCode = golgotha.form.getCombo(this); let combo = this;
let xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'airports.ws?' + opts.URLParams(), true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	let o = combo.options[combo.selectedIndex];
	let oldCodes = ((o) && (o.airport)) ? [o.airport.iata, o.airport.icao] : [null];
	let isChanged = (oldCodes.indexOf(oldCode) < 0);
	let jsData = JSON.parse(xmlreq.responseText);
	golgotha.airportLoad.setOptions(combo, jsData, opts);
	combo.setAirport(oldCode, isChanged);
	combo.disabled = false;
	golgotha.event.beacon('Airports', 'Load Airport List');
	return true;
};

combo.disabled = true;
xmlreq.send(null);
return true;
};

golgotha.airportLoad.loadSIDSTAR = function(code, type)
{
let oldValue = golgotha.form.getCombo(this); let combo = this;
let xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'troutes.ws?airportD=' + code + '&airportA=' + code, true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	let js = JSON.parse(xmlreq.responseText);
	let jsData = js[type];
	combo.options.length = jsData.length + 1;
	combo.options[0] = new Option('-', '');
	for (var i = 0; i < trs.length; i++)
		combo.options[i+1] = new Option(trs[i].code, trs[i].code);

	golgotha.form.setCombo(combo, oldValue);
	this.disabled = false;
	golgotha.event.beacon('Airports', 'Load SID/STAR List');
	return true;
};

combo.disabled = true;
xmlreq.send(null);
return true;
};

golgotha.airportLoad.changeAirline = function(combos, opts) {
	combos.forEach(function(c) { c.loadAirports(opts); });
	golgotha.event.beacon('Airports', 'Change Airline');
	return true;
};

golgotha.airportLoad.massageSelects = function(root) {
	let opts = golgotha.util.getElementsByClass('airport', 'option', root);
	opts.forEach(function(op) { op.airport = {name:op.text, iata:op.getAttribute('iata'), icao:op.getAttribute('icao')}; });
	return opts.length;
};

golgotha.airportLoad.codeMassage = function() {
	let e = window.event;
	let c = e.which || e.keyCode;
	if (((c > 64) && (c < 91)) || ((c > 96) && (c < 123)))
		return true;

	return golgotha.event.stop(e);
};
