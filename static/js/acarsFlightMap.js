golgotha.maps.acarsFlight = golgotha.maps.acarsFlight || {selectedFIRs:[], routePoints:[], routeMarkers:[], airspace:[], asPolygons:[], debugMarkers:[]}; 
golgotha.maps.acarsFlight.airspaceColors = {'P':{c:'#ee1010',tx:0.4,z:10}, 'R':{c:'#adad10',tx:0.2,z:5}, 'B':{c:'#10e0e0',tx:0.1,z:0}, 'C':{c:'#ffa018', tx:0.125,z:1}, 'D':{c:'#608040', tx:0.175,z:2}};
golgotha.maps.acarsFlight.getACARSData = function(pirepID, doToggle, showAirspace)
{
// Disable checkboxes
const f = document.forms[0];
f.showFDR.disabled = true; f.showRoute.disabled = true; f.showAirspace.disabled = true;
golgotha.util.disable(f.rwyDebug, true);

// Fetch the data
const p = fetch('acars_pirep.ws?id=' + pirepID + '&showAirspace=' + showAirspace, {signal:AbortSignal.timeout(5500)});
p.then(function(rsp) {
	const errE = document.getElementById('archiveError');
	if (rsp.status != 200) {
		errE.innerText = 'Error ' + rsp.status;
		golgotha.util.display(errE, true);
		return false;
	}

	rsp.json().then(function(js) {
		if (js.error) {
			errE.innerText = js.error;
			golgotha.util.display(errE, true);
		} else
			golgotha.util.display('archiveOK', true);

		js.positions.forEach(function(p) {
			let mrk; const ll = golgotha.maps.toLL(p.ll); ll.push(golgotha.maps.feet2Meter(p.alt * map.verticalEx + 2.5));
			golgotha.maps.acarsFlight.routePoints.push(ll);
			if (p.icon)
				mrk = new golgotha.maps.IconMarker({pal:p.pal, icon:p.icon, info:p.info, opacity:0.75, pt:ll});
			else if (p.color)
				mrk = new golgotha.maps.Marker({color:p.color, info:p.info, opacity:0.75, scale:0.5, pt:ll});
			else
				return false;

			// Add ATC data
			golgotha.maps.acarsFlight.routeMarkers.push(mrk);
			if (p.atc) {
				if ((p.atc.type != 'CTR') && (p.atc.type != 'FSS')) {
					mrk.range = p.atc.range; mrk.atcPosition = p.atc.ll;
					mrk.on('click', function() { golgotha.maps.acarsFlight.showAPP(this.atcPosition, this.range); });
				} else {
					mrk.atcID = p.atc.id;
					mrk.on('click', function() { golgotha.maps.acarsFlight.showFIR(this.atcID); });
				}
			} else
				mrk.on('click', golgotha.maps.acarsFlight.hideATC);
		});

		js.airspace.forEach(function(a) {
			if (a.exclude) return false;
			a.c = golgotha.maps.acarsFlight.airspaceColors[a.type];
			golgotha.maps.acarsFlight.airspace.push(a);
		});

		// Create the line, but don't show it
		golgotha.maps.acarsFlight.gRoute = new golgotha.maps.Line3D('flightPath', {color:'#c01933',width:4,opacity:0.875,visible:false}, golgotha.maps.acarsFlight.routePoints);
		golgotha.event.beacon('ACARS', 'Flight Data');

		if (f.rwyDebug) {
			golgotha.maps.acarsFlight.showRunway(js.runwayD, golgotha.local.takeoff);
			golgotha.maps.acarsFlight.showRunway(js.runwayA, golgotha.local.landing);
			golgotha.util.disable(f.rwyDebug, false);
			const isDebug = localStorage.getItem('golgotha.rwyDebug');
			if (isDebug == 'true') {
				f.rwyDebug.checked = true;
				golgotha.local.zoomTo(golgotha.local.landing.lat, golgotha.local.landing.lng, 16);
				map.toggle(golgotha.maps.acarsFlight.debugMarkers, true);
			} else
				map.toggle(golgotha.maps.acarsFlight.debugMarkers, false);
		}

		// Enable checkboxes
		golgotha.util.disable(f.showFDR, false);
		golgotha.util.disable(f.showRoute, false);
		golgotha.util.disable(f.showAirspace, false);
		if (doToggle) f.showRoute.click();
	});
});

return true;
};

golgotha.maps.acarsFlight.hideATC = function() {
	while (golgotha.maps.acarsFlight.selectedFIRs.length > 0) {
		const mrk = golgotha.maps.acarsFlight.selectedFIRs.shift();
		mrk.setMap(null);
	}
};

golgotha.maps.acarsFlight.showAPP = function(ctr, range) {
	golgotha.maps.acarsFlight.hideATC();
	const c = new golgotha.maps.Circle({radius:golgotha.maps.miles2Meter(range), color:'#efefff', width:1, opacity:0.85, fillColor:'#7f7f80', fillOpacity:0.25}, ctr);
	map.addLine(c);
	golgotha.maps.acarsFlight.selectedFIRs.push(c);
};

golgotha.maps.acarsFlight.addAirspace = function(as) {
	as.poly = new golgotha.maps.Polygon(as.id, {color:as.c.c, width:1, opacity:as.c.tx, fillColor:'#802020', fillOpacity:0.2, info:as.info}, as.border);
	map.addLine(as.poly);
	if (as.info) {
		map.on('click', as.poly.name, function(e) { 
			const p = new mapboxgl.Popup({closeOnClick:true,focusAfterOpen:false,maxWidth:'300px'});
			p.setHTML(e.features[0].properties.info).setLngLat(e.lngLat).addTo(map);
		});
	}
};

golgotha.maps.acarsFlight.removeAirspace = function(as) {
	if (!as.poly) return false;
	map.removeLayer(as.poly.name);
	map.removeSource(as.poly.name);
	delete as.poly;
};

golgotha.maps.acarsFlight.toggleAirspace = function(show) {
	golgotha.maps.acarsFlight.airspace.forEach(show ? golgotha.maps.acarsFlight.addAirspace : golgotha.maps.acarsFlight.removeAirspace);
};

golgotha.maps.acarsFlight.showRunway = function(rd, pd) {
	const rw = rd.threshold || rd.location;
	const dst = golgotha.maps.distance(rd.pt, pd);
	const l = new golgotha.maps.Line('RWY', {width:7.5, color:'#0000a1', opacity:0.25}, [rw,rd.pt]);
	golgotha.maps.acarsFlight.debugMarkers.push(l);
	map.addLine(l);
	
	//golgotha.maps.acarsFlight.debugMarkers.push(new google.maps.Circle({map:map, center:rd.pt, radius:golgotha.maps.feet2Meter(Math.abs(rd.distance)), strokeColor:'#0000a1', strokeOpacity:0.5, strokeWeight:1, fillColor:'#0000a1', fillOpacity:0.2, zIndex:golgotha.maps.z.POLYGON}));
	golgotha.maps.acarsFlight.debugMarkers.push(new golgotha.maps.IconMarker({map:map, pal:3, icon:38, opacity:0.5, info:'Actual Takeoff/Touchdown', pt:pd}));
	if (dst > 15)
		golgotha.maps.acarsFlight.debugMarkers.push(new golgotha.maps.IconMarker({map:map, pal:3, icon:53, opacity:0.5, info:'Runway Takeoff/Touchdown, distance=' + Math.round(dst) + ' meters'}, rd.pt));
	/* if (rd.threshold)
		golgotha.maps.acarsFlight.debugMarkers.push(new google.maps.Circle({map:map, center:rd.threshold, radius:golgotha.maps.feet2Meter(Math.abs(rd.distance)), strokeColor:'#a000a1', strokeOpacity:0.5, strokeWeight:1, fillColor:'#a000a1', fillOpacity:0.2, zIndex:golgotha.maps.z.POLYGON})); */
};

golgotha.maps.acarsFlight.toggleDebug = function(isEnabled) {
	map.toggle(golgotha.maps.acarsFlight.debugMarkers, isEnabled);
	localStorage.setItem('golgotha.rwyDebug', isEnabled);
};

golgotha.maps.distance = function (l1, l2) {
	const R = 3958.8; // Radius of the Earth in miles
	const rlat1 = l1.lat() * (Math.PI/180); // Convert to radians
	const rlat2 = l2.lat() * (Math.PI/180); // Convert to radians
    const difflat = rlat2-rlat1;
	const difflon = (l2.lng()-l1.lng()) * (Math.PI/180); // Radian difference (longitudes)
	const d = 2 * R * Math.asin(Math.sqrt(Math.sin(difflat/2)*Math.sin(difflat/2)+Math.cos(rlat1)*Math.cos(rlat2)*Math.sin(difflon/2)*Math.sin(difflon/2)));
	return golgotha.maps.miles2Meter(d);
};

golgotha.maps.acarsFlight.showFIR = function(code) {
	const p = fetch('fir.ws?id=' + code);
	p.then(function(rsp) {
		if (rsp.status != 200) return false;
		rsp.json().then(function(js) {
			golgotha.maps.acarsFlight.hideATC();	
			js.firs.forEach(function(fe) {
				if (fe.border.length == 0) return false;
				const p = new golgotha.maps.Polygon(fe.id, {color:'#efefff', width:1, opacity:0.5, fillColor:'#7f7f80', fillOpacity:0.25}, fe.border);
				map.addLine(p);
				golgotha.maps.acarsFlight.selectedFIRs.push(p);
			});		

			golgotha.event.beacon('ACARS', 'Show FIR', code);			
		});
	});
};
