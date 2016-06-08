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
<map:api version="3" />
<content:js name="progressBar" />
<content:js name="googleMapsWX" />
<content:googleAnalytics eventSupport="true" />
<script type="text/javascript">
var loaders = {};
loaders.series = new golgotha.maps.SeriesLoader();
loaders.series.setData('radar', 0.45, 'wxRadar', 1024);
loaders.series.setData('eurorad', 0.45, 'wxRadar', 512);
loaders.series.setData('aussieradar', 0.45, 'wxRadar', 512);
loaders.series.setData('temp', 0.275, 'wxTemp');
loaders.series.setData('windspeed', 0.325, 'wxWind');
loaders.series.onload(function() { golgotha.util.enable('#selImg'); });

golgotha.local.clickIcon = function() {
	if (this.uniqueID) {
		map.infoWindow.setContent(window.external.GetMarkerMessage(this.uniqueID));
		map.infoWindow.open(map, this);
	}

	return true;
};

golgotha.local.externalIconMarker = function(opts, p, id) {
	var mrk = new golgotha.maps.IconMarker(opts, p);
	mrk.uniqueID = id;
	google.maps.event.addListener(mrk, 'click', golgotha.local.clickIcon);
	return mrk;
};

externalIconMarker = function(palCode, iconCode, p, id) {
	return new golgotha.local.externalIconMarker({pal:palCode,icon:iconCode}, p, id);
};
<c:if test="${isDispatch}">
golgotha.local.mapZoom = function() {
	golgotha.event.beacon('Dispatch', 'Zoom/Pan');
	var b = map.getBounds();
	window.external.doPan(b.getNorthEast().lat(), b.getSouthWest().lng(), b.getSouthWest().lat(), b.getNorthEast().lng(), map.getZoom());
	return true;
};

golgotha.local.toggleObjects = function(mrks, visible)
{
if (mrks == null) return false;
for (var idx = 0; idx < mrks.length; idx++) {
	var m = mrks[idx];
	if (visible)
		m.setMap(map);
	else if (!m.isSelected)
		m.setMap(null);
}

return true;
};

mapZoom = golgotha.local.mapZoom;
toggleObjects = golgotha.local.toggleObjects;
</c:if>
</script>
</head>
<body onunload="void golgotha.maps.util.unload()">
<el:form action="dispatchMap.do" method="post" validate="return false">
<map:div ID="googleMap" height="625" /><div id="zoomLevel" class="small mapTextLabel"></div><div id="copyright" class="small mapTextLabel"></div>
<div id="mapStatus" class="small mapTextLabel"></div>
</el:form>
<script id="mapInit">
var mapOpts = {center:{lat:36.44,lng:-100.14}, zoom:6, minZoom:2, maxZoom:12, scrollwheel:false, clickableIcons:false, streetViewControl:false, mapTypeControlOptions:{mapTypeIds:golgotha.maps.DEFAULT_TYPES}};
var map = new golgotha.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', map.closeWindow);
google.maps.event.addListener(map, 'maptypeid_changed', golgotha.maps.updateMapText);
google.maps.event.addListener(map, 'zoom_changed', golgotha.maps.updateZoom);

// Create the jetstream layers
var jsOpts = {maxZoom:8, nativeZoom:5, opacity:0.55, zIndex:golgotha.maps.z.OVERLAY};
var hjsl = new golgotha.maps.ShapeLayer(jsOpts, 'High Jet', 'wind-jet');
var ljsl = new golgotha.maps.ShapeLayer(jsOpts, 'Low Jet', 'wind-lojet');

// Build the weather layer controls
var ctls = map.controls[google.maps.ControlPosition.BOTTOM_LEFT];
var worldRadar = function() { return [loaders.series.getLatest('radar'), loaders.series.getLatest('eurorad'), loaders.series.getLatest('aussieradar')]; };
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Radar', disabled:true, c:'selImg'}, worldRadar));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Temperature', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('temp'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Wind Speed', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('windspeed'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Lo Jetstream'}, ljsl));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Hi Jetstream'}, hjsl));
ctls.push(new golgotha.maps.LayerClearControl(map));

// Add controls
map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(document.getElementById('copyright'));
map.controls[google.maps.ControlPosition.LEFT_BOTTOM].push(document.getElementById('zoomLevel'));
map.controls[google.maps.ControlPosition.LEFT_BOTTOM].push(document.getElementById('mapStatus'));

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
google.maps.event.addListener(map, 'dragend', golgotha.local.mapZoom);</c:if>

// Load data async once tiles are loaded
google.maps.event.addListenerOnce(map, 'tilesloaded', function() {
	golgotha.util.createScript({id:'wxLoader', url:('//' + self.location.host + '/wx/serieslist.js?function=loaders.series.loadGinsu'), async:true});
	google.maps.event.trigger(map, 'maptypeid_changed');
	google.maps.event.trigger(map, 'zoom_changed');
});
</script>
</body>
</html>
