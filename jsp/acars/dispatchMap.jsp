<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> ACARS Dispatch Map</title>
<content:css name="dispatchMap" scheme="legacy" />
<content:pics />
<map:api version="2" />
<content:js name="googleMaps" />
<content:js name="acarsMapWX" />
<content:sysdata var="imgPath" name="path.img" />
<content:sysdata var="tileHost" name="weather.tileHost" />
<script src="http://${tileHost}/TileServer/jserieslist.do?function=loadSeries&amp;id=wx" type="text/javascript"></script>
<map:vml-ie />
<script language="JavaScript" type="text/javascript">
document.imgPath = '${imgPath}';
document.tileHost = '${tileHost}';
<c:if test="${isDispatch}">
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
if (this.isSelected) {
	if (map.getZoom() <= 7)
		return;
	newLine = new GPolyline(aw_points, "#8090A0", 1.5, 0.45, {geodesic:false});
} else {
	newLine = new GPolyline(aw_points, "#a0c0ff", 2.25, 0.8, {geodesic:false});
	newLine.isSelected = true;
}

newLine.AirwayID = this.AirwayID;
newLine.infoShow = clickLine;
map.removeOverlay(this);
map.addOverlay(newLine);
airways[this.AirwayID] = newLine;
GEvent.bind(newLine, 'click', newLine, newLine.infoShow)
return true;
}
</c:if>
function mapZoom()
{
var b = map.getBounds();
window.external.doPan(b.getNorthEast().lat(), b.getSouthWest().lng(), b.getSouthWest().lat(), b.getNorthEast().lng(), map.getZoom());
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
mrk.infoShow = clickIcon;
GEvent.bind(mrk, 'click', mrk, mrk.infoShow);
return mrk;
}
</script>
</head>
<body onunload="GUnload()">
<map:div ID="googleMap" x="100%" y="625" /><div id="copyright" class="sec bld"></div>
<script language="JavaScript" type="text/javascript">
// Load the map
map = new GMap2(document.getElementById('googleMap'), {mapTypes:[G_NORMAL_MAP, G_SATELLITE_MAP, G_PHYSICAL_MAP]});
map.addControl(new GLargeMapControl());
map.addControl(new GMapTypeControl());

// Display the map
map.setCenter(new GLatLng(36.44, -100.14), 6);
map.setMapType(G_SATELLITE_MAP);
map.enableDoubleClickZoom();
map.enableContinuousZoom();
map.enableScrollWheelZoom();

// Build the layer controls
var xPos = 70;
var rC = new WXOverlayControl(getTileOverlay("radar", 0.5), "Radar", new GSize(70, 3));
var srC = new WXOverlayControl(getTileOverlay("satrad", 0.4), "Sat/Rad", new GSize((xPos += 72), 3));
var sC = new WXOverlayControl(getTileOverlay("sat", 0.4), "Infrared", new GSize((xPos += 72), 3));
var tC = new WXOverlayControl(getTileOverlay("temp", 0.3), "Temparture", new GSize((xPos += 72), 3));
map.addControl(rC);
map.addControl(srC);
map.addControl(sC);
map.addControl(tC);
map.addControl(new WXClearControl(new GSize((xPos += 72), 3)));

// Display the copyright notice
var d = new Date();
var cp = document.getElementById("copyright");
cp.innerHTML = 'Weather Data &copy; ' + d.getFullYear() + ' The Weather Channel.'
var cpos = new GControlPosition(G_ANCHOR_BOTTOM_LEFT, new GSize((xPos += 72), 8));
cpos.apply(cp);
map.getContainer().appendChild(cp);

// Initialize arrays and collection
var route = new Array();
var routePoints = new Array();
var sidLine;
var starLine;
var routeLine;
var aL;

// Marker managers for navigation aids
var mm_vor = new GMarkerManager(map);
var mm_ndb = new GMarkerManager(map);
var mm_int = new GMarkerManager(map);

// Navaid marker arrays
var airways = new Array();
var mrks_aw = new Array();
var mrks_vor = new Array();
var mrks_ndb = new Array();
var mrks_int = new Array();
var mrks_sid = new Array();
var mrks_star = new Array();

// Initialize event listeners
GEvent.addListener(map, "moveend", mapZoom);
</script>
</body>
</map:xhtml>
