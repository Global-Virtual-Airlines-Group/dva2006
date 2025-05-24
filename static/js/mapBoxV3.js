// Initialize common data
golgotha.maps.PIN_SIZE = {w:12,h:20};
golgotha.maps.TILE_SIZE = {w:256,h:256};
golgotha.maps.DEFAULT_SHADOW = {src:'/' + golgotha.maps.IMG_PATH + '/maps/shadow.png', size:{w:22, h:20}, pt:{x:6, y:20}};
golgotha.maps.S_ICON_SIZE = {w:24, h:24};
golgotha.maps.S_ICON_SHADOW_SIZE = {w:(24 * (59 / 32)), h:24};
golgotha.maps.ICON_ANCHOR = {x:12, y:12};
golgotha.maps.DEFAULT_TYPES = [{l:'Satellite',style:'satellite-v9'}, {l:'Dark',style:'dark-v11'}, {l:'Terrain',style:'outdoors-v12'}];
golgotha.maps.z = {INFOWINDOW:100, POLYLINE:25, POLYGON:35, MARKER:50, OVERLAY:10};
golgotha.maps.info = localStorage.getItem('golgotha.mapInfo');
golgotha.maps.info = (golgotha.maps.info) ? JSON.parse(golgotha.maps.info) : {type:'sat', zoom:5};
golgotha.maps.instances = [];
golgotha.maps.ovLayers = [];
golgotha.maps.styles = {};
golgotha.maps.reload = 60000;
golgotha.maps.setOpacity = function(e, tx) { e.style.opacity = tx; };
golgotha.maps.zooms = [6100,2900,1600,780,390,195,90,50];

// Convert LatLng to LngLat (GoogleMaps -> MapBox)
golgotha.maps.toLL = function(ll) { return (ll instanceof Array) ? ll : [ll.lng,ll.lat]; };
golgotha.maps.util.isShape = function(o) { return (o) && golgotha.util.isFunction(o.getLayer); };

golgotha.maps.util.unload = function() {
	if (map) map.remove(); 
	return true;
};

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

golgotha.maps.CreateButtonDiv = function(txt) {
	const btn = document.createElement('div');
	btn.className = 'layerSelect';
	if (txt.length > 9)
		btn.style.width = '8em';
	else if (txt.length > 7)
		btn.style.width = '7em';
	else
		btn.style.width = '6em';

	btn.appendChild(document.createTextNode(txt));
	return btn;
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
golgotha.maps.TEXT_COLOR = {'satellite-v9':'#efefef','outdoors-v12':'#002010','dark-v11':'#efefef'};
golgotha.maps.updateMapText = function () {
	const id = this.getMapType();
	const newColor = golgotha.maps.TEXT_COLOR[id];
	const elements = golgotha.util.getElementsByClass('mapTextLabel');
	elements.forEach(function(e) { e.style.color = newColor; });
	golgotha.maps.displayedLayers.forEach(function(l) { map.addLayer(golgotha.maps.util.isShape(l) ? l.getLayer() : l); });
	return true;
};

golgotha.maps.updateZoom = function() {	return golgotha.util.setHTML('zoomLevel', 'Zoom Level ' + Math.round(this.getZoom() * 1000) / 1000); };
mapboxgl.Map.prototype.setCopyright = function(msg) { return golgotha.util.setHTML('copyright', msg); };
mapboxgl.Map.prototype.setStatus = function(msg) { return golgotha.util.setHTML('mapStatus', msg); };

golgotha.maps.displayedMarkers = [];
golgotha.maps.displayedLayers = [];

// Track map and marker instances
golgotha.maps.Map = function(div, opts) {
	opts.container = opts.container|| div; 
	const m = new mapboxgl.Map(opts); 
	golgotha.maps.instances.push(m); 
	return m;
};

mapboxgl.Map.prototype.getMapType = function() {
	const s = this.getStyle().sprite;
	return s.substring(s.lastIndexOf('/') + 1);
};

mapboxgl.Map.prototype.clearOverlays = function() {
	for (var mrk = golgotha.maps.displayedMarkers.pop(); (mrk != null); mrk = golgotha.maps.displayedMarkers.pop())
		mrk.remove();
	for (var l = golgotha.maps.displayedLayers.pop(); (l != null); l = golgotha.maps.displayedLayers.pop()) {
		this.removeLayer(l.name);
		this.removeSource(l.name);
	}

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
			this.removeLine(mrk);
	}
	
	return true;
};

mapboxgl.Map.prototype.addLine = function(l, data) {
	if (!l.name) l.name = l.id;
	if (!l.visible == null) l.visible = true;
	if (data)
		l.source = data;
	else if ((typeof l.source == 'string') && (this.getSource(l.source) == null))
		l.source = eval(l.source);

	this.addLayer(golgotha.maps.util.isShape(l) ? l.getLayer() : l);
	golgotha.maps.displayedLayers.push(l);
	return true;
};

mapboxgl.Map.prototype.removeLine = function(l) {
	const layer = golgotha.maps.util.isShape(l) ? l.getLayer() : l;
	this.removeLayer(layer.id);
	this.removeSource(layer.id);
	const dl = golgotha.maps.displayedLayers.find(function(ll) { return ll.getLayer().id == layer.id });
	if (dl)
		golgotha.maps.displayedLayers.remove(dl);
};

mapboxgl.Map.prototype.toggle = function(mrks, show) {
	if (!mrks) return false;
	mrks = (mrks instanceof Array) ? mrks : [mrks];
	for (var x = 0; x < mrks.length; x++) {
		const m = mrks[x];
		if (m.setMap)
			mrks[x].setMap(show ? this : null);
		else if (golgotha.maps.util.isShape(m)) {
			m.visible = show;
			map.setLayoutProperty(m.name, 'visibility', show ? 'visible' : 'none', {validate:false});
		}
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

golgotha.maps.createMarkerLabel = function(mrk, txt) {
	const dv = document.createElement('div');
	dv.className = 'mapMarkerLabel mapTextLabel';
	dv.innerHTML = txt;
	dv.style.position = 'absolute';
	dv.style.top = '20px';
	dv.style.left = '-6px'
	mrk.getElement().appendChild(dv);
	return true;
}

golgotha.maps.Marker = function(opts) {
	if ((opts == null) || (opts.color == 'null')) return pt;
	const hasLabel = (opts.label != null);
	const mrkOpts = {color:opts.color};
	mrkOpts.opacity = (opts.opacity) ? opts.opacity : 1.0;
	mrkOpts.scale = (opts.scale) ? opts.scale : 0.625;

	// Create the marker
	let mrk = new mapboxgl.Marker(mrkOpts);
	mrk.setMap = golgotha.maps.setMap; mrk.getElement().marker = mrk;
	mrk.setLngLat(opts.pt);
	if (opts.info) {
		const p = new mapboxgl.Popup({closeOnClick:true,focusAfterOpen:false,maxWidth:'300px'});
		p.setHTML(opts.info);
		mrk.setPopup(p);
	}

	if (hasLabel) golgotha.maps.createMarkerLabel(mrk, opts.label);
	if (opts.map != null) mrk.setMap(opts.map);
	return mrk;
};


golgotha.maps.IconMarker = function(opts) {
	const hasLabel = (opts.label != null);
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

	if (hasLabel) golgotha.maps.createMarkerLabel(mrk, opts.label);
	if (opts.map != null) mrk.setMap(opts.map);
	return mrk;
};

golgotha.maps.Line = function(name, opts, pts) {
	this.name = name;
	this._opts = opts;
	this._pts = pts.map(golgotha.maps.toLL);
	this.visible = (opts.visible != null) ? opts.visible : true;
};

golgotha.maps.Line.prototype.getType = function() { return 'Line'; };
golgotha.maps.Line.prototype.getLayer = function () {
	const src = {type:'geojson',data:{type:'Feature',properties:{},geometry:{type:'LineString',coordinates:this._pts}}};
	const po = {'line-color':this._opts.color,'line-opacity':this._opts.opacity,'line-width':this._opts.width};
	const lo = {'line-join':'round','line-cap':'round',visibility:(this.visible ? 'visible' : 'none')};
	const o = {id:this.name,type:'line',source:src,paint:po,layout:lo};
	return o;
};

golgotha.maps.Polygon = function(name, opts, pts) {
	this.name = name;
	this._opts = opts;
	this._pts = pts.map(golgotha.maps.toLL);
	this.visible = (opts.visible != null) ? opts.visible : true;
};

golgotha.maps.Polygon.prototype.getType = function() { return 'Polygon'; };
golgotha.maps.Polygon.prototype.getLayer = function () {
	const src = {type:'geojson',data:{type:'Feature',properties:{},geometry:{type:'Polygon',coordinates:[this._pts]}}};
	if (this._opts.hasOwnProperty('info'))
		src.data.properties.info = this._opts.info;
	
	const po = {'fill-color':this._opts.fillColor,'fill-opacity':this._opts.fillOpacity,'fill-outline-color':this._opts.color};
	const o = {id:this.name,type:'fill',source:src,paint:po,layout:{visibility:(this.visible ? 'visible' : 'none')}};
	return o;
};

golgotha.maps.util.generateCircle = function(map, ctr, radius) {
	if (radius <= 0) return [];
	const centerPt = map.project(ctr);
	const radiusPt = map.project([ctr.lng, ctr.lat + (radius / 69.16)]);

	// Build the circle
	const pts = [];
	const r = Math.floor(Math.sqrt(Math.pow((centerPt.x-radiusPt.x),2) + Math.pow((centerPt.y-radiusPt.y),2))); 
	for (var a = 0; a < 361; a += 6) {
    	const aRad = (Math.PI / 180) * a;
    	const py = centerPt.y + r * Math.sin(aRad);
    	const px = centerPt.x + r * Math.cos(aRad);
		const ll = map.unproject({x:px,y:py});
		pts.push([ll.lng, ll.lat]);
	} 

	return pts;
};

golgotha.maps.BaseMapControl = function(labels) { this._labels = labels; };
golgotha.maps.BaseMapControl.prototype.onAdd = function(map) {
	this._map = map;
	const cs = map.getMapType();
	const div = document.createElement('div'); div.id = 'baseCtl';
	div.className = 'mapBoxBaseControl mapboxgl-ctrl mapboxgl-ctrl-group';
	for (var x = 0; x < this._labels.length; x++) {
		const e = this._labels[x];
		const dv = golgotha.maps.CreateButtonDiv(e.l);
		dv.className = 'mapBoxLayerSelect layerSelect baseSelect';	
		dv.mapStyle = e.style; dv.map = map;
		if (cs == e.style) golgotha.util.addClass(dv, 'displayed');
		dv.addEventListener('click', function(e) {
			const isSelected = (this.map.getMapType() == this.mapStyle);
			if (isSelected) return;
			this.map.setStyle('mapbox://styles/mapbox/' + this.mapStyle);
			const btns = golgotha.util.getElementsByClass('baseSelect', 'div')
			btns.forEach(function(b) {golgotha.util.removeClass(b, 'displayed'); });
			golgotha.util.addClass(this, 'displayed');
		});

		div.appendChild(dv);
	}
	
	this._container = div;
	return div;
};

golgotha.maps.BaseMapControl.prototype.onRemove = function() {
	this._container.parentNode.removeChild(this._container);
	delete this._map;	
};

golgotha.maps.DIVControl = function(id) { this._div = document.getElementById(id); }
golgotha.maps.DIVControl.prototype.onAdd = function(map) { this._map = map; return this._div; };
golgotha.maps.DIVControl.prototype.onRemove = function() { this._div.parentNode.removeChild(this._div); delete this._map; };
