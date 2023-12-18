<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Live Map</title>
<content:expire expires="3600" />
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<map:api version="3" />
<content:js name="acarsMap" />
<content:js name="progressBar" />
<content:js name="googleMapsWX" />
<content:js name="wxParsers" />
<content:googleAnalytics eventSupport="true" />
<content:sysdata var="refreshInterval" name="acars.livemap.reload" />
<script>
var loaders = {};
loaders.fir = new golgotha.maps.LayerLoader('FIRs', golgotha.maps.FIRParser);
loaders.series = new golgotha.maps.SeriesLoader();
loaders.series.setData('twcRadarHcMosaic', 0.45, 'wxRadar');
loaders.series.setData('temp', 0.275, 'wxTemp');
loaders.series.setData('windSpeed', 0.325, 'wxWind');
loaders.series.onload(function() { golgotha.util.enable('#selImg'); });
loaders.fir.onload(function() { golgotha.util.enable('selFIR'); });

golgotha.maps.acars.reloadData = function(isAuto)
{
// Get auto refresh
var f = document.forms[0];
var doRefresh = f.autoRefresh.checked;

// Generate XMLHTTPRequest if we're not already viewing a flight
if (!document.pauseRefresh) {
	var xmlreq = golgotha.maps.acars.generateXMLRequest();
	xmlreq.send(null);
}

// Set timer to reload the data
if (doRefresh && isAuto)
	window.setTimeout('void golgotha.maps.acars.reloadData(true)', ${refreshInterval + 2000});

return true;
};
</script>
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
<content:empty var="emptyList" />

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
 <td class="data"><map:div ID="googleMap" height="550" /><div id="copyright" class="small mapTextLabel"></div>
<div id="mapStatus" class="small mapTextLabel"></div></td>
</tr>
</el:table>
</el:form>
<content:copyright />
<script>
<map:point var="golgotha.local.mapC" point="${mapCenter}" />
golgotha.maps.info.ctr = golgotha.maps.info.ctr || golgotha.local.mapC;
var mapOpts = {center:golgotha.maps.info.ctr, minZoom:3, zoom:golgotha.maps.info.zoom, maxZoom:17, scrollwheel:false, streetViewControl:false, clickableIcons:false, mapTypeControlOptions:{mapTypeIds:golgotha.maps.DEFAULT_TYPES}};

// Create the map
var map = new golgotha.maps.Map(document.getElementById('googleMap'), mapOpts);
map.setMapTypeId(golgotha.maps.info.type);
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map.infoWindow, 'closeclick', golgotha.maps.acars.infoClose);
google.maps.event.addListener(map, 'click', golgotha.maps.acars.infoClose);
google.maps.event.addListener(map, 'zoom_changed', golgotha.maps.updateZoom);
google.maps.event.addListener(map, 'maptypeid_changed', golgotha.maps.updateMapText);

// Add preload progress bar
map.controls[google.maps.ControlPosition.TOP_CENTER].push(golgotha.maps.util.progress.getDiv());

// Build the weather layer controls
var ctls = map.controls[google.maps.ControlPosition.BOTTOM_LEFT];
var loop = function() { return loaders.series.getLayers('radar', 12); };
var hjsl = new golgotha.maps.ShapeLayer({maxZoom:8, nativeZoom:6, opacity:0.55, zIndex:golgotha.maps.z.OVERLAY}, 'High Jet', 'wind-jet');
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Radar', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('twcRadarHcMosaic'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Temperature', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('temp'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Wind Speed', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('windSpeed'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Clouds', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('sat'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Jet Stream'}, hjsl));
ctls.push(new golgotha.maps.LayerAnimateControl({map:map, title:'Radar Loop', refresh:325, disabled:true, c:'selImg'}, loop));

// Add other layers
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'FIRs', disabled:true, id:'selFIR'}, function() { return loaders.fir.getLayer(); }));
ctls.push(new golgotha.maps.LayerClearControl(map));

// Display the copyright notice and text boxes
map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(document.getElementById('copyright'));
map.controls[google.maps.ControlPosition.RIGHT_TOP].push(document.getElementById('mapStatus'));
map.controls[google.maps.ControlPosition.LEFT_BOTTOM].push(document.getElementById('zoomLevel'));

// Load data async once tiles are loaded
google.maps.event.addListenerOnce(map, 'tilesloaded', function() {
	loaders.series.loadGinsu();
	golgotha.util.createScript({id:'FIRs', url:('//' + self.location.host + '/firs.ws?jsonp=loaders.fir.load'), async:true});
	google.maps.event.trigger(map, 'maptypeid_changed');
	google.maps.event.trigger(map, 'zoom_changed');
	golgotha.maps.acars.reloadData(true);
});
</script>
</body>
</html>
