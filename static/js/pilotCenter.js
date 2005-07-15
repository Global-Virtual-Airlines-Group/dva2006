function showNAV()
{
var sindex = document.forms[0].navaidType.selectedIndex;
var textbox = document.forms[0].navaidCode;

// Check that a code has been specified
if (textbox.value.length < 3) {
	alert('Please select an Airport/VOR code.');
	textbox.focus();
	return false;
}

// Don't strip out leading letter for METAR Info
if ((sindex != 3) && (textbox.value.length == 4))
	textbox.value = textbox.value.substring(1, 4);
	
// Calculate the proper URL
switch (sindex) {
	// Navigation aids
	case 0:	var URL = 'http://www.airnav.com/cgi-bin/navaid-info?';
				break;
				
	// Airfield information
	case 1:	var URL = 'http://www.airnav.com/cgi-bin/airport-get?';
				break;
				
	// METAR information
	case 2:	var URL = 'http://weather.noaa.gov/cgi-bin/mgetmetar.pl?cccc=';
				break;
}

// Open the window
URLflags = 'height=580,width=680,menubar=no,toolbar=no,status=no,resizable=yes,scrollbars=yes';
var w = window.open(URL + textbox.value.toUpperCase(), 'NavAID', URLflags);
return true;
}
