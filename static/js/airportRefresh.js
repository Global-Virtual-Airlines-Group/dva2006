function updateAirports(combo, cmdURL, doICAO, oldCode)
{
if (combo == null) return false;
var xmlreq = getXMLHttpRequest();
xmlreq.open('get', 'airports.ws?' + cmdURL, true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var xmlDoc = xmlreq.responseXML;
	var ac = xmlDoc.documentElement.getElementsByTagName('airport');
	createAirportCombo(combo, ac, doICAO);
	combo.disabled = false;
	setAirport(combo, oldCode);
	changeAirport(combo);
	gaEvent('Airports', 'Load Airport List');
	return true;
}

combo.disabled = true;
xmlreq.send(null);
return true;
}

function createAirportCombo(combo, nodes, doICAO)
{
combo.options.length = nodes.length + 1;
combo.options[0] = new Option('-', '');
var codeAttr = (doICAO) ? 'icao' : 'iata';
for (var i = 0; i < nodes.length; i++) {
	var a = nodes[i];
	var apCode = a.getAttribute(codeAttr);
	var apName = a.getAttribute('name') + ' (' + apCode + ')';
	var opt = new Option(apName, apCode);
	opt.icao = a.getAttribute('icao');
	opt.iata = a.getAttribute('iata');
	combo.options[i+1] = opt;
}

return true;
}

function changeAirline(aCombo, useSched)
{
var f = document.forms[0];
updateAirports(f.airportD, 'useSched=' + useSched + '&airline=' + getValue(aCombo), false, getValue(f.airportD));
updateAirports(f.airportA, 'useSched=' + useSched + '&airline=' + getValue(aCombo), false, getValue(f.airportA));
gaEvent('Airports', 'Change Airline');
return true;
}

function updateOrigin(combo)
{
var f = document.forms[0];
if (combo.selectedIndex != 0)
	updateAirports(f.airportA, 'code=' + getValue(combo), false, getValue(f.airportA));
	
return true;
}

function changeAirport(combo)
{
var text = document.getElementById(combo.name + 'Code');
if (text) text.value = combo.options[combo.selectedIndex].value.toUpperCase();
return true;
}

function getValue(combo)
{
if (combo.selectedIndex == -1) return null;
return combo.options[combo.selectedIndex].value;
}

function setAirport(combo, code, fireEvent)
{
if (code == null) return false;
code = code.toUpperCase();
for (var x = 0; x < combo.options.length; x++) {
	var opt = combo.options[x];
	if ((code == opt.value) || (code == opt.icao) || (code == opt.iata)) {
		combo.selectedIndex = x;
		if (fireEvent && combo.onchange)
			combo.onchange();

		return true;
	}
}

return false;
}

function updateSIDSTAR(combo, code, type)
{
if (combo == null) return false;
var oldValue = combo.options[combo.selectedIndex].value;
var xmlreq = getXMLHttpRequest();
xmlreq.open('get', 'troutes.ws?airportD=' + code + '&airportA=' + code, true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	var xmlDoc = xmlreq.responseXML;
	var trs = xmlDoc.documentElement.getElementsByTagName(type.toLowerCase());	
	combo.options.length = trs.length + 1;
	combo.options[0] = new Option('-', '');
	for (var i = 0; i < trs.length; i++) {
		var tr = trs[i];
		var trCode = tr.getAttribute('code')
		var opt = new Option(trCode, trCode);
		combo.options[i+1] = opt;		
	}

	combo.disabled = false;
	setCombo(combo, oldValue);
	gaEvent('Airports', 'Load SID/STAR List');
	return true;
}

combo.disabled = true;
xmlreq.send(null);
return true;
}
