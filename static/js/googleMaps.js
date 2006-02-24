var displayedMarkers = new Array();

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

function addMarkers(map, arrayName)
{
// Get the map data
var markers = eval(arrayName);
if (!markers) return false;

// Add the map data, either an array or a single element
if (isNaN(markers.length)) {
	map.addOverlay(markers);
} else if (markers.length > 0) {
	for (x = 0; x < markers.length; x++)
		map.addOverlay(markers[x]);
}

displayedMarkers[arrayName] = true;
return true;
}

function removeMarkers(map, arrayName)
{
// Get the map data
var markers = eval(arrayName);
if (!markers) return false;

// Remove the map data, either an array or a single element
if (isNaN(markers.length)) {
	map.removeOverlay(markers);
} else if (markers.length > 0) {
	for (x = 0; x < markers.length; x++)
		map.removeOverlay(markers[x]);
}
	
displayedMarkers[arrayName] = false;
return true;
}

function getDefaultZoom(distance)
{
if (distance > 6100) {
	return 15;
} else if (distance > 2900) {
	return 14;
} else if (distance > 1600) {
	return 13;
} else if (distance > 780) {
	return 12;
} else if (distance > 390) {
	return 11;
} else if (distance > 195) {
	return 10;
} else if (distance > 90) {
	return 9;
} else if (distance > 50) {
	return 8;
}

return 7;
}

function toggleMarkers(map, arrayName)
{
// Figure out if we add or remove the markers
var isToggled = displayedMarkers[arrayName];
if (isToggled) {
	removeMarkers(map, arrayName);
} else {
	addMarkers(map, arrayName);
}

updateOverlays();
return true;
}

function updateOverlays()
{
var page = Math.floor(map.getCenterLatLng().x / 360) * 360;
var ppage = page * 100000;

// Update the map overlays
for (var x = 0; x < map.overlays.length; x++) {
	var ov = map.overlays[x];
	if (ov.point) { // GPoint
		while (ov.point.x < page)
			ov.point.x += 360;

		while (ov.point.x > (page + 360))
			ov.point.x -= 360;

		ov.redraw(true);
	} else if (ov.points) { // GPolyline
		for (var p = 1; p < ov.points.length; p += 2) {
			while (ov.points[p] < ppage) 
				ov.points[p] += 36000000;

			while (ov.points[p] > (ppage + 36000000))
				ov.points[p] -= 36000000;
		}

		ov.redraw(true);	
	}
}

return true;
}
