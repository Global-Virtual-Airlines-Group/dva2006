// ImageSeries data
document.maxZoom = new Array();
document.wxLayers = new Array();
document.seriesDate = new Array();

function loadSeries(id, sdata)
{
for (var x = 0; x < sdata.seriesNames.length; x++)
{
	var series = sdata.seriesNames[x];
	document.maxZoom[series] = eval('sdata.seriesInfo.' + series + '.maxZoom');
	document.seriesDate[series] = eval('sdata.seriesInfo.' + series + '.series[0].unixDate');
}

return true;
}

function getTileOverlay(name)
{
var cpc = new GCopyrightCollection("Weather Imagery");
var cp = new GCopyright(111, new GLatLngBounds(new GLatLng(-90, -180), new GLatLng(90, 180)), 0, "The Weather Channel")
cpc.addCopyright(cp);

var newLayer = new GTileLayer(cpc, 1, document.maxZoom[name]);
newLayer.getOpacity = function() { return 0.55; }
newLayer.isPng = function() { return true; }
newLayer.myBaseURL = 'http://' + document.tileHost + '/TileServer/imgs/' + name + '/u' + document.seriesDate[name] + '/';
newLayer.getTileUrl = function(pnt, zoom) {
var url = '';
if (zoom > this.maxResolution())
	return url;

var masks = [0, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288];

// Get the tile numbers
for (x = zoom; x > 0; x--) {
	var digit1 = ((masks[x] & pnt.x) == 0) ? 0 : 1;
	var digit2 = ((masks[x] & pnt.y) == 0) ? 0 : 2;
	url = url + (digit1 + digit2);
}

return this.myBaseURL + url + '.png';
}

return new GTileLayerOverlay(newLayer);
}

function WXOverlayControl(tileLayer, name, padding) {
	this.layerName = name;
	this.padding = padding;
	document.wxLayers[name] = tileLayer;
}

WXOverlayControl.prototype = new GControl();
WXOverlayControl.prototype.initialize = function(map) {
	var container = document.createElement("div");
	var btn = document.createElement("div");
	this.setButtonStyle(btn);
	container.appendChild(btn);
	btn.appendChild(document.createTextNode(this.layerName));
	btn.layerName = this.layerName;
	GEvent.addDomListener(btn, "click", this.updateMap);
	map.getContainer().appendChild(container);
	return container;
}

WXOverlayControl.prototype.getDefaultPosition = function() {
	return new GControlPosition(G_ANCHOR_BOTTOM_LEFT, this.padding);
}

WXOverlayControl.prototype.updateMap = function() {
	if (map.wxData)
		map.removeOverlay(map.wxData);

	map.wxData = document.wxLayers[this.layerName];
	map.addOverlay(map.wxData);
	return true;
}

WXOverlayControl.prototype.setButtonStyle = function(button) {
	button.style.color = "#303030";
	button.style.backgroundColor = "white";
	button.style.font = "small Arial";
	button.style.fontSize = "10px";
	button.style.border = "1px solid black";
	button.style.padding = "2px";
	button.style.marginBottom = "3px";
	button.style.textAlign = "center";
	button.style.width = "6em";
	button.style.cursor = "pointer";
}

function WXClearControl(padding) {
	this.layerName = 'None';
	this.padding = padding;
}

WXClearControl.prototype = new GControl();
WXClearControl.prototype.setButtonStyle = WXOverlayControl.prototype.setButtonStyle;
WXClearControl.prototype.initialize = function(map) {
	var container = document.createElement("div");
	var btn = document.createElement("div");
	this.setButtonStyle(btn);
	container.appendChild(btn);
	btn.appendChild(document.createTextNode(this.layerName));
	btn.layerName = this.layerName;
	GEvent.addDomListener(btn, "click", this.clearWX);
	map.getContainer().appendChild(container);
	return container;
}

WXClearControl.prototype.getDefaultPosition = function() {
	return new GControlPosition(G_ANCHOR_BOTTOM_LEFT, this.padding);
}

WXClearControl.prototype.clearWX = function() {
	if (map.wxData) {
		map.removeOverlay(map.wxData);
		delete map.wxData;
	}
		
	return true;
}
