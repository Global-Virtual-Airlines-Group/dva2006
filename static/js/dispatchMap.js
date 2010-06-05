function mapZoom()
{
gaEvent('Dispatch', 'Zoom/Pan');
var b = map.getBounds();
window.external.doPan(b.getNorthEast().lat(), b.getSouthWest().lng(), b.getSouthWest().lat(), b.getNorthEast().lng(), map.getZoom());
return true;
}

function addWaypoint(code)
{
document.currentmarker.closeInfoWindow();
window.external.addWaypoint(code);
map.removeOverlay(document.currentmarker);
gaEvent('Dispatch', 'Add Waypoint', code);
return true;
}

function delWaypoint(code)
{
document.currentmarker.closeInfoWindow();
window.external.deleteWaypoint(code);
map.removeOverlay(document.currentmarker);
gaEvent('Dispatch', 'Remove Waypoint', code);
return true;
}

function toggleMarkers(mrks, visible)
{
if (mrks == null) return false;
for (var idx = 0; idx < mrks.length; idx++)
{
	var m = mrks[idx];
	if (visible)
		m.show();
	else if (!m.isSelected)
		m.hide();
}

return true;
}
