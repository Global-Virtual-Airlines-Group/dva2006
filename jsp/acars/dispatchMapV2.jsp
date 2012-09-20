<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Dispatch Map</title>
<meta http-equiv="cache-control" content="no-cache" />
<content:css name="dispatchMap" scheme="legacy" />
<content:pics />
<content:js name="common" />
<map:api version="2" />
<content:googleAnalytics eventSupport="true" />
<content:sysdata var="imgPath" name="path.img" />
<map:vml-ie />
<script type="text/javascript">
document.imgPath = '${imgPath}';
function updateZoomLevel(oldZoom, newZoom)
{
var level = document.getElementById('zoomLevel');
if (level)
	level.innerHTML = 'Zoom Level ' + newZoom;

return true;
}

function clickIcon()
{
if (this.uniqueID)
	this.openInfoWindowHtml(window.external.GetMarkerMessage(this.uniqueID));

document.currentmarker = this;
return true;
}

function externalMarker(color, point, id)
{
var mrk = googleMarker(document.imgPath, color, point, null);
mrk.uniqueID = id;
GEvent.bind(mrk, 'click', mrk, clickIcon);
return mrk;
}

function externalIconMarker(palCode, iconCode, point, id)
{
var mrk = googleIconMarker(palCode, iconCode, point, null);
mrk.uniqueID = id;
GEvent.bind(mrk, 'click', mrk, clickIcon);
return mrk;
}
</script>
<c:if test="${isDispatch}">
<script type="text/javascript">
function mapZoom()
{
gaEvent('Dispatch', 'Zoom/Pan');
var b = map.getBounds();
window.external.doPan(b.getNorthEast().lat(), b.getSouthWest().lng(), b.getSouthWest().lat(), b.getNorthEast().lng(), map.getZoom());
return true;
}

function toggleObjects(mrks, visible)
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
</script></c:if>
</head>
<body onunload="GUnload()">
<map:div ID="googleMap" x="100%" y="625" />
<script type="text/javascript">
// Load the map
map = new GMap2(document.getElementById('googleMap'), {mapTypes:[G_NORMAL_MAP, G_SATELLITE_MAP, G_PHYSICAL_MAP]});
map.addControl(new GLargeMapControl3D());
map.addControl(new GMapTypeControl());

// Display the map
map.setCenter(new GLatLng(36.44, -100.14), 6);
map.enableContinuousZoom();
map.enableScrollWheelZoom();
map.enableDoubleClickZoom();
<map:type map="map" type="${gMapType}" default="G_SATELLITE_MAP" />

// Initialize event listeners
GEvent.addListener(map, 'maptypechanged', updateMapText);
<c:if test="${isDispatch}">
GEvent.addListener(map, "moveend", mapZoom);
GEvent.trigger(map, "moveend");</c:if>

// Initialize arrays and collection
var route = [];
var routePoints = [];
var sidLine;
var starLine;
var routeLine;
var aL;
var mrks_sid = [];
var mrks_star = [];

GEvent.trigger(map, 'maptypechanged');
</script>
</body>
</html>
