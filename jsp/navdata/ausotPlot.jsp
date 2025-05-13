<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Australian Track Plotter</title>
<content:expire expires="3600" />
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<map:api version="3" js="markerWithLabel,googleMapsWX,oceanicPlot" callback="golgotha.local.mapInit" />
<content:googleAnalytics eventSupport="true" />
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="pacotplot.do" method="get" validate="return false">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> AUSTRALIAN ROUTE PLOTTER<span id="isLoading"></span></td>
</tr>
<tr>
 <td class="label">Date</td>
 <td class="data"><el:combo name="date" idx="*" firstEntry="-" options="${dates}" value="${param.date}" onChange="void golgotha.maps.oceanic.loadTracks('AUSOT')" /></td>
</tr>
<tr id="fetchData" style="display:none">
 <td class="label">Retrieved on</td>
 <td class="data"><span id="fetchDate" class="pri bld"></span> from <span id="fetchSrc" class="ita"></span></td>
</tr>
<tr>
 <td class="label"><span id="trackLabel">Track Data</span></td>
 <td class="data"><span id="trackData">N/A</span></td>
</tr>
<tr>
 <td class="label">Map Legend</td>
 <td class="data"><map:legend color="white" legend="Eastbound" />  <map:legend color="orange" legend="Westbound" /></td>
</tr>
<tr>
 <td class="label">Display Tracks</td>
 <td class="data"><el:check name="showTracks" idx="*" options="${trackTypes}" checked="${trackTypes}" width="100" cols="3" onChange="void golgotha.maps.oceanic.updateTracks(this)" /></td>
</tr>
<tr>
 <td colspan="2" class="data"><map:div ID="googleMap" height="550" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script async>
golgotha.local.mapInit = function() {
	// Create the map
	const mapOpts = {center:{lat:-26.0,lng:133.0}, zoom:4, minZoom:3, maxZoom:8, scrollwheel:false, clickableIcons:false, streetViewControl:false, mapTypeControlOptions:{mapTypeIds:[google.maps.MapTypeId.SATELLITE, google.maps.MapTypeId.TERRAIN]}};
	map = new golgotha.maps.Map(document.getElementById('googleMap'), mapOpts);
	map.setMapTypeId(golgotha.maps.info.type);
	map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW, headerDisabled:true});
	google.maps.event.addListener(map, 'click', map.closeWindow);
	google.maps.event.addListener(map.infoWindow, 'closeclick', map.closeWindow);
	google.maps.event.addListener(map, 'maptypeid_changed', golgotha.maps.updateMapText);

	// Weather layer loader
	golgotha.local.sl = new golgotha.maps.SeriesLoader();
	golgotha.local.sl.setData('infrared', 0.35, 'wxSat');
	golgotha.local.sl.setData('radar', 0.45, 'wxRadar');
	golgotha.local.sl.onload(function() { golgotha.util.enable('#selImg'); });

	// Add clouds and jet stream layers
	const ctls = map.controls[google.maps.ControlPosition.BOTTOM_LEFT];
	const jsl = new golgotha.maps.ShapeLayer({maxZoom:8, nativeZoom:6, opacity:0.375, zIndex:golgotha.maps.z.OVERLAY}, 'Jet', 'wind-jet');
	ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Radar', disabled:true, c:'selImg'}, function() { return golgotha.local.sl.getLatest('radar'); }));
	ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Satellite', disabled:true, c:'selImg'}, function() { return golgotha.local.sl.getLatest('infrared'); }));
	ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Jet Stream'}, jsl));
	ctls.push(new golgotha.maps.LayerClearControl(map));
	map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(document.getElementById('copyright'));

	// Load data async once tiles are loaded
	google.maps.event.addListenerOnce(map, 'tilesloaded', function() {
		golgotha.maps.oceanic.resetTracks();
		golgotha.local.loader.loadGinsu();
		google.maps.event.trigger(map, 'maptypeid_changed');
		window.setTimeout(function() { golgotha.local.sl.loadRV(); }, 500);
	});
};
</script>
</body>
</html>
