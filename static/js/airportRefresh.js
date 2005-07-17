function setOptions(combo, aCode)
{
var newOptions = airports[aCode];
combo.options.length = newOptions.length;

for (x = 0; x < newOptions.length; x++) {
	newOpt = newOptions[x];
	combo.options[x] = new Option(newOpt.text, newOpt.value);
}
 
combo.selectedIndex = 0;
return true;
}

function changeAirline(combo)
{
var f = document.forms[0];

// Get new airline code, and existing airport codes
var aCode = combo.options[combo.selectedIndex].value;
var oldAA = f.airportA.options[f.airportA.selectedIndex].value;
var oldAD = f.airportD.options[f.airportD.selectedIndex].value;

// Update the option lists
setOptions(f.airportD, aCode);
setAirport(f.airportD, oldAD);
changeAirport(f.airportD);
setOptions(f.airportA, aCode);
setAirport(f.airportA, oldAA);
changeAirport(f.airportA);
return true;
}

function changeAirport(combo)
{
var text = document.getElementById(combo.name + 'Code');
text.value = combo.options[combo.selectedIndex].value.toUpperCase();
return true;
}

function setAirport(combo, code)
{
code = code.toUpperCase();
for (x = 0; x < combo.options.length; x++) {
	if (code == combo.options[x].value) {
		combo.selectedIndex = x;
		return true;
	}
}

return true;
}
