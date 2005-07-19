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
}

return 8;
}
