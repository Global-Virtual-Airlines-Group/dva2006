<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<map:xhtml>
<head>
<title><content:airline /> ACARS Live Map</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="acarsMap" />
<content:googleAnalytics eventSupport="true" />
<content:sysdata var="tileHost" name="weather.tileHost" />
<content:sysdata var="multiHost" name="weather.multiHost" />
<content:sysdata var="refreshInterval" name="acars.livemap.reload" />
<map:api version="3" />
<c:if test="${!empty tileHost}">
<content:js name="acarsMapWX" />
<content:js name="acarsMapFF" />
<content:js name="progressBar" />
</c:if>
<script type="text/javascript">
function reloadData(isAuto)
{
// Get auto refresh
var f = document.forms[0];
var doRefresh = f.autoRefresh.checked;

// Generate XMLHTTPRequest if we're not already viewing a flight
if (!document.pauseRefresh) {
	var isLoading = document.getElementById('isLoading');
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
if (map.getMapTypeId() == google.maps.MapTypeId.SATELLITE)
	myType = 'sat';
else if (map.getMapTypeId() == google.maps.MapTypeId.ROADMAP)
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
<map:wxList layers="radar,eurorad,sat,temp,future_radar_ff" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:empty var="emptyList" />
<content:getCookie name="acarsMapZoomLevel" default="5" var="zoomLevel" />
<content:getCookie name="acarsMapType" default="map" var="gMapType" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="acarsMap.do" method="get" validate="return false">
<el:table className="form">
<tr class="title">
 <td colspan="2" class="caps"><content:airline /> LIVE ACARS MAP<span id="isLoading"></span></td>
 <td colspan="2" class="right"><span id="userSelect" style="display:none;"> ZOOM TO <el:combo ID="usrID" name="usrID" idx="*" options="${emptyList}" firstEntry="-" onChange="void zoomTo(this)" /></span></td>
</tr>
<tr>
 <td class="label">Map Options</td>
 <td class="data" colspan="3"><span class="bld"><el:box name="showProgress" idx="*" value="1" label="Show Flight Progress" checked="true" />&nbsp;
<el:box name="autoRefresh" idx="*" value="true" label="Automatically Refresh Map" checked="true" />&nbsp;
<el:box name="showInfo" idx="*" value="true" label="Show Flight Data" checked="true" />&nbsp;
<el:box name="showRoute" idx="*" value="true" label="Show Flight Plan" checked="false" />
<el:box name="zoomToPilot" idx="*" value="true" label="Zoom to Pilot" checked="false" /></span></td>
</tr>
<tr>
 <td class="label">Aircraft Legend</td>
 <td class="data" width="45%"><map:legend color="blue" legend="Cruising" /> <map:legend color="white" legend="On Ground" />
 <map:legend color="orange" legend="Climbing" /> <map:legend color="yellow" legend="Descending" /></td>
 <td class="label">Dispatcher Legend</td>
 <td class="data"><map:legend color="green" legend="Available" /> <map:legend color="purple" legend="Busy" /></td>
</tr>
<tr>
 <td class="label">Dispatch Service</td>
 <td class="data"><span id="dispatchStatus" class="bld caps">DISPATCH CURRENTLY OFFLINE</span></td>
 <td class="label">Weather Layer</td>
 <td class="data"><span id="wxLoading" class="small" style="width:150px;">None</span></td>
</tr>
<tr>
 <td class="data" colspan="4"><map:div ID="googleMap" x="100%" y="550" /><div id="ffSlices" style="top:30px; right:7px; visibility:hidden;">
 <span id="ffLabel" class="small bld mapTextLabel">Select Time</span> <el:combo name="ffSlice" size="1" className="small" options="${emptyList}" onChange="void updateFF(this)" />
 <el:button ID="AnimateButton" label="ANIMATE" onClick="void animateFF()" /></div></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr class="title">
 <td><el:button ID="RefreshButton" onClick="void reloadData(false)" label="REFRESH ACARS DATA" />&nbsp;
<el:button ID="SettingsButton" onClick="void saveSettings()" label="SAVE SETTINGS" />&nbsp;
<el:button ID="ClearButton" onClick="void clearSettings()" label="CLEAR SETTINGS" />&nbsp;
<el:button ID="EarthButton" onClick="void showEarth()" label="DISPLAY IN GOOGLE EARTH" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<div id="copyright" class="small mapTextLabel" style="bottom:20px; right:2px; visibility:hidden;"></div>
<script type="text/javascript">
<map:point var="mapC" point="${mapCenter}" />
var mapTypes = {mapTypeIds: golgotha.maps.DEFAULT_TYPES};
var mapOpts = {center:mapC, minZoom:2, maxZoom:18, zoom:${zoomLevel}, scrollwheel:false, streetViewControl:false, mapTypeControlOptions: mapTypes};

// Create the map
var map = new google.maps.Map(document.getElementById('googleMap'), mapOpts);
map.getOptions = function() { return mapOpts; };
<c:if test="${!empty tileHost}">
// Load the tile overlays
getTileOverlay('radar', 0.45);
getTileOverlay('eurorad', 0.45);
getTileOverlay('sat', 0.35);
getTileOverlay('temp', 0.25);

// Load the ff tile overlays
var ffLayers = ['future_radar_ff'];
for (var i = 0; i < ffLayers.length; i++) {
	var layerName = ffLayers[i];
	var dates = getFFSlices(layerName);
	document.ffSlices[layerName] = dates;
	document.ffOptions[layerName] = getFFComboOptions(dates);
	for (var x = 0; x < dates.length; x++)
		getFFOverlay(layerName, 0.45, dates[x]);
}

// Build the layer controls
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXOverlayControl('Radar', ['radar', 'eurorad']));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXOverlayControl('Infrared', 'sat'));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXOverlayControl('Temperature', 'temp'));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new FFOverlayControl('Future Radar', 'future_radar_ff'));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXClearControl());

// Display the copyright notice
var d = new Date();
var cp = document.getElementById('copyright');
cp.innerHTML = 'Weather Data &copy; ' + d.getFullYear() + ' The Weather Channel.'
</c:if>
// Add map controls
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map.infoWindow, 'closeclick', infoClose);
google.maps.event.addListener(map, 'click', infoClose);
var pBar = progressBar({strokeWidth:225, strokeColor:'#0020ff'});
pBar.getDiv().style.right = '4px';
pBar.getDiv().style.top = '50px';
google.maps.event.addListener(map, 'maptypeid_changed', golgotha.maps.updateMapText);

// Placeholders for route/positions
var routeData;
var routeWaypoints;
var acPositions = [];
var dcPositions = [];

// Reload ACARS data
document.dispatchOnline = false;
document.doRefresh = true;
reloadData(true);
<c:if test="${!empty tileHost}">
// Initialize FastForward elements
google.maps.event.addListener(map, 'maptypeid_changed', hideAllSlices);
google.maps.event.addListenerOnce(map, 'tilesloaded', function() { 
	addOverlay(map, 'ffSlices'); 
	addOverlay(map, 'copyright'); 
	addOverlay(map, pBar.getDiv());
	google.maps.event.trigger(this, 'maptypeid_changed');
});
</c:if></script>
</body>
</map:xhtml>
