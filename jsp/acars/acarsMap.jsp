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
<content:sysdata var="imgPath" name="path.img" />
<content:sysdata var="tileHost" name="weather.tileHost" />
<content:sysdata var="refreshInterval" name="acars.livemap.reload" />
<map:api version="2" />
<map:vml-ie />
<c:if test="${!empty tileHost}"><content:js name="acarsMapWX" /></c:if>
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
	var isLoading = getElement('isLoading');
	isLoading.innerHTML = ' - LOADING...';
	var xmlreq = generateXMLRequest();
	xmlreq.send(null);
}

// Set timer to reload the data
if (doRefresh && isAuto)
	window.setTimeout('void reloadData(true)', ${refreshInterval + 2000});

return true;
}

function saveSettings()
{
// Get the latitude, longitude and zoom level
var myLat = map.getCenter().lat();
var myLng = map.getCenter().lng();
var myZoom = map.getZoom();
var myType = 'terrain';
if (map.getCurrentMapType() == G_SATELLITE_MAP)
	myType = 'sat';
else if (map.getCurrentMapType() == G_NORMAL_MAP)
	myType = 'map';

// Save the cookies
var expiryDate = new Date(${cookieExpiry});
document.cookie = 'acarsMapLat=' + myLat + '; expires=' + expiryDate.toGMTString();
document.cookie = 'acarsMapLng=' + myLng + '; expires=' + expiryDate.toGMTString();
document.cookie = 'acarsMapZoomLevel=' + myZoom + '; expires=' + expiryDate.toGMTString();
document.cookie = 'acarsMapType=' + myType + '; expires=' + expiryDate.toGMTString();

// Display confirmation message
alert('Your <content:airline /> ACARS Map preferences have been saved.');
return true;
}

function clearSettings()
{
// Clear cookies
var expiryDate = new Date();
document.cookie = 'acarsMapLat=; expires=' + expiryDate.toGMTString();
document.cookie = 'acarsMapLng=; expires=' + expiryDate.toGMTString();
document.cookie = 'acarsMapZoomLevel=; expires=' + expiryDate.toGMTString();
document.cookie = 'acarsMapType=; expires=' + expiryDate.toGMTString();

// Display confirmation message
alert('Your <content:airline /> ACARS Map preferences have been cleared.');
return true;
}

function showEarth()
{
self.location = '/acars_map_earth.ws';
return true;
}
</script>
<c:if test="${!empty tileHost}"><script src="http://${tileHost}/TileServer/jserieslist.do?function=loadSeries&amp;id=wx" type="text/javascript"></script></c:if>
</head>
<content:copyright visible="false" />
<body onunload="GUnload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:getCookie name="acarsMapZoomLevel" default="5" var="zoomLevel" />
<content:getCookie name="acarsMapType" default="map" var="gMapType" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="acarsMap.do" method="post" validate="return false">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="4"><content:airline /> LIVE ACARS MAP<span id="isLoading" /></td>
</tr>
<tr>
 <td class="label">Map Options</td>
 <td class="data" colspan="3"><span class="bld"><el:box name="showProgress" idx="*" value="1" label="Show Flight Progress" checked="true" />&nbsp;
<el:box name="autoRefresh" idx="*" value="1" label="Automatically Refresh Map" checked="true" />&nbsp;
<el:box name="showInfo" idx="*" value="1" label="Show Flight Data" checked="true" />&nbsp;
<el:box name="showRoute" idx="*" value="1" label="Show Flight Plan" checked="false" /></span></td>
</tr>
<tr>
 <td class="label">Aircraft Legend</td>
 <td class="data"><map:legend color="blue" legend="Cruising" /> <map:legend color="white" legend="On Ground" />
 <map:legend color="orange" legend="Climbing" /> <map:legend color="yellow" legend="Descending" /></td>
 <td class="label">Dispatcher Legend</td>
 <td class="data"><map:legend color="green" legend="Available" /> <map:legend color="purple" legend="Busy" /></td>
</tr>
<tr>
 <td class="label">Dispatch Service</td>
 <td class="data" colspan="3"><span id="dispatchStatus" class="bld caps">DISPATCH CURRENTLY OFFLINE</span></td>
</tr>
<tr>
 <td class="label" valign="top">Live Flight Map</td>
 <td class="data" colspan="3"><map:div ID="googleMap" x="100%" y="545" /><div id="copyright" class="small"></div></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr class="title">
 <td><el:button ID="RefreshButton" className="BUTTON" onClick="void reloadData(false)" label="REFRESH ACARS DATA" />&nbsp;
<el:button ID="SettingsButton" className="BUTTON" onClick="void saveSettings()" label="SAVE SETTINGS" />&nbsp;
<el:button ID="ClearButton" className="BUTTON" onClick="void clearSettings()" label="CLEAR SETTINGS" />&nbsp;
<el:button ID="EarthButton" className="BUTTON" onClick="void showEarth()" label="DISPLAY IN GOOGLE EARTH" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script language="JavaScript" type="text/javascript">
<map:point var="mapC" point="${mapCenter}" />
// Create the map
var map = new GMap2(getElement("googleMap"), {mapTypes:[G_NORMAL_MAP, G_SATELLITE_MAP, G_PHYSICAL_MAP]});
<c:if test="${!empty tileHost}">
// Load the tile overlays
getTileOverlay("radar", 0.45);
getTileOverlay("eurorad", 0.45);
getTileOverlay("sat", 0.35);
getTileOverlay("temp", 0.25);

// Build the layer controls
var xPos = 70;
var rC = new WXOverlayControl("Radar", ["radar", "eurorad"], new GSize(70, 7));
var sC = new WXOverlayControl("Infrared", "sat", new GSize((xPos += 72), 7));
var tC = new WXOverlayControl("Temparture", "temp", new GSize((xPos += 72), 7));
map.addControl(rC);
map.addControl(sC);
map.addControl(tC);
map.addControl(new WXClearControl(new GSize((xPos += 72), 7)));
</c:if>
// Add map controls
map.addControl(new GLargeMapControl());
map.addControl(new GMapTypeControl());
map.setCenter(mapC, ${zoomLevel});
map.enableDoubleClickZoom();
map.enableContinuousZoom();
<map:type map="map" type="${gMapType}" default="G_PHYSICAL_MAP" />
GEvent.addListener(map, 'maptypechanged', updateMapText);

// Placeholder for route
var routeData;
var routeWaypoints;
var acPositions = new Array();
var dcPositions = new Array();

// Reload ACARS data
document.dispatchOnline = false;
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
GEvent.trigger(map, 'maptypechanged');
</c:if></script>
<content:googleAnalytics />
</body>
</map:xhtml>
