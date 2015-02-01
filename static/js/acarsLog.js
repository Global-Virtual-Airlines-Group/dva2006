function validate(f)
{
if (!golgotha.form.check()) return false;

// Check the search type
var sType;
for (var x = 0; x < f.searchType.length; x++) {
	if (f.searchType[x].checked)
		sType = f.searchType[x].value;
}

// Do different validation depending on the search type
switch (sType) {
	case 'USR' :
		if (!validateText(f.pilotCode, 4, 'Pilot Code')) return false;
		break;
	
	case 'id' :
		if (!validateNumber(f.pilotCode, 1000, 'Pilot Database ID')) return false;
		break;
		
	case 'DATE' :
		if (!validateText(f.startDate, 10, 'Start Date')) return false;
		if (!validateText(f.endDate, 10, 'End Date')) return false;
		break;
		
	case 'LATEST' :
		if (!validateNumber(f.viewCount, 0, 'Maximum Results')) return false;
		break;
	
	default :
		alert('Please select a Search Type.');
		f.searchType[0].focus();
		return false;
}

golgotha.form.submit();
disableButton('SearchButton');
return true;
}
