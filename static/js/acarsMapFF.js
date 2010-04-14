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
results.push(new Option('< SELECT >', ''));
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
var cpc = new GCopyrightCollection('Weather Imagery');
var cp = new GCopyright(111, new GLatLngBounds(new GLatLng(-90, -180), new GLatLng(90, 180)), 0, 'The Weather Channel')
cpc.addCopyright(cp);

var newLayer = new GTileLayer(cpc, 1, document.maxZoom[name], {isPng:true, opacity:tx});
newLayer.myBaseURL = 'http://' + document.tileHost + '/TileServer/ff/' + name + '/u' + document.seriesDate[name] + '/u' + date.getTime() + '/';
newLayer.getTileUrl = function(pnt, zoom) {
if (zoom > this.maxResolution()) return '';
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

var ovLayer = new GTileLayerOverlay(newLayer);
ovLayer.layerName = name;
ovLayer.layerDate = date;
ovLayer.isFF = true;
document.wxLayers[name + '!' + date.getTime()] = ovLayer;
return ovLayer;
}

function FFOverlayControl(title, name, padding) {
	this.buttonTitle = title;
	this.layerNames = name;
	this.padding = padding;
}

// Initialize from WXOverlayControl
FFOverlayControl.prototype = new GControl();
FFOverlayControl.prototype.initialize = WXOverlayControl.prototype.initialize;
FFOverlayControl.prototype.setButtonStyle = WXOverlayControl.prototype.setButtonStyle;
FFOverlayControl.prototype.getDefaultPosition = WXOverlayControl.prototype.getDefaultPosition; 
FFOverlayControl.prototype.updateMap = function() {
	clearWX();
	map.ffLayer = this.layerNames;
	
	// Show the combobox and populate its options
	var f = document.forms[0];
	var options = document.ffOptions[this.layerNames];
	f.ffSlice.options.length = options.length;
	for (var x = 0; x < options.length; x++)
		f.ffSlice.options[x] = options[x];

	var ffs = getElement('ffSlices');
	if (ffs) ffs.style.visibility = 'visible';
	return true;
}

function updateFF(combo)
{
if (map.wxData)
	map.removeOverlay(map.wxData);
	
var utc = combo.options[combo.selectedIndex].value;
map.wxData = document.wxLayers[map.ffLayer + '!' + utc];
if (map.wxData)
	map.addOverlay(map.wxData);

return true;
}

function getVisibleTiles()
{
var tiles = [];
var bnds = map.getBounds();
var p = map.getCurrentMapType().getProjection();
var maxX = p.getWrapWidth(map.getZoom()) >> 8;
var nw = new GLatLng(bnds.getNorthEast().lat(), bnds.getSouthWest().lng());
var se = new GLatLng(bnds.getSouthWest().lat(), bnds.getNorthEast().lng());

// Get the pixel points of the tiles
var nwp = p.fromLatLngToPixel(nw, map.getZoom());
var sep = p.fromLatLngToPixel(se, map.getZoom());
var nwAddr = new GPoint((nwp.x >> 8), (nwp.y >> 8));
var seAddr = new GPoint((sep.x >> 8), (sep.y >> 8));

// Load the tile addresses
for (var x = nwAddr.x; x <= seAddr.x; x++) {
	for (var y = nwAddr.y; y <= seAddr.y; y++)
		tiles.push(new GPoint(((x > maxX) ? (x - maxX) : x), y));
}

return tiles;
}

function tileLoaded()
{
progressBar.updateLoader(1);
var ov = this.layer;
var imgs = ov.tileImages;
imgs.remove(this.src);
if (imgs.length == 0)
	loadSlices.remove(ov);

if (loadSlices.length == 0) {
	var btn = getElement('AnimateButton');
	btn.value = 'STOP';
	enableObject(btn, true);
	progressBar.remove();
	showSliceLayer(0);
}

return true;
}

function preloadImages(layerName, dates)
{
removeSlices();

// Get the tiles
var tilePoints = getVisibleTiles();
progressBar.start(tilePoints.length * dates.length);
for (var x = 0; x < dates.length; x++) {
	var dt = dates[x];
	var ov = document.wxLayers[layerName + '!' + dt.getTime()];
	var tileLayer = ov.getTileLayer();
	ov.tileImages = [];
	ov.isPreloaded = false;
	for (var y = 0; y < tilePoints.length; y++) {
		var img = new Image();
		img.layer = ov;
		img.onload = tileLoaded;
		img.onerror = tileLoaded;
		img.src = tileLayer.getTileUrl(tilePoints[y], map.getZoom());
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
if (map.isAnimating) {
	removeSlices();
	btn.value = 'ANIMATE';
	enableObject(btn, true);
	enableObject(f.ffSlice, true);
	progressBar.remove();
	map.enableScrollWheelZoom();
	map.enableDoubleClickZoom();
	map.enableDragging();
	map.addControl(mCtl);
	map.isAnimating = false;
} else {
	enableObject(f.ffSlice, false);
	map.disableDragging();
	map.disableDoubleClickZoom();
	map.disableScrollWheelZoom();
	map.removeControl(mCtl);

	// Preload the tiles for each tile layer
	enableObject(btn, false);
	map.isAnimating = true;
	preloadImages(map.ffLayer, document.ffSlices[map.ffLayer]);
}

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
	ov.show();	
} else {
	map.addOverlay(ov);
	hideAllSlices();
	ov.show();
	ov.isPreloaded = true;
	timerInterval += 15;
}

window.setTimeout('showSliceLayer(' + newOfs + ')', timerInterval);
return true;
}

function removeSlices()
{
for (var x in animateSlices)
	map.removeOverlay(animateSlices[x]);

loadSlices.length = 0;
animateSlices.length = 0;
return true;
}

function hideAllSlices()
{
for (var x in animateSlices) {
	var ov = animateSlices[x];
	if (ov.isPreloaded)
		ov.hide();
}
	
return true;
}
