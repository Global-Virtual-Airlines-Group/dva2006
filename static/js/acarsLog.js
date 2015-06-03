golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;

// Do different validation depending on the search type
var isOK = false;
if (f.pilotCode.length > 0) { 
	golgotha.form.validate({f:f.pilotCode, l:4, t:'Pilot Code'});
	isOK = true;
}
	
if ((f.startDate.length > 0) || (f.endDate.length > 0)) {
	golgotha.form.validate({f:f.startDate, l:10, t:'Start Date'});
	golgotha.form.validate({f:f.endDate, l:10, t:'End Date'});
	isOK = true;
}
	
if ((f.viewCount) && (f.viewCount.length > 0)) {
	golgotha.form.validate({f:f.viewCount, min:0, t:'Maximum Results'});
	isOK = true;
}

if (!isOK)
	throw new golgotha.event.ValidationError('Please select a Search Type.', f.searchType[0]);

golgotha.form.submit(f);
return true;
};
