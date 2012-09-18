<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Live Map</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<map:api version="3" libraries="weather" />
<content:js name="acarsMap" />
<content:js name="progressBar" />
<content:js name="googleMapsWX" />
<content:googleAnalytics eventSupport="true" />
<content:sysdata var="tileHost" name="weather.tileHost" />
<content:sysdata var="refreshInterval" name="acars.livemap.reload" />
<c:if test="${!empty tileHost}">
<script type="text/javascript">
var gsLoader;
gsLoader = new golgotha.maps.GinsuLoader(2);
gsLoader.setData('radar', 0.4, 'wxRadar');
gsLoader.setData('eurorad', 0.4, 'wxRadar');
gsLoader.setData('temp', 0.275, 'wxTemp');
gsLoader.setData('future_radar_ff', 0.375, 'ffRadar');
</script>
<map:wxList layers="radar,eurorad,temp,future_radar_ff" function="gsLoader.load" max="8" /></c:if>
<script type="text/javascript">
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
</head>
<content:copyright visible="false" />
<body>
<content:empty var="emptyList" />
<content:getCookie name="acarsMapZoomLevel" default="5" var="zoomLevel" />
<content:getCookie name="acarsMapType" default="map" var="gMapType" />

<!-- Main Body Frame -->
<el:form action="acarsMap.do" method="get" validate="return false">
<el:table className="form">
<tr>
 <td class="data"><span class="bld"><el:box name="showProgress" idx="*" value="1" label="Show Flight Progress" checked="true" />&nbsp;
<el:box name="autoRefresh" idx="*" value="1" label="Automatically Refresh Map" checked="true" />&nbsp;
<el:box name="showInfo" idx="*" value="1" label="Show Flight Data" checked="true" />&nbsp;
<el:box name="showRoute" idx="*" value="1" label="Show Flight Plan" checked="false" /></span></td>
</tr>
<c:if test="${!isDispatch}">
<tr>
 <td class="data"><map:legend color="blue" legend="Cruising" /> <map:legend color="white" legend="On Ground" />
 <map:legend color="orange" legend="Climbing" /> <map:legend color="yellow" legend="Descending" />
 <map:legend color="green" legend="Available Dispatcher" /> <map:legend color="purple" legend="Busy Dispatcher" /></td>
</tr>
</c:if>
<tr>
 <td class="data"><map:div ID="googleMap" x="100%" y="510" /><div id="copyright" class="small mapTextLabel"></div>
<div id="mapStatus" class="small mapTextLabel"></div></td>
</tr>
</el:table>
</el:form>
<content:copyright />
<script type="text/javascript">
<map:point var="mapC" point="${mapCenter}" />
var mapTypes = {mapTypeIds:golgotha.maps.DEFAULT_TYPES};
var mapOpts = {center:mapC, minZoom:2, zoom:${zoomLevel}, maxZoom:17, scrollwheel:false, streetViewControl:false, mapTypeControlOptions:mapTypes};

// Create the map
var map = new google.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map.infoWindow, 'closeclick', infoClose);
google.maps.event.addListener(map, 'click', infoClose);
google.maps.event.addListener(map, 'maptypeid_changed', golgotha.maps.updateMapText);
map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(document.getElementById('copyright'));
map.controls[google.maps.ControlPosition.LEFT_BOTTOM].push(document.getElementById('mapStatus'));
<c:if test="${!empty tileHost}">
// Build the layer controls
var ff = gsLoader.combine(8, 'radar', 'future_radar_ff');
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerSelectControl(map, 'Radar', [gsLoader.getLatest('radar'), gsLoader.getLatest('eurorad')]));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerSelectControl(map, 'Temperature', gsLoader.getLatest('temp')));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerAnimateControl(map, 'Radar Loop', ff, 333));
map.controls[google.maps.ControlPosition.TOP_CENTER].push(golgotha.maps.util.progress.getDiv());
</c:if>
// Build the standard weather layers
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerSelectControl(map, 'Clouds', new google.maps.weather.CloudLayer()));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerClearControl(map));
google.maps.event.trigger(this, 'maptypeid_changed');

// Placeholder for route
var routeData;
var routeWaypoints;
var acPositions = [];
var dcPositions = [];

// Reload ACARS data
document.doRefresh = true;
reloadData(true);
</script>
</body>
</html>
