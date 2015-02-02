golgotha.local.validate = function(f)
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
		golgotha.form.validate({f:f.pilotCode, l:4, t:'Pilot Code'});
		break;
	
	case 'id' :
		golgotha.form.validate({f:f.pilotCode, min:1000, t:'Pilot Database ID'});
		break;
		
	case 'DATE' :
		golgotha.form.validate({f:f.startDate, l:10, t:'Start Date'});
		golgotha.form.validate({f:f.endDate, l:10, t:'End Date'});
		break;
		
	case 'LATEST' :
		golgotha.form.validate({f:f.viewCount, min:0, t:'Maximum Results'});
		break;
	
	default:
		throw new golgotha.util.ValidationError('Please select a Search Type.', f.searchType[0]);
}

golgotha.form.submit(f);
return true;
};
