<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> Route History Map - ${pilot.name}</title>
<content:css name="main" browserSpecific="true" />
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
var mapOpts = {center:mapC, zoom:3, scrollwheel:false, streetViewControl:false, mapTypeControlOptions: mapTypes};

// Create the map
var map = new google.maps.Map(getElement('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content: ''});
google.maps.event.addListener(map, 'click', function() { map.infoWindow.close(); });

// Create the routes
var routes = [];
<c:forEach var="route" items="${routes}">
// ${route}
<c:choose>
<c:when test="${fn:sizeof(route.points) > 2}">
<map:points var="rtPoints" items="${route.points}" />
var route = new google.maps.Polyline({path:rtPoints, strokeColor:'#4080af', strokeWeight:1.5, strokeOpacity:0.55, geodesic:false, clickable:false});
</c:when>
<c:otherwise>
<map:point var="aD" point="${route.airportD}" />
<map:point var="aA" point="${route.airportA}" />
var route = new google.maps.Polyline({path:[aD, aA], strokeColor:'#4080af', strokeWeight:1.5, strokeOpacity:0.55, geodesic:true, clickable:false});
</c:otherwise>
</c:choose>

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
</map:xhtml>
