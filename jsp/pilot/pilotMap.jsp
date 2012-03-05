<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> Pilot Map</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<map:api version="3" />
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
xmlreq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
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
 <td class="data" colspan="2"><map:div ID="googleMap" x="100%" y="525" /></td>
</tr>
<tr class="title caps">
 <td colspan="2">PILOT LOCATION FILTERING</td>
</tr>
<tr>
 <td class="label">Equipment Program</td>
 <td class="data"><el:combo name="eqType" size="1" firstEntry="ALL" options="${eqTypes}" onChange="void updateMarkers()" /></td>
</tr>
<tr>
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
// Build the map
<map:point var="mapC" point="${mapCenter}" />
<map:marker var="hq" point="${hq}" />
var mapTypes = {mapTypeIds: golgotha.maps.DEFAULT_TYPES};
var mapOpts = {center:mapC, zoom:6, streetViewControl:false, scrollwheel:false, mapTypeControlOptions: mapTypes};

var allMarkers = [];
var map = new google.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', function() { map.infoWindow.close(); });
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
</map:xhtml>
