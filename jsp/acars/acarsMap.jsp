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
<content:expire expires="3600" />
<content:css name="main" />
<content:css name="form" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:googleAnalytics eventSupport="true" />
<content:sysdata var="refreshInterval" name="acars.livemap.reload" />
<map:api version="3" />
<content:js name="acarsMap" />
<content:js name="progressBar" />
<content:js name="markerWithLabel" />
<content:js name="dayNightLayer" />
<content:js name="googleMapsWX" />
<content:js name="wxParsers" />
<script type="text/javascript">
var loaders = {};
loaders.fir = new golgotha.maps.LayerLoader('FIRs', golgotha.maps.FIRParser);
loaders.fr = new golgotha.maps.LayerLoader('Fronts', golgotha.maps.fronts.FrontParser);
loaders.lg = new golgotha.maps.LayerLoader('Lightning', golgotha.maps.LightningParser);
loaders.series = new golgotha.maps.SeriesLoader();
loaders.series.setData('radar', 0.45, 'wxRadar', 1024);
loaders.series.setData('eurorad', 0.45, 'wxRadar', 512);
loaders.series.setData('aussieradar', 0.45, 'wxRadar', 512);
loaders.series.setData('future_radar_ff', 0.45, 'wxRadar', 1024);
loaders.series.setData('temp', 0.275, 'wxTemp');
loaders.series.setData('windspeed', 0.325, 'wxWind');
loaders.series.onload(function() { golgotha.util.enable('#selImg'); });
loaders.fr.onload(function() { golgotha.util.enable('selFronts'); });
loaders.lg.onload(function() { golgotha.util.enable('selLG'); });
loaders.fir.onload(function() { golgotha.util.enable('selFIR'); });

golgotha.maps.acars.reloadData = function(isAuto)
{
// Get auto refresh
var f = document.forms[0];
var doRefresh = f.autoRefresh.checked;

// Generate XMLHTTPRequest if we're not already viewing a flight
if (!document.pauseRefresh) {
	var isLoading = document.getElementById('isLoading');
	isLoading.innerHTML = ' - LOADING...';
	var xmlreq = golgotha.maps.acars.generateXMLRequest();
	xmlreq.send(null);
}

// Set timer to reload the data
if (doRefresh && isAuto)
	window.setTimeout(golgotha.maps.acars.reloadData, ${refreshInterval + 2000}, true);

return true;
};

golgotha.maps.acars.saveSettings = function()
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
alert('Your <content:airline /> ACARS Map preferences have been saved.');
return true;
};

golgotha.maps.acars.clearSettings = function()
{
var expiryDate = new Date();
document.cookie = 'acarsMapLat=; expires=' + expiryDate.toGMTString();
document.cookie = 'acarsMapLng=; expires=' + expiryDate.toGMTString();
document.cookie = 'acarsMapZoomLevel=; expires=' + expiryDate.toGMTString();
document.cookie = 'acarsMapType=; expires=' + expiryDate.toGMTString();
alert('Your <content:airline /> ACARS Map preferences have been cleared.');
return true;
};

golgotha.maps.acars.showEarth = function() {
	self.location = '/acars_map_earth.ws';
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
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
 <td colspan="2" class="right nophone"><span id="userSelect" style="display:none;"> ZOOM TO <el:combo ID="usrID" name="usrID" idx="*" options="${emptyList}" firstEntry="-" onChange="void golgotha.maps.acars.zoomTo(this)" /></span></td>
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
 <td class="label nophone">Dispatcher Legend</td>
 <td class="data nophone"><map:legend color="green" legend="Available" /> <map:legend color="purple" legend="Busy" /></td>
</tr>
<tr>
 <td class="label">Dispatch Service</td>
 <td class="data"><span id="dispatchStatus" class="bld caps">DISPATCH CURRENTLY OFFLINE</span></td>
 <td class="label nophone">Weather Layer</td>
 <td class="data nophone"><span id="wxLoading" class="small" style="width:150px;">None</span></td>
</tr>
<tr>
 <td class="data" colspan="4"><map:div ID="googleMap" height="550" /><div id="copyright" class="small mapTextLabel"></div>
<div id="mapStatus" class="small mapTextLabel"></div><div id="zoomLevel" class="small mapTextLabel"></div><div id="seriesRefresh" class="small mapTextLabel"></div></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr class="title">
 <td><el:button ID="RefreshButton" onClick="void golgotha.maps.acars.reloadData(false)" label="REFRESH ACARS DATA" />&nbsp;
<el:button ID="SettingsButton" onClick="void golgotha.maps.acars.saveSettings()" label="SAVE SETTINGS" />&nbsp;
<el:button ID="ClearButton" onClick="void golgotha.maps.acars.clearSettings()" label="CLEAR SETTINGS" />&nbsp;
<el:button ID="EarthButton" onClick="void golgotha.maps.acars.showEarth()" label="DISPLAY IN GOOGLE EARTH" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:sysdata var="wuAPI" name="security.key.wunderground" />
<script id="mapInit">
<map:point var="golgotha.local.mapC" point="${mapCenter}" />
var mapOpts = {center:golgotha.local.mapC, minZoom:2, maxZoom:17, zoom:${zoomLevel}, scrollwheel:false, clickableIcons:false, streetViewControl:false, mapTypeControlOptions:{mapTypeIds:golgotha.maps.DEFAULT_TYPES}};

// Create the map
var map = new golgotha.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map.infoWindow, 'closeclick', golgotha.maps.acars.infoClose);
google.maps.event.addListener(map, 'click', golgotha.maps.acars.infoClose);
google.maps.event.addListener(map, 'zoom_changed', golgotha.maps.updateZoom);
google.maps.event.addListener(map, 'maptypeid_changed', golgotha.maps.updateMapText);

// Add preload progress bar
map.controls[google.maps.ControlPosition.TOP_CENTER].push(golgotha.maps.util.progress.getDiv());

// Build the weather layer controls
var ctls = map.controls[google.maps.ControlPosition.BOTTOM_LEFT];
var loop = function() { return loaders.series.combine(12, 'radar', 'future_radar_ff'); };
//var loop = function() { return loaders.series.getLayers('radar', 12); };
var worldRadar = function() { return [loaders.series.getLatest('radar'), loaders.series.getLatest('eurorad'), loaders.series.getLatest('aussieradar')]; };
var hjsl = new golgotha.maps.ShapeLayer({maxZoom:8, nativeZoom:6, opacity:0.55, zIndex:golgotha.maps.z.OVERLAY}, 'High Jet', 'wind-jet');
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Radar', disabled:true, c:'selImg'}, worldRadar));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Temperature', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('temp'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Wind Speed', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('windspeed'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Clouds', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('sat'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Fronts', disabled:true, id:'selFronts'}, function() { return loaders.fr.getLayer(); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Jet Stream'}, hjsl));
ctls.push(new golgotha.maps.LayerAnimateControl({map:map, title:'Radar Loop', refresh:325, disabled:true, c:'selImg'}, loop));

// Add other layers
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'FIRs', disabled:true, id:'selFIR'}, function() { return loaders.fir.getLayer(); }));
map.DN = new DayNightOverlay({fillColor:'rgba(40,48,56,0.33)'});
ctls.push(new golgotha.maps.SelectControl('Day/Night', function() { map.DN.setMap(map); }, function() { map.DN.setMap(null); }));
ctls.push(new golgotha.maps.LayerClearControl(map));

// Display the copyright notice and text boxes
map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(document.getElementById('copyright'));
map.controls[google.maps.ControlPosition.RIGHT_TOP].push(document.getElementById('mapStatus'));
map.controls[google.maps.ControlPosition.LEFT_BOTTOM].push(document.getElementById('zoomLevel'));
map.controls[google.maps.ControlPosition.LEFT_BOTTOM].push(document.getElementById('seriesRefresh'));

// Load data async once tiles are loaded
google.maps.event.addListenerOnce(map, 'tilesloaded', function() {
	golgotha.maps.reloadData(true);
	golgotha.util.createScript({id:'FIRs', url:('//' + self.location.host + '/firs.ws?jsonp=loaders.fir.load'), async:true});
	golgotha.util.createScript({id:'wuFronts', url:'//api.wunderground.com/api/${wuAPI}/fronts/view.json?callback=loaders.fr.load', async:true});
	google.maps.event.trigger(map, 'maptypeid_changed');
	google.maps.event.trigger(map, 'zoom_changed');
	golgotha.maps.acars.reloadData(true);
});

golgotha.maps.reloadData = function(isReload) {
	if (isReload) 
		window.setInterval(golgotha.maps.reloadData, golgotha.maps.reload);
	
	// Check if we're loading/animating
	if ((map.preLoad) || (map.animator)) {
		console.log('Animating Map - reload skipped');
		return false;
	}
	
	var dv = document.getElementById('seriesRefresh');
	if (dv != null) {
		var txtDate = new Date().toString();
		dv.innerHTML = txtDate.substring(0, txtDate.indexOf('('));
	}

	golgotha.util.createScript({id:'wxLoader', url:('//' + self.location.host + '/wx/serieslist.js?function=loaders.series.loadGinsu'), async:true});
	return true;
};
</script>
</body>
</html>
