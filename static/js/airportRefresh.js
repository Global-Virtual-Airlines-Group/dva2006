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
var aCode = combo.options[combo.selectedIndex].value;
setOptions(document.forms[0].airportD, aCode);
setOptions(document.forms[0].airportA, aCode);
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
