<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Route History Map - ${pilot.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<map:api version="3" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:getCookie name="acarsMapType" default="map" var="gMapType" />

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> ROUTE HISTORY FOR ${pilot.name}</td>
</tr>
<tr>
 <td class="label">Map Legend</td>
 <td class="data"><map:legend color="blue" legend="Airports" /> <map:legend color="white" legend="Home Airport" /></td>
</tr>
<tr>
 <td colspan="2"><map:div ID="googleMap" x="100%" y="510" /></td>
</tr>
</el:table>
<br />
<content:copyright />
</content:region>
</content:page>
<script type="text/javascript">
<map:point var="mapC" point="${home}" />
var mapTypes = {mapTypeIds: golgotha.maps.DEFAULT_TYPES};
var mapOpts = {center:mapC, minZoom:2, zoom:3, scrollwheel:false, streetViewControl:false, mapTypeControlOptions: mapTypes};

// Create the map
var map = new google.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', function() { map.infoWindow.close(); });

// Create the routes
var routes = [];
<c:forEach var="route" items="${routes}">
<c:set var="opacity" value="${(route.flights * 0.75 / maxFlights * 0.4) + 0.3}" scope="page" />
<map:points var="rtPoints" items="${route.points}" />
var route = new google.maps.Polyline({path:rtPoints, strokeColor:'#4080af', strokeWeight:1.25, strokeOpacity:${opacity}, geodesic:false, clickable:false, zIndex:golgotha.maps.z.POLYLINE});
route.srcA = '${route.airportD.ICAO}';
route.dstA = '${route.airportA.ICAO}';
route.setMap(map);
routes.push(route);
</c:forEach>
// Add the airports
<map:markers var="airports" items="${airports}" color="blue" marker="true" />
addMarkers(map, 'airports');

// Add the home airport
<map:marker var="airportH" point="${home}" color="white" marker="true" />
airportH.setMap(map);
</script>
<content:googleAnalytics />
</body>
</html>
