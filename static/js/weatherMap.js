

// Calculate GMT offset in seconds from local
golgotha.maps.GMTOffset = new Date().getTimezoneOffset() * 60000;

// Prototypes to add/remove weather layers from the map
google.maps.Map.prototype.addWeather = function(layer) { this.overlayMapTypes.insertAt(0, layer); }
google.maps.Map.prototype.removeWeather = function(layer)
{
for (var x = 0; x < this.overlayMapTypes.getLength(); x++) {
	var ovLayer = this.overlayMapTypes.getAt(x);
	if (ovLayer == layer) {
		this.overlayMapTypes.removeAt(x);
		return true;
	}
}	

return false;
}

// Prototype to calculate visible tile addresses for map
google.maps.Map.prototype.getVisibleTiles = function()
{
var bnds = this.getBounds();
var nw = new google.maps.LatLng(bnds.getNorthEast().lat(), bnds.getSouthWest().lng());
var se = new google.maps.LatLng(bnds.getSouthWest().lat(), bnds.getNorthEast().lng());

// Get the pixel points of the tiles
var p = map.getProjection();
var nwp = p.fromLatLngToPoint(nw); nwp.x = Math.round(nwp.x << map.getZoom()); nwp.y = Math.round(nwp.y << map.getZoom());
var sep = p.fromLatLngToPoint(se); sep.x = Math.round(sep.x << map.getZoom()); sep.y = Math.round(sep.y << map.getZoom());
var nwAddr = new google.maps.Point((nwp.x >> 8), (nwp.y >> 8));
var seAddr = new google.maps.Point((sep.x >> 8), (sep.y >> 8));

// Load the tile addresses
var tiles = [];
for (var x = nwAddr.x; x <= seAddr.x; x++) {
	for (var y = nwAddr.y; y <= seAddr.y; y++)
		tiles.push(new google.maps.Point(x, y));
}

return tiles;
}

// Prototype to show certain types of map tile images
google.maps.Map.prototype.showOverlay = function(ovLayer)
{
var imgs = getElementsByClass(ovLayer.imgClass, 'img', this.getDiv());
for (var x = 0; x < imgs.length; x++) {
	var img = imgs[x];
	img.style.opacity = this.opacity;
	img.className = 'visibleTile ' + img.defaultClass;
}

return true;
}

// Prototype to hide certain types of map tile images
google.maps.Map.prototype.hideOverlay = function(ovLayer)
{
var imgs = getElementsByClass(ovLayer.imgClass, 'img', this.getDiv());
for (var x = 0; x < imgs.length; x++) {
	var img = imgs[x];
	img.style.opacity = 0;
	img.className = img.defaultClass;
}

return true;
}


// Weather.com TileServer series list paser
golgotha.maps.seriesParser = function(id, sdata)
{
gaEvent('WeatherMap', 'Load Series List');
var sd = golgotha.maps.seriesData;
sd.timestamp = new Date(sdata.timestamp);
for (var x = 0; x < sdata.seriesNames.length; x++) {
	var series = sdata.seriesNames[x];
	var sliceData = eval('sdata.seriesInfo.' + series);

	// Create the layer and get the slices
	var layer = new golgotha.maps.WeatherLayer(series, slicData.nativeZoom, sliceData.maxZoom);
	layer.isFF = (series.substr(series.length - 3) == '_ff');
	var slices = layer.isFF ? sliceData.series[0].ff : sliceData.series;
	if (layer.isFF)
		layer.baseDate = sliceData.series[0].unixDate;

	// Load the slice timestamps
	var maxSlices = Math.min(slices.length, golgotha.maps.MaxSlices);
	for (var y = 0; y < maxSlices; y++)
		layer.addSlice(slices[y].unixDate);

	sd[series] = layer;
}

return true;
}

// Weather layer object
function golgotha.maps.WeatherLayer(name, nativeZoom, maxZoom)
{
this.name = name;
this.nativeZoom = Math.max(1, nativeZoom);
this.maxZoom = Math.max(this.nativeZoom, maxZoom);
this.slices = [];
this.mapTypes = [];
}

// Adds a slice
golgotha.maps.WeatherLayer.prototype.addSlice = function(uxTimestamp)
{
this.slices.push(uxTimestamp);
return true;
}

golgotha.maps.WeatherLayer.prototype.getMapType = function(tx, ofs)
{
var layerOpts = {minZoom:1, maxZoom:this.maxZoom, isPng:true, opacity:tx, tileSize:new google.maps.Size(256,256)};
var baseURL = 'http://' + golgotha.maps.tileHost + '/TileServer/imgs/';
if (this.isFF)
	baseURL += '/ff/' + this.name + '/u' + this.baseDate + '/u';
else
	baseURL += this.name + '/u';

baseURL += this.slices[ofs] + '/';
layerOpts.myBaseURL = baseURL;
layerOpts.getTileUrl = function(pnt, zoom) {
	if (zoom > this.maxZoom) return '';
	var url = this.myBaseURL;
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
}


}
