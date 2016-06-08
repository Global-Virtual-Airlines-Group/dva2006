<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Airport Weather Finder</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<map:api version="3" />
<content:js name="googleMapsWX" />
<content:js name="wxParsers" />
<content:googleAnalytics eventSupport="true" />
<script type="text/javascript">
var loaders = {};
loaders.series = new golgotha.maps.SeriesLoader();
loaders.lg = new golgotha.maps.LayerLoader('Lightning', golgotha.maps.LightningParser);
loaders.fr = new golgotha.maps.LayerLoader('Fronts', golgotha.maps.fronts.FrontParser);
loaders.series.setData('radar', 0.45, 'wxRadar', 1024);
loaders.series.setData('eurorad', 0.45, 'wxRadar', 512);
loaders.series.setData('aussieradar', 0.45, 'wxRadar', 512);
loaders.series.setData('temp', 0.275, 'wxTemp');
loaders.series.setData('windspeed', 0.325, 'wxWind');
loaders.series.onload(function() { golgotha.util.enable('#selImg'); });
loaders.fr.onload(function() { golgotha.util.enable('selFronts'); });
loaders.lg.onload(function() { golgotha.util.enable('selLG'); });

golgotha.local.filterTypes = function(combo)
{
var minIDX = Math.max(1, combo.selectedIndex + 1);
for (var x = 0; x < golgotha.local.wxAirports.length; x++) {
	var mrk = golgotha.local.wxAirports[x];
	if ((mrk.ILS < minIDX) && mrk.getVisible())
		mrk.setVisible(false);
	else if ((mrk.ILS >= minIDX) && !mrk.getVisible())
		mrk.setVisible(true);
}
	
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:getCookie name="acarsMapType" default="map" var="gMapType" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="wxfinder.do" method="get" validate="return false">
<el:table className="form">
<tr class="title caps">
 <td colspan="2" class="left"><content:airline /> AIRPORT WEATHER FINDER</td>
</tr>
<tr>
 <td class="label">ILS Category</td>
 <td class="data"><el:combo name="ils" size="1" idx="*" options="${ilsClasses}" onChange="void golgotha.local.filterTypes(this)" /></td>
</tr>
<tr>
 <td class="label">Map Legend</td>
 <td class="data"><map:legend color="green" legend="CATI" /> <map:legend color="blue" legend="CATII" /> <map:legend color="orange" legend="CATIIIa" />
 <map:legend color="purple" legend="CATIIIb" /> <map:legend color="red" legend="CATIIIc" /></td>
</tr>
<tr>
 <td class="data" colspan="2"><map:div ID="googleMap" height="480" /><div id="copyright" class="small mapTextLabel"></div>
<div id="mapStatus" class="small mapTextLabel"></div><div id="zoomLevel" class="mapTextLabel"></div></td>
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
var mapTypes = {mapTypeIds:golgotha.maps.DEFAULT_TYPES};
var mapOpts = {center:golgotha.local.mapC, zoom:4, minZoom:2, maxZoom:9, scrollwheel:false, streetViewControl:false, clickableIcons:false, mapTypeControlOptions:mapTypes};

// Create the map
var map = new golgotha.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', map.closeWindow);
google.maps.event.addListener(map, 'maptypeid_changed', golgotha.maps.updateMapText);
google.maps.event.addListener(map, 'zoom_changed', golgotha.maps.updateZoom);

// Add airports -- don't use the standard TAG so we can add some info to the tags
golgotha.local.wxAirports = [];
<c:forEach var="ap" items="${metars}">
<map:marker var="mrk" marker="true" point="${ap}" color="${ap.iconColor}" label="${ap.infoBox}" />
mrk.ILS = ${ap.ILS.ordinal()};
golgotha.local.wxAirports.push(mrk);
</c:forEach>
map.addMarkers(golgotha.local.wxAirports);

// Build the layer controls
var ctls = map.controls[google.maps.ControlPosition.BOTTOM_LEFT];
var worldRadar = function() { return [loaders.series.getLatest('radar'), loaders.series.getLatest('eurorad'), loaders.series.getLatest('aussieradar')]; };
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Radar', disabled:true, c:'selImg'}, worldRadar));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Temperature', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('temp'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Wind Speed', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('windSpeed'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Clouds', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('sat'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Fronts', disabled:true, id:'selFronts'}, function() { return loaders.fr.getLayer(); }));
ctls.push(new golgotha.maps.LayerClearControl(map));

// Display the copyright notice and text boxes
map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(document.getElementById('copyright'));
map.controls[google.maps.ControlPosition.LEFT_BOTTOM].push(document.getElementById('zoomLevel'));
map.controls[google.maps.ControlPosition.RIGHT_TOP].push(document.getElementById('mapStatus'));

// Load data async once tiles are loaded
google.maps.event.addListenerOnce(map, 'tilesloaded', function() {
	golgotha.maps.reloadData(true);
	golgotha.util.createScript({id:'wuFronts', url:'//api.wunderground.com/api/${wuAPI}/fronts/view.json?callback=loaders.fr.load', async:true});
	google.maps.event.trigger(map, 'zoom_changed');
	google.maps.event.trigger(map, 'maptypeid_changed');
});

golgotha.maps.reloadData = function(isReload) {
	if (isReload) 
		window.setInterval(golgotha.maps.reloadData, golgotha.maps.reload);

	var dv = document.getElementById('seriesRefresh');
	if (dv != null) dv.innerHTML = new Date();
	golgotha.util.createScript({id:'wxLoader', url:('//' + self.location.host + '/wx/serieslist.js?function=loaders.series.loadGinsu'), async:true});
	golgotha.util.createScript({id:'lgAlert', url:'/wxd/LGRecord/CURRENT?jsonp=loaders.lg.load', async:true});
	return true;
};
</script>
</body>
</html>
