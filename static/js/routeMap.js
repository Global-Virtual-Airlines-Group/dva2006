golgotha.routeMap = golgotha.routeMap || {routes:[], airports:[], aps:[]};
golgotha.routeMap.updateAirports = function(combo)
{
golgotha.util.setHTML('isLoading', ' - CLEARING...');
const showInfoCB = document.getElementById('showInfo');
const showInfo = ((showInfoCB) && (showInfoCB.checked));

// Remove airports/routes from the map
map.removeMarkers(golgotha.routeMap.aps);
map.removeMarkers(golgotha.routeMap.routes);
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
const p = fetch('rmap_airports.ws?airline=' + aCode, {signal:AbortSignal.timeout(5000)});
p.then(function(rsp) {
	if (!rsp.ok) return false;
	golgotha.util.setHTML('isLoading', ' - REDRAWING...');
	golgotha.routeMap.aps = [];
	rsp.json().then(function(js) {
		js.airports.forEach(function(a) {
			const mrk = new golgotha.maps.Marker({color:a.color, info:(showInfo ? a.info : null), pt:a.ll});
			mrk.icao = a.icao; mrk.iata = a.iata;
			mrk.infoShow = golgotha.routeMap.showRoutes;
			mrk.getElement().addEventListener('click', mrk.infoShow);
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

golgotha.routeMap.showRoutes = function() {
	golgotha.util.setHTML('isLoading', ' - LOADING...');

	// Get the airline code
	const mrk = this.marker;
	const aCode = golgotha.form.getCombo(document.getElementById('airlineCode'));
	map.removeMarkers(golgotha.routeMap.routes);

	// Fetch the data
	const p = fetch('rmap_routes.ws?icao=' + mrk.icao + '&airline=' + aCode, {signal:AbortSignal.timeout(5000)});
	p.then(function(rsp) {
		if (!rsp.ok) return false;
		golgotha.util.setHTML('isLoading', ' - REDRAWING...');
		golgotha.routeMap.routes.length = 0;
		rsp.json().then(function(js) {
			js.routes.forEach(function(rt) {
				if (rt.airline != aCode) return false;
				const routeLine = new golgotha.maps.Line(rt.from + '-' + rt.to, {color:rt.color, width:2, opacity:0.625}, rt.positions);
				map.addLine(routeLine);
				golgotha.routeMap.routes.push(routeLine);
			});	
		
			// Focus on the map
			golgotha.util.setHTML('isLoading', '');
			golgotha.event.beacon('Route Map', 'Routes', this.icao);
			return true;
		});
	});
};
