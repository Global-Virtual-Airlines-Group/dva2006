function mapZoom()
{
var b = map.getBounds();
window.external.doPan(b.getNorthEast().lat(), b.getSouthWest().lng(), b.getSouthWest().lat(), b.getNorthEast().lng(), map.getZoom());
return true;
}

function addWaypoint(code)
{
document.currentmarker.closeInfoWindow();
window.external.addWaypoint(code);
map.removeOverlay(document.currentmarker);
return true;
}

function delWaypoint(code)
{
document.currentmarker.closeInfoWindow();
window.external.deleteWaypoint(code);
map.removeOverlay(document.currentmarker);
return true;
}

function toggleMarkers(mrks, visible)
{
for (var idx in mrks)
{
	var m = mrks[idx];
	if (visible)
		m.show();
	else if (!m.isSelected)
		m.hide();
}

return true;
}

function clickLine(latlng)
{
var newLine;
var aw_points = mrks_aw[this.AirwayID];
var oldLine = airways[this.AirwayID];
if (this.isSelected) {
	if (map.getZoom() <= 7)
		return;
	
	map.closeInfoWindow();
	newLine = new GPolyline(aw_points, "#8090A0", 1.5, 0.45, {geodesic:false});
	for (var x = 0; x < oldLine.markers.length; x++)
		mm_aw.removeMarker(oldLine.markers[x]);

	window.external.ShowAirwayInput(false, null);
} else {
	newLine = new GPolyline(aw_points, "#A0C0FF", 2.25, 0.8, {geodesic:false});
	newLine.isSelected = true;
	for (var x = 0; x < oldLine.markers.length; x++)
		mm_aw.addMarker(oldLine.markers[x], 7);

	selectedAirways.push(newLine);
	map.openInfoWindowHtml(latlng, window.external.GetAirwayMessage(this.AirwayID), { maxWidth : 275 });
	window.external.ShowAirwayInput(true, this.AirwayID);
}

// Add the airway line
newLine.AirwayID = this.AirwayID;
newLine.markers = oldLine.markers;
map.removeOverlay(this);
map.addOverlay(newLine);
airways[this.AirwayID] = newLine;
GEvent.bind(newLine, 'click', newLine, clickLine)
return true;
}

function clearSelectedAirways()
{
mm_aw.clearMarkers();
for (var x = 0; x < selectedAirways.length; x++)
{
	var aw = selectedAirways[x];
	var oldLine = airways[this.AirwayID];
	var aw_points = mrks_aw[this.AirwayID];
	newLine = new GPolyline(aw_points, "#8090A0", 1.5, 0.45, {geodesic:false});
	newLine.markers = oldLine.markers;
	map.removeOverlay(oldLine);
	map.addOverlay(newLine);
}

return true;
}
