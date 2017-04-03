// Initialize common data
golgotha.maps.PIN_SIZE = new google.maps.Size(12, 20);
golgotha.maps.TILE_SIZE = new google.maps.Size(256, 256);
golgotha.maps.DEFAULT_SHADOW = new google.maps.MarkerImage('/' + golgotha.maps.IMG_PATH + '/maps/shadow.png', new google.maps.Size(22, 20), null, new google.maps.Point(6, 20));
golgotha.maps.S_ICON_SIZE = new google.maps.Size(24, 24);
golgotha.maps.S_ICON_SHADOW_SIZE = new google.maps.Size(24 * (59 / 32), 24);
golgotha.maps.ICON_ANCHOR = new google.maps.Point(12, 12);
golgotha.maps.DEFAULT_TYPES = [google.maps.MapTypeId.ROADMAP, google.maps.MapTypeId.SATELLITE, google.maps.MapTypeId.TERRAIN];
golgotha.maps.z = {INFOWINDOW:100, POLYLINE:25, POLYGON:35, MARKER:50, OVERLAY:10};
golgotha.maps.instances = [];
golgotha.maps.ovLayers = [];
golgotha.maps.styles = {};
golgotha.maps.reload = 60000;
golgotha.maps.masks = [0, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288];
golgotha.maps.zooms = [6100,2900,1600,780,390,195,90,50];
golgotha.maps.util = {isIE:golgotha.util.isIE, oldIE:golgotha.util.oldIE, isIOS:golgotha.util.isIOS};
golgotha.maps.util.isIE10 = (golgotha.maps.util.isIE && (navigator.appVersion.indexOf('IE 10.0') > 0));
golgotha.maps.util.isIE11 = ((navigator.appname == 'Netscape') && (navigator.userAgent.contains('Trident/')));
golgotha.maps.util.unload = function() { 
	for (m = golgotha.maps.instances.pop(); (m != null); m = golgotha.maps.instances.pop())
		google.maps.event.clearListeners(m);

	return true;
};

// Cross-browser opacity set
golgotha.maps.setOpacity = (golgotha.maps.util.isIE && (!golgotha.maps.util.isIE10) && (!golgotha.maps.util.isIE11)) ? function(e, tx) { e.style.filter = 'alpha(opacity=' + (tx*100) + ')'; } : function(e, tx) { e.style.opacity = tx; };

// Timer class
golgotha.maps.util.Timer = function(doStart) { this.runTime = -1; if (doStart) this.start(); };
golgotha.maps.util.Timer.prototype.start = function() {
	if (this.startTime != null) return false;
	this.startTime = new Date();
	return true;
};

golgotha.maps.util.Timer.prototype.stop = function() {
	if (this.startTime == null) return -1;	
	var now = new Date();
	this.runTime = (now.getTime() - this.startTime.getTime());
	this.startTime = null;
	return this.runTime;
};

// Resize map based on window size
golgotha.maps.util.resize = function() {
	var wh = window.innerHeight	|| document.documentElement.clientHeight || document.body.clientHeight;
	var ratio = wh / 800;
	var divs = golgotha.util.getElementsByClass('googleMapV3', 'div');
	for (var d = divs.pop(); (d != null); d = divs.pop()) {
		var h = d.getAttribute('h');
		if (h != null)
			d.style.height = Math.max(200, Math.floor(h * ratio)) + 'px';
	}

	golgotha.maps.instances.forEach(function(m) { google.maps.event.trigger(m, 'resize'); });
	return true;
};

golgotha.onDOMReady(golgotha.maps.util.resize);
if (golgotha.util.oldIE)
	document.attachEvent('onresize', golgotha.maps.util.resize); 
else
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

// Set best text color for map types
golgotha.maps.TEXT_COLOR = {roadmap:'#002010', satellite:'#efefef', terrain:'#002010', hybrid:'#efefef', acars_trackmap:'#dfefef'};
golgotha.maps.updateMapText = function () {
	var newColor = golgotha.maps.TEXT_COLOR[this.getMapTypeId()];
	var elements = golgotha.util.getElementsByClass('mapTextLabel');
	elements.forEach(function(e) { e.style.color = newColor; });
	return true;
};

golgotha.maps.updateZoom = function() {	return golgotha.util.setHTML('zoomLevel', 'Zoom Level ' + this.getZoom()); };
google.maps.Map.prototype.setCopyright = function(msg) { return golgotha.util.setHTML('copyright', msg); };
google.maps.Map.prototype.setStatus = function(msg) { return golgotha.util.setHTML('mapStatus', msg); };

golgotha.maps.displayedMarkers = [];
golgotha.maps.setMap = function(map) {
	if (map == null)
		golgotha.maps.displayedMarkers.remove(this);
	else
		golgotha.maps.displayedMarkers.push(this);

	this.setMap_OLD(map);
	return true;
};

// Track instances
golgotha.maps.Map = function(div, opts) { var m = new google.maps.Map(div, opts); golgotha.maps.instances.push(m); return m; };

// Track overlays
google.maps.Marker.prototype.setMap_OLD = google.maps.Marker.prototype.setMap;
google.maps.Polyline.prototype.setMap_OLD = google.maps.Polyline.prototype.setMap;
google.maps.Polygon.prototype.setMap_OLD = google.maps.Polygon.prototype.setMap;
google.maps.Circle.prototype.setMap_OLD = google.maps.Circle.prototype.setMap;
google.maps.Marker.prototype.setMap = golgotha.maps.setMap;
google.maps.Polyline.prototype.setMap = golgotha.maps.setMap;
google.maps.Polygon.prototype.setMap = golgotha.maps.setMap;
google.maps.Circle.prototype.setMap = golgotha.maps.setMap;
google.maps.Map.prototype.clearOverlays = function() {
	while (golgotha.maps.displayedMarkers.length > 0) {
		var mrk = golgotha.maps.displayedMarkers.shift();
		mrk.setMap(null);
	}
	
	return true;
};

// Adds a layer to the map
google.maps.Map.prototype.addLayer = function(l) {
	golgotha.maps.ovLayers.push(l);
	l.setMap(this);
	return true;
};

// Closes map infoWindow
google.maps.Map.prototype.closeWindow = function() {
	if (this.infoWindow) this.infoWindow.close();
	return true;
};

google.maps.Map.prototype.addMarkers = function(mrks) {
	if (!mrks) return false;
	mrks = (mrks instanceof Array) ? mrks : [mrks];
	for (var x = 0; x < mrks.length; x++) {
		var mrk = mrks[x];
		if (mrk.setMap) mrk.setMap(this);
	}

	return true;
};

google.maps.Map.prototype.removeMarkers = function(mrks) {
	if (!mrks) return false;
	mrks = (mrks instanceof Array) ? mrks : [mrks];
	for (var x = 0; x < mrks.length; x++) {
		var mrk = mrks[x];
		if ((mrk.getMap) && (mrk.setMap) && (mrk.getMap() == this)) mrk.setMap(null);
	}
	
	return true;
};

google.maps.Map.prototype.toggle = function(mrks, show) {
	if (!mrks) return false;
	this.closeWindow();
	mrks = (mrks instanceof Array) ? mrks : [mrks];
	for (var x = 0; x < mrks.length; x++)
		mrks[x].setMap(show ? this : null);
	
	return true;
}

// Disable zoom
google.maps.Map.prototype.disableZoom = function() { 
	map.setOptions({disableDoubleClickZoom:true, draggable:false, panControl:false, scaleControl:false});
	return true;
};

google.maps.Map.prototype.enableZoom = function() {
	map.setOptions({disableDoubleClickZoom:false, draggable:true, panControl:true, scaleControl:true});
	return true;
};

// Clears all map overlay layers
google.maps.Map.prototype.clearLayers = function() {
	if (map.animator) {
		map.animator.stop();
		map.animator.clear();
		try { delete map.animator; } catch (err) { map.animator = null; }
	}

	for (var ov = golgotha.maps.ovLayers.pop(); (ov != null); ov = golgotha.maps.ovLayers.pop())
		ov.setMap(null);
		
	this.overlayMapTypes.clear();
	return true;
};

// Clears all map selection controls
google.maps.Map.prototype.clearSelects = function(cl) {
	cl = (cl instanceof Array) ? cl : [cl];
	this.clearLayers();
	for (var x = 0; x < cl.length; x++) {
		var lsc = golgotha.util.getElementsByClass(cl[x], 'div', map.getDiv());
		lsc.forEach(function(dv) {
			if (!dv.isSelected) return false;
			google.maps.event.trigger(dv, 'click');
			google.maps.event.trigger(dv, 'stop');
		});
	}

	return true;
};

// Prototype to calculate visible tile addresses for map
google.maps.Map.prototype.getVisibleTiles = function(zoom, sz)
{
var bnds = this.getBounds();
var nw = new google.maps.LatLng(bnds.getNorthEast().lat(), bnds.getSouthWest().lng());
var se = new google.maps.LatLng(bnds.getSouthWest().lat(), bnds.getNorthEast().lng());

// Get the pixel points of the tiles
if (sz == null) sz = golgotha.maps.TILE_SIZE;
var p = map.getProjection(); if (!zoom) zoom = map.getZoom();
var nwp = p.fromLatLngToPoint(nw); nwp.x = Math.round(nwp.x << zoom); nwp.y = Math.round(nwp.y << zoom);
var sep = p.fromLatLngToPoint(se); sep.x = Math.round(sep.x << zoom); sep.y = Math.round(sep.y << zoom);
var nwAddr = new google.maps.Point((nwp.x / sz.width), (nwp.y / sz.height));
var seAddr = new google.maps.Point((sep.x / sz.width), (sep.y / sz.height));

// Load the tile addresses
var tiles = [];
for (var x = nwAddr.x; x <= seAddr.x; x++) {
	for (var y = nwAddr.y; y <= seAddr.y; y++)
		tiles.push(new google.maps.Point(x, y));
}

return tiles;
};

golgotha.maps.CreateButtonDiv = function(txt) {
	var btn = document.createElement('div');
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

golgotha.maps.SelectControl = function(title, onSelect, onClear, ctx) {
	var container = document.createElement('div'); 
	var btn = golgotha.maps.CreateButtonDiv(title); 
	container.appendChild(btn);
	if (ctx == null) ctx = window;
	google.maps.event.addDomListener(btn, 'click', function() {
		if (this.isSelected) {
			golgotha.util.removeClass(btn, 'displayed');
			try { delete btn.isSelected; } catch (err) { btn.isSelected = false; }
			if (onClear != null) onClear.call(ctx);
		} else {
			golgotha.util.addClass(btn, 'displayed');
			btn.isSelected = true;
			if (onSelect != null) onSelect.call(ctx);
		}
	});

	return container;
};

golgotha.maps.LayerSelectControl = function(opts, layers) {
	var container = document.createElement('div');
	var btn = golgotha.maps.CreateButtonDiv(opts.title);
	container.appendChild(btn);
	container.enable = function() { btn.disabled = false; golgotha.util.removeClass(btn, 'disabled'); };	
	if (opts.disabled) { btn.disabled = true; golgotha.util.addClass(btn, 'disabled'); }
	if (opts.id != null) container.setAttribute('id', opts.id);
	if (opts.c != null) golgotha.util.addClass(container, opts.c);
	btn.layerFunc = (golgotha.util.isFunction(layers)) ? layers : (function() { return layers; });
	google.maps.event.addDomListener(btn, 'click', function() {
		if (btn.disabled) return;
		if (this.isSelected) {
			golgotha.util.removeClass(btn, 'displayed');
			try { delete btn.isSelected; } catch (err) { btn.isSelected = false; }
		} else {
			golgotha.util.addClass(btn, 'displayed');
			btn.isSelected = true;
		}

		var ovL = this.layerFunc(); if (!(ovL instanceof Array)) ovL = [ovL];
		for (var x = 0; x < ovL.length; x++) {
			var ov = ovL[x];
			if ((ov.getMap != null) && (ov.getMap() != null) && (ov.getMap() != opts.map))
				return true;

			if (this.isSelected) {
				ov.setMap(opts.map); if (golgotha.util.isFunction(ov.display)) ov.display(true);
				if (ov.getCopyright) opts.map.setCopyright(ov.getCopyright());
				if (ov.getTextDate) opts.map.setStatus(ov.getTextDate());
			} else {
				ov.setMap(null);
				if (ov.getCopyright) opts.map.setCopyright('');
				if (ov.getTextDate) opts.map.setStatus('');
			}
		}
	});

	return container;
};

golgotha.maps.LayerClearControl = function(map, opts) {
	opts = opts || {};
	var container = document.createElement('div');
	var btn = golgotha.maps.CreateButtonDiv('None');
	btn.className = 'layerClear';
	container.appendChild(btn);
	if (opts.id != null) container.setAttribute('id', opts.id);
	if (opts.c != null) golgotha.util.addClass(container, opts.c);
	google.maps.event.addDomListener(btn, 'click', function() { map.clearSelects('layerSelect'); });
	return container;
};

// Create an arbitrary overlay layer
golgotha.maps.ShapeLayer = function(opts, name, imgClass) {
	if (opts.tileSize == null) opts.tileSize = golgotha.maps.TILE_SIZE;
	if (opts.host == null) opts.host = self.location.host;
	this.maxZoom = opts.maxZoom;
	this.nativeZoom = opts.nativeZoom;
	this.tileSize = opts.tileSize;
	this.baseURL = self.location.protocol + '//' + opts.host + '/tile/' + imgClass + '/';
	this.imgClass = imgClass;
	this.opacity = opts.opacity;
	this.makeURL = golgotha.maps.util.getTileUrl;
	this.getTile = golgotha.maps.util.buildTile;
};	

golgotha.maps.ShapeLayer.prototype.getMap = function() { return this.map; };
golgotha.maps.ShapeLayer.prototype.setMap = function(m) {
	if ((this.map != null) && (m != null)) {
		if (m == this.map) return false;
		setMap(null);
	}

	if (m != null) {
		golgotha.maps.ovLayers.push(this);
		m.overlayMapTypes.insertAt(0, this);
		this.map = m;
	} else if (this.map != null) {
		m = this.map;
		this.map = null;
		golgotha.maps.ovLayers.remove(this);
		for (var x = 0; x < m.overlayMapTypes.getLength(); x++) {
			var l = m.overlayMapTypes.getAt(x);
			if (l == this) {
				m.overlayMapTypes.removeAt(x);
				return true;
			}
		}
	}

	return true;
};

// Arbitrary marker layer
golgotha.maps.MarkerLayer = function(opts, name) {
	var l = {opts:opts, name:name, mrks:[], map:null};
	l.getMap = function() { return this.map; };
	l.setMap = function(m) { this.map = m; for (var x = 0; x < this.mrks.length; x++) this.mrks[x].setMap(m); };
	l.set = function(k, v) { this.opts[k] = v; };
	l.get = function(k) { return this.opts[k]; };
	l.add = function(m) { if (!this.mrks.contains(m)) this.mrks.push(m); };
	l.remove = function(m) { this.mrks.remove(m); };
	l.getTextDate = function() {
		var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
		var d = this.timestamp; if (d == null) return '';
		return d.getDate() + '-' + months[d.getMonth()] + '-' + d.getFullYear() + '  ' + d.getHours() + ':' + ((d.getMinutes() < 10) ? '0' + d.getMinutes() : d.getMinutes());	
	};
	
	return l;
};

golgotha.maps.Marker = function(opts, pt) {
	if ((opts == null) || (opts.color == 'null')) return pt;
	var hasLabel = (opts.label != null);
	if (hasLabel && (typeof(MarkerWithLabel) == 'undefined')) {
		console.log('MarkerWithLabel not loaded!');
		hasLabel = false;
	}
	
	var icn = new google.maps.MarkerImage('/' + golgotha.maps.IMG_PATH + '/maps/point_' + opts.color + '.png', null, null, null, golgotha.maps.PIN_SIZE);
	var mrkOpts = {position:pt, icon:icn, shadow:golgotha.maps.DEFAULT_SHADOW, zIndex:golgotha.maps.z.MARKER};
	mrkOpts.opacity = (opts.opacity) ? opts.opacity : 1.0;
	if (hasLabel) {
		mrkOpts.labelClass = 'mapMarkerLabel';
		if (opts.labelClass)
			mrkOpts.labelClass += (' ' + opts.labelClass);

		mrkOpts.labelContent = opts.label;
		mrkOpts.labelStyle = opts.labelStyle;
		mrkOpts.labelAnchor = new google.maps.Point((opts.label.length * 3), 0);
	}
	
	var mrk = hasLabel ? new MarkerWithLabel(mrkOpts) : new google.maps.Marker(mrkOpts);
	if (opts.info != null) {
		mrk.info = opts.info;	
		google.maps.event.addListener(mrk, 'click', function() { map.infoWindow.setContent(this.info); map.infoWindow.open(map, this); });	
	}

	if (opts.map != null) mrk.setMap(map);
	return mrk;
};

golgotha.maps.IconMarker = function(opts, pt) {
	if (opts == null) opts = {pal:0, icon:0};
	var imgBase = null;
	if (opts.pal > 0)
		imgBase = self.location.protocol + '//maps.google.com/mapfiles/kml/pal' + opts.pal + '/icon' + opts.icon;
	else
		imgBase = '/' + golgotha.maps.IMG_PATH + '/maps/pal' + opts.pal + '/icon' + opts.icon;

	var icn = new google.maps.MarkerImage(imgBase + '.png', null, null, golgotha.maps.ICON_ANCHOR, golgotha.maps.S_ICON_SIZE);
	var shd = new google.maps.MarkerImage(imgBase + 's.png', null, null, golgotha.maps.ICON_ANCHOR, golgotha.maps.S_ICON_SHADOW_SIZE);
	var mrkOpts = {position:pt, icon:icn, shadow:shd, zIndex:golgotha.maps.z.MARKER};
	mrkOpts.opacity = (opts.opacity) ? opts.opacity : 1.0;
	if (opts.label != null)
		mrkOpts.label = opts.label;

	var mrk = new google.maps.Marker(mrkOpts);
	if (opts.info != null) {
		mrk.info = opts.info;
		google.maps.event.addListener(mrk, 'click', function() { map.infoWindow.setContent(this.info); map.infoWindow.open(map, this); });
	}

	if (opts.map != null) mrk.setMap(map);
	return mrk;
};

// Async Loader results
golgotha.maps.LoaderResults = function() { return {opts:{timestamp:new Date()}, mrks:[], fn:{}, success:false}; };

// Generic async layer loader
golgotha.maps.LayerLoader = function(name, parser) { this.onLoad = []; this.name = name; this.parser = parser; this.isLoaded = false; };
golgotha.maps.LayerLoader.prototype.getLayer = function() { return this.ovLayer; };
golgotha.maps.LayerLoader.prototype.onload = function(f) { this.onLoad.push(f); return true; };
golgotha.maps.LayerLoader.prototype.isLoaded = function() { return (this.lastLoad != null); };
golgotha.maps.LayerLoader.prototype.getAge = function() { if (this.lastLoad == null) return -1; return ((new Date().getTime() - this.lastLoad.getTime()) / 1000); };
golgotha.maps.LayerLoader.prototype.load = function(data)
{
if (this.parser == null) {
	alert('No custom parser defined!');
	return;
}
	
// Get layer data and add markers
var l = new golgotha.maps.MarkerLayer({}, this.name); var ld = null;
if (data != null) {
	try {
		ld = this.parser(data);
	} catch (e) {
		console.log('Parse error - ' + e);
		ld = new golgotha.maps.LoaderResults();
	}

	if (!ld.success) return false;
	ld.mrks.forEach(function(mrk) { l.add(mrk); });

	// Add custom functions to the layer
	for (var fnName in ld.fn) {
		if (!ld.fn.hasOwnProperty(fnName)) continue;
		var fn = ld.fn[fnName];
		if (golgotha.util.isFunction(fn)) l[fnName] = fn;
	}
}

// If we're displaying the layer, refresh it
if ((this.ovLayer != null) && (this.ovLayer.getMap() != null)) {
	var ol = this.ovLayer;
	l.setMap(ol.getMap());
	window.setTimeout(function() { ol.setMap(null); }, 10);
}

// Fire event handlers
this.ovLayer = l; this.lastLoad = new Date();
for (var x = 0; x < this.onLoad.length; x++)
	this.onLoad[x].call(this);

return true;
};

// Generic async data loader
golgotha.maps.DataLoader = function(parser) { this.onLoad = []; this.parser = parser; this.results = {}; this.reqLoad = 1; this.loadCount = 0; };
golgotha.maps.DataLoader.prototype.getData = function() { return this.results; };
golgotha.maps.DataLoader.prototype.onload = function(f) { this.onLoad.push(f); return true; };
golgotha.maps.DataLoader.prototype.isLoaded = function() { return (this.lastLoad != null); };
golgotha.maps.DataLoader.prototype.getAge = function() { if (this.lastLoad == null) return -1; return ((new Date().getTime() - this.lastLoad.getTime()) / 1000); };
golgotha.maps.DataLoader.prototype.load = function(data)
{
if (this.parser == null) {
	alert('No custom parser defined!');
	return;
}
	
// Get data and fire event handlers
try {
	this.results = this.parser(data); this.lastLoad = new Date(); this.loadCount++;
	if (this.loadCount == this.reqLoad) {
		for (var x = 0; x < this.onLoad.length; x++)
			this.onLoad[x].call(this);
	}
} catch (e) {
	console.log('Parse error - ' + e);
}

return true;
};

golgotha.maps.util.updateTab = function(ofs, size)
{
if ((ofs < 0) || (ofs > this.tabs.length)) ofs = 0;
var tab = this.tabs[ofs];
var txt = '<div ';
if (!size) size = this.tabSize;
if (size) {
	txt += ' style="width:';
	txt += size.width;
	txt += 'px; height:'
	txt += size.height;
	txt += 'px;"';
	this.tabSize = size;
}

txt += '>';
txt += tab.content;
txt += '<br /><br />';
txt += golgotha.maps.util.renderTabChoices(this.tabs, ofs);
txt += '</div>';
this.getMap().infoWindow.setContent(txt);
return true;
};

golgotha.maps.util.renderTabChoices = function(tabs, selectedOfs)
{
var txt = '<span class="tabMenu">';
for (var x = 0; x < tabs.length; x++) {
	var tab = tabs[x];
	if (x != selectedOfs) {
		txt += '<a href="javascript:void map.infoWindow.marker.updateTab(' + x + ')">';
		txt += tab.name;
		txt += '</a> ';
	} else
		txt += '<span class="selectedTab">' + tab.name + '<span> '; 
}

txt += '</span>';
return txt;
};
