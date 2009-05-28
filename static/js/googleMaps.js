var defaultIconSize = 24;
var displayedMarkers = [];
var mapTextElements = [];

var ge;
var kmlProgress = null;
function getEarthInstanceCB(object)
{
ge = object;
return true;
}

function googleMarker(imgPath, color, point, label)
{
if (color == 'null')
	return point;

var icon = new GIcon();
icon.image = '/' + imgPath + '/maps/point_' + color + '.png';
icon.shadow = '/' + imgPath + '/maps/shadow.png';
icon.iconSize = new GSize(12, 20);
icon.shadowSize = new GSize(22, 20);
icon.iconAnchor = new GPoint(6, 20);
icon.infoWindowAnchor = new GPoint(5, 1);
var marker = new GMarker(point, icon);
if (label != null)
	GEvent.addListener(marker, 'click', function() { marker.openInfoWindowHtml(label); });

return marker;
}

function googleIconMarker(palCode, iconCode, point, label)
{
var imgBase = null; var icon = new GIcon();
if (palCode > 0)
	imgBase = 'http://maps.google.com/mapfiles/kml/pal' + palCode + '/icon' + iconCode;
else
	imgBase = '/' + document.imgPath + '/maps/pal' + palCode + '/icon' + iconCode;

icon.image = imgBase + '.png';
icon.shadow = imgBase + 's.png';
icon.iconSize = new GSize(defaultIconSize, defaultIconSize);
icon.shadowSize = new GSize(defaultIconSize * (59 / 32), defaultIconSize);
icon.iconAnchor = new GPoint(defaultIconSize / 2, defaultIconSize / 2);
icon.infoWindowAnchor = new GPoint(defaultIconSize * 0.75, defaultIconSize * 0.25);
var marker = new GMarker(point, icon);
if (label != null)
	GEvent.addListener(marker, 'click', function() { marker.openInfoWindowHtml(label); });

return marker;
}

function addMarkers(map, arrayName)
{
// Get the map data
var markers = eval(arrayName);
if (!markers) return false;

// Add the map data, either an array or a single element
if (isNaN(markers.length))
	map.addOverlay(markers);
else if (markers.length > 0) {
	for (var x = 0; x < markers.length; x++)
		map.addOverlay(markers[x]);
}

displayedMarkers[arrayName] = true;
return true;
}

function removeMarkers(map, arrayName)
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
	map.removeOverlay(markers);
else if (markers.length > 0) {
	for (var x = 0; x < markers.length; x++)
		map.removeOverlay(markers[x]);
}
	
displayedMarkers[arrayName] = false;
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
// Figure out if we add or remove the markers
var isToggled = !check.checked;
if (isToggled)
	removeMarkers(map, arrayName);
else
	addMarkers(map, arrayName);

return true;
}

function updateMapText()
{
var newColor = this.getCurrentMapType().getTextColor();
for (var x = 0; x < mapTextElements.length; x++) {
	var el = mapTextElements[x];
	el.style.color = newColor;
}

return true;
}
