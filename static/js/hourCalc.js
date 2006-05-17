function hoursCalc()
{
var f = document.forms[0];
var h = parseInt(f.tmpHours.value);
var m = parseInt(f.tmpMinutes.value);
if ((h == Number.NaN) || (m == Number.NaN)) {
	alert('Please fill in both Hours and Minutes.');
	f.tmpHours.focus();
	return false;
}

// Check for negative number
if ((h < 0) || (m < 0)) {
	alert('Hours and minutes cannnot be negative.');
	f.tmpHours.focus();
	return false;
}

// Turn into a single number
var tmpHours = (h + (m / 60));
var hrs = Math.round(tmpHours * 10) / 10;

// Update the combobox
var combo = f.flightTime;
for (x = 0; x < combo.options.length; x++) {
	var opt = combo.options[x];
	if (opt.text == hrs) {
		opt.selected = true;
		break;
	}
}

return true;
}
