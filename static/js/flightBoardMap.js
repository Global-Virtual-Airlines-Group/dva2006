golgotha.flightBoard = golgotha.flightBoard || {atc:[], pilots:[], months:['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'], network:'?'};
golgotha.flightBoard.updateMap = function(isAuto)
{
const xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'si_data.ws?network=' + golgotha.flightBoard.network + '&time=' + golgotha.util.getTimestamp(5000) + '&atc=true', true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	if (xmlreq.status != 200) {
		golgotha.util.setHTML('isLoading', ' - ERROR ' + xmlreq.status);
		return false;
	}

	map.clearOverlays();
	golgotha.util.display('userSelect', false);
	const cbo = document.getElementById('usrID');
	const selectedATC = cbo.options[cbo.selectedIndex].value;
	cbo.options.length = 1;
	const js = JSON.parse(xmlreq.responseText);

	// Display effective date
	const dt = new Date(js.date);
	golgotha.util.setHTML('isLoading', ' - VALID AS OF ' + golgotha.flightBoard.formatDate(dt));

	// Display pilots
	golgotha.flightBoard.pilots.length = 0;
	js.pilots.forEach(function(wp) {
		const mrk = new golgotha.maps.Marker({color:wp.color, info:wp.info, map:map, pt:[wp.ll.lng, wp.ll.lat]});
		mrk.networkID = wp.id; mrk.callsign = wp.callsign;
		mrk.getElement().addEventListener('click', function(e) {
			const mrk = e.currentTarget.marker;
			golgotha.flightBoard.infoClose(); 
			golgotha.flightBoard.showRoute(mrk.networkID); 
		});
		
		golgotha.flightBoard.pilots[mrk.callsign] = mrk;
	});

	// Display controllers
	js.atc.forEach(function(cp) {
		const mrk = new golgotha.maps.Marker({color:cp.color, info:cp.info, map:map, pt:[cp.ll.lng, cp.ll.lat]});
		mrk.networkID = cp.id; mrk.callsign = cp.callsign;
		if ((cp.type == 'CTR') || (cp.type == 'FSS')) {
			mrk.getElement().addEventListener('click', function(e) {
				const mrk = e.currentTarget.marker; 
				golgotha.flightBoard.infoClose(); 
				golgotha.flightBoard.showFIR(mrk.callsign); 
			});
		} else if (cp.type == 'APP') {
			mrk.range = cp.range;
			mrk.getElement().addEventListener('click', function(e) {
				const mrk = e.currentTarget.marker;
				golgotha.flightBoard.infoClose(); 
				golgotha.flightBoard.showAPP(mrk);
			});
		}

		golgotha.flightBoard.atc[mrk.callsign] = mrk;

		// Add to ATC list
		const o = new Option(mrk.callsign, mrk.callsign);
		o.mrk = mrk;
		cbo.add(o, null);
		if (selectedATC == mrk.callsign)
			cbo.selectedIndex = (cbo.options.length - 1);
	});

	golgotha.util.display('userSelect', (cbo.options.length > 1));
	if (isAuto)
		window.setTimeout(golgotha.flightBoard.updateMap, 90000);

	return true;
};

xmlreq.send(null);
return true;
};

golgotha.flightBoard.formatDate = function(dt) {
	let fdt = golgotha.flightBoard.months[dt.getMonth()] + ' ' + dt.getDate() + ' ' + dt.getFullYear() + ' ';
	if (dt.getHours() < 10) fdt += '0';
	fdt += dt.getHours();
	fdt += ':';
	if (dt.getMinutes() < 10) fdt += '0';
	fdt += dt.getMinutes();
	return fdt;
}

golgotha.flightBoard.infoClose = function() {
	if (golgotha.flightBoard.selectedRoute) {
		map.removeMarkers(golgotha.flightBoard.selectedRoute);
		delete golgotha.flightBoard.selectedRoute;
	}

	if (golgotha.flightBoard.selectedTrack) {
		map.removeMarkers(golgotha.flightBoard.selectedTrack);
		delete golgotha.flightBoard.selectedTrack;
	}

	if (golgotha.flightBoard.waypoints) {
		map.removeMarkers(golgotha.flightBoard.waypoints);
		delete golgotha.flightBoard.waypoints;
	}

	return true;
};

golgotha.flightBoard.zoomTo = function(combo) {
	const opt = combo.options[combo.selectedIndex];
	if ((!opt) || (!opt.mrk)) return false;	
	map.panTo(opt.mrk.getLngLat());
	opt.mrk.getElement().dispatchEvent(new Event('click'));
	opt.mrk.togglePopup();
	return true;
};

golgotha.flightBoard.setNetwork = function(combo) {
	location.href = '/flightboardmap.do?id=' + combo.options[combo.selectedIndex].text + '&op=map';
	return true;
};

golgotha.flightBoard.showAPP = function(mrk) {
	golgotha.flightBoard.infoClose();
	const c = new golgotha.maps.Circle(mrk.callsign, {color:'#208040', opacity:0.55, width:2, fillOpacity:0.25, fillColor:'#208040', radius:mrk.range}, mrk.getLngLat());
	map.addLine(c);
	golgotha.flightBoard.selectedRoute = c;
};

golgotha.flightBoard.showFIR = function(code) {
	golgotha.flightBoard.infoClose();
	const p = fetch('fir.ws?id=' + code, {signal:AbortSignal.timeout(2500)});
	p.then(function(rsp) {
		if (rsp.status != 200) return false;
		golgotha.flightBoard.infoClose();
		rsp.json().then(function(js) {
			golgotha.flightBoard.selectedRoute = [];	
			js.firs.forEach(function(fe) {
				if (fe.border.length == 0) return false;
				fe.border.push(fe.border[0]);
				const pl = new golgotha.maps.Polygon(fe.id, {color:'#efefff', width:1, opacity:0.85, fillColor:'#7f7f80', fillOpacity:0.25}, fe.border);
				map.addLine(pl);
				golgotha.flightBoard.selectedRoute.push(pl);
			});
		});
	});
};

golgotha.flightBoard.showRoute = function(pilotID) {
	const p = fetch('si_route.ws?network=' + golgotha.flightBoard.network + '&id=' + pilotID + '&time=' + golgotha.util.getTimestamp(5000), {signal:AbortSignal.timeout(2500)});
	p.then(function(rsp) {
		if (rsp.status != 200) return false;
		golgotha.flightBoard.infoClose();	
		rsp.json().then(function(js) {
			if (js.route instanceof Array) {
				golgotha.flightBoard.selectedRoute = new golgotha.maps.Line('rt-' + pilotID, {color:'#af8040', width:2, opacity:0.625}, js.route);
				map.addLine(golgotha.flightBoard.selectedRoute);
			}
				
			if (js.track instanceof Array) {
				golgotha.flightBoard.selectedTrack = new golgotha.maps.Line('trk-' + pilotID, {color:'#4080af', width:2, opacity:0.75}, js.track);
				map.addLine(golgotha.flightBoard.selectedTrack);
			}
				
			if (js.waypoints instanceof Array) {
				golgotha.flightBoard.waypoints = [];
				js.waypoints.forEach(function(wp) {
					const mrk = new golgotha.maps.IconMarker({map:map, pal:wp.pal, icon:wp.icon, info:wp.info, opacity:0.55, pt:wp.ll});
					golgotha.flightBoard.waypoints.push(mrk);
				});
			}		
		});
	});
};
