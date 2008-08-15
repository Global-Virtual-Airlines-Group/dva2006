// Combobox Options and Timeslices for FF layers
document.ffOptions = new Array();
document.ffSlices = new Array();

var animateSlices = new Array();
var loadSlices = new Array();

function getFFSlices(seriesName)
{
var now = new Date();
var dates = new Array();
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
var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
var results = new Array();
results.push(new Option("< SELECT >", ""));
for (var x = 0; x < dates.length; x++) {
	var utc = dates[x];
	var d = new Date(utc.getTime() - GMTOffset);
	var dt = d.getDate() + "-" + months[d.getMonth()] + "-" + d.getFullYear() + "  " 
		+ d.getHours() + ":" + ((d.getMinutes() < 10) ? "0" + d.getMinutes() : d.getMinutes()); 
	results.push(new Option(dt, utc.getTime()));
}

return results;
}

function getFFOverlay(name, tx, date)
{
var cpc = new GCopyrightCollection("Weather Imagery");
var cp = new GCopyright(111, new GLatLngBounds(new GLatLng(-90, -180), new GLatLng(90, 180)), 0, "The Weather Channel")
cpc.addCopyright(cp);

var newLayer = new GTileLayer(cpc, 1, document.maxZoom[name], {isPng:true, opacity:tx});
newLayer.myBaseURL = 'http://' + document.tileHost + '/TileServer/ff/' + name + '/u' + document.seriesDate[name] + '/u' + date.getTime() + '/';
newLayer.getTileUrl = function(pnt, zoom) {
var url = '';
if (zoom > this.maxResolution()) return url;
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

	var ffs = document.getElementById("ffSlices");
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
if (!map.visibleTiles)
	map.visibleTiles = new Array();

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
map.visibleTiles.length = 0;
for (var x = nwAddr.x; x <= seAddr.x; x++) {
	for (var y = nwAddr.y; y <= seAddr.y; y++)
		map.visibleTiles.push(new GPoint(((x > maxX) ? (x - maxX) : x), y));
}

map.isPreloaded = false;
return true;
}

function preloadImages(layerName, dates)
{
if (!map.isAnimating) return false;
for (var x = 0; ((x < dates.length) && (map.isAnimating)); x++) {
	var dt = dates[x];
	var ov = document.wxLayers[layerName + '!' + dt.getTime()];
	animateSlices.push(ov);
	loadSlices.push(ov);
}

sliceLoaded.handler = GEvent.addListener(map, 'tilesloaded', sliceLoaded);
map.addOverlay(loadSlices.shift());
return true;	
}

function sliceLoaded()
{
if (loadSlices.length == 0)
{
	GEvent.removeListener(sliceLoaded.handler);
	map.isPreloaded = true;
	return true;
}

// Load the next slice
map.addOverlay(loadSlices.shift());
return true;	
}

function animateFF()
{
var f = document.forms[0];
var btn = getElement('AnimateButton');
if (map.isAnimating) {
	// clear animateSlices
	for (var x = 0; x < animateSlices.length; x++)
		map.removeOverlay(animateSlices[x]);

	animateSlices.length = 0;
	btn.value = 'ANIMATE';
	enableObject(f.ffSlice, true);
	map.enableDoubleClickZoom();
	map.enableDragging();
	delete map.lastSlice;
	delete map.isAnimating;
} else {
	enableObject(f.ffSlice, false);
	map.disableDragging();
	map.disableDoubleClickZoom();

	// Preload the tiles for each tile layer
	map.isAnimating = true;
	preloadImages(map.ffLayer, document.ffSlices[map.ffLayer]);
	btn.value = 'STOP';

	// Set the start offset and fire the timer at 5 seconds
	var startOfs = Math.max(0, f.ffSlice.selectedIndex - 1);
	window.setTimeout('showSliceLayer(' + startOfs + ')', 2500);
}

return true;
}

function showSliceLayer(ofs)
{
if (!map.isAnimating) return false;
if (!map.isPreloaded) {
	window.setTimeout('showSliceLayer(' + ofs + ')', 1000);
	return false;
}

// Hide any visible slices
hideAllSlices();

// Get the overlay
var ov = animateSlices[ofs];
ov.show();

// Fire off the next timer in 1.5 seconds
var newOfs = (ofs >= (dates.length - 1)) ? 0 : (ofs + 1);
window.setTimeout('showSliceLayer(' + newOfs + ')', (newOfs == 0) ? 1150 : 525);
return true;
}

function hideAllSlices()
{
if (!map.isAnimating) return false;
for (var x = 0; x < animateSlices.length; x++) {
	var ov = animateSlices[x];
	ov.hide();
}
	
return true;
}
