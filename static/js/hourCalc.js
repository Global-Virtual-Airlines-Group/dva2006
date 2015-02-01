golgotha.util.hoursCalc = function(f)
{
var h = parseInt(f.tmpHours.value);
var m = parseInt(f.tmpMinutes.value);
if ((h == Number.NaN) || (m == Number.NaN)) {
	var fe = (h == Number.NaN) ? f.tmpHours : f.tmpMinutes;
	throw new golgotha.event.ValidationError('Please fill in both Hours and Minutes.', fe);
}
	
if ((h < 0) || (m < 0)) {
	var fe = (h < 0) ? f.tmpHours : f.tmpMinutes;
	throw new golgotha.event.ValidationError('Hours and minutes cannnot be negative.', fe);
}

// Turn into a single number
var tmpHours = (h + (m / 60));
var hrs = Math.round(tmpHours * 10) / 10;
var combo = f.flightTime;
for (x = 0; x < combo.options.length; x++) {
	var opt = combo.options[x];
	if (opt.text == hrs) {
		opt.selected = true;
		return true;
	}
}

return true;
};
