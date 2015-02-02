golgotha.routeMap = golgotha.routeMap || {routes:[], airports:[], aps:[]};
golgotha.routeMap.updateAirports = function(combo)
{
var isLoading = document.getElementById('isLoading');
isLoading.innerHTML = ' - CLEARING...';

// Remove airports/routes from the map
map.removeMarkers(golgotha.routeMap.aps);
map.removeMarkers(golgotha.routeMap.routes);
map.closeWindow();
if (!golgotha.form.comboSet(combo)) {
	isLoading.innerHTML = '';
	return false;
}

// Check if we have cached airports
var aCode = golgotha.form.getCombo(combo);
golgotha.routeMap.aps = golgotha.routeMap.airports[aCode];
if (golgotha.routeMap.aps) {
	map.addMarkers(golgotha.routeMap.aps);
	isLoading.innerHTML = '';
	return true;
}

// Build the XML Requester
isLoading.innerHTML = ' - LOADING...';
var xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'rmap_airports.ws?airline=' + aCode, true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var isLoading = document.getElementById('isLoading');
	isLoading.innerHTML = ' - REDRAWING...';
	golgotha.routeMap.aps = [];
	var f = document.forms[0];

	// Parse the XML
	var xdoc = xmlreq.responseXML;
	var wsdata = xdoc.documentElement;
	var els = wsdata.getElementsByTagName('airport');
	for (var x = 0; x < els.length; x++) {
		var a = els[x];
		var mrk = new golgotha.maps.Marker({color:a.getAttribute('color')}, {lat:parseFloat(a.getAttribute('lat')),lng:parseFloat(a.getAttribute('lng'))});
		mrk.icao = a.getAttribute('icao');
		mrk.iata = a.getAttribute('iata');
		mrk.infoShow = golgotha.routeMap.showRoutes;
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
		golgotha.routeMap.aps.push(mrk);
	}

	// Save in the hashmap
	map.addMarkers(golgotha.routeMap.aps);
	golgotha.routeMap.airports[aCode] = golgotha.routeMap.aps;
	isLoading.innerHTML = '';
	golgotha.event.beacon('Route Map', 'Airports', aCode);
	return true;
};

xmlreq.send(null);
return true;
}

golgotha.routeMap.showRoutes = function()
{
// Update status
var isLoading = document.getElementById('isLoading');
isLoading.innerHTML = ' - LOADING...';

// Get the airline code
var aCombo = document.getElementById('airlineCode');
var aCode = aCombo.options[aCombo.selectedIndex].value;
map.removeMarkers(golgotha.routeMap.routes);
map.closeWindow();

// Build the XML Requester
var xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'rmap_routes.ws?icao=' + this.icao + '&airline=' + aCode, true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var isLoading = document.getElementById('isLoading');
	isLoading.innerHTML = ' - REDRAWING...';
	golgotha.routeMap.routes.length = 0;

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
				positions.push({lat:parseFloat(pe.getAttribute('lat')), lng:parseFloat(pe.getAttribute('lng'))});
			}

			// Draw the line
			var routeLine = new google.maps.Polyline({map:map, path:positions, strokeColor:'#4080af', strokeWeight:2, strokeOpacity:0.8, geodesic:true, zIndex:golgotha.maps.z.POLYLINE});
			golgotha.routeMap.routes.push(routeLine);
		}
	}

	// Focus on the map
	isLoading.innerHTML = '';
	golgotha.event.beacon('Route Map', 'Routes', this.icao);
	return true;
};

xmlreq.send(null);
var showInfo = document.getElementById('showInfo');
if ((showInfo) && (showInfo.checked)) {
	map.infoWindow.setContent(this.infoLabel);
	map.infoWindow.open(map, this);
}

return true;
};
