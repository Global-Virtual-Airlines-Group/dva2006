<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> ACARS Dispatch Map</title>
<content:css name="dispatchMap" scheme="legacy" />
<content:pics />
<map:api version="2" />
<content:js name="common" />
<content:js name="googleMaps" />
<content:js name="acarsMapWX" />
<content:js name="acarsMapFF" />
<c:if test="${isDispatch}"><content:js name="dispatchMap" /></c:if>
<content:js name="markermanager" />
<content:sysdata var="imgPath" name="path.img" />
<content:sysdata var="tileHost" name="weather.tileHost" />
<c:if test="${!empty tileHost}">
<script src="http://${tileHost}/TileServer/jserieslist.do?function=loadSeries&amp;id=wx&amp;type=radar,sat,temp,future_radar_ff" type="text/javascript"></script></c:if>
<map:vml-ie />
<script language="JavaScript" type="text/javascript">
document.imgPath = '${imgPath}';
document.tileHost = '${tileHost}';

function updateZoomLevel(oldZoom, newZoom)
{
var level = document.getElementById("zoomLevel");
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
</head>
<body onunload="GUnload()">
<el:form action="dispatchMap.do" method="post" validate="return false">
<map:div ID="googleMap" x="100%" y="625" /><div id="copyright" class="small"></div><div id="zoomLevel" class="small"></div>
<div id="ffSlices" style="visibility:hidden;"><span id="ffLabel" class="small bld">Select Time</span>
 <el:combo name="ffSlice" size="1" className="small" options="${emptyList}" onChange="void updateFF(this)" />
 <el:button ID="AnimateButton" className="BUTTON" label="ANIMATE" onClick="void animateFF()" /></div>
</el:form>
<script language="JavaScript" type="text/javascript">
// Load the map
map = new GMap2(document.getElementById('googleMap'), {mapTypes:[G_NORMAL_MAP, G_SATELLITE_MAP, G_PHYSICAL_MAP]});
map.addControl(new GLargeMapControl());
map.addControl(new GMapTypeControl());

// Display the map
map.setCenter(new GLatLng(36.44, -100.14), 6);
// map.enableContinuousZoom();
map.enableScrollWheelZoom();
map.enableDoubleClickZoom();
<map:type map="map" type="${gMapType}" default="G_SATELLITE_MAP" />

<c:if test="${!empty tileHost}">
//Load the tile overlays
getTileOverlay("radar", 0.45);
getTileOverlay("eurorad", 0.45);
getTileOverlay("sat", 0.35);
getTileOverlay("temp", 0.25);

//Load the ff tile overlays
var ffLayers = ["future_radar_ff"];
for (var i = 0; i < ffLayers.length; i++) {
	var layerName = ffLayers[i];
	var dates = getFFSlices(layerName);
	document.ffSlices[layerName] = dates;
	document.ffOptions[layerName] = getFFComboOptions(dates);
	for (var x = 0; x < dates.length; x++)
		getFFOverlay(layerName, 0.45, dates[x]);
}

// Build the layer controls
var xPos = 70;
map.addControl(new WXOverlayControl("Radar", ["radar", "eurorad"], new GSize(xPos, 4)));
map.addControl(new WXOverlayControl("Clouds", "sat", new GSize((xPos += 72), 4)));
map.addControl(new WXOverlayControl("Temperature", "temp", new GSize((xPos += 72), 4)));
map.addControl(new FFOverlayControl("Future Radar", "future_radar_ff", new GSize((xPos += 81), 4)));
map.addControl(new WXClearControl(new GSize((xPos += 92), 4)));

// Display the copyright notice
var d = new Date();
var cp = document.getElementById("copyright");
cp.innerHTML = 'Weather Data &copy; ' + d.getFullYear() + ' The Weather Channel.'
var cpos = new GControlPosition(G_ANCHOR_BOTTOM_RIGHT, new GSize(2, 18));
cpos.apply(cp);
var zl = document.getElementById("zoomLevel");
zl.innerHTML = 'Zoom Level ' + map.getZoom();
var zpos = new GControlPosition(G_ANCHOR_TOP_LEFT, new GSize(60, 4));
zpos.apply(zl);
mapTextElements.push(cp);
mapTextElements.push(zl);
map.getContainer().appendChild(cp);
map.getContainer().appendChild(zl);

//Initialize FastForward elements
var ffs = document.getElementById("ffSlices");
var ffpos = new GControlPosition(G_ANCHOR_TOP_RIGHT, new GSize(8, 30));
ffpos.apply(ffs);
map.getContainer().appendChild(ffs);
var ffl = document.getElementById("ffLabel");
mapTextElements.push(ffl);

// Initialize event listeners
GEvent.addListener(map, 'maptypechanged', updateMapText);
GEvent.addListener(map, 'zoomend', updateZoomLevel);
GEvent.addListener(map, 'maptypechanged', hideAllSlices);
</c:if>
// Initialize arrays and collection
var route = new Array();
var routePoints = new Array();
var sidLine;
var starLine;
var routeLine;
var aL;
var findMrk;

// Marker managers for navigation aids
var mm_vor = new MarkerManager(map);
var mm_ndb = new MarkerManager(map);
var mm_int = new MarkerManager(map);
var mm_aw = new MarkerManager(map, {maxZoom:14, borderPadding:32});

// Navaid marker arrays
var airways = new Array();
var mrks_aw = new Array();
var mrks_vor = new Array();
var mrks_ndb = new Array();
var mrks_int = new Array();
var mrks_sid = new Array();
var mrks_star = new Array();
var selectedAirways = new Array();

<c:if test="${isDispatch}">
GEvent.addListener(map, "moveend", mapZoom);
GEvent.trigger(map, "moveend");
</c:if>
GEvent.trigger(map, 'maptypechanged');
</script>
</body>
</map:xhtml>
