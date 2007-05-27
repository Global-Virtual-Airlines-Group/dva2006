function generateXMLRequest(imgPath)
{
// Build the XML Requester
var f = document.forms[0];
var xmlreq = GXmlHttp.create();
xmlreq.open("GET", "pilotmap.ws", true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;

	// Parse the XML
	var noFilter = f.noFilter;
	var xmlDoc = xmlreq.responseXML;
	var ac = xmlDoc.documentElement.getElementsByTagName("pilot");
	for (var i = 0; i < ac.length; i++) {
		var a = ac[i];
		var label = a.firstChild;
		var p = new GLatLng(parseFloat(a.getAttribute("lat")), parseFloat(a.getAttribute("lng")));
		var mrk = googleMarker(imgPath, a.getAttribute("color"), p, null);
		mrk.infoLabel = label.data;
		mrk.rank = a.getAttribute("rank");
		mrk.eqType = a.getAttribute("eqType");

		// Set the the click handler and add to the list
		GEvent.bind(mrk, 'click', mrk, function() { this.openInfoWindowHtml(this.infoLabel); } );
		allMarkers.push(mrk);
		mm.addMarker(mrk, noFilter ? 1 : parseInt(a.getAttribute("minZoom")));
	} // for

	// Display the markers
	isLoading.innerHTML = '';
	enableElement(f.noFilter, true);
	enableElement(f.eqType, true);
	enableElement(f.rank, true);
	return true;
} // function

var isLoading = getElement('isLoading');
isLoading.innerHTML = ' - LOADING...';
enableElement(f.noFilter, false);
enableElement(f.eqType, false);
enableElement(f.rank, false);
return xmlreq;
}

function updateMarkers()
{
// Get the rank/program values
var f = document.forms[0];
var checkRank = (f.rank.selectedIndex > 0);
var checkEQ = (f.eqType.selectedIndex > 0);
var rank = f.rank.options[f.rank.selectedIndex].value;
var eqType = f.eqType.options[f.eqType.selectedIndex].value;

// Add or remove the markers
for (var x = 0; x < allMarkers.length; x++) {
	var mrk = allMarkers[x];
	var rankOK = (!checkRank) || (mrk.rank == rank);
	var eqOK = (!checkEQ) || (mrk.eqType == eqType);

	// Update visibility
	if (rankOK && eqOK)
		mrk.show();
	else
		mrk.hide();
}

return true;
}
