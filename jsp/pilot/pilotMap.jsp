<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Pilot Map</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<map:api version="3" libraries="visualization" />
<content:js name="pilotMap" />
<content:googleAnalytics eventSupport="true" />
<content:js name="progressBar" />
<script type="text/javascript">
function reloadMap()
{
var xmlreq = generateXMLRequest();
xmlreq.send(null);
return true;
}
<content:filter roles="HR">
function deleteMarker(id)
{
var xmlreq = getXMLHttpRequest();
xmlreq.open('post', 'pilotmapclear.ws', true);
xmlreq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;

	// Find the marker and remove it
	for (var x = 0; x < allMarkers.length; x++) {
		var mrk = allMarkers[x];
		if (mrk.ID == id) {
			allMarkers.splice(x, 1);
			mrk.setMap(null);
			return true;
		}
	}

	return false;
} // function

gaEvent('Pilot Map', 'Delete Invalid Marker');
xmlreq.send('id=0x' + id.toString(16));
return true;
}
</content:filter>
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:enum var="ranks" className="org.deltava.beans.Rank" />
<content:getCookie name="acarsMapType" default="map" var="gMapType" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="pilotboard.do" method="get" validate="return false">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> PILOT LOCATIONS<span id="isLoading" /></td>
</tr>
<tr>
 <td class="label">Map Type</td>
 <td class="data"><el:check name="mapOpts" type="radio" options="${mapOptions}" value="LOC" onChange="void updateMapOptions(this)" /></td>
</tr>
<tr>
 <td class="data" colspan="2"><map:div ID="googleMap" x="100%" y="525" /></td>
</tr>
<tr class="title caps locFilter">
 <td colspan="2">PILOT LOCATION FILTERING</td>
</tr>
<tr class="locFilter">
 <td class="label">Equipment Program</td>
 <td class="data"><el:combo name="eqType" size="1" firstEntry="ALL" options="${eqTypes}" onChange="void updateMarkers()" /></td>
</tr>
<tr class="locFilter">
 <td class="label">Pilot Ranks</td>
 <td class="data"><el:combo name="rank" size="1" firstEntry="ALL" options="${ranks}" onChange="void updateMarkers()" /></td>
</tr>
</el:table>

<content:filter roles="Pilot">
<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:cmdbutton url="geolocate" label="UPDATE MY LOCATION" /></td>
</tr>
</el:table>
</content:filter>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script type="text/javascript">
<map:point var="mapC" point="${mapCenter}" />
<map:marker var="hq" point="${hq}" />
var mapTypes = {mapTypeIds: golgotha.maps.DEFAULT_TYPES};
var mapOpts = {center:mapC, zoom:6, minZoom:2, maxZoom:11, streetViewControl:false, scrollwheel:false, mapTypeControlOptions: mapTypes};

var allMarkers = [];
var heatMapData = [];
var map = new google.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', function() { map.infoWindow.close(); });
var hmap = new google.maps.visualization.HeatmapLayer({opacity:0.625, radius:2, dissipating:false});
var pBar = progressBar(map, {strokeWidth:200, strokeColor:'#0000a1'});
pBar.getDiv().style.right = '4px';
pBar.getDiv().style.top = '30px';
google.maps.event.addListenerOnce(map, 'tilesloaded', function() {
	hq.setMap(map);
	addOverlay(map, pBar.getDiv());
	reloadMap();
});
</script>
</body>
</html>
