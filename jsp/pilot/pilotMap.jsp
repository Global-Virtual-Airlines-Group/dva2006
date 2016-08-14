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
<content:expire expires="3600" />
<content:css name="main" />
<content:css name="form" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<map:api version="3" libraries="visualization" />
<content:js name="pilotMap" />
<content:googleAnalytics eventSupport="true" />
<content:js name="progressBar" />
<content:filter roles="HR">
<script type="text/javascript">
golgotha.pilotMap.deleteMarker = function(id)
{
var xmlreq = new XMLHttpRequest();
xmlreq.open('POST', 'pilotmapclear.ws', true);
xmlreq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	for (var x = 0; x < golgotha.pilotMap.mrks.length; x++) {
		var mrk = golgotha.pilotMap.mrks[x];
		if (mrk.ID == id) {
			golgotha.pilotMap.mrks.remove(mrk);
			mrk.setMap(null);
			return true;
		}
	}

	return false;
}

golgotha.event.beacon('Pilot Map', 'Delete Invalid Marker');
xmlreq.send('id=0x' + id.toString(16));
return true;
};
</script></content:filter>
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
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
 <td colspan="2"><content:airline /> PILOT LOCATIONS<span id="isLoading"></span></td>
</tr>
<tr>
 <td class="label">Map Type</td>
 <td class="data"><el:check name="mapOpts" type="radio" options="${mapOptions}" value="LOC" onChange="void golgotha.pilotMap.updateMapOptions(this)" /></td>
</tr>
<tr>
 <td class="data" colspan="2"><map:div ID="googleMap" height="525" /></td>
</tr>
<tr class="title caps locFilter">
 <td colspan="2">PILOT LOCATION FILTERING</td>
</tr>
<tr class="locFilter">
 <td class="label nophone">Equipment Program</td>
 <td class="data"><el:combo name="eqType" size="1" firstEntry="[ ALL PROGRAMS ]" options="${eqTypes}" onChange="void golgotha.pilotMap.updateMarkers()" /></td>
</tr>
<tr class="locFilter">
 <td class="label nophone">Pilot Ranks</td>
 <td class="data"><el:combo name="rank" size="1" firstEntry="[ ALL RANKS ]" options="${ranks}" onChange="void golgotha.pilotMap.updateMarkers()" /></td>
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
<script id="mapInit" defer>
<map:point var="golgotha.local.mapC" point="${mapCenter}" />
<map:marker var="hq" point="${hq}" />
var mapOpts = {center:golgotha.local.mapC, zoom:6, minZoom:2, maxZoom:11, streetViewControl:false, clickableIcons:false, scrollwheel:false, mapTypeControlOptions:{mapTypeIds:golgotha.maps.DEFAULT_TYPES}};
var map = new golgotha.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', map.closeWindow);
golgotha.pilotMap.hmap = new google.maps.visualization.HeatmapLayer({opacity:0.625, radius:2, dissipating:false});
golgotha.pilotMap.pBar = progressBar(map, {strokeWidth:200, strokeColor:'#0000a1'});
golgotha.pilotMap.pBar.getDiv().style.right = '4px';
golgotha.pilotMap.pBar.getDiv().style.top = '30px';
map.controls[google.maps.ControlPosition.RIGHT_TOP].push(golgotha.pilotMap.pBar.getDiv());
google.maps.event.addListenerOnce(map, 'tilesloaded', function() {
	hq.setMap(map);
	var xmlreq = golgotha.pilotMap.generateXMLRequest();
	xmlreq.send(null);	
});
</script>
</body>
</html>
