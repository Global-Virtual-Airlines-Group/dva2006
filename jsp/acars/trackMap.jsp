<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_mapbox.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Track Map</title>
<content:expire expires="7200" />
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:googleAnalytics />
<map:api version="3" />
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="trackmap.do" method="get" validate="return false">
<el:table className="form">
<tr class="title">
 <td class="caps"><span class="nophone"><content:airline />&nbsp;</span>ACARS TRACK MAP</td>
 <td class="right"><span id="localAirports" class="right" style="display:none;"><el:combo name="localAP" size="1" firstEntry="[ SELECT AIRPORT ]" options="${localAP}" className="small localAP" onChange="void golgotha.local.selectLocal(this)" /></span></td>
</tr>
<tr>
 <td colspan="2" class="data"><map:div ID="mapBox" height="620" /><div id="zoomLevel" class="small bld mapTextLabel right"></div></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<c:set var="maxZoomLevel" value="${(empty localAP) ? 9 : 12}" scope="page" />
<script async>
golgotha.local.TrackSource = function(opacity) { this._opacity = opacity; };
golgotha.local.TrackSource.prototype.getType = 'Tiles';
golgotha.local.TrackSource.prototype.getLayer = function() {
	const to = location.protocol + '//' + location.host + '/track/{x}/{y}/{z}/tile.png';
	const so = {type:'raster',tileSize:golgotha.maps.TILE_SIZE.w,tiles:[to]};
	return {id:'Tracks',type:'raster',paint:{'raster-opacity':this._opacity},source:so};
};

<map:token />
<map:point var="golgotha.local.mapC" point="${mapCenter}" />

// Create the map
const map = new golgotha.maps.Map(document.getElementById('mapBox'), {center:golgotha.local.mapC, minZoom:3, maxZoom:${maxZoomLevel}, zoom:6, projection:'globe', style:'mapbox://styles/mapbox/dark-v11'});
map.addControl(new mapboxgl.FullscreenControl(), 'top-right');
map.addControl(new mapboxgl.NavigationControl(), 'top-right');
map.addControl(new golgotha.maps.DIVControl('zoomLevel'), 'bottom-right');
map.on('zoomend', golgotha.maps.updateZoom);
map.on('style.load', golgotha.maps.updateMapText);

<c:if test="${!empty localAP}">
map.on('zoomend', function() { golgotha.util.display('localAirports', (this.getZoom() > 6)); });</c:if>
map.once('load', function() {
	golgotha.local.tl = new golgotha.local.TrackSource(0.525); 
	map.addLine(golgotha.local.tl);
	map.fire('zoomend');
});

golgotha.local.airportCoords = {};
<c:forEach var="ap" items="${localAP}">golgotha.local.airportCoords['${ap.ICAO}'] = <map:point point="${ap}" /></c:forEach>

golgotha.local.selectLocal = function(combo) {
	if (combo.selectedIndex < 1) return false;
	const o = combo[combo.selectedIndex];
	const ll = golgotha.local.airportCoords[o.getAttribute('icao')];
	if (ll != null) map.panTo(ll);
	return true;
};
</script>
</body>
</html>
