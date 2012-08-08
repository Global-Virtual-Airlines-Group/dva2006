// DIV creation function
golgotha.maps.util = {};
golgotha.maps.util.buildTile = function(pnt, zoom, doc)
{
var div = doc.createElement('div');
div.style.width = '256px';
div.style.height = '256px';
var img = doc.createElement('img');
img.src = this.getTileUrl(pnt, zoom);
img.setAttribute('class', 'wxTile ' + this.imgClass);
img.defaultClass = img.className;
img.opacity = this.getOpacity();
img.style.opacity = img.opacity;
div.appendChild(img);
return div;	
}

// Create a ginsu weather overlay type
golgotha.maps.WeatherLayer = function(opts, name, timestamp) {
	if (!opts) opts = {};
	opts.getTileUrl = golgotha.maps.util.getTileUrl;
	opts.name = name;
	if (!opts.baseURL) opts.baseURL = 'http://' + golgotha.maps.tileHost + '/TileServer/imgs/' + name + '/u' + timestamp + '/';
	var ov = new google.maps.ImageMapType(opts);
	ov.getTileUrl = opts.getTileUrl;
	ov.maxZoom = opts.maxZoom;
	ov.baseURL = opts.baseURL;
	ov.getTile = golgotha.maps.util.buildTile;
	ov.getTextDate = golgotha.maps.util.getTextDate;
	ov.getCopyright = function() {
		var d = new Date();
		return 'Weather Data &copy; ' + d.getFullYear() + ' The Weather Channel.'
	}
	
	ov.preloadTiles = function(handler) {
		if (this.preload) return true;
		var vizTiles = this.getMap().getVisibleTiles();
		var imgsToLoad = [];
		for (var x = 0; x < vizTiles.length; x++) {
			var src = ov.getTileUrl(vizTiles[x], map.getZoom());
			var img = new Image();
			img.onload = function() {
				imgsToLoad.remove(this.src);
				if ((imgsToLoad.length == 0) && (handler != null))
					handler.call();
				
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

golgotha.maps.util.getTextDate = function()
{
var d = this.get('timestamp');
var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
return d.getDate() + '-' + months[d.getMonth()] + '-' + d.getFullYear() + '  ' 
	+ d.getHours() + ':' + ((d.getMinutes() < 10) ? '0' + d.getMinutes() : d.getMinutes());	
}

golgotha.maps.util.getTileUrl = function(pnt, zoom) {
	if (zoom > this.maxZoom) return '';
	var url = this.baseURL;
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
			url = url.substr(0, pos) + lastDigit +  url.substr(pos + 1);
	}

	return url + '.png';
}; 

// Create a ginsu FF weather overlay type
golgotha.maps.FFWeatherLayer = function(opts, name, timestamp, effective) {
	opts.effDate = new Date(effective);
	opts.baseURL = 'http://' + golgotha.maps.tileHost + '/TileServer/ff/' + name + '/u' + effective + '/u' + timestamp + '/';
	return golgotha.maps.WeatherLayer(opts, name, timestamp);
}

// Create a GVA/Wunderground weather overlay type
golgotha.maps.WUWeatherLayer = function(opts, name, timestamp) {
	opts.baseURL = 'http://' + self.location.host + '/tile/' + name + '/u' + timestamp + '/';
	var ov = golgotha.maps.WeatherLayer(opts, name, timestamp);
	ov.getCopyright = function() {
		var d = new Date();
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

golgotha.maps.GinsuLoader.prototype.load = function(id, seriesData)
{
this.layers.ts = seriesData.timestamp;
for (var x = 0; x < seriesData.seriesNames.length; x++) {
	var layerName = seriesData.seriesNames[x];
	var layerData = eval('seriesData.seriesInfo.' + layerName);
	if (layerData == undefined) {
		console.log('Cannot find ' + layerName);
		continue;
	} else if (layerData.series.length == 0) {
		console.log('No series data for ' + layerName);
		continue;
	}
	
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
		ovLayer.imgClass = myLayerData.imgClass;
		ovLayer.nativeZoom = layerData.nativeZoom;
		slices.push(ovLayer);
	}
	
	this.layers.names.push(layerName);
	this.layers.data[layerName] = slices;
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
