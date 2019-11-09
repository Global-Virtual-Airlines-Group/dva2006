golgotha.routeMap = golgotha.routeMap || {routes:[], airports:[], aps:[]};
golgotha.routeMap.updateAirports = function(combo)
{
golgotha.util.setHTML('isLoading', ' - CLEARING...');

// Remove airports/routes from the map
map.removeMarkers(golgotha.routeMap.aps);
map.removeMarkers(golgotha.routeMap.routes);
map.closeWindow();
if (!golgotha.form.comboSet(combo)) {
	golgotha.util.setHTML('isLoading', '');
	return false;
}

// Check if we have cached airports
const aCode = golgotha.form.getCombo(combo);
golgotha.routeMap.aps = golgotha.routeMap.airports[aCode];
if (golgotha.routeMap.aps) {
	map.addMarkers(golgotha.routeMap.aps);
	golgotha.util.setHTML('isLoading', '');
	return true;
}

// Build the XML Requester
golgotha.util.setHTML('isLoading', ' - LOADING...');
const xmlreq = new XMLHttpRequest();
xmlreq.open('get', 'rmap_airports.ws?airline=' + aCode, true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	golgotha.util.setHTML('isLoading', ' - REDRAWING...');
	golgotha.routeMap.aps = [];
	const f = document.forms[0];

	// Parse the JSON
	const js = JSON.parse(xmlreq.responseText);
	for (var x = 0; x < js.airports.length; x++) {
		const a = js.airports[x];
		const mrk = new golgotha.maps.Marker({color:a.color}, a.ll);
		mrk.icao = a.icao; mrk.iata = a.iata;
		mrk.infoShow = golgotha.routeMap.showRoutes;
		mrk.infoLabel = a.info;
		google.maps.event.addListener(mrk, 'click', mrk.infoShow);
		golgotha.routeMap.aps.push(mrk);
	}

	// Save in the hashmap
	map.addMarkers(golgotha.routeMap.aps);
	golgotha.routeMap.airports[aCode] = golgotha.routeMap.aps;
	golgotha.util.setHTML('isLoading', '');
	golgotha.event.beacon('Route Map', 'Airports', aCode);
	return true;
};

xmlreq.send(null);
return true;
}

golgotha.routeMap.showRoutes = function()
{
// Update status
golgotha.util.setHTML('isLoading', ' - LOADING...');

// Get the airline code
const aCombo = document.getElementById('airlineCode');
const aCode = aCombo.options[aCombo.selectedIndex].value;
map.removeMarkers(golgotha.routeMap.routes);
map.closeWindow();

// Build the XML Requester
const xmlreq = new XMLHttpRequest();
xmlreq.open('get', 'rmap_routes.ws?icao=' + this.icao + '&airline=' + aCode, true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	golgotha.util.setHTML('isLoading', ' - REDRAWING...');
	golgotha.routeMap.routes.length = 0;

	// Parse the JSON
	const js = JSON.parse(xmlreq.responseText);
	js.routes.forEach(function(rt) {
		if (rt.airline != aCode) return false;
		var routeLine = new google.maps.Polyline({map:map, path:rt.positions, strokeColor:rt.color, strokeWeight:2, strokeOpacity:0.8, geodesic:true, zIndex:golgotha.maps.z.POLYLINE});
		golgotha.routeMap.routes.push(routeLine);
	});

	// Focus on the map
	golgotha.util.setHTML('isLoading', '');
	golgotha.event.beacon('Route Map', 'Routes', this.icao);
	return true;
};

xmlreq.send(null);
const showInfo = document.getElementById('showInfo');
if ((showInfo) && (showInfo.checked)) {
	map.infoWindow.setContent(this.infoLabel);
	map.infoWindow.open(map, this);
}

return true;
};
