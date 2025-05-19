// Initialize common data
golgotha.maps.PIN_SIZE = {w:12,h:20};
golgotha.maps.TILE_SIZE = {w:256,h:256};
golgotha.maps.DEFAULT_SHADOW = {src:'/' + golgotha.maps.IMG_PATH + '/maps/shadow.png', size:{w:22, h:20}, pt:{x:6, y:20}};
golgotha.maps.S_ICON_SIZE = {w:24, h:24};
golgotha.maps.S_ICON_SHADOW_SIZE = {w:(24 * (59 / 32)), h:24};
golgotha.maps.ICON_ANCHOR = {x:12, y:12};

golgotha.maps.z = {INFOWINDOW:100, POLYLINE:25, POLYGON:35, MARKER:50, OVERLAY:10};
golgotha.maps.info = localStorage.getItem('golgotha.mapInfo');
golgotha.maps.info = (golgotha.maps.info) ? JSON.parse(golgotha.maps.info) : {type:'sat', zoom:5};
golgotha.maps.instances = [];
golgotha.maps.ovLayers = [];
golgotha.maps.styles = {};
golgotha.maps.reload = 60000;
golgotha.maps.setOpacity = function(e, tx) { e.style.opacity = tx; };
golgotha.maps.zooms = [6100,2900,1600,780,390,195,90,50];
golgotha.maps.util = golgotha.maps.util || {};

// Convert LatLng to LngLat (GoogleMaps -> MapBox)
golgotha.maps.toLL = function(ll) { return [ll.lng,ll.lat]; };
golgotha.maps.util.isShape = function(o) { return (o) && golgotha.util.isFunction(o.getLayer); };

// Timer class
golgotha.maps.util.Timer = function(doStart) { this.runTime = -1; if (doStart) this.start(); };
golgotha.maps.util.Timer.prototype.start = function() {
	if (this.startTime != null) return false;
	this.startTime = new Date();
	return true;
};

golgotha.maps.util.Timer.prototype.stop = function() {
	if (this.startTime == null) return -1;	
	const now = new Date();
	this.runTime = (now.getTime() - this.startTime.getTime());
	this.startTime = null;
	return this.runTime;
};

// Resize map based on window size
golgotha.maps.util.resize = function() {
	const wh = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
	const ratio = wh / 800;
	const divs = golgotha.util.getElementsByClass('mapBoxV3', 'div');
	for (var d = divs.pop(); (d != null); d = divs.pop()) {
		const h = d.getAttribute('h');
		if (h != null)
			d.style.height = Math.max(200, Math.floor(h * ratio)) + 'px';
	}

	golgotha.maps.instances.forEach(function(m) { m.resize(); });
	return true;
};

golgotha.onDOMReady(golgotha.maps.util.resize);
window.addEventListener('resize', golgotha.maps.util.resize);

// Calculate default zoom for flight distance
golgotha.maps.util.getDefaultZoom = function(distance) {
	for (var x = 0; x < golgotha.maps.zooms.length; x++)
		if (distance > golgotha.maps.zooms[x]) return (x+2);

	return 10;
};

// Calculate GMT offset in seconds from local
golgotha.maps.GMTOffset = new Date().getTimezoneOffset() * 60000;

// Distance conversion functions
golgotha.maps.miles2Meter = function(mi) { return mi * 1609.344 };
golgotha.maps.degreesToMiles = function(d) { return d * 69.172 };
golgotha.maps.feet2Meter = function (ft) { return ft / 3.2808399 };

// Set best text color for map types
golgotha.maps.TEXT_COLOR = {roadmap:'#002010', satellite:'#efefef', terrain:'#002010', hybrid:'#efefef', acars_trackmap:'#dfefef'};
golgotha.maps.updateMapText = function () {
	const newColor = golgotha.maps.TEXT_COLOR[this.getMapTypeId()];
	const elements = golgotha.util.getElementsByClass('mapTextLabel');
	elements.forEach(function(e) { e.style.color = newColor; });
	return true;
};

golgotha.maps.updateZoom = function() {	return golgotha.util.setHTML('zoomLevel', 'Zoom Level ' + this.getZoom()); };
mapboxgl.Map.prototype.setCopyright = function(msg) { return golgotha.util.setHTML('copyright', msg); };
mapboxgl.Map.prototype.setStatus = function(msg) { return golgotha.util.setHTML('mapStatus', msg); };

golgotha.maps.displayedMarkers = [];
golgotha.maps.displayedLayers = [];

// Track map and marker instances
golgotha.maps.Map = function(_div, opts) { const m = new mapboxgl.Map(opts); golgotha.maps.instances.push(m); return m; };
mapboxgl.Map.prototype.clearOverlays = function() {
	for (var mrk = golgotha.maps.displayedMarkers.pop(); (mrk != null); mrk = golgotha.maps.displayedMarkers.pop())
		mrk.remove();
	for (var mrk = golgotha.maps.displayedLayers.pop(); (mrk != null); mrk = golgotha.maps.displayedLayers.pop())
		this.removeLayer(mrk.name);

	return true;
};

mapboxgl.Map.prototype.addMarkers = function(mrks) {
	if (!mrks) return false; const mp = this;
	mrks = (mrks instanceof Array) ? mrks : [mrks];
	mrks.forEach(function(m) { m.setMap(mp); });
	return true;
};

mapboxgl.Map.prototype.removeMarkers = function(mrks) {
	if (!mrks) return false;
	mrks = (mrks instanceof Array) ? mrks : [mrks];
	for (var x = 0; x < mrks.length; x++) {
		const mrk = mrks[x];
		if (mrk.setMap)
			mrk.setMap(null);
		else if (golgotha.maps.util.isShape(mrk))
			this.removeLayer(mrk.name);
	}
	
	return true;
};

mapboxgl.Map.prototype.addLine = function(layer, data) {
	if (golgotha.maps.util.isShape(layer))
		layer = layer.getLayer();

	if (data) layer.source = data;
	this.addLayer(layer);
	golgotha.maps.displayedLayers[layer.id] = true;
	return true;
};

mapboxgl.Map.prototype.toggle = function(mrks, show) {
	if (!mrks) return false;
	mrks = (mrks instanceof Array) ? mrks : [mrks];
	for (var x = 0; x < mrks.length; x++) {
		const m = mrks[x];
		if (m.setMap)
			mrks[x].setMap(show ? this : null);
		else if (golgotha.maps.util.isShape(m))
			map.setLayoutProperty(m.name, 'visibility', show ? 'visible' : 'none', {validate:false});
		else if (m.hasOwnProperty('id'))
			map.setLayoutProperty(m.id, 'visibility', show ? 'visible' : 'none', {validate:false});
	}
	
	return true;
}

golgotha.maps.setMap = function(map) {
	if (map == null) {
		golgotha.maps.displayedMarkers.remove(this);
		this.remove();
	} else {
		golgotha.maps.displayedMarkers.push(this);
		this.addTo(map);
	}
	
	return true;
};

golgotha.maps.Marker = function(opts) {
	if ((opts == null) || (opts.color == 'null')) return pt;
	const hasLabel = (opts.label != null);
	const mrkOpts = {color:opts.color, shadow:golgotha.maps.DEFAULT_SHADOW, zIndex:golgotha.maps.z.MARKER};
	mrkOpts.opacity = (opts.opacity) ? opts.opacity : 1.0;
	mrkOpts.scale = (opts.scale) ? opts.scale : 0.625;
	if (hasLabel) {
		mrkOpts.labelClass = 'mapMarkerLabel';
		if (opts.labelClass)
			mrkOpts.labelClass += (' ' + opts.labelClass);

		mrkOpts.labelContent = opts.label;
		mrkOpts.labelStyle = opts.labelStyle;
	}

	const mrk = new mapboxgl.Marker(mrkOpts);
	mrk.setMap = golgotha.maps.setMap; mrk.getElement().marker = mrk;
	mrk.setLngLat(opts.pt);
	if (opts.info) {
		const p = new mapboxgl.Popup({closeOnClick:true,focusAfterOpen:false,maxWidth:'300px'});
		p.setHTML(opts.info);
		mrk.setPopup(p);
	}

	if (opts.map != null) mrk.setMap(opts.map);
	return mrk;
};


golgotha.maps.IconMarker = function(opts) {
	const imgBase = '/' + golgotha.maps.IMG_PATH + '/maps/kml/pal' + opts.pal + '/icon' + opts.icon;
	const dv = document.createElement('div');
	dv.className = 'marker';
	dv.style.backgroundImage = 'url(' + imgBase + '.png)';
	dv.style.width = golgotha.maps.S_ICON_SIZE.w + 'px';
	dv.style.height = golgotha.maps.S_ICON_SIZE.h + 'px';
	dv.style.backgroundSize = '100%';

	const mrk = new mapboxgl.Marker(dv);
	mrk.setMap = golgotha.maps.setMap; dv.marker = mrk;
	mrk.setLngLat(opts.pt);
	if (opts.info != null) {
		const p = new mapboxgl.Popup({closeOnClick:true,focusAfterOpen:false,maxWidth:'300px'});
		p.setHTML(opts.info);
		mrk.setPopup(p);
	}

	if (opts.map != null) mrk.setMap(opts.map);
	return mrk;
};

golgotha.maps.Line = function(name, opts, pts) {
	this.name = name;
	this._opts = opts;
	this._pts = pts.map(golgotha.maps.toLL);
};

golgotha.maps.Line.prototype.getType = function() { return 'Line'; };
golgotha.maps.Line.prototype.getLayer = function () {
	const src = {type:'geojson',data:{type:'Feature',properties:{},geometry:{type:'LineString',coordinates:this._pts}}};
	const v = (this._opts.visible != null) ? this._opts.visible : true;
	const po = {'line-color':this._opts.color,'line-opacity':this._opts.opacity,'line-width':this._opts.width};
	const lo = {'line-join':'round','line-cap':'round',visibility:(v ? 'visible' : 'none')};
	const o = {id:this.name,type:'line',source:src,paint:po,layout:lo};
	return o;
};

golgotha.maps.Polygon = function(name, opts, pts) {
	this.name = name;
	this._opts = opts;
	this._pts = pts.map(golgotha.maps.toLL);
};

golgotha.maps.Polygon.prototype.getType = function() { return 'Polygon'; };
golgotha.maps.Polygon.prototype.getLayer = function () {
	const src = {type:'geojson',data:{type:'Feature',properties:{},geometry:{type:'Polygon',coordinates:[this._pts]}}};
	if (this._opts.hasOwnProperty('info'))
		src.data.properties.info = this._opts.info;
	
	const v = (this._opts.visible != null) ? this._opts.visible : true;
	const po = {'fill-color':this._opts.fillColor,'fill-opacity':this._opts.fillOpacity,'fill-outline-color':this._opts.color};
	const o = {id:this.name,type:'fill',source:src,paint:po,layout:{visibility:(v ? 'visible' : 'none')}};
	return o;
};
