<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Live Map</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="acarsMap" />
<content:googleAnalytics eventSupport="true" />
<content:sysdata var="tileHost" name="weather.tileHost" />
<content:sysdata var="refreshInterval" name="acars.livemap.reload" />
<map:api version="3" libraries="weather" />
<content:js name="progressBar" />
<content:js name="googleMapsWX" />
<c:if test="${!empty tileHost}">
<script type="text/javascript">
var gsLoader;
gsLoader = new golgotha.maps.GinsuLoader(2);
gsLoader.setData('radar', 0.4, 'wxRadar');
gsLoader.setData('eurorad', 0.4, 'wxRadar');
gsLoader.setData('temp', 0.275, 'wxTemp');
gsLoader.setData('windspeed', 0.325, 'wxWind');
gsLoader.setData('future_radar_ff', 0.375, 'ffRadar');
</script>
<map:wxList layers="radar,eurorad,temp,windspeed,future_radar_ff" function="gsLoader.load" max="8" /></c:if>
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
 <td class="data" style="width:45%;"><map:legend color="blue" legend="Cruising" /> <map:legend color="white" legend="On Ground" />
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
 <td class="data" colspan="4"><map:div ID="googleMap" x="100%" y="550" /><div id="copyright" class="small mapTextLabel"></div>
<div id="mapStatus" class="small mapTextLabel"></div></td>
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
<script type="text/javascript">
<map:point var="mapC" point="${mapCenter}" />
var mapTypes = {mapTypeIds:golgotha.maps.DEFAULT_TYPES};
var mapOpts = {center:mapC, minZoom:2, maxZoom:17, zoom:${zoomLevel}, scrollwheel:false, streetViewControl:false, mapTypeControlOptions:mapTypes};

// Create the map
var map = new google.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map.infoWindow, 'closeclick', infoClose);
google.maps.event.addListener(map, 'click', infoClose);
google.maps.event.addListener(map, 'zoom_changed', golgotha.maps.updateZoom);
google.maps.event.addListener(map, 'maptypeid_changed', golgotha.maps.updateMapText);
map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(document.getElementById('copyright'));
map.controls[google.maps.ControlPosition.LEFT_BOTTOM].push(document.getElementById('mapStatus'));
<c:if test="${!empty tileHost}">
// Build the layer controls
var ff = gsLoader.combine(8, 'radar', 'future_radar_ff');
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerSelectControl(map, 'Radar', [gsLoader.getLatest('radar'), gsLoader.getLatest('eurorad')]));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerSelectControl(map, 'Temperature', gsLoader.getLatest('temp')));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerSelectControl(map, 'Wind Speed', gsLoader.getLatest('windspeed')));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerAnimateControl(map, 'Radar Loop', ff, 333));
map.controls[google.maps.ControlPosition.TOP_CENTER].push(golgotha.maps.util.progress.getDiv());
</c:if>
// Add other layers
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerSelectControl(map, 'Clouds', new google.maps.weather.CloudLayer()));
var k = new google.maps.KmlLayer(self.location.protocol + '//' + self.location.host + '/servinfo/firs.kmz', {preserveViewport:true, clickable:false});
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerSelectControl(map, 'FIRs', k));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerClearControl(map));
google.maps.event.trigger(this, 'maptypeid_changed');
google.maps.event.trigger(map, 'zoom_changed');

// Placeholders for route/positions
var routeData;
var routeWaypoints;
var acPositions = [];
var dcPositions = [];

// Reload ACARS data
document.dispatchOnline = false;
document.doRefresh = true;
reloadData(true);
</script>
</body>
</html>
