golgotha.airportLoad = golgotha.airportLoad || {config:{doICAO:false, notVisited:false, useSched:true, dst:false, myRated:false}};
golgotha.airportLoad.config.clone = function() {
	var o = {};
	for (p in this) {
		if (this.hasOwnProperty(p))
			o[p] = this[p];
	}

	return o;
};

golgotha.airportLoad.config.URLParams = function() {
	var params = [];
	for (p in this) {
		var v = this[p];
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

golgotha.airportLoad.updateAirportCode = function() {
	var txt = this.form[this.name + 'Code'];
	if (!txt) return false;
	var o = this.options[this.selectedIndex];
	txt.value = golgotha.airportLoad.config.getCode(o);
	return true;
};

golgotha.airportLoad.updateOrigin = function(combo) {
	var f = document.forms[0];
	var cfg = golgotha.airportLoad.config.clone();
	cfg.useSched = true; cfg.notVisited = combo.notVisited; cfg.dst = true;
	cfg.airline = golgotha.form.getCombo(f.airline); 
	cfg.code = (combo.selectedIndex > 0) ? golgotha.form.getCombo(combo) : null;
	f.airportA.loadAirports(cfg);
	return true;
};

golgotha.airportLoad.setAirport = function(code, fireEvent, sender)
{
if (code == null) return false;
if (code.length < 2) {
	var oldIdx = this.selectedIndex; this.selectedIndex = 0;
	if (fireEvent && this.onchange && (oldIdx != 0)) this.onchange();
	return true;
}

code = code.toUpperCase();
for (var x = 0; x < this.options.length; x++) {
	var opt = this.options[x]; var ap = opt.airport || {};
	if ((code == opt.value) || (code == ap.icao) || (code == ap.iata)) {
		this.selectedIndex = x;
		if (fireEvent && this.onchange) this.onchange();
		return true;
	}
}

if (sender != null) sender.value = '';
return false;
};

golgotha.airportLoad.setOptions = function(combo, data, opts)
{
combo.options.length = data.length + 1;
combo.options[0] = new Option('-', '');
var codeAttr = (opts.doICAO) ? 'icao' : 'iata';
for (var i = 0; i < data.length; i++) {
	var a = data[i];
	var apCode = a[codeAttr];
	var opt = new Option(a.name + ' (' + apCode + ')', apCode);
	opt.airport = a;
	combo.options[i+1] = opt;
}

return true;
};

golgotha.airportLoad.loadAirports = function(opts)
{
var oldCode = golgotha.form.getCombo(this); var combo = this;
var xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'airports.ws?' + opts.URLParams(), true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var o = combo.options[combo.selectedIndex];
	var oldCodes = ((o) && (o.airport)) ? [o.airport.iata, o.airport.icao] : [null];
	var isChanged = (oldCodes.indexOf(oldCode) < 0);
	var jsData = JSON.parse(xmlreq.responseText);
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
var oldValue = golgotha.form.getCombo(this); var combo = this;
var xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'troutes.ws?airportD=' + code + '&airportA=' + code, true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var js = JSON.parse(xmlreq.responseText);
	var jsData = js[type];
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
	for (var x = 0; x < combos.length; x++)
		combos[x].loadAirports(opts);

	golgotha.event.beacon('Airports', 'Change Airline');
	return true;
};

golgotha.airportLoad.massageSelects = function(root) {
	var opts = golgotha.util.getElementsByClass('airport', 'option', root);
	for (var op = opts.pop(); (op != null); op = opts.pop())
		op.airport = {name:op.text, iata:op.getAttribute('iata'), icao:op.getAttribute('icao')};

	return opts.length;
};

golgotha.airportLoad.codeMassage = function() {
	var e = window.event;
	var c = e.which || e.keyCode;
	if (((c > 64) && (c < 91)) || ((c > 96) && (c < 123)))
		return true;

	return golgotha.event.stop(e);
};
