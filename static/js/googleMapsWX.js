// DIV creation function
if (window.progressBar) golgotha.maps.util.progress = progressBar({strokeWidth:512, strokeColor:'#0020ff'});
golgotha.maps.util.buildTile = function(pnt, zoom, doc)
{
var sz = this.get('tileSize');
var div = doc.createElement('div');
div.style.width = sz.width + 'px';
div.style.height = sz.height + 'px';
div.style.overflow = 'hidden';
div.style.position = 'relative';
var img = doc.createElement('img');
var nZ = this.get('nativeZoom');
if (zoom > nZ) {
	var numTiles = Math.pow(2, zoom - nZ);
	var imgSize = numTiles * sz.width; // assuming square
	var offsetX = pnt.x % numTiles;
	var offsetY = pnt.y % numTiles;
	img.src = this.getTileUrl(golgotha.maps.convertAddr(pnt, zoom, nZ), nZ);
	img.width = imgSize;
	img.height = imgSize;
	img.style.position = 'relative';
	if (offsetX > 0)
		img.style.marginLeft = (sz.width * -offsetX) + 'px';
	if (offsetY > 0)
		img.style.marginTop = (sz.height * -offsetY) + 'px';
} else {
	img.src = this.getTileUrl(pnt, zoom);
	img.width = sz.width;
	img.height = sz.height;
}

var c = 'wxTile ' + this.get('imgClass') + ' ' + this.get('imgClass') + '-' + this.get('date');
img.setAttribute('class', c);
if (golgotha.maps.util.oldIE) img.IE8 = c;
var tx = (this.tempZeroOpacity == true) ? 0 : this.getOpacity();
golgotha.maps.setOpacity(img, tx);
div.appendChild(img);
return div;	
};

// Tile selection function
golgotha.maps.util.getTileImgs = function(cName, eName, parent)
{
if (parent == null) parent = document;	
var elements = [];
var all = parent.getElementsByTagName((eName == null) ? '*' : eName);
for (var x = 0; x < all.length; x++) {
	var e = all[x];
	var cl = (e.IE8) ? e.IE8 : e.className;
	if (cl.split && (cl.split(' ').indexOf(cName) > -1))
		elements.push(e);
}

return elements;
};

// Tile URL generation functions
golgotha.maps.util.GinsuOverlayLayer = function(name, ts) { return 'http://' + golgotha.maps.tileHost + '/TileServer/imgs/' + name + '/u' + ts + '/'; };
golgotha.maps.util.S3OverlayLayer = function(name, ts, size) { return 'http://' + golgotha.maps.s3 + '/tile/' + name + '/' + size + '/' + ts + '/'; };

// Make cloud layer behave like our layers
if (google.maps.weather.CloudLayer) {
	google.maps.weather.CloudLayer.prototype.setMap_OLD = google.maps.weather.CloudLayer.prototype.setMap; 
	google.maps.weather.CloudLayer.prototype.setMap = function(map) {
		if ((map != null) && (golgotha.maps.ovLayers.indexOf(this) == -1))
			golgotha.maps.ovLayers.push(this);

		this.setMap_OLD(map);
		return true;
	}
}

// Create a ginsu weather overlay type
golgotha.maps.WeatherLayer = function(opts, name, timestamp) {
	opts.name = name;
	var tileURLFunc = (golgotha.maps.tileURL) ? golgotha.maps.tileURL : golgotha.maps.util.GinsuOverlayLayer;	
	var ov = new google.maps.ImageMapType(opts);
	ov.set('maxZoom', opts.maxZoom);
	ov.set('baseURL', tileURLFunc(name, timestamp));
	ov.set('date', timestamp);
	ov.set('timestamp', new Date(timestamp));
	ov.set('tileSize', opts.tileSize);
	ov.getTileUrl = golgotha.maps.util.getTileUrl;
	ov.getTile = golgotha.maps.util.buildTile;
	ov.getMap = function() { return this.map; }
	ov.setMap = function(m) {
		if ((this.map != null) && (m != null)) {
			if (m == this.map) return false;
			setMap(null);
		}

		if (m != null) {
			golgotha.maps.ovLayers.push(this);
			m.overlayMapTypes.insertAt(0, this);
			this.map = m;
		} else if (this.map != null) {
			try { delete this.tempZeroOpacity; } catch (err) { this.tempZeroOpacity = null; }
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
	}

	ov.getCopyright = function() {
		var d = this.get('timestamp');
		return 'Weather Data &copy; ' + d.getFullYear() + ' The Weather Channel.'
	}
	
	ov.getTextDate = function() {
		var d = this.get('timestamp');
		var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
		return d.getDate() + '-' + months[d.getMonth()] + '-' + d.getFullYear() + '  ' 
			+ d.getHours() + ':' + ((d.getMinutes() < 10) ? '0' + d.getMinutes() : d.getMinutes());	
	}

	ov.display = function(isVisible) {
		if (!isVisible && (this.map == null)) {
			this.tempZeroOpacity = true;
			return true;
		}
		
		var o = isVisible ? this.getOpacity() : 0;
		var imgs = golgotha.maps.util.getTileImgs(this.get('imgClass') + '-' + this.get('date'), 'img');
		for (var x = 0; x < imgs.length; x++)
			golgotha.maps.setOpacity(imgs[x], o);
		
		return true;
	}

	ov.hideAll = function() {
		var imgs = golgotha.maps.util.getTileImgs(this.get('imgClass'));
		for (var x = 0; x < imgs.length; x++)
			golgotha.maps.setOpacity(imgs[x], 0);

		return true;
	}
	
	ov.preload = function(map, handler, tileLoadHandler) {
		if (this.preloaded) return true;
		var vizTiles = map.getVisibleTiles(Math.min(map.getZoom(), this.get('nativeZoom')));
		var imgsToLoad = []; var ov = this;
		for (var x = 0; x < vizTiles.length; x++) {
			var src = ov.getTileUrl(vizTiles[x], map.getZoom());
			var img = new Image();
			img.loadCount = 1;
			img.onload = function(e) {
				imgsToLoad.remove(this.src);
				try { delete this.loadCount; } catch (err) { this.loadCount = null; }
				if (tileLoadHandler != null)
					tileLoadHandler.call();
				if (imgsToLoad.length == 0) {
					this.preloaded = true;
					if (handler != null)
						handler(ov);
				}

				return true;
			}

			img.onerror = function(e) {
				console.log('Error ' + this.loadCount + ' loading ' + this.src);
				if (this.loadCount > 2) {
					this.onload();
					return false;
				}
			
				var img = new Image();
				img.loadCount = (this.loadCount+1);
				img.onload = this.onload;
				img.onerror = this.onerror;
				img.src = this.src;
				return true;
			}

			imgsToLoad.push(src);
			img.src = src;
		}

		return true;
	}

	return ov;
}

golgotha.maps.util.getTileUrl = function(pnt, zoom) {
	if (zoom > this.get('maxZoom')) return '';
	var url = this.get('baseURL');
	var masks = [0, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288];

	// Get the tile numbers
	for (var x = zoom; x > 0; x--) {
		var digit1 = ((masks[x] & pnt.x) == 0) ? 0 : 1;
		var digit2 = ((masks[x] & pnt.y) == 0) ? 0 : 2;
		url = url + (digit1 + digit2);
	}

	// Check for multi-host
	var pos = url.indexOf('%');
	if (pos > -1) {
		var lastDigit = url.charAt(url.length - 1);
		url = url.substr(0, pos) + lastDigit + url.substr(pos + 1);
	}

	return url + '.png';
};

// Create a ginsu FF weather overlay type
golgotha.maps.FFWeatherLayer = function(opts, name, timestamp, effective) {
	var ov = golgotha.maps.WeatherLayer(opts, name, timestamp);
	ov.set('effDate', new Date(effective));
	ov.set('baseURL', 'http://' + golgotha.maps.tileHost + '/TileServer/ff/' + name + '/u' + effective + '/u' + timestamp + '/');
	return ov;
};

// Utility functions
golgotha.maps.convertAddr = function(pnt, srcZ, dstZ)
{
var deltaZ = Math.abs(srcZ - dstZ);
if (srcZ > dstZ)
	return {x:(pnt.x >> deltaZ), y:(pnt.y >> deltaZ)};

return {x:(pnt.x << deltaZ), y:(pnt.y << deltaZ)};
};

// Series list reloader function
golgotha.maps.loadSeries = function(url, id, interval)
{
var sc = document.createElement('script');
sc.setAttribute('id', id);
sc.type = 'text/javascript';
sc.src = url;

var oldSC = document.getElementById(id);
if (oldSC != null)
	document.body.replaceChild(odSC, sc)
else
	document.body.appendChild(sc);

//window.setInterval("golgotha.maps.loadSeries('" + url + "','" + id + "'," + interval + ")", interval);
return true;
};

// Ginsu series list parser
golgotha.maps.GinsuLoader = function(minZoom) { this.imgData = []; this.minZoom = Math.max(1, minZoom); this.layers = { names:[], ts:0, data:[] }; };
golgotha.maps.GinsuLoader.prototype.getNames = function() { return this.layers.names; }
golgotha.maps.GinsuLoader.prototype.getLatest = function(name) { return this.getLayers(name)[0]; }
golgotha.maps.GinsuLoader.prototype.getLayers = function(name, max)
{ 
var data = this.layers.data[name];
if (data == null) return null;
var mx = (max == null) ? data.length : max;
var d = data.clone();
if (mx < d.length) d.splice(mx, data.length);
return d;
};

golgotha.maps.GinsuLoader.prototype.setData = function(name, tx, imgClassName, sz)
{
if (!sz) sz = golgotha.maps.TILE_SIZE.width;
this.imgData[name] = {opacity:tx, imgClass:imgClassName, size:sz};
return true;
};

golgotha.maps.GinsuLoader.prototype.clear = function() { this.layers.names = []; this.layers.data = []; return true; };
golgotha.maps.GinsuLoader.prototype.combine = function(max, ln1, ln2)
{
var l1 = this.getLayers(ln1); var l2 = this.getLayers(ln2);
var results = [];	
for (var x = 0; ((x < l1.length) && (results.length < max)); x++) {
	var ov = l1[x];
	results.push(ov);
}

max = results.length * 2;
var maxDate = results[results.length-1].get('timestamp');
for (var x = 0; ((x < l2.length) && (results.length < max)); x++) {
	var ov = l2[x];
	var d = ov.get('timestamp');
	if (d.getTime() > maxDate)
		results.push(ov);
}

return results;
}

golgotha.maps.GinsuLoader.prototype.loadNew = function(seriesData) { return this.load('id', seriesData); }
golgotha.maps.GinsuLoader.prototype.load = function(id, sd)
{
this.clear();
var seriesData = sd.hasOwnProperty('seriesList') ? sd.seriesList : sd.seriesInfo;
for (layerName in seriesData) {
    if (!seriesData.hasOwnProperty(layerName)) continue;
    var layerData = seriesData[layerName];
    if (layerData.nativeZoom == null) continue;
    if (layerData.maxZoom == null) layerData.maxZoom = layerData.nativeZoom + 7;

	// Peek inside the series data - if the first entry is FF, iterate through its timestamps
	var isFF = (layerData.series[0].ff instanceof Array) && (layerData.series[0].ff.length > 0);
	var timestamps = isFF ? layerData.series[0].ff : layerData.series;
	var myLayerData = this.imgData[layerName];
	if (myLayerData == null) myLayerData = {opacity:0.5, imgClass:layerName, size:256};

	var slices = [];
	for (var tsX = 0; tsX < timestamps.length; tsX++) {
		var ts = timestamps[tsX];
		var tsz = new google.maps.Size(myLayerData.size, myLayerData.size);
		var layerOpts = {minZoom:this.minZoom, maxZoom:layerData.maxZoom, isPng:true, opacity:myLayerData.opacity, tileSize:tsz, zIndex:golgotha.maps.z.OVERLAY};
		var ovLayer = isFF ? new golgotha.maps.FFWeatherLayer(layerOpts, layerName, parseInt(ts.unixDate), parseInt(layerData.series[0].unixDate)) :  
				new golgotha.maps.WeatherLayer(layerOpts, layerName, parseInt(ts.unixDate));
		ovLayer.set('imgClass', myLayerData.imgClass);
		ovLayer.set('nativeZoom', layerData.nativeZoom);
		slices.push(ovLayer);
	}

	this.layers.names.push(layerName);
	this.layers.data[layerName] = slices;
}
	
return true;
};

golgotha.maps.LayerAnimateControl = function(map, title, layers, refresh) {
	var container = document.createElement('div');
	var btn = document.createElement('div');
	btn.className = 'layerAnimate layerSelect';
	container.layers = layers;
	if (title.length > 9)
		btn.style.width = '8em';
	else if (title.length > 7)
		btn.style.width = '7em';
	else
		btn.style.width = '6em';

	container.appendChild(btn);
	btn.appendChild(document.createTextNode(title));
	container.animator = new golgotha.maps.Animator(refresh);
	container.animator.animate(layers);
	var layersToLoad = [];

	container.complete = function(layer) {
		layersToLoad.remove(layer);
		if (layersToLoad.length > 0)
			return false;

		map.preload = false;
		container.isPreloaded = true;
		golgotha.maps.util.progress.hide();
		container.init();
		map.animator = container.animator;
		container.animator.start();
		return true;
	}

	container.IOSinit = function() {
		for (var x = 0; x < container.layers.length; x++) {
			var ov = container.layers[x];
			ov.display(false);
			if (x < 2)
				ov.setMap(map);
			else
				ov.display(false);
		}

		return true;
	}
	
	container.STDinit = function() {
		for (var x = 0; x < container.layers.length; x++) {
			var ov = container.layers[x];
			ov.display(false);
			ov.setMap(map);
		}

		return true;
	}

	container.init = function() { golgotha.maps.util.isIOS ? this.IOSinit() : this.STDinit(); }
	container.update = function() {
		var pb = golgotha.maps.util.progress;
		pb.setCurrent(pb.getCurrent() + 1);
		return true;
	}

	google.maps.event.addDomListener(btn, 'click', function() {
		if (map.animator) {
			var isPlaying = map.animator.isPlaying;
			map.animator.stop();
			if (map.animator != container.animator) {
				map.animator.clear();
				try { delete map.animator; } catch (err) { map.animator = null; }
			} else if (isPlaying == true)
				return true;
		}
		
		if (map.preLoad) return false;
		if (!this.isSelected)
			document.addClass(this, 'displayed');

		if (!container.isPreloaded) {
			map.preload = true; var nZ = Math.min(map.getZoom(), container.layers[0].get('nativeZoom'));
			golgotha.maps.util.progress.start(map.getVisibleTiles(nZ).length * container.layers.length);
			for (var x = 0; x < container.layers.length; x++) {
				var ov = container.layers[x];
				layersToLoad.push(ov);
				ov.preload(map, container.complete, container.update);
			}
		} else {
			container.init();
			map.animator = container.animator;
			container.animator.reset();
			container.animator.start();
		}
		
		this.isSelected = true;
		return true;
	});

	google.maps.event.addDomListener(btn, 'stop', function() {
		if ((!this.isSelected) || map.preload) return false;
		map.setStatus('');
		document.removeClass(this, 'displayed');
		try { delete this.isSelected; } catch (err) { this.isSelected = null; }
		if (map.animator) {
			map.animator.clear();
			try { delete map.animator; } catch (err) { map.animator = null; }
		}
		
		return true;
	});
	
	return container;
};

// Map animator object
golgotha.maps.Animator = function(interval) {
	this.layers = []; this.ofs = 0;
	this.isPlaying = false;
	this.eventHandlers = [];
	this.interval = isNaN(interval) ? 250 : Math.max(250, interval);
};

golgotha.maps.Animator.prototype.reset = function() { this.ofs = 0; }
golgotha.maps.Animator.prototype.animate = function(slices) {
	this.clear();
	for (var x = slices.length; x > 0; x--)
		this.layers.push(slices[x-1]);

	return true;
};

golgotha.maps.Animator.prototype.clear = function() {
	if (this.isPlaying) this.stop();
	for (var l = this.eventHandlers.pop(); (l != null); l = this.eventHandlers.pop())
		google.maps.event.removeListener(l);

	for (var x = 0; x < this.layers.length; x++) {
		var ov = this.layers[x];
		ov.display(false);
		ov.setMap(null);
	}

	return true;
};

golgotha.maps.Animator.prototype.previousFrame = function() {
	return (this.ofs == 0) ? (this.layers.length-1) : (this.ofs-1);
};

golgotha.maps.Animator.prototype.nextFrame = function() {
	var result = this.ofs+1;
	return (result == this.layers.length) ? 0 : result;
};

golgotha.maps.Animator.prototype.doFrame = function() {
	if (!this.isPlaying) return false;
	var pov = this.layers[this.ofs];
	pov.display(false);
	this.ofs = this.nextFrame();
	var ov = this.layers[this.ofs];
	ov.getMap().setStatus(ov.getTextDate());
	ov.display(true);
	var nov = this.layers[this.nextFrame()];
	nov.display(false);
	if (golgotha.maps.util.isIOS) { nov.setMap(ov.getMap()); pov.setMap(null); }
	var a = this;
	window.setTimeout(function() { a.doFrame() }, (this.ofs == (this.layers.length-1)) ? 1250 : this.interval);
	return true;
};

golgotha.maps.Animator.prototype.start = function() {
	console.log('Animator started');
	this.isPlaying = true;
	var ov = this.layers[this.ofs];
	ov.display(true);

	// Register a zoom listener to pause while panning zoom_changed, idle
	var m = ov.getMap();
	if (this.eventHandlers.length == 0) {
		this.eventHandlers.push(google.maps.event.addListener(map, 'zoom_changed', this.pauseHandler));
		this.eventHandlers.push(google.maps.event.addListener(map, 'dragstart', this.pauseHandler));
		this.eventHandlers.push(google.maps.event.addListener(map, 'idle', this.idleHandler));
	}

	// Start animation
	var a = this;
	setTimeout(function() { a.doFrame() }, this.interval);
	return true;
};

golgotha.maps.Animator.prototype.stop = function() {
	if (!this.isPlaying) return false;
	console.log('Animator stopped');
	this.isPlaying = false;
	return true;
};

golgotha.maps.Animator.prototype.pauseHandler = function() {
	var a = this.animator;
	if (a.isPlaying) {
		console.log('Pausing');
		a.stop();
	}

	return true;
};

golgotha.maps.Animator.prototype.idleHandler = function() {
	var a = this.animator;
	if (!a.isPlaying) {
		console.log('Resuming');
		a.start();
	}

	return true;
};

// Front options
golgotha.maps.frontOpts = {
        "WARM":{strokeOpacity:1, strokeColor:"#ff0000", icons:[{icon:{path:"M 0,-2 A 2,2 0 0 1 0,2 z",fillColor:"red",fillOpacity:1,strokeWeight:1,scale:3},offset:"12px",repeat:"24px"}], zIndex:3, geodesic:true},
        "COLD":{strokeOpacity:1, strokeColor:"#0000ff", icons:[{icon:{path:"M 0,-2 0,2 -2,0 z",fillColor:"blue",fillOpacity:1,strokeWeight:1,scale:3},offset:"12px",repeat:"24px"}], zIndex:3, geodesic:true},
        "OCFNT":{strokeOpacity:1, strokeColor:"#912CEE", icons:[{icon:{path:"M 0,-2 0,2 -2,0 z",fillColor:"#912CEE",fillOpacity:1,strokeWeight:1,scale:3},offset:"12px",repeat:"36px"},{icon:{path:"M 0,-2 A 2,2 0 0 1 0,2 z",fillColor:"#912CEE",fillOpacity:1,strokeWeight:1, scale:3},offset:"24px", repeat:"36px"}], zIndex:3, geodesic:true},
        "STNRY":{strokeOpacity:0, icons:[{icon:{path:"M 0,-4 0,4",strokeWeight:3,strokeOpacity:1,strokeColor:"blue"},offset:"0px",repeat:"48px"},{icon:{path:"M 0,-4 0,4",strokeWeight:3,strokeOpacity:1,strokeColor:"red"},offset:"24px",repeat:"48px"},{icon:{path:"M 0,-2 0,2 -2,0 z",fillColor:"blue",fillOpacity:1,scale:3},offset:"0",repeat:"48px"},{icon:{path:"M 0,-2 A 2,2 50 0 1 0,2",fillColor:"red",fillOpacity:1,scale:3},offset:"24px",repeat:"48px"}], zIndex:3, geodesic:true},
        "TROF":{strokeOpacity:0, icons:[{icon:{path:"M 0,-1 0,1",strokeOpacity:1,strokeWeight:2,scale:5,strokeColor:"#999999"},offset:"0",repeat:"20px"}],zIndex:3, geodesic:true}
};

golgotha.maps.FrontsLayer = function(opts, name) {
	var fl = {opts:opts, name:name, lines:[], map:null};
	fl.getMap = function() { return this.map; };
	fl.setMap = function(m) {
		for (var x = 0; x < this.lines.length; x++) {
			var l = this.lines[x];
			l.setMap(m);
		}
	};
	
	fl.set = function(k, v) { this.opts[k] = v; };
	fl.get = function(k) { return this.opts[k]; };
	fl.addFront = function(l) { if (!this.lines.contains(l)) this.lines.push(l); };
	fl.removeFront = function(l) { this.lines.remove(l); };
	fl.getCopyright = function() {
		var d = this.get('timestamp');
		return 'Weather Data &copy; ' + d.getFullYear() + ' Weather Underground.'
	};
	
	fl.getTextDate = function() {
		var d = this.get('timestamp');
		var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
		return d.getDate() + '-' + months[d.getMonth()] + '-' + d.getFullYear() + '  ' 
			+ d.getHours() + ':' + ((d.getMinutes() < 10) ? '0' + d.getMinutes() : d.getMinutes());	
	};
	
	return fl;
};

golgotha.maps.FrontLoader = function() { this.ovLayer = null; };
golgotha.maps.FrontLoader.prototype.getLayer = function() { return this.ovLayer; };
golgotha.maps.FrontLoader.prototype.load = function(seriesData) {
	var data = seriesData.FRONTS;
	var d = data.UTCDATE;
	var ts = new Date(d.year, d.mon-1, d.day, d.hour, 0, 0);
	var fl = new golgotha.maps.FrontsLayer({timestamp:new Date()}, 'Fronts');
	
	// Parse fronts
	var fd = data.FRONTS;
	for (var x = 0; x < fd.length; x++) {
		var f = fd[x]; var l = new google.maps.Polyline(golgotha.maps.frontOpts[f.type]);
		var pts = [];
		for (var y = 0; y < f.points.length; y++) {
			var pt = f.points[y];
			pts.push(new google.maps.LatLng(pt.lat, pt.lon));
		}

		l.setPath(pts);
		fl.addFront(l);
	}
	
	// Parse Highs
	var hd = data.HIGHS;
	var hm = new google.maps.MarkerImage('/' + golgotha.maps.IMG_PATH + '/wx/H.png', new google.maps.Size(30, 30), null, new google.maps.Point(15, 15));
	for (var x = 0; x < hd.length; x++) {
		var h = hd[x];
		var ll = new google.maps.LatLng(h.lat, h.lon);
		var mrk = new google.maps.Marker({icon:hm, position:ll, title:'High Pressure (' + h.pressuremb + 'mb)'});
		fl.addFront(mrk);
	}
	
	// Parse Lows
	var ld = data.LOWS;
	var lm = new google.maps.MarkerImage('/' + golgotha.maps.IMG_PATH + '/wx/L.png', new google.maps.Size(30, 30), null, new google.maps.Point(15, 15));
	for (var x = 0; x < ld.length; x++) {
		var l = ld[x];
		var ll = new google.maps.LatLng(l.lat, l.lon);
		var mrk = new google.maps.Marker({icon:lm, position:ll, title:'Low Pressure (' + l.pressuremb + 'mb)'});
		fl.addFront(mrk);
	}
	
	this.ovLayer = fl;
	return true;
};
