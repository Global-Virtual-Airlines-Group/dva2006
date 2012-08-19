// DIV creation function
golgotha.maps.util.progress = progressBar({strokeWidth:512, strokeColor:'#0020ff'});
golgotha.maps.util.buildTile = function(pnt, zoom, doc)
{
var div = doc.createElement('div');
div.style.width = '256px';
div.style.height = '256px';
var img = doc.createElement('img');
img.src = this.getTileUrl(pnt, zoom);
img.setAttribute('class', 'wxTile ' + this.get('imgClass') + ' ' + this.get('imgClass') + '-' + this.get('date'));
var tx = this.tempZeroOpacity ? 0 : this.getOpacity();
if (golgotha.maps.util.oldIE)
	img.style.filter = 'alpha(opacity=' + (tx*100) + ')';
else
	img.style.opacity = tx;

div.appendChild(img);
return div;	
}

// Create a ginsu weather overlay type
golgotha.maps.WeatherLayer = function(opts, name, timestamp) {
	opts.name = name;
	var ov = new google.maps.ImageMapType(opts);
	ov.set('maxZoom', opts.maxZoom);
	ov.set('baseURL', 'http://' + golgotha.maps.tileHost + '/TileServer/imgs/' + name + '/u' + timestamp + '/');
	ov.set('date', timestamp);
	ov.set('timestamp', new Date(timestamp));
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
			delete this.tempZeroOpacity;
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
		
		var imgs = getElementsByClass(this.get('imgClass') + '-' + this.get('date'), 'img');
		for (var x = 0; x < imgs.length; x++) {
			var img = imgs[x];
			img.style.opacity = isVisible ? this.getOpacity() : 0;
		}
		
		return true;
	}

	ov.hideAll = function() {
		var imgs = getElementsByClass(this.get('imgClass'));
		for (var x = 0; x < imgs.length; x++) {
			var img = imgs[x];
			img.style.opacity = 0;
		}

		return true;
	}
	
	ov.preload = function(map, handler, tileLoadHandler) {
		if (this.preloaded) return true;
		var vizTiles = map.getVisibleTiles();
		var imgsToLoad = []; var ov = this;
		for (var x = 0; x < vizTiles.length; x++) {
			var src = ov.getTileUrl(vizTiles[x], map.getZoom());
			var img = new Image();
			img.onload = function() {
				imgsToLoad.remove(this.src);
				if (tileLoadHandler != null)
					tileLoadHandler.call();
				if (imgsToLoad.length == 0) {
					this.preloaded = true;
					if (handler != null)
						handler(ov);
				}

				return true;
			}

			img.onerror = img.onload;
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
	if (golgotha.maps.multiHost) {
		var lastDigit = url.charAt(url.length - 1);
		var pos = url.indexOf('%');
		if (pos > -1)
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
}

// Create a GVA/Wunderground weather overlay type
golgotha.maps.WUWeatherLayer = function(opts, name, timestamp) {
	var ov = golgotha.maps.WeatherLayer(opts, name, timestamp);
	ov.set('baseURL', 'http://' + self.location.host + '/tile/' + name + '/u' + timestamp + '/');
	ov.getCopyright = function() {
		var d = this.get('timestamp');
		return 'Weather Data &copy; ' + d.getFullYear() + ' Weather Underground.'
	}
	
	return ov;
}

// Utility functions
golgotha.maps.convertAddr = function(pnt, srcZ, dstZ)
{
var deltaZ = Math.abs(srcZ - dstZ);
if (srcZ > dstZ)
	return {x:(pnt.x >> deltaZ), y:(pnt.y >> deltaZ)};

return {x:(pnt.x << deltaZ), y:(pnt.y << deltaZ)};
}

// Ginsu series list parser
golgotha.maps.GinsuLoader = function(minZoom)
{
	this.imgData = []; this.minZoom = Math.max(1, minZoom);
	this.layers = { names:[], ts:0, data:[] };
}

golgotha.maps.GinsuLoader.prototype.getNames = function() { return this.layers.names; }
golgotha.maps.GinsuLoader.prototype.getLayers = function(name) { return this.layers.data[name]; }
golgotha.maps.GinsuLoader.prototype.getTime = function() { return this.layers.ts; } 
golgotha.maps.GinsuLoader.prototype.setData = function(name, tx, imgClassName)
{
this.imgData[name] = {opacity:tx, imgClass:imgClassName};
return true;
}

golgotha.maps.GinsuLoader.prototype.clear = function()
{
this.layers.names = [];	
this.layers.data = [];
return true;
}

golgotha.maps.GinsuLoader.prototype.load = function(id, seriesData)
{
this.clear();
this.layers.ts = seriesData.timestamp;
for (var x = 0; x < seriesData.seriesNames.length; x++) {
	var layerName = seriesData.seriesNames[x];
	var layerData = eval('seriesData.seriesInfo.' + layerName);
	if (layerData == undefined)
		continue;
	else if (layerData.series.length == 0)
		continue;

	// Peek inside the series data - if the first entry is FF, iterate through its timestamps
	var isFF = (layerData.series[0].ff instanceof Array) && (layerData.series[0].ff.length > 0);
	var timestamps = isFF ? layerData.series[0].ff : layerData.series;
	var myLayerData = this.imgData[layerName];
	if (myLayerData == null)
		myLayerData = {opacity:0.5, imgClass:layerName};

	var slices = [];
	for (var tsX = 0; tsX < timestamps.length; tsX++) {
		var ts = timestamps[tsX];
		var layerOpts = {minZoom:this.minZoom, maxZoom:layerData.maxZoom, isPng:true, opacity:myLayerData.opacity, tileSize:golgotha.maps.TILE_SIZE, zIndex:golgotha.maps.z.OVERLAY};
		var ovLayer = isFF ? new golgotha.maps.FFWeatherLayer(layerOpts, layerName, parseInt(ts.unixDate), parseInt(layerData.series[0].unixDate)) :  
				new golgotha.maps.WeatherLayer(layerOpts, layerName, parseInt(ts.unixDate));
		ovLayer.set('imgClass', myLayerData.imgClass);
		ovLayer.set('nativeZoom', layerData.nativeZoom);
		slices.push(ovLayer);
	}

	this.layers.names.push(layerName);
	slices.reverse();
	this.layers.data[layerName] = slices;
}
	
return true;
}

golgotha.maps.LayerAnimateControl = function(map, title, layers, refresh) {
	var container = document.createElement('div');
	container.setAttribute('class', 'layerAnimate');
	var btn = document.createElement('div');
	container.layers = layers;
	golgotha.maps.setButtonStyle(btn);
	container.appendChild(btn);
	btn.appendChild(document.createTextNode(title));
	container.animator = new golgotha.maps.Animator(refresh);
	container.animator.animate(layers);
	var layersToLoad = [];

	container.complete = function(layer) {
		layersToLoad.remove(layer);
		layer.display(false);
		if (layersToLoad.length > 0)
			return false;

		// Put first two slices on the map
		for (var x = 0; x < Math.max(1, layers.length); x++) {
			var ov = container.layers[x];
			ov.setMap(map);
		}

		map.preload = false;
		this.isPreloaded = true;
		golgotha.maps.util.progress.hide();
		map.animator = container.animator;
		container.animator.start();
		return true;
	}

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
				delete map.animator;
			} else if (isPlaying)
				return true;
		}
		
		if (map.preLoad) return false;
		if (!container.isPreloaded) {
			map.preload = true;
			golgotha.maps.util.progress.start(map.getVisibleTiles().length * container.layers.length);
			for (var x = 0; x < container.layers.length; x++) {
				var ov = container.layers[x];
				layersToLoad.push(ov);
				ov.preload(map, container.complete, container.update);
			}
		} else {
			map.animator = container.animator;
			container.animator.start();
		}
		
		return true;
	});
	
	return container;
}

// Map animator object
golgotha.maps.Animator = function(interval) {
	this.layers = []; this.ofs = 0;
	this.isPlaying = false;
	this.eventHandlers = [];
	this.interval = isNaN(interval) ? 250 : Math.max(250, interval);
}

golgotha.maps.Animator.prototype.animate = function(slices) {
	this.clear();
	for (var x = 0; x < slices.length; x++)
		this.layers.push(slices[x]);

	return true;
}

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
}

golgotha.maps.Animator.prototype.previousFrame = function() {
	return (this.ofs == 0) ? (this.layers.length-1) : (this.ofs-1);
}

golgotha.maps.Animator.prototype.nextFrame = function() {
	var result = this.ofs+1;
	return (result == this.layers.length) ? 0 : result;
}

golgotha.maps.Animator.prototype.doFrame = function() {
	if (!this.isPlaying) return false;
	var pov = this.layers[this.ofs];
	pov.display(false);
	this.ofs = this.nextFrame();
	var ov = this.layers[this.ofs];
	ov.display(true);
	var nov = this.layers[this.nextFrame()];
	nov.display(false);
	nov.setMap(ov.getMap());
	pov.setMap(null);
	var a = this;
	window.setTimeout(function() { a.doFrame() }, (this.ofs == (this.layers.length-1)) ? 1250 : this.interval);
	return true;
}

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
}

golgotha.maps.Animator.prototype.stop = function() {
	if (!this.isPlaying) return false;
	console.log('Animator stopped');
	this.isPlaying = false;
	return true;
}

golgotha.maps.Animator.prototype.pauseHandler = function() {
	var a = this.animator;
	if (a.isPlaying) {
		console.log('Pausing');
		a.stop();
	}

	return true;
}

golgotha.maps.Animator.prototype.idleHandler = function() {
	var a = this.animator;
	if (!a.isPlaying) {
		console.log('Resuming');
		a.start();
	}

	return true;
}

/* 
if (zoom > this.nativeZoom) {
	var dZ = (zoom - this.nativeZoom) + 1;
	var nativeAddr = convertAddr(pnt, zoom, this.nativeZoom);
	var baseAddr = convertAddr(nativeAddr, this.nativeZoom, zoom);
	img.src = this.getTileUrl(nativeAddr, this.nativeZoom);
	img.style.height = (256 * dZ) + 'px';
	img.style.width = (256 * dZ) + 'px';
	
	var dX = (nativeAddr.x - baseAddr.x) << 8;
	var dY = (nativeAddr.y - baseAddr.y) << 8;
	if (dY > 0)
		img.style.top = (dY * -256) + 'px';
	if (dX > 0)
		img.style.left = (dX * -256) + 'px';
	
	console.log(img.style.height + ' ' + img.style.top);
} else
	img.src = this.getTileUrl(pnt, zoom);
	
*/
