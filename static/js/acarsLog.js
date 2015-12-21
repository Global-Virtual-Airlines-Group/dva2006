golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;

// Do different validation depending on the search type
var isOK = false;
if (f.pilotCode.value.length > 0) { 
	golgotha.form.validate({f:f.pilotCode, l:4, t:'Pilot Code'});
	isOK = true;
}
	
if ((f.startDate.value.length > 0) || (f.endDate.value.length > 0)) {
	golgotha.form.validate({f:f.startDate, l:10, t:'Start Date'});
	golgotha.form.validate({f:f.endDate, l:10, t:'End Date'});
	isOK = true;
}
	
if (f.viewCount.value.length > 0) {
	golgotha.form.validate({f:f.viewCount, min:0, t:'Maximum Results'});
	isOK = true;
}

if ((f.searchStr) && (f.searchStr.value.length > 0)) {
	golgotha.form.validate({f:f.searchStr, l:4, t:'Search Text'});
	isOK = true;
}

if (!isOK)
	throw new golgotha.event.ValidationError('Please select a Search Type.');

golgotha.form.submit(f);
return true;
};
