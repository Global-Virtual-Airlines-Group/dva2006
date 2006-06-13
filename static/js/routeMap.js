function updateAirports(combo)
{
var isLoading = getElement('isLoading');
isLoading.innerHTML = ' - REDRAWING...';

// Remove airports/routes from the map
map.clearOverlays();

// Get the new airline code
var aCode = combo.options[combo.selectedIndex].value;

// Check if we have cached airports
var aps = airports[aCode];
if (aps) {
	addMarkers(map, 'aps');
	isLoading.innerHTML = '';
	return true;
}

// Build the XML Requester
isLoading.innerHTML = ' - LOADING...';
var xmlreq = GXmlHttp.create();
xmlreq.open("GET", "rmap_airports.ws?airline=" + aCode, true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	var isLoading = getElement('isLoading');
	isLoading.innerHTML = ' - REDRAWING...';
	var aps = new Array();
	var f = document.forms[0];

	// Parse the XML
	var xdoc = xmlreq.responseXML;
	var wsdata = xdoc.documentElement;
	var els = wsdata.getElementsByTagName("airport");
	for (var x = 0; x < els.length; x++) {
		var a = els[x];
		var p = new GLatLng(parseFloat(a.getAttribute("lat")), parseFloat(a.getAttribute("lng")));
		var mrk = googleMarker(document.imgPath, a.getAttribute("color"), p, null);
		mrk.icao = a.getAttribute("icao");
		mrk.iata = a.getAttribute("iata");
		mrk.infoShow = showRoutes;
		GEvent.addListener(mrk, 'infowindowclose', function() { removeMarkers(map, 'routes'); });
		var label = a.firstChild;
		mrk.infoLabel = label.data;
		GEvent.bind(mrk, 'click', mrk, mrk.infoShow);

		// Add to array and map
		aps.push(mrk);
		map.addOverlay(mrk);
	}
	
	// Save in the hashmap
	airports[aCode] = aps;
	isLoading.innerHTML = '';
	return true;
}

// Send the XMLHTTP request
xmlreq.send(null);
return true;
}

function showRoutes()
{
// Update status
var isLoading = getElement('isLoading');
isLoading.innerHTML = ' - LOADING...';

// Build the XML Requester
var xmlreq = GXmlHttp.create();
xmlreq.open("GET", "rmap_routes.ws?icao=" + this.icao, true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	var isLoading = getElement('isLoading');
	isLoading.innerHTML = ' - REDRAWING...';
	routes = new Array();
	
	// Parse the XML
	var xdoc = xmlreq.responseXML;
	var wsdata = xdoc.documentElement;
	var rts = wsdata.getElementsByTagName("route");
	for (var x = 0; x < rts.length; x++) {
		var rt = rts[x];
		var aCode = rt.getAttribute("airline");
		var aBox = getElement(aCode);
		if ((aBox) && aBox.checked) {
			var pos = rt.getElementsByTagName("pos");
			var positions = new Array();
	
			// Get the positions
			for (var i = 0; i < pos.length; i++) {
				var pe = pos[i];
				var p = new GLatLng(parseFloat(pe.getAttribute("lat")), parseFloat(pe.getAttribute("lng")));
				positions.push(p);
			} // for

			// Draw the line
			var routeLine = new GPolyline(positions, '#4080AF', 2, 0.8);
			map.addOverlay(routeLine);
			routes.push(routeLine);
		}
	}

	// Focus on the map
	isLoading.innerHTML = '';
	return true;
} // function

// Send the XMLHTTP request
xmlreq.send(null);
this.openInfoWindowHtml(this.infoLabel);
return true;
}
