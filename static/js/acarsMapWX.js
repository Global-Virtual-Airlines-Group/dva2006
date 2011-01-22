// ImageSeries data
document.maxZoom = [];
document.wxLayers = [];
document.seriesDate = [];

// Calculate GMT offset in seconds from local
var GMTOffset = new Date().getTimezoneOffset() * 60000;

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

function loadSeries(id, sdata)
{
gaEvent('WeatherMap', 'Load Series List');
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
var layerOpts = {minZoom:1, maxZoom:document.maxZoom[name], isPng:true, opacity:tx, tileSize:new google.maps.Size(256,256)};
layerOpts.myBaseURL = 'http://' + document.tileHost + '/TileServer/imgs/' + name + '/u' + document.seriesDate[name] + '/';
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
if (document.multiHost) {
	var lastDigit = url.charAt(url.length - 1);
	var pos = url.indexOf('%');
	if (pos > -1)
		url = url.substr(0, pos) + lastDigit +  url.substr(pos + 1);
}

return url + '.png';
}

var ovLayer = new google.maps.ImageMapType(layerOpts);
ovLayer.getTileUrl = layerOpts.getTileUrl;
ovLayer.layerName = name;
ovLayer.layerDate = new Date(parseInt(document.seriesDate[name]));
document.wxLayers[name] = ovLayer;
return ovLayer;
}

function WXOverlayControl(title, names) {
	this.buttonTitle = title;
	this.layerNames = names;
	
	this.container = document.createElement('div');
	var btn = document.createElement('div');
	this.setButtonStyle(btn);
	this.container.appendChild(btn);
	btn.appendChild(document.createTextNode(this.buttonTitle));
	btn.layerNames = this.layerNames;
	google.maps.event.addDomListener(btn, 'click', this.updateMap);
	return this.container;
}

WXOverlayControl.prototype.updateMap = function() {
	clearWX();
	var multiLayers = (this.layerNames instanceof Array);
	if (multiLayers) {
		map.wxData = [];
		var layerInfo = [];
		for (var x = 0; x < this.layerNames.length; x++) {
			var layer = document.wxLayers[this.layerNames[x]];
			map.wxData.push(layer);
			map.addWeather(layer);
			layerInfo.push(layer.layerName + ' (' + fmtDate(new Date(layer.layerDate.getTime() - GMTOffset)) + ')');
		}

		gaEvent('WeatherMap', 'Show Weather', this.layerNames.join(' '));
		setWXStatus('Showing ' + layerInfo.join(', '));
	} else { 
		var layer = document.wxLayers[this.layerNames];
		map.wxData = layer;
		map.addWeather(layer);
		gaEvent('WeatherMap', 'Show Weather', this.layerNames);
		setWXStatus('Showing ' + layer.layerName + ' (' + fmtDate(new Date(layer.layerDate.getTime() - GMTOffset)) + ')');
	}

	showObject(getElement('copyright'), true);
	delete map.ffLayer;
	return true;
}

WXOverlayControl.prototype.setButtonStyle = function(button) {
	button.style.color = '#303030';
	button.style.backgroundColor = 'white';
	button.style.font = 'small Arial';
	button.style.fontSize = '10px';
	button.style.border = '1px solid black';
	button.style.padding = '2px';
	button.style.marginBottom = '3px';
	button.style.textAlign = 'center';
	button.style.cursor = 'pointer';
	if (!this.buttonTitle)
		button.style.width = '6em';
	else if (this.buttonTitle.length > 11)
		button.style.width = '8em';
	else if (this.buttonTitle.length > 9)
		button.style.width = '7em';
	else
		button.style.width = '6em';
}

function WXClearControl() {
	this.container = document.createElement('div');
	var btn = document.createElement('div');
	this.setButtonStyle(btn);
	this.container.appendChild(btn);
	btn.appendChild(document.createTextNode('None'));
	btn.layerName = this.layerName;
	google.maps.event.addDomListener(btn, 'click', clearWX);
	return this.container;
}

WXClearControl.prototype.setButtonStyle = WXOverlayControl.prototype.setButtonStyle;

function clearWX()
{
gaEvent('WeatherMap', 'Clear Weather');
var ffs = document.getElementById('ffSlices');
if (ffs) ffs.style.visibility = 'hidden';
if (!map.wxData) return false;

if (map.wxData instanceof Array) {
	for (var x = 0; x < map.wxData.length; x++)
		map.removeWeather(map.wxData[x]);
} else
	map.removeWeather(map.wxData);

try {
	if (animateSlices) {
		for (var x = 0; x < animateSlices.length; x++)
			map.removeWeather(animateSlices[x]);
	}
} catch (err) { }

delete map.wxData;
delete map.ffLayer;
showObject(getElement('copyright'), false);
setWXStatus('None');
return true;
}

function setWXStatus(msg)
{
var sp = getElement('wxLoading');	
if (sp)	sp.innerHTML = msg;
return true;
}

function fmtDate(d)
{
var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
return d.getDate() + '-' + months[d.getMonth()] + '-' + d.getFullYear() + '  ' 
	+ d.getHours() + ':' + ((d.getMinutes() < 10) ? '0' + d.getMinutes() : d.getMinutes());	
}
