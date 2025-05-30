<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Airport Weather Finder</title>
<content:expire expires="600" />
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<map:api version="3" js="googleMapsWX,wxParsers" callback="golgotha.local.mapInit" />
<content:googleAnalytics />
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

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
 <td class="data" colspan="2"><map:div ID="googleMap" height="640" /><div id="copyright" class="small mapTextLabel"></div><div id="mapStatus" class="small mapTextLabel"></div><div id="zoomLevel" class="mapTextLabel"></div></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script async>
golgotha.local.filterTypes = function(combo) {
	const minIDX = Math.max(1, combo.selectedIndex + 1);
	for (var x = 0; x < golgotha.local.wxAirports.length; x++) {
		const mrk = golgotha.local.wxAirports[x];
		if ((mrk.ILS < minIDX) && mrk.getVisible())
			mrk.setVisible(false);
		else if ((mrk.ILS >= minIDX) && !mrk.getVisible())
			mrk.setVisible(true);
	}

	return true;
};

golgotha.local.mapInit = function() {
	<map:point var="golgotha.local.mapC" point="${mapCenter}" />

	// Create the map
	const mapOpts = {center:golgotha.local.mapC, zoom:4, minZoom:3, maxZoom:9, scrollwheel:false, streetViewControl:false, clickableIcons:false, mapTypeControlOptions:{mapTypeIds:golgotha.maps.DEFAULT_TYPES}};
	map = new golgotha.maps.Map(document.getElementById('googleMap'), mapOpts);
	map.setMapTypeId(golgotha.maps.info.type);
	map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW, headerDisabled:true});
	google.maps.event.addListener(map, 'click', map.closeWindow);
	google.maps.event.addListener(map, 'maptypeid_changed', golgotha.maps.updateMapText);
	google.maps.event.addListener(map, 'zoom_changed', golgotha.maps.updateZoom);

	// Weather layer loader
	golgotha.local.sl = new golgotha.maps.SeriesLoader();
	golgotha.local.sl.setData('infrared', 0.35, 'wxSat');
	golgotha.local.sl.setData('radar', 0.45, 'wxRadar');
	golgotha.local.sl.onload(function() { golgotha.util.enable('#selImg'); });

	// Add airports
	golgotha.local.wxAirports = [];
	<c:forEach var="ap" items="${metars}">
	<map:marker var="golgotha.local.mrk" marker="true" point="${ap}" color="${ap.iconColor}" label="${ap.infoBox}" />
	golgotha.local.mrk.ILS = ${ap.ILS.ordinal()};
	golgotha.local.wxAirports.push(golgotha.local.mrk);
	</c:forEach>
	map.addMarkers(golgotha.local.wxAirports);

	// Build the layer controls
	const ctls = map.controls[google.maps.ControlPosition.BOTTOM_LEFT];
	const jsl = new golgotha.maps.ShapeLayer({maxZoom:8, nativeZoom:6, opacity:0.425, zIndex:golgotha.maps.z.OVERLAY}, 'Jet', 'wind-jet');
	ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Radar', disabled:true, c:'selImg'}, function() { return golgotha.local.sl.getLatest('radar'); }));
	ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Satellite', disabled:true, c:'selImg'}, function() { return golgotha.local.sl.getLatest('infrared'); }));
	ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Jet Stream'}, jsl));
	ctls.push(new golgotha.maps.LayerClearControl(map));
	
	// Display the copyright notice and text boxes
	map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(document.getElementById('copyright'));
	map.controls[google.maps.ControlPosition.LEFT_BOTTOM].push(document.getElementById('zoomLevel'));
	map.controls[google.maps.ControlPosition.RIGHT_TOP].push(document.getElementById('mapStatus'));

	// Load data async once tiles are loaded
	google.maps.event.addListenerOnce(map, 'tilesloaded', function() {
		google.maps.event.trigger(map, 'zoom_changed');
		google.maps.event.trigger(map, 'maptypeid_changed');
		window.setTimeout(function() { golgotha.maps.reloadData(true); }, 500);
	});
};

golgotha.maps.reloadData = function(isReload) {
	if (isReload) 
		window.setInterval(golgotha.maps.reloadData, golgotha.maps.reload);

	const dv = document.getElementById('seriesRefresh');
	if (dv != null) dv.innerHTML = new Date();
	golgotha.local.sl.loadRV();
	return true;
};
</script>
</body>
</html>
