<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Track Map</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="acarsMap" />
<content:googleAnalytics eventSupport="true" />
<map:api version="3" />
<content:js name="googleMapsStyles" />
<script type="text/javascript">
golgotha.maps.ShapeLayer = function(tx, minZ, maxZ)
{
var layerOpts = {minZoom:minZ, maxZoom:maxZ, isPng:true, opacity:tx, tileSize:new google.maps.Size(256,256)};
layerOpts.myBaseURL = 'http://' + location.host + '/track/';
layerOpts.getTileUrl = function(pnt, zoom) {
	if (zoom > this.maxZoom) return '';
	var url = this.myBaseURL;
	var masks = [0, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288];

	// Get the tile numbers
	for (var x = zoom; x > 0; x--) {
		var digit1 = ((masks[x] & pnt.x) == 0) ? 0 : 1;
		var digit2 = ((masks[x] & pnt.y) == 0) ? 0 : 2;
		url = url + (digit1 + digit2);
	}

	return url + '.png';
}

return new google.maps.ImageMapType(layerOpts);
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<tr class="title">
 <td class="caps"><content:airline /> ACARS TRACK MAP</td>
</tr>
<tr>
 <td class="data"><map:div ID="googleMap" x="100%" y="620" /></td>
</tr>
</el:table>
<br />
<content:copyright />
</content:region>
</content:page>
<div id="zoomLevel" class="small bld mapTextLabel"></div>
<script type="text/javascript">
<map:point var="mapC" point="${mapCenter}" />
var mapTypes = {mapTypeIds: ['acars_trackmap', google.maps.MapTypeId.SATELLITE]};
var mapOpts = {center:mapC, minZoom:3, maxZoom:13, zoom:6, scrollwheel:false, streetViewControl:false, mapTypeControlOptions:mapTypes};

// Create the map
var map = new google.maps.Map(document.getElementById('googleMap'), mapOpts);
var tmStyledMap = new google.maps.StyledMapType(golgotha.maps.styles.TRACKMAP, {name:'Track Map'});
map.mapTypes.set('acars_trackmap', tmStyledMap);
map.setMapTypeId('acars_trackmap');
google.maps.event.addListener(map, 'maptypeid_changed', golgotha.maps.updateMapText);
var trkLayer = new golgotha.maps.ShapeLayer(0.425, 3, 12);
map.overlayMapTypes.insertAt(0, trkLayer);
google.maps.event.addListener(map, 'zoom_changed', golgotha.maps.updateZoom);
google.maps.event.addListenerOnce(map, 'tilesloaded', function() {
	map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(document.getElementById('zoomLevel'));
	google.maps.event.trigger(this, 'maptypeid_changed');
	google.maps.event.trigger(this, 'zoom_changed');
});
</script>
</body>
</html>
