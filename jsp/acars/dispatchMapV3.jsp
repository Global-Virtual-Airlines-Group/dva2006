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
<content:css name="dispatchMap" scheme="legacy" />
<content:pics />
<content:js name="common" />
<map:api version="3" libraries="weather" />
<content:js name="progressBar" />
<content:js name="googleMapsWX" />
<content:googleAnalytics eventSupport="true" />
<content:sysdata var="tileHost" name="weather.tileHost" />
<c:if test="${!empty tileHost}">
<script type="text/javascript">
var gsLoader;
gsLoader = new golgotha.maps.GinsuLoader(2);
gsLoader.setData('radar', 0.45, 'wxRadar');
gsLoader.setData('eurorad', 0.45, 'wxRadar');
gsLoader.setData('temp', 0.275, 'wxTemp');
gsLoader.setData('windspeed', 0.325, 'wxWind');
</script>
<map:wxList layers="radar,eurorad,temp,windspeed,future_radar_ff" function="gsLoader.load" max="8" />
</c:if>
<script type="text/javascript">
function clickIcon()
{
if (this.uniqueID) {
	map.infoWindow.setContent(window.external.GetMarkerMessage(this.uniqueID));
	map.infoWindow.open(map, this);
}

return true;
}

function externalMarker(color, point, id)
{
var mrk = googleMarker(color, point, null);
mrk.uniqueID = id;
google.maps.event.addListener(mrk, 'click', clickIcon);
return mrk;
}

function externalIconMarker(palCode, iconCode, point, id)
{
var mrk = googleIconMarker(palCode, iconCode, point, null);
mrk.uniqueID = id;
google.maps.event.addListener(mrk, 'click', clickIcon);
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
		m.setMap(map);
	else if (!m.isSelected)
		m.setMap(null);
}

return true;
}
</script></c:if>
</head>
<body>
<el:form action="dispatchMap.do" method="post" validate="return false">
<map:div ID="googleMap" x="100%" y="625" /><div id="zoomLevel" class="small mapTextLabel"></div><div id="copyright" class="small mapTextLabel"></div>
<div id="mapStatus" class="small mapTextLabel"></div>
</el:form>
<script type="text/javascript">
var mapTypes = {mapTypeIds:golgotha.maps.DEFAULT_TYPES};
var mapOpts = {center:new google.maps.LatLng(36.44, -100.14), zoom:6, minZoom:2, maxZoom:12, scrollwheel:false, streetViewControl:false, mapTypeControlOptions:mapTypes};

// Load the map
var map = new google.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', function() { map.infoWindow.close(); });
google.maps.event.addListener(map, 'maptypeid_changed', golgotha.maps.updateMapText);
google.maps.event.addListener(map, 'zoom_changed', golgotha.maps.updateZoom);
map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(document.getElementById('copyright'));
map.controls[google.maps.ControlPosition.LEFT_BOTTOM].push(document.getElementById('zoomLevel'));
map.controls[google.maps.ControlPosition.LEFT_BOTTOM].push(document.getElementById('mapStatus'));
<c:if test="${!empty tileHost}">
// Build the layer controls
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerSelectControl(map, 'Radar', [gsLoader.getLatest('radar'), gsLoader.getLatest('eurorad')]));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerSelectControl(map, 'Temperature', gsLoader.getLatest('temp')));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerSelectControl(map, 'Wind Speed', gsLoader.getLatest('windspeed')));
</c:if>
// Build the standard weather layers
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerSelectControl(map, 'Clouds', new google.maps.weather.CloudLayer()));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerClearControl(map));

// Initialize arrays and collection
var route = [];
var routePoints = [];
var sidLine;
var starLine;
var routeLine;
var aL;
var mrks_sid = [];
var mrks_star = [];
<c:if test="${isDispatch}">
google.maps.event.addListener(map, 'dragend', mapZoom);</c:if>
google.maps.event.trigger(map, 'maptypeid_changed');
google.maps.event.trigger(map, 'zoom_changed');
</script>
</body>
</html>
