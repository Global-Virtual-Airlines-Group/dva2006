<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> ACARS Live Map</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="googleMaps" />
<content:js name="acarsMap" />
<content:googleAnalytics eventSupport="true" />
<content:sysdata var="imgPath" name="path.img" />
<content:sysdata var="tileHost" name="weather.tileHost" />
<content:sysdata var="refreshInterval" name="acars.livemap.reload" />
<map:api version="2" />
<map:vml-ie />
<c:if test="${!empty tileHost}">
<content:js name="acarsMapWX" />
<content:js name="acarsMapFF" />
</c:if>
<script language="JavaScript" type="text/javascript">
document.imgPath = '${imgPath}';
<c:if test="${!empty tileHost}">document.tileHost = '${tileHost}';</c:if>

function reloadData(isAuto)
{
// Get auto refresh
var f = document.forms[0];
var doRefresh = f.autoRefresh.checked;

// Generate XMLHTTPRequest if we're not already viewing a flight
if (!document.pauseRefresh) {
	var xmlreq = generateXMLRequest();
	xmlreq.send(null);
}

// Set timer to reload the data
if (doRefresh && isAuto)
	window.setTimeout('void reloadData(true)', ${refreshInterval + 2000});

return true;
}
</script>
<c:if test="${!empty tileHost}"><script src="http://${tileHost}/TileServer/jserieslist.do?function=loadSeries&amp;id=wx&amp;type=radar,sat,temp,future_radar_ff" type="text/javascript"></script></c:if>
</head>
<content:copyright visible="false" />
<body onunload="GUnload()">
<content:getCookie name="acarsMapZoomLevel" default="5" var="zoomLevel" />
<content:getCookie name="acarsMapType" default="map" var="gMapType" />

<!-- Main Body Frame -->
<el:form action="acarsMap.do" method="post" validate="return false">
<el:table className="form" space="default" pad="default">
<tr>
 <td class="data"><span class="bld"><el:box name="showProgress" idx="*" value="1" label="Show Flight Progress" checked="true" />&nbsp;
<el:box name="autoRefresh" idx="*" value="1" label="Automatically Refresh Map" checked="true" />&nbsp;
<el:box name="showInfo" idx="*" value="1" label="Show Flight Data" checked="true" />&nbsp;
<el:box name="showRoute" idx="*" value="1" label="Show Flight Plan" checked="false" /></span></td>
</tr>
<c:if test="${!isDispatch}">
<tr>
 <td class="data"><map:legend color="blue" legend="Cruising" /> <map:legend color="white" legend="On Ground" />
 <map:legend color="orange" legend="Climbing" /> <map:legend color="yellow" legend="Descending" /></td>
</tr>
</c:if>
<tr>
 <td class="data"><map:div ID="googleMap" x="100%" y="510" /><div id="copyright" class="small"></div></td>
</tr>
</el:table>
<div id="ffSlices" style="visibility:hidden;"><span id="ffLabel" class="small">Select Time</span>
 <el:combo name="ffSlice" size="1" className="small" options="${emptyList}" onChange="void updateFF(this)" /></div>
</el:form>
<content:copyright />
<script language="JavaScript" type="text/javascript">
<map:point var="mapC" point="${mapCenter}" />
// Create the map
var map = new GMap2(getElement("googleMap"), {mapTypes:[G_NORMAL_MAP, G_SATELLITE_MAP, G_PHYSICAL_MAP]});
<c:if test="${!empty tileHost}">
//Load the tile overlays
getTileOverlay("radar", 0.45);
getTileOverlay("eurorad", 0.45);
getTileOverlay("sat", 0.35);
getTileOverlay("temp", 0.25);

// Load the ff tile overlays
var ffLayers = ["future_radar_ff"];
for (var i = 0; i < ffLayers.length; i++) {
	var layerName = ffLayers[i];
	var dates = getFFSlices(layerName);
	document.ffOptions[layerName] = getFFComboOptions(dates);
	for (var x = 0; x < dates.length; x++)
		getFFOverlay(layerName, 0.4, dates[x]);
}

// Build the layer controls
var xPos = 70;
map.addControl(new WXOverlayControl("Radar", ["radar", "eurorad"], new GSize(xPos, 7)));
map.addControl(new WXOverlayControl("Infrared", "sat", new GSize((xPos += 72), 7)));
map.addControl(new WXOverlayControl("Temperature", "temp", new GSize((xPos += 72), 7)));
map.addControl(new FFOverlayControl("Future Radar", "future_radar_ff", new GSize((xPos += 82), 7)));
map.addControl(new WXClearControl(new GSize((xPos += 91), 7)));
</c:if>
// Add map controls
map.addControl(new GLargeMapControl3D());
map.addControl(new GMapTypeControl());
map.setCenter(mapC, ${zoomLevel});
map.enableDoubleClickZoom();
// map.enableContinuousZoom();
<map:type map="map" type="${gMapType}" default="G_PHYSICAL_MAP" />
GEvent.addListener(map, 'maptypechanged', updateMapText);
GEvent.addListener(map, 'maptypechanged', hideAllSlices);

// Placeholder for route
var routeData;
var routeWaypoints;
var acPositions = [];
var dcPositions = [];

// Reload ACARS data
document.doRefresh = true;
reloadData(true);
<c:if test="${!empty tileHost}">
// Display the copyright notice
var d = new Date();
var cp = document.getElementById("copyright");
cp.innerHTML = 'Weather Data &copy; ' + d.getFullYear() + ' The Weather Channel.'
var cpos = new GControlPosition(G_ANCHOR_BOTTOM_RIGHT, new GSize(4, 16));
cpos.apply(cp);
mapTextElements.push(cp);
map.getContainer().appendChild(cp);

// Initialize FastForward elements
var ffs = document.getElementById("ffSlices");
var ffpos = new GControlPosition(G_ANCHOR_TOP_RIGHT, new GSize(8, 30));
ffpos.apply(ffs);
map.getContainer().appendChild(ffs);
var ffl = document.getElementById("ffLabel");
mapTextElements.push(ffl);

GEvent.trigger(map, 'maptypechanged');
</c:if></script>
</body>
</map:xhtml>
