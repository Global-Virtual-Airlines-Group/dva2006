<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_mapbox.tld" prefix="map" %>
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
<map:api version="3" />
<content:js name="mapBoxWX" />
<content:csp type="CONNECT" host="tilecache.rainviewer.com" />
<content:csp type="STYLE" host="'unsafe-inline'" />
<content:googleAnalytics />
<content:cspHeader />
<style type="text/css">
div.wxPopup {
 flex-wrap:wrap
}
</style>
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
 <td class="data" colspan="2"><map:div ID="mapBox" height="640" /><div id="copyright" class="small mapTextLabel"></div><div id="zoomLevel" class="small right mapTextLabel"></div></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script async>
<map:token />
<map:point var="golgotha.local.mapC" point="${mapCenter}" />

// Create the map
const map = new golgotha.maps.Map(document.getElementById('mapBox'), {center:golgotha.local.mapC, zoom:4, minZoom:3, maxZoom:9, scrollZoom:false, projection:'globe', style:'mapbox://styles/mapbox/outdoors-v12'});
map.addControl(new mapboxgl.FullscreenControl(), 'top-right');
map.addControl(new mapboxgl.NavigationControl(), 'top-right');
map.on('style.load', golgotha.maps.updateMapText);
map.on('zoomend', golgotha.maps.updateZoom);

// Weather layer loader
golgotha.local.sl = new golgotha.maps.wx.SeriesLoader();
golgotha.local.sl.setData('infrared', 0.35, 'wxSat');
golgotha.local.sl.setData('radar', 0.45, 'wxRadar');
golgotha.local.sl.onload(function() { golgotha.util.enable('#selImg'); });

// Add airports
golgotha.local.wxAirports = [];
<c:forEach var="ap" items="${metars}">
<map:marker var="golgotha.local.mrk" marker="true" point="${ap}" color="${ap.iconColor}" label="${ap.infoBox}" />
golgotha.local.mrk.getPopup().addClassName('wxPopup');
golgotha.local.mrk.ILS = ${ap.ILS.ordinal()};
golgotha.local.wxAirports.push(golgotha.local.mrk);</c:forEach>
map.addMarkers(golgotha.local.wxAirports);

// Add selection controls
golgotha.maps.wx.ctl = new golgotha.maps.wx.WXLayerControl();
golgotha.maps.wx.ctl.addLayer({name:'Radar', c:'selImg', disabled:true, f:function() { return golgotha.local.sl.getLatest('radar'); }});
golgotha.maps.wx.ctl.addLayer({name:'Satellite', c:'selImg', disabled:true, id:'infrared', f:function() { return golgotha.local.sl.getLatest('infrared'); }});
map.addControl(golgotha.maps.wx.ctl, 'bottom-left');
map.addControl(new golgotha.maps.DIVControl('copyright'), 'bottom-right');
map.addControl(new golgotha.maps.DIVControl('zoomLevel'), 'bottom-right');

// Load data async once tiles are loaded
map.once('load', function() {
	map.addControl(new golgotha.maps.BaseMapControl(golgotha.maps.DEFAULT_TYPES), 'top-left');
	window.setTimeout(function() { golgotha.maps.reloadData(true); }, 500);
	map.fire('zoomend');
});

golgotha.maps.reloadData = function(isReload) {
	if (isReload) 
		window.setInterval(golgotha.maps.reloadData, golgotha.maps.reload);

	const dv = document.getElementById('seriesRefresh');
	if (dv) dv.innerHTML = new Date();
	golgotha.local.sl.loadRV();
	return true;
};

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
</script>
</body>
</html>
