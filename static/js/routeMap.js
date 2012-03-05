function updateAirports(combo)
{
var isLoading = document.getElementById('isLoading');
isLoading.innerHTML = ' - CLEARING...';

// Remove airports/routes from the map
removeMarkers('aps');
removeMarkers('routes');
map.infoWindow.close();
if (combo.selectedIndex == 0) {
	isLoading.innerHTML = '';
	return false;
}

// Get the new airline code
var aCode = combo.options[combo.selectedIndex].value;

// Check if we have cached airports
aps = airports[aCode];
if (aps) {
	addMarkers(map, 'aps');
	isLoading.innerHTML = '';
	return true;
}

// Build the XML Requester
isLoading.innerHTML = ' - LOADING...';
var xmlreq = getXMLHttpRequest();
xmlreq.open('get', 'rmap_airports.ws?airline=' + aCode, true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var isLoading = document.getElementById('isLoading');
	isLoading.innerHTML = ' - REDRAWING...';
	aps = [];
	var f = document.forms[0];

	// Parse the XML
	var xdoc = xmlreq.responseXML;
	var wsdata = xdoc.documentElement;
	var els = wsdata.getElementsByTagName('airport');
	for (var x = 0; x < els.length; x++) {
		var a = els[x];
		var p = new google.maps.LatLng(parseFloat(a.getAttribute('lat')), parseFloat(a.getAttribute('lng')));
		var mrk = googleMarker(a.getAttribute('color'), p, null);
		mrk.icao = a.getAttribute('icao');
		mrk.iata = a.getAttribute('iata');
		mrk.infoShow = showRoutes;
		for (var nidx = a.childNodes.length; nidx > 0; nidx--) {
			var nd = a.childNodes[nidx - 1];
			try {
				if (nd.nodeType == 4) {
					mrk.infoLabel = nd.data;
					nidx = 0;
				}
			} catch (e) { }
		}

		google.maps.event.addListener(mrk, 'click', mrk.infoShow);
		document.lastAirport = a;
		aps.push(mrk);
	}

	// Save in the hashmap
	addMarkers(map, 'aps');
	airports[aCode] = aps;
	isLoading.innerHTML = '';
	gaEvent('Route Map', 'Airports', aCode);
	return true;
}

// Send the XMLHTTP request
xmlreq.send(null);
return true;
}

function showRoutes()
{
// Update status
var isLoading = document.getElementById('isLoading');
isLoading.innerHTML = ' - LOADING...';

// Get the airline code
var aCombo = document.getElementById('airlineCode');
var aCode = aCombo.options[aCombo.selectedIndex].value;
removeMarkers('routes');
map.infoWindow.close();

// Build the XML Requester
var xmlreq = getXMLHttpRequest();
xmlreq.open('get', 'rmap_routes.ws?icao=' + this.icao + '&airline=' + aCode, true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var isLoading = document.getElementById('isLoading');
	isLoading.innerHTML = ' - REDRAWING...';
		routes.length = 0;

	// Parse the XML
	var xdoc = xmlreq.responseXML;
	var wsdata = xdoc.documentElement;
	var rts = wsdata.getElementsByTagName('route');
	for (var x = 0; x < rts.length; x++) {
		var rt = rts[x];
		var al = rt.getAttribute('airline');
		if (al == aCode) {
			var positions = [];

			// Get the positions
			var pos = rt.getElementsByTagName('pos');
			for (var i = 0; i < pos.length; i++) {
				var pe = pos[i];
				var p = new google.maps.LatLng(parseFloat(pe.getAttribute('lat')), parseFloat(pe.getAttribute('lng')));
				positions.push(p);
			} // for

			// Draw the line
			var routeLine = new google.maps.Polyline({path:positions, strokeColor:'#4080af', strokeWeight:2, strokeOpacity:0.8, geodesic:true, zIndex:golgotha.maps.z.POLYLINE});
			routeLine.setMap(map);
			routes.push(routeLine);
		}
	}

	// Focus on the map
	isLoading.innerHTML = '';
	gaEvent('Route Map', 'Routes', this.icao);
	return true;
} // function

// Send the XMLHTTP request
xmlreq.send(null);
var showInfo = document.getElementById('showInfo');
if ((showInfo) && (showInfo.checked)) {
	map.infoWindow.setContent(this.infoLabel);
	map.infoWindow.open(map, this);
}

return true;
}
