// Combobox Options and Timeslices for FF layers
document.ffOptions = [];
document.ffSlices = [];

var animateSlices = [];
var loadSlices = [];

function getFFSlices(seriesName)
{
var now = new Date();
var dates = [];
var slices = document.seriesData[seriesName].series[0].ff;
for (var x = 0; x < slices.length; x++) {
	var d = slices[x];
	var dt = new Date(parseInt(d.unixDate)); 
	if ((dt.getTime() - GMTOffset) > now.getTime())
		dates.push(dt);
}
	
return dates;
}

function getFFComboOptions(dates)
{
var results = [];
results.push(new Option('[ SELECT ]', ''));
for (var x = 0; x < dates.length; x++) {
	var utc = dates[x];
	var o = new Option(fmtDate(new Date(utc.getTime() - GMTOffset)), utc.getTime());
	o.seriesDate = utc.getTime();
	results.push(o);
}

return results;
}

function getFFOverlay(name, tx, date)
{
var layerOpts = {minZoon:1, maxZoom:document.maxZoom[name], isPng:true, opacity:tx, tileSize:new google.maps.Size(256,256)};
layerOpts.myBaseURL = 'http://' + document.tileHost + '/TileServer/ff/' + name + '/u' + document.seriesDate[name] + '/u' + date.getTime() + '/';
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
ovLayer.myBaseURL = layerOpts.myBaseURL;
ovLayer.layerName = name;
ovLayer.layerDate = date;
ovLayer.isFF = true;
document.wxLayers[name + '!' + date.getTime()] = ovLayer;
return ovLayer;
}

function FFOverlayControl(title, name) {
	this.buttonTitle = title;
	this.layerNames = name;
	
	var container = document.createElement('div');
	var btn = document.createElement('div');
	this.setButtonStyle(btn);
	container.appendChild(btn);
	btn.appendChild(document.createTextNode(this.buttonTitle));
	btn.layerNames = this.layerNames;
	google.maps.event.addDomListener(btn, 'click', this.updateMap);
	return container;
}

// Initialize from WXOverlayControl
FFOverlayControl.prototype.setButtonStyle = WXOverlayControl.prototype.setButtonStyle;
FFOverlayControl.prototype.updateMap = function() {
	clearWX();
	map.ffLayer = this.layerNames;
	
	// Show the combobox and populate its options
	var f = document.forms[0];
	var options = document.ffOptions[this.layerNames];
	f.ffSlice.options.length = options.length;
	for (var x = 0; x < options.length; x++)
		f.ffSlice.options[x] = options[x];

	showObject(getElement('copyright'), true);
	showObject(getElement('ffSlices'), true);
	return true;
}

function updateFF(combo)
{
if (map.wxData)
	map.removeWeather(map.wxData);
	
var utc = combo.options[combo.selectedIndex].value;
map.wxData = document.wxLayers[map.ffLayer + '!' + utc];
if (map.wxData)
	map.addWeather(map.wxData);

return true;
}

function getVisibleTiles()
{
var bnds = map.getBounds();
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

function tileLoaded()
{
pBar.setCurrent(pBar.getCurrent() + 1);
var ov = this.layer;
var imgs = ov.tileImages;
imgs.remove(this.src);
if (imgs.length == 0)
	loadSlices.remove(ov);

if (loadSlices.length == 0) {
	var btn = getElement('AnimateButton');
	btn.value = 'STOP';
	enableObject(btn, true);
	pBar.hide();
	showSliceLayer(0);
}

return true;
}

function preloadImages(layerName, dates)
{
removeSlices();

// Get the tiles
var tilePoints = getVisibleTiles();
pBar.start(tilePoints.length * dates.length);
for (var x = 0; x < dates.length; x++) {
	var dt = dates[x];
	var ov = document.wxLayers[layerName + '!' + dt.getTime()];
	ov.tileImages = [];
	ov.isPreloaded = false;
	for (var y = 0; y < tilePoints.length; y++) {
		var img = new Image();
		img.layer = ov;
		img.onload = tileLoaded;
		img.onerror = tileLoaded;
		img.src = ov.getTileUrl(tilePoints[y], map.getZoom());
		ov.tileImages.push(img.src);
	}
	
	loadSlices.push(ov);
	animateSlices.push(ov);
}

return true;	
}

function animateFF()
{
var f = document.forms[0];
var btn = getElement('AnimateButton');
var opts = map.getOptions();
if (map.isAnimating) {
	removeSlices();
	btn.value = 'ANIMATE';
	enableObject(btn, true);
	enableObject(f.ffSlice, true);
	pBar.hide();
	opts.disableDoubleClickZoom = false;
	opts.scrollwheel = true;
	opts.dragging = true;
	opts.navigationControl = true;
	map.isAnimating = false;
} else {
	enableObject(f.ffSlice, false);
	opts.dragging = false;
	opts.scrollwheel = false;
	opts.disableDoubleClickZoom = true;
	opts.navigationControl = false;

	// Preload the tiles for each tile layer
	enableObject(btn, false);
	map.isAnimating = true;
	preloadImages(map.ffLayer, document.ffSlices[map.ffLayer]);
}

map.setOptions(opts);
return true;
}

function showSliceLayer(ofs)
{
if (!map.isAnimating) return false;

// Calculate timer interval
var newOfs = (ofs >= (dates.length - 1)) ? 0 : (ofs + 1);
var timerInterval = (newOfs == 0) ? 900 : 450;

// Load the layer
var ov = animateSlices[ofs];
setWXStatus('Showing ' + ov.layerName + ' (' + fmtDate(new Date(ov.layerDate.getTime() - GMTOffset)) + ')');
if (ov.isPreloaded) {
	hideAllSlices();
	map.addWeather(ov);
} else {
	map.addWeather(ov);
	hideAllSlices();
	ov.isPreloaded = true;
	//timerInterval += 15;
}

window.setTimeout('showSliceLayer(' + newOfs + ')', timerInterval);
return true;
}

function removeSlices()
{
for (var x in animateSlices)
	map.removeWeather(animateSlices[x]);

loadSlices.length = 0;
animateSlices.length = 0;
return true;
}

function hideAllSlices()
{
for (var x in animateSlices) {
	var ov = animateSlices[x];
	if (ov.isPreloaded)
		map.removeWeather(ov);
}
	
return true;
}
