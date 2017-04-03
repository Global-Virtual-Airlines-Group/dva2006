<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Unvisited Airports Map - ${pilot.name}<c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<map:api version="3" />
<script>
golgotha.local.filter = function(combo)
{
map.clearOverlays();
if (combo.selectedIndex < 1) return false;	
var codes = golgotha.local.accs[golgotha.form.getCombo(combo)];
if (!codes) return false;
codes.forEach(function(c) { 
	var a = golgotha.local.allAirports[c];
	if (!a)
		console.log('Unknown airport ' + c);
	else
		a.setMap(map);
});

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
<el:form action="accairportmap.do" method="get" validate="return false">
<el:table className="form">
<tr class="title caps">
 <td width="65%"><content:airline /> UNVISITED AIRPORTS FOR ${pilot.name}</td>
 <td class="right">ACCOMPLISHMENT <el:combo name="acc" idx="*" firstEntry="[ SELECT ]"  options="${accs}" onChange="void golgotha.local.filter(this)"  /></td>
</tr>
<tr>
 <td class="data" colspan="3"><map:div ID="googleMap" height="540" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script id="mapInit" defer>
<map:point var="golgotha.local.mapC" point="${mapCenter}" />
var mapOpts = {center:golgotha.local.mapC, zoom:6, minZoom:2, maxZoom:11, streetViewControl:false, clickableIcons:false, scrollwheel:true, mapTypeControlOptions:{mapTypeIds:golgotha.maps.DEFAULT_TYPES}};
var map = new golgotha.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', map.closeWindow);
golgotha.local.allAirports = {};
<c:forEach var="ap" items="${airports}">
golgotha.local.allAirports['${ap.ICAO}'] = <map:marker point="${ap}" />;</c:forEach>
golgotha.local.accs = ${jsData};
</script>
</body>
</html>
