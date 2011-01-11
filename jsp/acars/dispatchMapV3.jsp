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
<content:js name="common" />
<map:api version="3" />
<content:googleAnalytics eventSupport="true" />
<content:sysdata var="tileHost" name="weather.tileHost" />
<content:sysdata var="multiHost" name="weather.multiHost" />
<c:if test="${!empty tileHost}">
<content:js name="acarsMapWX" />
<content:js name="acarsMapFF" />
<content:js name="progressBar" />
<map:wxList layers="radar,eurorad,sat,temp,future_radar_ff" />
</c:if>
<script type="text/javascript">
<c:if test="${!empty tileHost}">
document.tileHost = '${tileHost}';
document.multiHost = ${multiHost};</c:if>
function updateZoomLevel(oldZoom, newZoom)
{
var level = document.getElementById('zoomLevel');
level.innerHTML = 'Zoom Level ' + newZoom;
return true;
}

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
<map:div ID="googleMap" x="100%" y="625" />
<div id="ffSlices" style="position:absolute; top:30px; right:7px; visibility:hidden; z-index:20;"><span id="ffLabel" class="small bld mapTextLabel">Select Time</span>
 <el:combo name="ffSlice" size="1" className="small" options="${emptyList}" onChange="void updateFF(this)" />
 <el:button ID="AnimateButton" label="ANIMATE" onClick="void animateFF()" /></div>
<div id="zoomLevel" class="small mapTextLabel"></div><div id="copyright" class="small mapTextLabel" style="bottom:17px; right:2px; visibility:hidden;"></div>
</el:form>
<script type="text/javascript">
var mapTypes = {mapTypeIds: golgotha.maps.DEFAULT_TYPES};
var mapOpts = {center:new google.maps.LatLng(36.44, -100.14), zoom:6, scrollwheel:false, streetViewControl:false, mapTypeControlOptions: mapTypes};

// Load the map
var map = new google.maps.Map(getElement('googleMap'), mapOpts);
map.getOptions = function() { return mapOpts; };
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content: ''});
google.maps.event.addListener(map, 'click', function() { map.infoWindow.close(); });

<c:if test="${!empty tileHost}">
// Load the tile overlays
getTileOverlay('radar', 0.45);
getTileOverlay('eurorad', 0.45);
getTileOverlay('sat', 0.35);
getTileOverlay('temp', 0.25);

//Load the ff tile overlays
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
var zl = document.getElementById('zoomLevel');
zl.innerHTML = 'Zoom Level ' + map.getZoom();
map.controls[google.maps.ControlPosition.BOTTOM_RIGHT].push(zl);

// Initialize FastForward elements
var pBar = progressBar({strokeWidth:225, strokeColor:'#0020ff'});
pBar.getDiv().style.right = '4px';
pBar.getDiv().style.top = '50px';
google.maps.event.addListenerOnce(map, 'tilesloaded', function() { 
	addOverlay(map, 'ffSlices'); 
	addOverlay(map, 'copyright'); 
	addOverlay(map, pBar.getDiv());
	google.maps.event.trigger(map, 'maptypeid_changed');
	google.maps.event.trigger(map, 'dragend');
});

// Initialize event listeners
google.maps.event.addListener(map, 'maptypeid_changed', updateMapText);
google.maps.event.addListener(map, 'zoomend', updateZoomLevel);
google.maps.event.addListener(map, 'maptypeid_changed', hideAllSlices);
</c:if>
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
</script>
</body>
</map:xhtml>
