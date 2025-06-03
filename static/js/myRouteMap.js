golgotha.routeMap = golgotha.routeMap || {airports:[], routes:[], trks:[], busy:false, rtOpts:{}};
golgotha.routeMap.hideRoutes = function(show, icao) {
	for (var x = 0; x < golgotha.routeMap.routes.length; x++) {
		const r = this.routes[x];
		const opts = golgotha.routeMap.rtOpts[r.name];
		const myAP = r.name.includes(icao);
		const s =  myAP ? !show : show;
		map.setLayoutProperty(r.name, 'visibility', s ? 'visible' : 'none', {validate:false});
		if (s) {
			map.setPaintProperty(r.name, 'line-color', (s && myAP) ? '#e0e0ff' : opts.renderOpts.c, {validate:false});
			map.setPaintProperty(r.name, 'line-opacity', (s && myAP) ? 1 : opts.renderOpts.o, {validate:false});
		}
	}
};

golgotha.routeMap.hideAllRoutes = function() {
	golgotha.routeMap.routes.forEach(function(rt) {
		map.setLayoutProperty(rt.name, 'visibility', 'none', {validate:false});
	});
};

golgotha.routeMap.hideTracks = function() {
	for (var trk = this.trks.pop(); (trk != null); trk = this.trks.pop())
		map.removeLine(trk);
};

golgotha.routeMap.reset = function() {
	delete golgotha.routeMap.tracks;
	golgotha.routeMap.hideTracks();
	golgotha.routeMap.hideRoutes(true);
};

golgotha.routeMap.getColor = function(ratio) {
	const bC = {r:72, g:96, b:128}; var mxC = {r:192, g:232, b:255};
	ratio = (ratio - (ratio % 10)) / 100;
	return 'rgb(' + Math.round(bC.r + ((mxC.r - bC.r) * ratio)) + ',' +	Math.round(bC.g + ((mxC.g - bC.g) * ratio)) + ',' + Math.round(bC.b + ((mxC.b - bC.b) * ratio)) + ')';
};

golgotha.routeMap.showRoute = function(e) {
	const l = e.features[0].layer;
	const opts = golgotha.routeMap.rtOpts[l.name];
	if (!opts) return false;
	
	const p = new mapboxgl.Popup();
	p.setLngLat(opts.ctr).setHTML(opts.desc).addTo(map);
	if (!map.getBounds().contains(opts.ctr)) map.panTo(opts.ctr);
};

// Display airport info
golgotha.routeMap.showAirport = function(a, isDST) {
	golgotha.routeMap.hideAllRoutes();
	golgotha.routeMap.loadTracks(a.icao, isDST);
	a.togglePopup();
};

// Update date filter
golgotha.routeMap.updateDates = function(cb) {
	const days = golgotha.form.getCombo(cb);
	return golgotha.routeMap.load(days);
};

// Load airport/route data
golgotha.routeMap.load = function(days) {
	if (this.busy) return false;
	golgotha.util.setHTML('isLoading', ' - LOADING...');
	const p = fetch('myroutemap.ws?id=' + this.id + '&days=' + days, {signal:AbortSignal.timeout(5000)});
	p.then(function(rsp) {
		if (!rsp.ok) {
			golgotha.util.setHTML('isLoading', ' - ERROR ' + rsp.status);
			golgotha.routeMap.busy = false;
			return false;
		}

		golgotha.util.setHTML('isLoading', '');
		rsp.json().then(function(js) {
			map.removeMarkers(golgotha.routeMap.routes);	
			js.airports.forEach(function(a) {
				const mrk = new golgotha.maps.IconMarker({pal:2, icon:48, label:a.code, pt:a.ll, info:a.desc});
				mrk.icao = a.icao; mrk.desc = a.desc;
				mrk.setMap(map);
				mrk.getElement().addEventListener('contextmenu', function(e) { golgotha.routeMap.tracks = true; golgotha.routeMap.showAirport(this.marker, true); e.preventDefault(); });
				mrk.getPopup().on('open', function() { if (golgotha.routeMap.tracks) return; golgotha.routeMap.hideRoutes(false, this._marker.icao); });
				mrk.getPopup().on('close', golgotha.routeMap.reset);
				golgotha.routeMap.airports.push(mrk);
			});

			js.routes.forEach(function(r) {
				const w = 1 + (r.ratio / 66);
				const opts = {color:golgotha.routeMap.getColor(r.ratio), width:w, opacity:0.65};
				const rt = new golgotha.maps.Line('rt-' + r.src + '-' + r.dst, opts, r.points);
				golgotha.routeMap.routes.push(rt);
				golgotha.routeMap.rtOpts[rt.name] = {desc:r.desc, ctr:r.ll, src:r.src, dst:r.dst, renderOpts:{o:opts.opacity, c:opts.color}};
				map.addLine(rt);
				map.on('click', rt.name, golgotha.routeMap.showRoute);
			});

			golgotha.routeMap.busy = false;
			return true;
		});
	});

	return true;
};

// Load tracks to/from airport
golgotha.routeMap.loadTracks = function(icao, isDST) {
	if (this.busy) return false;
	golgotha.util.setHTML('isLoading', ' - LOADING FLIGHT TRACKS...');
	const p = fetch('mytracks.ws?id=' + this.id + '&icao=' + icao + '&dst=' + isDST, {signal:AbortSignal.timeout(6500)});
	p.then(function(rsp) {
		if (!rsp.ok) {
			golgotha.util.setHTML('isLoading', ' - ERROR ' + rsp.status);
			golgotha.routeMap.busy = false;
			return false;
		}

		rsp.json().then(function(js) {
			golgotha.util.setHTML('isLoading', '');
			js.routes.forEach(function(rt) {
				const c = rt.isDST ? '#80c0d8' : '#e0b080';
				const rl = new golgotha.maps.Line('trk-' + rt.id, {color:c, width:2, opacity:0.55}, rt.trk);
				golgotha.routeMap.trks.push(rl);
				map.addLine(rl);
			});

			golgotha.routeMap.busy = false;
			return true;
		});
	});

	return true;
};
