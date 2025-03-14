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

// Fetch the data
golgotha.util.setHTML('isLoading', ' - LOADING...');
const p = fetch('rmap_airports.ws?airline=' + aCode);
p.then(function(rsp) {
	if (!rsp.ok) return false;
	golgotha.util.setHTML('isLoading', ' - REDRAWING...');
	golgotha.routeMap.aps = [];

	// Parse the JSON
	rsp.json().then(function(js) {
		js.airports.forEach(function(a) {
			const mrk = new golgotha.maps.Marker({color:a.color}, a.ll);
			mrk.icao = a.icao; mrk.iata = a.iata;
			mrk.infoShow = golgotha.routeMap.showRoutes;
			mrk.infoLabel = a.info;
			google.maps.event.addListener(mrk, 'click', mrk.infoShow);
			golgotha.routeMap.aps.push(mrk);	
		});

		// Save in the hashmap
		map.addMarkers(golgotha.routeMap.aps);
		golgotha.routeMap.airports[aCode] = golgotha.routeMap.aps;
		golgotha.util.setHTML('isLoading', '');
		golgotha.event.beacon('Route Map', 'Airports', aCode);
	});	
});

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

// Fetch the data
const p = fetch('rmap_routes.ws?icao=' + this.icao + '&airline=' + aCode);
p.then(function(rsp) {
	if (!rsp.ok) return false;
	golgotha.util.setHTML('isLoading', ' - REDRAWING...');
	golgotha.routeMap.routes.length = 0;
	
	// Parse the JSON
	rsp.json().then(function(js) {
		js.routes.forEach(function(rt) {
			if (rt.airline != aCode) return false;
			const routeLine = new google.maps.Polyline({map:map, path:rt.positions, strokeColor:rt.color, strokeWeight:2, strokeOpacity:0.8, geodesic:true, zIndex:golgotha.maps.z.POLYLINE});
			golgotha.routeMap.routes.push(routeLine);
		});	
		
		// Focus on the map
		golgotha.util.setHTML('isLoading', '');
		golgotha.event.beacon('Route Map', 'Routes', this.icao);
		return true;
	});
});

const showInfo = document.getElementById('showInfo');
if ((showInfo) && (showInfo.checked)) {
	map.infoWindow.setContent(this.infoLabel);
	map.infoWindow.open(map, this);
}

return true;
};
