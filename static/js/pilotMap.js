function generateXMLRequest(imgPath)
{
// Build the XML Requester
var xmlreq = GXmlHttp.create();
xmlreq.open("GET", "pilotmap.ws", true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	var isLoading = getElement('isLoading');
	isLoading.innerHTML = ' - REDRAWING...';
	
	// Parse the XML
	var xmlDoc = xmlreq.responseXML;
	var ac = xmlDoc.documentElement.getElementsByTagName("pilot");
	for (var i = 0; i < ac.length; i++) {
		var a = ac[i];
		var label = a.firstChild;
		var p = new GPoint(parseFloat(a.getAttribute("lng")), parseFloat(a.getAttribute("lat")));
		var mrk = googleMarker(imgPath, a.getAttribute("color"), p, null);
		mrk.infoLabel = label.data;
		mrk.rank = a.getAttribute("rank");
		mrk.eqType = a.getAttribute("eqType");
		mrk.infoShow = showInfo;
		mrk.isVisible = true;
		
		// Set the the click handler and add to the map
		GEvent.bind(mrk, 'click', mrk, mrk.infoShow);
		map.addOverlay(mrk);
		
		// Save in the eqType and rank hashmaps
		if (pMarkers[mrk.rank]) pMarkers[mrk.rank].push(mrk);
		if (pMarkers[mrk.eqType]) pMarkers[mrk.eqType].push(mrk);
	} // for
	
	// Restore the checkboxes
	enableElement('eq', true);
	enableElement('rnk', true);
	isLoading.innerHTML = '';
	return true;
} // function

return xmlreq;
}

function showInfo()
{
this.openInfoWindowHtml(this.infoLabel);
return true;
}

function updateMarkers(checkbox)
{
var f = document.forms[0];

// Get the markers we are going to add or remove
var markers = pMarkers[checkbox.value];
if (!markers) return false;

// Load equipment types
var showOptions = new Array();
for (var x = 0; x < f.eqTypes.length; x++)
	showOptions[f.eqTypes[x].value] = f.eqTypes[x];

// Load rank names
for (var x = 0; x < f.ranks.length; x++)
	showOptions[f.ranks[x].value] = f.ranks[x];

// Add or remove the markers
for (var x = 0; x < markers.length; x++) {
	var mrk = markers[x];
	if (checkbox.checked && (!mrk.isVisible)) {
		mrk.isVisible = true;
		map.addOverlay(mrk);
	} else if ((!checkbox.checked) && mrk.isVisible) {
		var showMe = (showOptions[mrk.eqType].checked || showOptions[mrk.rank].checked);
		if (!showMe) {
			mrk.isVisible = false;
			map.removeOverlay(mrk);
		}
	}
}

return true;
}
