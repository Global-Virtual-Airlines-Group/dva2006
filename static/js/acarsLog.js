function valdiate(form)
{
if (!checkSubmit()) return false;

// Check the search type
var sType;
for (var x = 0; x < form.searchType.length; x++) {
	if (form.searchType[x].checked)
		sType = form.searchType[x].value;
}

// Do different validation depending on the search type
switch (sType) {
	case 'USR' :
		if (!validateText(form.pilotCode, 4, 'Pilot Code')) return false;
		break;
	
	case 'id' :
		if (!validateNumber(form.pilotCode, 1000, 'Pilot Database ID')) return false;
		break;
		
	case 'DATE' :
		if (!validateText(form.startDate, 10, 'Start Date')) return false;
		if (!validateText(form.endDate, 10, 'End Date')) return false;
		break;
		
	case 'LATEST' :
		if (!validateNumber(form.viewCount, 0, 'Maximum Results')) return false;
		break;
	
	default :
		alert('Please select a Search Type.');
		form.searchType[0].focus();
		return false;
}

setSubmit();
disableButton('SearchButton');
return true;
}
