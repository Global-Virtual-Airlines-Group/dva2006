<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Track Map</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:googleAnalytics eventSupport="true" />
<map:api version="3" />
<content:js name="googleMapsStyles" />
<style>
select.localAP { background-color:#000810; }
</style>
<script type="text/javascript">
golgotha.maps.track = golgotha.maps.track || {};
golgotha.maps.track.ShapeLayer = function(tx, minZ, maxZ)
{
var layerOpts = {minZoom:minZ, maxZoom:maxZ, isPng:true, opacity:tx, tileSize:golgotha.maps.TILE_SIZE};
layerOpts.myBaseURL = location.protocol + '//' + location.host + '/track/';
layerOpts.getTileUrl = function(pnt, zoom) {
	if (zoom > this.maxZoom) return '';
	var url = this.myBaseURL;
	for (var x = zoom; x > 0; x--) {
		var digit1 = ((golgotha.maps.masks[x] & pnt.x) == 0) ? 0 : 1;
		var digit2 = ((golgotha.maps.masks[x] & pnt.y) == 0) ? 0 : 2;
		url += (digit1 + digit2);
	}

	return url + '.png';
};

return new google.maps.ImageMapType(layerOpts);
};
</script>
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload(map)">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="trackmap.do" method="get" validate="return false">
<el:table className="form">
<tr class="title">
 <td class="caps"><content:airline /> ACARS TRACK MAP</td>
</tr>
<tr>
 <td class="data"><map:div ID="googleMap" height="620" /></td>
</tr>
</el:table>
<div id="localAirports" style="display:none;"><el:combo name="localAP" size="1" firstEntry="[ SELECT AIRPORT ]" options="${localAP}" className="small localAP" onChange="void golgotha.maps.track.selectLocal(this)" /></div>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<c:set var="maxZoomLevel"  value="${(empty localAP) ? 9 : 13}" scope="request" />
<div id="zoomLevel" class="small bld mapTextLabel"></div>
<script id="mapInit" defer>
<map:point var="mapC" point="${mapCenter}" />
var mapTypes = {mapTypeIds: ['acars_trackmap', google.maps.MapTypeId.SATELLITE]};
var mapOpts = {center:mapC, minZoom:3, maxZoom:${maxZoomLevel}, zoom:6, scrollwheel:false, streetViewControl:false, mapTypeControlOptions:mapTypes};

// Create the map
var map = new google.maps.Map(document.getElementById('googleMap'), mapOpts);
var tmStyledMap = new google.maps.StyledMapType(golgotha.maps.styles.TRACKMAP, {name:'Track Map'});
map.mapTypes.set('acars_trackmap', tmStyledMap);
map.setMapTypeId('acars_trackmap');
google.maps.event.addListener(map, 'maptypeid_changed', golgotha.maps.updateMapText);
var trkLayer = new golgotha.maps.track.ShapeLayer(0.45, 3, ${maxZoomLevel});
map.overlayMapTypes.insertAt(0, trkLayer);
google.maps.event.addListener(map, 'zoom_changed', golgotha.maps.updateZoom);
<c:if test="${!empty localAP}">
google.maps.event.addListener(map, 'zoom_changed', function() { golgotha.util.display('localAirports', (this.getZoom() > 9)); });</c:if>
google.maps.event.addListenerOnce(map, 'tilesloaded', function() {
	map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(document.getElementById('zoomLevel'));
	map.controls[google.maps.ControlPosition.RIGHT_TOP].push(document.getElementById('localAirports'));
	google.maps.event.trigger(this, 'maptypeid_changed');
	google.maps.event.trigger(this, 'zoom_changed');
});

var airportCoords = {};
<c:forEach var="ap" items="${localAP}">
airportCoords['${ap.ICAO}'] = <map:point point="${ap}" /></c:forEach>
golgotha.maps.track.selectLocal = function(combo)
{
if (combo.selectedIndex < 1) return false;
var o = combo[combo.selectedIndex];
var ll = airportCoords[o.getAttribute('icao')];
if (ll != null) map.panTo(ll);
return true;
};
</script>
</body>
</html>
