// ImageSeries data
document.maxZoom = new Array();
document.wxLayers = new Array();
document.seriesDate = new Array();

// Calculate GMT offset in seconds from local
var GMTOffset = new Date().getTimezoneOffset() * 60000;

function loadSeries(id, sdata)
{
for (var x = 0; x < sdata.seriesNames.length; x++)
{
	var series = sdata.seriesNames[x];
	document.maxZoom[series] = eval('sdata.seriesInfo.' + series + '.maxZoom');
	document.seriesDate[series] = eval('sdata.seriesInfo.' + series + '.series[0].unixDate');
	sdata.seriesInfo[series].isFF = (series.substr(series.length - 3) == '_ff');
}

document.seriesData = sdata.seriesInfo;
return true;
}

function getTileOverlay(name, tx)
{
var cpc = new GCopyrightCollection("Weather Imagery");
var cp = new GCopyright(111, new GLatLngBounds(new GLatLng(-90, -180), new GLatLng(90, 180)), 0, "The Weather Channel")
cpc.addCopyright(cp);

var newLayer = new GTileLayer(cpc, 1, document.maxZoom[name], {isPng:true, opacity:tx});
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

var ovLayer = new GTileLayerOverlay(newLayer);
ovLayer.layerName = name;
document.wxLayers[name] = ovLayer;
return ovLayer;
}

function WXOverlayControl(title, names, padding) {
	this.buttonTitle = title;
	this.layerNames = names;
	this.padding = padding;
}

WXOverlayControl.prototype = new GControl();
WXOverlayControl.prototype.initialize = function(map) {
	var container = document.createElement("div");
	var btn = document.createElement("div");
	this.setButtonStyle(btn);
	container.appendChild(btn);
	btn.appendChild(document.createTextNode(this.buttonTitle));
	btn.layerNames = this.layerNames;
	GEvent.addDomListener(btn, "click", this.updateMap);
	map.getContainer().appendChild(container);
	return container;
}

WXOverlayControl.prototype.getDefaultPosition = function() {
	return new GControlPosition(G_ANCHOR_BOTTOM_LEFT, this.padding);
}

WXOverlayControl.prototype.updateMap = function() {
	clearWX();

	var multiLayers = (this.layerNames instanceof Array);
	if (multiLayers) {
		map.wxData = new Array();
		for (var x = 0; x < this.layerNames.length; x++) {
			map.wxData.push(document.wxLayers[this.layerNames[x]]);
			map.addOverlay(document.wxLayers[this.layerNames[x]]);
		}
	} else { 
		map.wxData = document.wxLayers[this.layerNames];
		map.addOverlay(map.wxData);
	}

	delete map.ffLayer;
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
	button.style.cursor = "pointer";
	if (!this.buttonTitle)
		button.style.width = "6em";
	else if (this.buttonTitle.length > 11)
		button.style.width = "8em";
	else if (this.buttonTitle.length > 9)
		button.style.width = "7em";
	else
		button.style.width = "6em";
}

function WXClearControl(padding) {
	this.padding = padding;
}

WXClearControl.prototype = new GControl();
WXClearControl.prototype.setButtonStyle = WXOverlayControl.prototype.setButtonStyle;
WXClearControl.prototype.initialize = function(map) {
	var container = document.createElement("div");
	var btn = document.createElement("div");
	this.setButtonStyle(btn);
	container.appendChild(btn);
	btn.appendChild(document.createTextNode("None"));
	btn.layerName = this.layerName;
	GEvent.addDomListener(btn, "click", clearWX);
	map.getContainer().appendChild(container);
	return container;
}

WXClearControl.prototype.getDefaultPosition = function() {
	return new GControlPosition(G_ANCHOR_BOTTOM_LEFT, this.padding);
}

function clearWX()
{
var ffs = document.getElementById("ffSlices");
if (ffs) ffs.style.visibility = 'hidden';
	
if (!map.wxData) return false;
var multiLayers = (map.wxData instanceof Array);
if (multiLayers) {
	for (var x = 0; x < map.wxData.length; x++)
		map.removeOverlay(map.wxData[x]);
} else
	map.removeOverlay(map.wxData);

delete map.wxData;
delete map.ffLayer;
return true;
}
