function getFFSlices(seriesName)
{
// Calculate GMT offset
var d = new Date();
var offset = d.getTimezoneOffset() * -60000;

var dates = new Array();
var slices = document.seriesData[seriesName].series[0].ff;
for (var x = 0; x < slices.length; x++) {
	var d = slices[x];
	dates.push(new Date(parseInt(d.unixDate) + offset));
}
	
return dates;
}

function getFFComboOptions(dates)
{
var results = new Array();
results.push(new Option("< SELECT >", ""));
for (var x = 0; x < dates.length; x++) {
	var d = dates[x];
	var utc = d.getTime() + (d.getTimezoneOffset() * 60000);
	results.push(new Option(d.toLocaleTimeString(), utc));
}

return results;
}

function getFFOverlay(name, opacity, date)
{
var cpc = new GCopyrightCollection("Weather Imagery");
var cp = new GCopyright(111, new GLatLngBounds(new GLatLng(-90, -180), new GLatLng(90, 180)), 0, "The Weather Channel")
cpc.addCopyright(cp);

var newLayer = new GTileLayer(cpc, 1, document.maxZoom[name]);
newLayer.tx = opacity;
newLayer.getOpacity = function() { return this.tx; }
newLayer.isPng = function() { return true; }
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
map.addOverlay(map.wxData);
return true;
}
