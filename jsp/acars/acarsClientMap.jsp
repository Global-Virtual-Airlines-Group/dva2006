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
<map:api version="3" />
<content:js name="acarsMap" />
<content:googleAnalytics eventSupport="true" />
<content:sysdata var="tileHost" name="weather.tileHost" />
<content:sysdata var="multiHost" name="weather.multiHost" />
<content:sysdata var="refreshInterval" name="acars.livemap.reload" />
<c:if test="${!empty tileHost}">
<content:js name="acarsMapWX" /></c:if>
<script type="text/javascript">
<c:if test="${!empty tileHost}">document.tileHost = '${tileHost}';
document.multiHost = ${multiHost};</c:if>
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
<map:wxList layers="radar,eurorad,sat,temp" />
</head>
<content:copyright visible="false" />
<body>
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
 <td class="data"><map:div ID="googleMap" x="100%" y="510" /></td>
</tr>
</el:table>
<div id="ffSlices" style="visibility:hidden;"><span id="ffLabel" class="small mapTextLabel">Select Time</span>
 <el:combo name="ffSlice" size="1" className="small" options="${emptyList}" onChange="void updateFF(this)" /></div>
</el:form>
<content:copyright />
<div id="copyright" class="small mapTextLabel" style="bottom:17px; right:2px; visibility:hidden;"></div>
<script type="text/javascript">
<map:point var="mapC" point="${mapCenter}" />
var mapTypes = {mapTypeIds: golgotha.maps.DEFAULT_TYPES};
var mapOpts = {center:mapC, zoom:${zoomLevel}, scrollwheel:false, streetViewControl:false, mapTypeControlOptions: mapTypes};

// Create the map
var map = new google.maps.Map(getElement('googleMap'), mapOpts);
map.getOptions = function() { return mapOpts; };
<c:if test="${!empty tileHost}">
// Load the tile overlays
getTileOverlay('radar', 0.45);
getTileOverlay('eurorad', 0.45);
getTileOverlay('sat', 0.35);
getTileOverlay('temp', 0.25);

// Build the layer controls
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXOverlayControl('Radar', ['radar', 'eurorad']));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXOverlayControl('Infrared', 'sat'));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXOverlayControl('Temperature', 'temp'));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXClearControl());

// Display the copyright notice
var d = new Date();
var cp = document.getElementById('copyright');
cp.innerHTML = 'Weather Data &copy; ' + d.getFullYear() + ' The Weather Channel.'
</c:if>
// Add map controls
<map:type map="map" type="${gMapType}" default="TERRAIN" />
google.maps.event.addListener(map, 'maptypeid_changed', updateMapText);
map.infoWindow = new google.maps.InfoWindow({content: ''});
google.maps.event.addListener(map.infoWindow, 'closeclick', infoClose);
google.maps.event.addListener(map, 'click', infoClose);

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
</map:xhtml>
