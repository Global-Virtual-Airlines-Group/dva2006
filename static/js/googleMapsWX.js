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
img.onerror = golgotha.maps.util.blankImg;
var c = 'wxTile ' + this.get('imgClass') + ' ' + this.get('imgClass') + '-' + this.get('date');
var imgURL = '';
var nZ = this.get('nativeZoom');
if (zoom > nZ) {
	var numTiles = Math.pow(2, zoom - nZ);
	var imgSize = numTiles * sz.width; // assuming square
	var offsetX = pnt.x % numTiles;
	var offsetY = pnt.y % numTiles;
	var addr = new golgotha.maps.TileAddress(pnt.x, pnt.y, zoom);
	imgURL = this.getTileUrl(addr.convert(nZ), nZ);
	if (imgURL.length == 0) return div;
	img.width = imgSize;
	img.height = imgSize;
	img.style.position = 'relative';
	c += ' subNative';
	if (offsetX > 0)
		img.style.marginLeft = (sz.width * -offsetX) + 'px';
	if (offsetY > 0)
		img.style.marginTop = (sz.height * -offsetY) + 'px';
} else {
	imgURL = this.getTileUrl(pnt, zoom);
	if (imgURL.length == 0) return div;
	img.width = sz.width;
	img.height = sz.height;
}

img.src = imgURL;
img.setAttribute('class', c);
if (golgotha.maps.util.oldIE) img.IE8 = c;
var tx = (this.tempZeroOpacity == true) ? 0 : this.getOpacity();
golgotha.maps.setOpacity(img, tx);
div.appendChild(img);
return div;	
};

golgotha.maps.util.blankImg = function(e)
{
var img = e.target;
img.onerror = null;
img.src = golgotha.maps.BLANK_IMG;
return true;
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
golgotha.maps.util.S3OverlayLayer = function(name, ts, size) { return 'http://' + golgotha.maps.tileHost + '/tile/' + name + '/' + size.height + '/' + ts + '/'; };

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

// Create a weather overlay type
golgotha.maps.WeatherLayer = function(opts, timestamp) {
	if (opts.range == null) opts.range = [];
	var tileURLFunc = (opts.tileURL) ? opts.tileURL : golgotha.maps.util.S3OverlayLayer;
	var ov = new google.maps.ImageMapType(opts);
	ov.set('maxZoom', opts.maxZoom);
	ov.set('baseURL', tileURLFunc(opts.name, timestamp, opts.tileSize));
	ov.set('date', timestamp);
	ov.set('range', opts.range);
	ov.set('timestamp', new Date(timestamp));
	ov.set('tileSize', opts.tileSize);
	ov.getTileUrl = golgotha.maps.util.getTileUrl;
	ov.getTile = golgotha.maps.util.buildTile;
	ov.isPreloaded = function(l) { if (this.pl == null) { this.pl = []; return false; } else return (this.pl.indexOf(l) > -1); };
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
		return 'Weather Data &copy; ' + d.getFullYear() + ' The Weather Company.'
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
		} else if (this.tempZeroOpacity) {
			try { delete this.tempZeroOpacity; } catch (e) { this.tempZeroOpacity = undefined; }
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
		var lvl = Math.min(map.getZoom(), this.get('nativeZoom'));
		if (this.isPreloaded(lvl)) return true;
		var vizTiles = map.getVisibleTiles(lvl, this.get('tileSize'));
		var imgsToLoad = []; var ov = this;
		for (var x = 0; x < vizTiles.length; x++) {
			var src = ov.getTileUrl(vizTiles[x], map.getZoom());
			var img = new Image();
			img.loadCount = 1;
			img.onload = function(e) {
				imgsToLoad.remove(this.src);
				try { delete this.loadCount; } catch (err) { this.loadCount = undefined; }
				if (tileLoadHandler != null) tileLoadHandler.call();
				if (imgsToLoad.length == 0) {
					ov.pl.push(lvl);
					if (handler != null) handler(ov);
				}

				return true;
			};

			img.onerror = function(e) {
				console.log('Error ' + this.loadCount + ' loading ' + this.src);
				if (this.loadCount > 1) {
					this.onload();
					return false;
				}
			
				var img = new Image();
				img.loadCount = (this.loadCount+1);
				img.onload = this.onload;
				img.onerror = this.onerror;
				img.src = this.src;
				return true;
			};

			imgsToLoad.push(src);
			img.src = src;
		}

		return true;
	}

	return ov;
}

golgotha.maps.util.getTileUrl = function(pnt, zoom) {
	if (zoom > this.get('maxZoom')) return '';
	var addr = new golgotha.maps.TileAddress(pnt.x, pnt.y, zoom);

	// Check if we are in the bounding box
	var bb = this.get('range');
	if ((bb != null) && (bb.length > 0) && (zoom > 2)) {
		var notOK = true;
		for (var x = 0; notOK && (x < bb.length); x++) {
			var bt = bb[x];
			var pt = addr.convert(bt.z);
			notOK &= ((bt.x != pt.x) || (bt.y != pt.y));
		}
		
		if (notOK) return '';
	}

	// Get the tile numbers
	var tileID = '';
	for (var x = zoom; x > 0; x--) {
		var digit1 = ((golgotha.maps.masks[x] & pnt.x) == 0) ? 0 : 1;
		var digit2 = ((golgotha.maps.masks[x] & pnt.y) == 0) ? 0 : 2;
		tileID = tileID + (digit1 + digit2);
	}

	// Check for multi-host
	var url = this.get('baseURL'); var pos = url.indexOf('%');
	if (pos > -1) {
		var lastDigit = url.charAt(tileID.length - 1);
		url = url.substr(0, pos) + lastDigit + url.substr(pos + 1);
	}

	return url + tileID + '.png';
};

// Tile Address class
golgotha.maps.TileAddress = function(x, y, z) { this.z = z; this.x = this.normalize(x, z); this.y = this.normalize(y, z); };
golgotha.maps.TileAddress.prototype.normalize = function(c, z) { var MAX = Math.pow(2, z); if (c < 0) return (c + MAX); return (c >= MAX) ? (c - MAX) : c; };
golgotha.maps.TileAddress.prototype.toString = function() { return this.x + '.' + this.y + '.' + this.z; };
golgotha.maps.TileAddress.prototype.convert = function(newZ) {
	var dZ = Math.abs(this.z - newZ);
	return (this.z > newZ) ? new golgotha.maps.TileAddress((this.x >> dZ), (this.y >> dZ), newZ) : new golgotha.maps.TileAddress((this.x << dZ), (this.y << dZ), newZ);
};

golgotha.maps.TileAddress.parse = function(addr)
{
var level = addr.length; var x = 0; var y = 0;
for (var z = 0; z < level; z++) {
	switch (addr.charAt(z)) {
		case '1':
			x += golgotha.maps.masks[level - z];
			break;

		case '2':
			y += golgotha.maps.masks[level - z];
			break;

		case '3':
			x += golgotha.maps.masks[level - z];
			y += golgotha.maps.masks[level - z];
			break;
	}
}

return new golgotha.maps.TileAddress(x, y, level);
};

// Create a FF weather overlay type
golgotha.maps.FFWeatherLayer = function(opts, timestamp, effective) {
	var ov = golgotha.maps.WeatherLayer(opts, effective);
	var base = ov.get('baseURL');
	ov.set('effDate', new Date(effective));
	ov.set('baseURL', base + timestamp + '/');
	return ov;
};

//Reboot series list parser
golgotha.maps.SeriesLoader = function() { this.imgData = []; this.layers = { names:[], ts:0, data:[] }; this.onLoad = []; };
golgotha.maps.SeriesLoader.prototype.getNames = function() { return this.layers.names; };
golgotha.maps.SeriesLoader.prototype.getLatest = function(name) { return this.getLayers(name, 1)[0]; };
golgotha.maps.SeriesLoader.prototype.getLayers = function(name, max)
{ 
var data = this.layers.data[name];
if (data == null) return null;
var mx = (max == null) ? data.length : max;
var d = data.clone();
if (mx < d.length) d.splice(mx, data.length);
return d;
};

golgotha.maps.SeriesLoader.prototype.setData = function(name, tx, imgClassName, imgSize)
{
var d = {opacity:tx, imgClass:imgClassName};
d.size = (imgSize) ? imgSize : golgotha.maps.TILE_SIZE.width;
this.imgData[name] = d;
return true;
};

golgotha.maps.SeriesLoader.prototype.onload = function(f) { this.onLoad.push(f); return true; };
golgotha.maps.SeriesLoader.prototype.clear = function() { this.layers.names = []; this.layers.data = []; return true; };
golgotha.maps.SeriesLoader.prototype.combine = function(max, ln1, ln2)
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
};

// Returns currently displayed layers created by this loader
golgotha.maps.SeriesLoader.prototype.getDisplayed = function(m)
{
var results = {};
for (var x = 0; x < this.layers.names.length; x++) {
	var ln = this.layers.names[x]; var ldata = this.layers.data[ln]; var mapLayers = [];
	for (var y = 0; y < ldata.length; y++) {
		var ov = ldata[y];
		var mm = ov.getMap();
		if ((mm != null) && ((m == null) || (mm == m)))
			mapLayers.push(ov);
	}

	if (mapLayers.length > 0) results[ln] = {layers:mapLayers};
}

return results;
};

golgotha.maps.SeriesLoader.prototype.load = function(sd)
{
var displayedLayers = this.getDisplayed(); this.clear();
for (var ldoc = sd.body.pop(); (ldoc != null); ldoc = sd.body.pop()) {
	if (ldoc.status != 200) continue;
	var layerData = ldoc.doc; var layerName = layerData.layer;
	if (layerData.nativeZoom == null) continue;
	if (layerData.maxZoom == null) layerData.maxZoom = layerData.nativeZoom + 7;
	if (layerData.sizes == null) layerData.sizes = [256];
	var dl = displayedLayers[layerName] || {layers:[], data:[]};

	// Peek inside the series data - if the first entry is FF, iterate through its timestamps
	var isFF = (layerData.series[0].ff instanceof Array) && (layerData.series[0].ff.length > 0);
	var timestamps = isFF ? layerData.series[0].ff : layerData.series;
	var myLayerData = this.imgData[layerName];
	if (myLayerData == null)
		myLayerData = {opacity:0.5, imgClass:layerName, size:golgotha.maps.TILE_SIZE.width};

	var slices = []; var bbZoomLevel = 3;
	for (var tsX = 0; tsX < timestamps.length; tsX++) {
		var ts = timestamps[tsX];
		
		// Determine if our preferred tile size is available
		if (!layerData.sizes.contains(myLayerData.size))
			myLayerData.size = golgotha.maps.TILE_SIZE.width;
		
		// Calculate bounding box
		var bb = []; var keys = [];
		var seriesBB = isFF ? layerData.series[0].boundingBox : ts.boundingBox;
		for (var bbX = 0; bbX < seriesBB.length; bbX++) {
			var bbAddr = golgotha.maps.TileAddress.parse(seriesBB[bbX]);
			if (myLayerData.size != golgotha.maps.TILE_SIZE.width) {
				var newZ = bbZoomLevel - (Math.log(myLayerData.size) / Math.LN2 - Math.log(golgotha.maps.TILE_SIZE.width) / Math.LN2);
				bbAddr = bbAddr.convert(newZ);
				bbAddr.z = bbZoomLevel;
			}
			
			// Check if we already have it
			var k = bbAddr.toString();
			if (keys[k] == null) {
				keys[k] = bbAddr;
				bb.push(bbAddr);	
			}
		}

		var tsz = new google.maps.Size(myLayerData.size, myLayerData.size);
		var layerOpts = {minZoom:2, name:layerName, maxZoom:layerData.maxZoom, isPng:true, opacity:myLayerData.opacity, tileSize:tsz, range:bb, zIndex:golgotha.maps.z.OVERLAY};
		var ovLayer = isFF ? new golgotha.maps.FFWeatherLayer(layerOpts, ts.unixDate, layerData.series[0].unixDate) :  
				new golgotha.maps.WeatherLayer(layerOpts, ts.unixDate);
		ovLayer.set('imgClass', myLayerData.imgClass);
		ovLayer.set('nativeZoom', layerData.nativeZoom);
		ovLayer.set('tileSize', tsz);
		ovLayer.latest = (tsX == 0);
		
		// Determine whether we display it - it's either the latest or an arbitrary timeslice
		for (var x = 0; x < dl.layers.length; x++) {
			var dspL = dl.layers[x]; if (dspL == null) continue; 
			var dt = dspL.get('date');
			if (ovLayer.latest == true) { // If we're processing the latest
				if (dt == ts.unixDate) {
					ovLayer = dspL;
					dl.layers[x] = null;
					console.log('Keeping latest ' + layerName + ' ' + dspL.get('timestamp'));
				} else if (dspL.latest) {
					console.log('Replacing latest ' + layerName + ', replacing ' + dspL.get('timestamp') + ' with ' + ovLayer.get('timestamp'));
					dspL.force = true;
					ovLayer.setMap(dspL.getMap());
				}
			} else if (dt == ts.unixDate) {
				if ((!dspL.latest) && (!dspL.force)) {
					ovLayer = dspL;
					dl.layers[x] = null;
					console.log('Keeping ' + layerName + ' ' + dspL.get('timestamp'));
				}
			}
		}
		
		slices.push(ovLayer);
	}
	
	for (var x = 0; x < dl.layers.length; x++) {
		var ov = dl.layers[x];
		if (ov != null)
			ov.setMap(null);
	}

	this.layers.names.push(layerName);
	this.layers.data[layerName] = slices;
}

// Fire event handlers
for (var l = this.onLoad.pop(); (l != null); l = this.onLoad.pop())
	l.call(this);
	
return true;
};

golgotha.maps.LayerAnimateControl = function(opts, layers) {
	var container = document.createElement('div');
	var btn = golgotha.maps.CreateButtonDiv(opts.title);
	btn.className = 'layerAnimate layerSelect';
	container.appendChild(btn);
	container.enable = function() { btn.disabled = false; document.removeClass(btn, 'disabled'); };	
	if (opts.disabled) { btn.disabled = true; document.addClass(btn, 'disabled'); }
	if (opts.id != null) container.setAttribute('id', opts.id);
	if (opts.c != null) document.addClass(container, opts.c);
	container.layers = layers;
	container.animator = new golgotha.maps.Animator(opts.refresh);
	container.timer = new golgotha.maps.util.Timer();
	var layersToLoad = [];

	container.complete = function(layer) {
		layersToLoad.remove(layer);
		if (layersToLoad.length > 0) return false;
		try { delete opts.map.preload; } catch (e) { opts.map.preload = undefined; }
		golgotha.maps.util.progress.hide();
		var tt = container.timer.stop();
		if (tt > 0) console.log('Preloaded tiles in ' + tt + 'ms');
		container.init();
		opts.map.animator = container.animator;
		container.animator.reset();
		container.animator.start();
		return true;
	};

	container.IOSinit = function() {
		var ll = container.layers();
		for (var x = 0; x < ll.length; x++) {
			var ov = ll[x];
			ov.display(false);
			if (x < 2)
				ov.setMap(opts.map);
			else
				ov.display(false);
		}

		return true;
	};
	
	container.STDinit = function() {
		var ll = container.layers();
		for (var x = 0; x < ll.length; x++) {
			var ov = ll[x];
			ov.display(false);
			ov.setMap(opts.map);
		}

		return true;
	};

	container.init = function() { golgotha.maps.util.isIOS ? this.IOSinit() : this.STDinit(); };
	container.update = function() { var pb = golgotha.maps.util.progress; pb.setCurrent(pb.getCurrent() + 1); return true; };
	google.maps.event.addDomListener(btn, 'click', function() {
		if (map.animator) {
			var isPlaying = opts.map.animator.isPlaying;
			opts.map.animator.stop();
			if (opts.map.animator != container.animator) {
				opts.map.animator.clear();
				try { delete opts.map.animator; } catch (err) { opts.map.animator = undefined; }
			} else if (isPlaying == true)
				return true;
		}
		
		if (opts.map.preLoad) return false;
		if (!this.isSelected)
			document.addClass(this, 'displayed');

		var ll = container.layers(); var lz = ll[0]; container.animator.animate(ll);
		var nZ = Math.min(opts.map.getZoom(), lz.get('nativeZoom'));
		var tilesPerLayer = opts.map.getVisibleTiles(nZ, lz.get('tileSize')).length;
		for (var x = 0; x < ll.length; x++) {
			var ov = ll[x];
			var doLoad = !ov.isPreloaded(nZ);
			if (doLoad) layersToLoad.push(ov);
		}
	
		this.isSelected = true;
		if (layersToLoad.length > 0) {
			opts.map.preload = true; console.log('Preloading ' + layersToLoad.length + ' layers');
			golgotha.maps.util.progress.start(tilesPerLayer * layersToLoad.length);
			container.timer.start();
			for (var x = 0; x < layersToLoad.length; x++)
				layersToLoad[x].preload(opts.map, container.complete, container.update);
		} else
			container.complete();
	
		return true;
	});

	google.maps.event.addDomListener(btn, 'stop', function() {
		if ((!this.isSelected) || opts.map.preload) return false;
		opts.map.setStatus('');
		document.removeClass(this, 'displayed');
		try { delete this.isSelected; } catch (err) { this.isSelected = undefined; }
		if (opts.map.animator) {
			opts.map.animator.clear();
			try { delete opts.map.animator; } catch (err) { opts.map.animator = undefined; }
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

golgotha.maps.Animator.prototype.reset = function() { this.ofs = 0; };
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

golgotha.maps.Animator.prototype.previousFrame = function() { return (this.ofs == 0) ? (this.layers.length-1) : (this.ofs-1); };
golgotha.maps.Animator.prototype.nextFrame = function() { var result = this.ofs+1; return (result == this.layers.length) ? 0 : result; };
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
	window.setTimeout(function() { a.doFrame(); }, (this.ofs >= (this.layers.length-1)) ? 1250 : this.interval);
	return true;
};

golgotha.maps.Animator.prototype.start = function() {
	console.log('Animator started');
	this.isPlaying = true;
	var ov = this.layers[this.ofs];
	ov.display(true);

	// Register a zoom listener to pause while panning zoom_changed, idle
	map.disableZoom();
	if (this.eventHandlers.length == 0) {
		this.eventHandlers.push(google.maps.event.addListener(map, 'zoom_changed', this.pauseHandler));
		this.eventHandlers.push(google.maps.event.addListener(map, 'dragstart', this.pauseHandler));
		this.eventHandlers.push(google.maps.event.addListener(map, 'idle', this.idleHandler));
	}

	// Start animation
	var a = this;
	setTimeout(function() { a.doFrame(); }, this.interval);
	return true;
};

golgotha.maps.Animator.prototype.stop = function() {
	if (!this.isPlaying) return false;
	var ov = this.layers[this.ofs];
	ov.getMap().enableZoom();
	console.log('Animator stopped');

	// Clear registered event handlers
	for (var eH = this.eventHandlers.pop(); (eH != null); eH = this.eventHandlers.pop())
		google.maps.event.removeListener(eH);
	
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
