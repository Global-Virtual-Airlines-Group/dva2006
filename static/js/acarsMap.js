golgotha.maps.acars = golgotha.maps.acars || {acPositions:[], dcPositions:[], routeData:null, routeWaypoints:null, tempData:null, routeMarkers:[]};
golgotha.maps.acars.generateXMLRequest = function()
{
const xmlreq = new XMLHttpRequest();
xmlreq.timeout = 2500;
xmlreq.open('get', 'acars_map_json.ws?time=' + golgotha.util.getTimestamp(3000), true);
xmlreq.ontimeout = function() { golgotha.util.setHTML('isLoading', ' - TIMEOUT'); return true; };
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	if (xmlreq.status != 200) {
		golgotha.util.setHTML('isLoading', ' - ERROR ' + xmlreq.status);
		return false;
	}

	// Clean up the map - don't strip out the weather layer
	golgotha.util.setHTML('isLoading', ' - REDRAWING...');
	golgotha.maps.acars.infoClose();
	map.removeMarkers(golgotha.maps.acars.acPositions);
	map.removeMarkers(golgotha.maps.acars.dcPositions);
	golgotha.maps.acars.acPositions.length = 0;
	golgotha.maps.acars.dcPositions.length = 0;
	golgotha.util.display('userSelect', false);
	const cbo = document.forms[0].usrID;
	if (cbo != null) {
		selectedPilot = cbo.options[cbo.selectedIndex].value;
		cbo.options.length = 1;
	}

	// Parse the JSON
	const js = JSON.parse(xmlreq.responseText);
	if (js.aircraft.length > 0) golgotha.event.beacon('ACARS', 'Aircraft Positions');
	const allAC = js.aircraft.sort(golgotha.maps.acars.sort);
	allAC.forEach(function(a) {
		const mrk = new golgotha.maps.AircraftMarker({color:a.color, pt:a.ll, hdg:a.hdg});
		mrk.isExternal = a.hasOwnProperty('external_id');
		mrk.flight_id = mrk.isExternal ? a.external_id : a.flight_id;
		mrk.isBusy = a.busy;
		const p = new mapboxgl.Popup({closeOnClick:true,focusAfterOpen:false,maxWidth:'320px'});
		if (a.tabs.length != 0) {
			mrk.updateTab = golgotha.maps.util.updateTab;
			mrk.tabs = a.tabs;
			p.setHTML(mrk.updateTab(0));
		} else
			p.setHTML(a.info);

		// Set click handlers
		p.on('close', golgotha.maps.acars.infoClose);
		p.on('open', function(e) { 
			golgotha.maps.selectedMarker = e.target._marker;
			golgotha.maps.acars.clickAircraft(golgotha.maps.selectedMarker);
		});

		// Add the user ID
		if ((a.pilot) && (cbo != null)) {
			let lbl = a.pilot.name;
			if (a.pilot.code != null)
				lbl += (' (' + a.pilot.code + ')');

			const o = new Option(lbl, a.pilot.code);
			o.mrk = mrk;
			cbo.add(o, null);
			if (selectedPilot == a.pilot.code)
				cbo.selectedIndex = (cbo.options.length - 1);
		}

		mrk.setPopup(p);
		golgotha.maps.acars.acPositions.push(mrk);
		mrk.setMap(map);
	});

	if (js.dispatch.length > 0) golgotha.event.beacon('ACARS', 'Dispatch Positions');
	const allDSP = js.dispatch.sort(golgotha.maps.acars.sort);
	allDSP.forEach(function(d) { 
		const mrk = (d.pal) ? new golgotha.maps.IconMarker({pal:d.pal, icon:d.icon, pt:d.ll}) : new golgotha.maps.Marker({color:d.color, pt:d.ll});  
		mrk.range = d.range;
		mrk.isBusy = d.busy;
		const p = new mapboxgl.Popup({closeOnClick:true,focusAfterOpen:false,maxWidth:'320px'});
		if (d.tabs.length != 0) {
			mrk.updateTab = golgotha.maps.util.updateTab;
			mrk.tabs = d.tabs;
			p.setHTML(mrk.updateTab(0));
		} else
			p.setHTML(d.info);
			
		// Set click handlers
		p.on('close', golgotha.maps.acars.infoClose);			
		p.on('open', function(e) { 
			golgotha.maps.selectedMarker = e.target._marker;
			golgotha.maps.acars.clickDispatch(golgotha.maps.selectedMarker);
		});

		// Add the user ID
		if ((d.pilot) && (cbo != null)) {
			const o = new Option(d.pilot.name + ' (' + d.pilot.code + '/Dispatcher)', d.pilot.code);
			o.mrk = mrk;
			cbo.add(o, null);
			if (selectedPilot == d.pilot.code)
				cbo.selectedIndex = (cbo.options.length - 1);
		}

		mrk.setPopup(p);
		golgotha.maps.acars.dcPositions.push(mrk);
		mrk.setMap(map);
	});

	// Enable the Google Earth button depending on if we have any aircraft
	golgotha.util.disable('EarthButton', (js.aircraft.length == 0));

	// Display dispatch status
	const de = document.getElementById('dispatchStatus');
	if ((de) && (js.dispatch.length > 0)) {
		de.className = 'ter bld caps';
		de.innerHTML = 'Dispatcher Currently Online';
	} else if (de) {
		de.className = 'bld caps';	
		de.innerHTML = 'Dispatcher Currently Offline';
	}

	// Focus on the map
	golgotha.util.setHTML('isLoading', ' - ' + (js.aircraft.length + js.dispatch.length) + ' CONNECTIONS');
	if (cbo)
		golgotha.util.display('userSelect', (cbo.options.length > 1));

	return true;
};

return xmlreq;
};

golgotha.maps.acars.clickAircraft = function(mrk)
{
// Check what info we display
const f = document.forms[0];
const isProgress = f.showProgress.checked;
const isRoute = f.showRoute.checked;
golgotha.event.beacon('ACARS', 'Flight Info');

// Display flight progress / route
if (isProgress || isRoute)
	golgotha.maps.acars.showFlightProgress(mrk, isProgress, isRoute);

document.pauseRefresh = true;
return true;
};

golgotha.maps.acars.clickDispatch = function(mrk)
{
golgotha.event.beacon('ACARS', 'Dispatch Info');

// Display flight progress / route
if (!this.rangeCircle) {
	this.rangeCircle = golgotha.maps.acars.getServiceRange(mrk, this.range);
	if (this.rangeCircle) {
		golgotha.event.beacon('ACARS', 'Dispatch Service Range');
		this.rangeCircle.setMap(map);
	}
} else
	this.rangeCircle.setMap(map);

document.pauseRefresh = true;
return true;
};

golgotha.maps.acars.infoClose = function() {
	document.pauseRefresh = false;
	if (!golgotha.maps.selectedMarker) return false;
	delete golgotha.maps.selectedMarker;
	/* if ((map.infoWindow.marker) && (map.infoWindow.marker.rangeCircle))
		map.infoWindow.marker.rangeCircle.setMap(null); */

	map.removeLine(golgotha.maps.acars.routeData);
	map.removeLine(golgotha.maps.acars.tempData);
	map.removeLine(golgotha.maps.acars.routeWaypoints);
	map.removeMarkers(golgotha.maps.acars.routeMarkers);
	return true;	
};

golgotha.maps.acars.showFlightProgress = function(marker, doProgress, doRoute) {
	const p = fetch('acars_progress_json.ws?id=' + marker.flight_id + '&time=' + golgotha.util.getTimestamp(3000) + '&route=' + doRoute + '&isExternal=' + marker.isExternal, {signal:AbortSignal.timeout(3500)});
	p.then(function(rsp) {
		if (!rsp.ok) return false;
		rsp.json().then(function(js) {
			if (doRoute) {
				js.waypoints.forEach(function(wp) { 
					const mrk = (wp.pal) ? new golgotha.maps.IconMarker({pal:wp.pal, icon:wp.icon, opacity:0.5, pt:wp.ll, label:wp.code}) : new golgotha.maps.Marker({color:wp.color, opacity:0.5, pt:wp.ll, label:wp.code});   
					mrk.setMap(map);
					golgotha.maps.acars.routeMarkers.push(mrk);
				});

				golgotha.event.beacon('ACARS', 'Flight Route Info');
				golgotha.maps.acars.routeWaypoints = new golgotha.maps.Line('fp-' + marker.flight_id, {color:'#af8040', width:2, opacity:0.7}, js.routePathPoints);
				map.addLine(golgotha.maps.acars.routeWaypoints);
			}		

			if (doProgress) {
				golgotha.event.beacon('ACARS', 'Flight Progress Info');
				golgotha.maps.acars.routeData = new golgotha.maps.Line('svpos-' + marker.flight_id, {color:'#4080af', width:2, opacity:0.8}, js.savedPositions);
				golgotha.maps.acars.tempData = new golgotha.maps.Line('tmpos-' + marker.flight_id, {color:'#20a0bf', width:2, opacity:0.625}, js.tempPositions);
				map.addLine(golgotha.maps.acars.routeData);
				map.addLine(golgotha.maps.acars.tempData);
			}
		});
	});
};

golgotha.maps.acars.sort = function(e1, e2) { return e1.pilot.name.localeCompare(e2.pilot.name); };
golgotha.maps.acars.getServiceRange = function(marker, range) {
	const bC = marker.isBusy ? '#c02020' : '#20c060';
	const fC = marker.isBusy ? '#802020' : '#208040';
	const fOp = marker.isBusy ? 0.1 : 0.2;
	return new google.maps.Circle({center:marker.getPosition(), radius:golgotha.maps.miles2Meter(range), strokeColor:bC, strokeWeight:1, strokeOpacity:0.65, fillColor:fC, fillOpacity:fOp, zIndex:golgotha.maps.z.POLYGON});
};

golgotha.maps.acars.zoomTo = function(combo) {
	const opt = combo.options[combo.selectedIndex];
	if ((!opt) || (!opt.mrk)) return false;

	// Check if we zoom or just pan
	const f = document.forms[0];
	if (f.zoomToPilot.checked) map.setZoom(9);
	map.panTo(opt.mrk.getLngLat());
	opt.mrk.getElement().dispatchEvent(new Event('click'));
	return true;
};

golgotha.maps.clear = function() { localStorage.removeItem('golgotha.mapInfo'); return true; };
golgotha.maps.save = function(m) {
	const inf = {type:m.getMapType(), zoom:m.getZoom(), ctr:map.getCenter()};
	localStorage.setItem('golgotha.mapInfo', JSON.stringify(inf));
	return true;
};

golgotha.maps.acars.reloadData = function(isAuto) {
	const isVisible = !document.visibilityState || (document.visibilityState == 'visible');
	const doRefresh = document.forms[0].autoRefresh.checked;

	// Generate XMLHTTPRequest if we're not already viewing a flight
	if (!document.pauseRefresh && isVisible) {
		const isLoading = document.getElementById('isLoading');
		isLoading.innerHTML = ' - LOADING...';
		const xmlreq = golgotha.maps.acars.generateXMLRequest();
		xmlreq.send(null);
	}

	// Set timer to reload the data
	if (doRefresh && isAuto)
		window.setTimeout(golgotha.maps.acars.reloadData, golgotha.local.refresh, true);

	return true;
};

golgotha.maps.acars.showLegend = function(box) {
	const rows = golgotha.util.getElementsByClass('mapLegend', 'tr');
	rows.forEach(function(r) { golgotha.util.display(r, box.checked); });
	return true;
};

golgotha.maps.acars.showEarth = function() {
	self.location = '/acars_map_earth.ws';
	return true;
};

golgotha.maps.acars.updateSettings = function() {
	golgotha.maps.save(map);
	return true;
};
