golgotha.routeMap = golgotha.routeMap || {airports:[], routes:[], trks:[], busy:false};
golgotha.routeMap.dimAirports = function(icao) {
	for (var x = 0; x < this.airports.length; x++) {
		const a = this.airports[x];
		if (a.icao != icao)
			a.setOpacity((icao == null) ? 1.0 : 0.25);
	}
};

golgotha.routeMap.hideRoutes = function(show, icao) {
	for (var x = 0; x < this.routes.length; x++) {
		const r = this.routes[x];
		const myAP = ((r.src == icao) || (r.dst == icao));
		const s =  myAP ? !show : show;
		r.setVisible(s);
		r.setOptions({strokeOpacity:((s && myAP) ? 1 : r.renderOpts.o), strokeColor:((s && myAP) ? '#e0e0ff' : r.renderOpts.c)});
	}
};

golgotha.routeMap.hideTracks = function() {
	for (var trk = this.trks.pop(); (trk != null); trk = this.trks.pop())
		trk.setMap(null);
};

golgotha.routeMap.reset = function() {
	this.hideTracks();
	this.dimAirports(null);
	this.hideRoutes(true);
};

golgotha.routeMap.getColor = function(ratio) {
	const bC = {r:72, g:96, b:128}; var mxC = {r:192, g:232, b:255};
	ratio = (ratio - (ratio % 10)) / 100;
	return 'rgb(' + Math.round(bC.r + ((mxC.r - bC.r) * ratio)) + ',' +	Math.round(bC.g + ((mxC.g - bC.g) * ratio)) + ',' + Math.round(bC.b + ((mxC.b - bC.b) * ratio)) + ')';
};

golgotha.routeMap.showRoute = function() {
	map.infoWindow.setContent(this.desc);
	map.infoWindow.setPosition(this.ctr);
	map.infoWindow.open(map);
	if (!map.getBounds().contains(this.ctr)) map.panTo(this.ctr);
	return true;
};

// Display airport info
golgotha.routeMap.showAirport = function(a, isDST) {
	map.infoWindow.setContent(a.desc);
	map.infoWindow.setPosition(a.getPosition());
	map.infoWindow.open(map);
	golgotha.routeMap.hideRoutes(false);
	golgotha.routeMap.dimAirports(a.icao);
	golgotha.routeMap.loadTracks(a.icao, isDST);
	if (!map.getBounds().contains(a.getPosition())) map.panTo(a.getPosition());
	return true;
};

// Update date filter
golgotha.routeMap.updateDates = function(cb) {
	const days = golgotha.form.getCombo(cb);
	return golgotha.routeMap.load(days);
};

// Load airport/route data
golgotha.routeMap.load = function(days) {
	if (this.busy) return false;
	const xmlreq = new XMLHttpRequest();
	golgotha.util.setHTML('isLoading', ' - LOADING...');
	xmlreq.open('get', 'myroutemap.ws?id=' + this.id + '&days=' + days, true);
	xmlreq.onreadystatechange = function() {
		if (xmlreq.readyState != 4) return false;
		if (xmlreq.status != 200) {
			golgotha.util.setHTML('isLoading', ' - ERROR ' + xmlreq.status);
			golgotha.routeMap.busy = false;
			return false;
		}

		const js = JSON.parse(xmlreq.responseText);
		golgotha.util.setHTML('isLoading', '');
		map.removeMarkers(golgotha.routeMap.routes);
		js.airports.forEach(function(a) {
			const mrk = new golgotha.maps.Marker({color:'blue', map:map, label:a.code, zIndex:golgotha.maps.z.POLYLINE+10}, a.ll);
			mrk.icao = a.icao; mrk.desc = a.desc;
			google.maps.event.addListener(mrk, 'rightclick', function() { golgotha.routeMap.showAirport(this, true); });
			google.maps.event.addListener(mrk, 'click', function() { golgotha.routeMap.dimAirports(this.icao); golgotha.routeMap.hideRoutes(false, this.icao); });
			golgotha.routeMap.airports.push(mrk);
		});

		js.routes.forEach(function(r) {
			const z = golgotha.maps.z.POLYLINE + Math.round(r.ratio / 20);
			const w = 1 + (r.ratio / 66);
			const opts = {path:r.points, strokeColor:golgotha.routeMap.getColor(r.ratio), map:map, strokeWeight:w, strokeOpacity:0.65, clickable:true, geodesic:true, zIndex:z};
			const rt = new google.maps.Polyline(opts);
			rt.desc = r.desc; rt.ctr = r.ll; rt.src = r.src; rt.dst = r.dst;
			rt.renderOpts = {o:opts.strokeOpacity, c:opts.strokeColor};
			golgotha.routeMap.routes.push(rt);
			google.maps.event.addListener(rt, 'click', golgotha.routeMap.showRoute);
		});

		golgotha.routeMap.busy = false;
		return true;
	};

	xmlreq.send(null);
	return true;
};

// Load tracks to/from airport
golgotha.routeMap.loadTracks = function(icao, isDST) {
	if (this.busy) return false;
	const xmlreq = new XMLHttpRequest();
	golgotha.util.setHTML('isLoading', ' - LOADING FLIGHT TRACKS...');
	xmlreq.open('get', 'mytracks.ws?id=' + this.id + '&icao=' + icao + '&dst=' + isDST, true);	
	xmlreq.onreadystatechange = function() {
		if (xmlreq.readyState != 4) return false;
		if (xmlreq.status != 200) {
			golgotha.util.setHTML('isLoading', ' - ERROR ' + xmlreq.status);
			golgotha.routeMap.busy = false;
			return false;
		}

		const js = JSON.parse(xmlreq.responseText);
		golgotha.util.setHTML('isLoading', '');
		js.routes.forEach(function(rt) {
			const c = rt.isDST ? '#80c0d8' : '#e0b080';
			const rl = new google.maps.Polyline({path:rt.trk, strokeColor:c, map:map, strokeWeight:1, strokeOpacity:0.55, clickable:false, geodesic:true, zIndex:golgotha.maps.z.POLYLINE});
			golgotha.routeMap.trks.push(rl);
		});

		golgotha.routeMap.busy = false;
		return true;
	};
	
	xmlreq.send(null);
	return true;
};