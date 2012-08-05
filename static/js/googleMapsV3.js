// Initialize common data
golgotha.maps.PIN_SIZE = new google.maps.Size(12, 20);
golgotha.maps.DEFAULT_SHADOW = new google.maps.MarkerImage('/' + golgotha.maps.IMG_PATH + '/maps/shadow.png', new google.maps.Size(22, 20), null, new google.maps.Point(6, 20));
golgotha.maps.S_ICON_SIZE = new google.maps.Size(24, 24);
golgotha.maps.S_ICON_SHADOW_SIZE = new google.maps.Size(24 * (59 / 32), 24);
golgotha.maps.ICON_ANCHOR = new google.maps.Point(12, 12);
golgotha.maps.DEFAULT_TYPES = [google.maps.MapTypeId.ROADMAP, google.maps.MapTypeId.SATELLITE, google.maps.MapTypeId.TERRAIN];
golgotha.maps.z = {INFOWINDOW:100, POLYLINE:25, POLYGON:35, MARKER:50, OVERLAY:10};
golgotha.maps.ovLayers = [];

// Set best text color for map types
golgotha.maps.TextColor = {roadmap:'#002010', satellite:'#efefef', terrain:'#002010'};

golgotha.maps.displayedMarkers = [];
golgotha.maps.setMap = function(map) {
	if (map == null)
		golgotha.maps.displayedMarkers.remove(this);
	else
		golgotha.maps.displayedMarkers.push(this);
	
	this.setMap_OLD(map);
}

// Track overlays
google.maps.Marker.prototype.setMap_OLD = google.maps.Marker.prototype.setMap;
google.maps.Polyline.prototype.setMap_OLD = google.maps.Polyline.prototype.setMap;
google.maps.Polygon.prototype.setMap_OLD = google.maps.Polygon.prototype.setMap;
google.maps.Circle.prototype.setMap_OLD = google.maps.Circle.prototype.setMap;
google.maps.Marker.prototype.setMap = golgotha.maps.setMap;
google.maps.Polyline.prototype.setMap = golgotha.maps.setMap;
google.maps.Polygon.prototype.setMap = golgotha.maps.setMap;
google.maps.Circle.prototype.setMap = golgotha.maps.setMap;
google.maps.Map.prototype.clearOverlays = function() {
	while (golgotha.maps.displayedMarkers.length > 0) {
		var mrk = golgotha.maps.displayedMarkers.shift();
		mrk.setMap(null);
	}
	
	return true;
}

google.maps.Map.prototype.addOverlay = function(mrk) {
	mrk.setMap(this);
	return true;
}

golgotha.maps.setButtonStyle = function(button) {
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

golgotha.maps.LayerSelectControl = function(title, layer) {
	var container = document.createElement('div');
	var btn = document.createElement('div');
	btn.ovLayer = layer;
	golgotha.maps.setButtonStyle(btn);
	container.appendChild(btn);
	btn.appendChild(document.createTextNode(title));
	google.maps.event.addDomListener(btn, 'click', function() {
		if (this.ovLayer.getMap() != null)
			return true;

		golgotha.maps.ovLayers.push(this.ovLayer);
		this.ovLayer.setMap(map);
		return true;
	});

	return container;
}

golgotha.maps.LayerClearControl = function() {
	var container = document.createElement('div');
	var btn = document.createElement('div');
	golgotha.maps.setButtonStyle(btn);
	container.appendChild(btn);
	btn.appendChild(document.createTextNode('None'));
	google.maps.event.addDomListener(btn, 'click', function() {
		for (var ov = golgotha.maps.ovLayers.pop(); (ov != null); ov = golgotha.maps.ovLayers.pop())
			ov.setMap(null);

		return true;
	});

	return container;
}

function googleMarker(color, point, label)
{
if (color == 'null') return point;
var icn = new google.maps.MarkerImage('/' + golgotha.maps.IMG_PATH + '/maps/point_' + color + '.png', null, null, null, golgotha.maps.PIN_SIZE);
var marker = new google.maps.Marker({position:point, icon:icn, shadow:golgotha.maps.DEFAULT_SHADOW, zIndex:golgotha.maps.z.MARKER});
if (label != null) {
	marker.info = label;
	google.maps.event.addListener(marker, 'click', function() { map.infoWindow.setContent(this.info); map.infoWindow.open(map, this); });
}

return marker;
}

function googleIconMarker(palCode, iconCode, point, label)
{
var imgBase = null;
if (palCode > 0)
	imgBase = 'http://maps.google.com/mapfiles/kml/pal' + palCode + '/icon' + iconCode;
else
	imgBase = '/' + golgotha.maps.IMG_PATH + '/maps/pal' + palCode + '/icon' + iconCode;

var icn = new google.maps.MarkerImage(imgBase + '.png', null, null, golgotha.maps.ICON_ANCHOR, golgotha.maps.S_ICON_SIZE);
var shd = new google.maps.MarkerImage(imgBase + 's.png', null, null, golgotha.maps.ICON_ANCHOR, golgotha.maps.S_ICON_SHADOW_SIZE);
var marker = new google.maps.Marker({position:point, icon:icn, shadow:shd, zIndex:golgotha.maps.z.MARKER});
if (label != null) {
	marker.info = label;
	google.maps.event.addListener(marker, 'click', function() { map.infoWindow.setContent(this.info); map.infoWindow.open(map, this); });
}

return marker;
}

function addMarkers(map, arrayName)
{
// Get the map data
var markers = eval(arrayName);
if (!markers) return false;

// Add the map data, either an array or a single element
if (isNaN(markers.length))
	markers.setMap(map);
else if (markers.length > 0) {
	for (var x = 0; x < markers.length; x++)
		markers[x].setMap(map);
}

return true;
}

function removeMarkers(arrayName)
{
// Get the map data
try {
	var markers = eval(arrayName);
	if (!markers) return false;
} catch (err) {
	return false;
}

// Remove the map data, either an array or a single element
if (isNaN(markers.length))
	markers.setMap(null);
else if (markers.length > 0) {
	for (var x = 0; x < markers.length; x++)
		markers[x].setMap(null);
}
	
return true;
}

function getDefaultZoom(distance)
{
if (distance > 6100)
	return 2;
else if (distance > 2900)
	return 3;
else if (distance > 1600)
	return 4;
else if (distance > 780)
	return 5;
else if (distance > 390)
	return 6;
else if (distance > 195)
	return 7;
else if (distance > 90)
	return 8
else if (distance > 50)
	return 9;

return 10;
}

function toggleMarkers(map, arrayName, check)
{
if (map.infoWindow)
	map.infoWindow.close();

// Figure out if we add or remove the markers
if (!check.checked)
	removeMarkers(arrayName);
else
	addMarkers(map, arrayName);

return true;
}

function updateMapText()
{
var newColor = golgotha.maps.TextColor[this.getMapTypeId()];
var elements = getElementsByClass('mapTextLabel');
for (var x = 0; x < elements.length; x++) {
	var el = elements[x];
	el.style.color = newColor;
}

return true;
}

function addOverlay(map, id)
{
var div = (id.style == undefined) ? document.getElementById(id) : id;
if (!div || !map) return false;

// Set z-index
div.style.zIndex = 20;
div.style.position = 'absolute';
	
// Add to div
map.getDiv().firstChild.appendChild(div);
return true;	
}

function updateTab(mrk, ofs, size)
{
if ((ofs < 0) || (ofs > mrk.tabs.length)) ofs = 0;
var tab = mrk.tabs[ofs];
var txt = '<div class="infoTab"';
if (!size) size = mrk.tabSize;
if (size) {
	txt += ' style="width:';
	txt += size.width;
	txt += 'px; height:'
	txt += size.height;
	txt += 'px;"';
	mrk.tabSize = size;
}

txt += '><span class="selectedTabContent">';
txt += tab.content;
txt += '</span><br /><br />';
txt += renderTabChoices(mrk.tabs, ofs);
txt += '</div>';
map.infoWindow.setContent(txt);
return true;
}

function renderTabChoices(tabs, selectedOfs)
{
var txt = '<span class="tabMenu">';
for (var x = 0; x < tabs.length; x++) {
	var tab = tabs[x];
	if (x != selectedOfs) {
		txt += '<a href="javascript:void updateTab(map.infoWindow.marker,' + x + ')">';
		txt += tab.name;
		txt += '</a> ';
	} else
		txt += '<span class="bld">' + tab.name + '<span> '; 
}

txt += '</span>';
return txt;
}
