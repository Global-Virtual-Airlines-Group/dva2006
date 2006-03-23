function generateXMLRequest(imgPath)
{
// Build the XML Requester
var d = new Date();
var xmlreq = GXmlHttp.create();
xmlreq.open("GET", "pilotmap.ws?time=" + d.getTime(), true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	
	// Parse the XML
	var xmlDoc = xmlreq.responseXML;
	var ac = xmlDoc.documentElement.getElementsByTagName("pilot");
	map.clearOverlays();
	pMarkers = new Array();
	for (var i = 0; i < ac.length; i++) {
		var a = ac[i];
		var label = a.firstChild;
		var p = new GPoint(parseFloat(a.getAttribute("lng")), parseFloat(a.getAttribute("lat")));
		var mrk = googleMarker(imgPath, a.getAttribute("color"), p, null);
		mrk.infoLabel = label.data;
		mrk.rank = a.getAttribute("rank");
		mrk.eqType = a.getAttribute("eqType");
		
		// Set the the click handler
		GEvent.bind(mrk, 'click', mrk, mrk.openInfoWindowHtml(mrk.infoLabel));
		map.addOverlay(mrk);
		pMarkers.push(mrk);
	} // for
	
	// Restore the checkboxes
	enableElement('eq', true);
	enableElement('rnk', true);
	return true;
} // function

return xmlreq;
}
