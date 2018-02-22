<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Route History Map - ${pilot.name}</title>
<content:expire expires="3600" />
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<content:json />
<map:api version="3" />
<content:js name="markerWithLabel" />
<content:js name="myRouteMap" />
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> ROUTE HISTORY FOR ${pilot.name}<span id="isLoading"></span></td>
</tr>
<tr>
 <td class="label">Map Legend</td>
 <td class="data"><map:legend color="blue" legend="Airports" /> <map:legend color="white" legend="Home Airport" /></td>
</tr>
<tr>
 <td colspan="2"><map:div ID="googleMap" height="510" /></td>
</tr>
</el:table>
<br />
<content:copyright />
</content:region>
</content:page>
<script id="mapInit">
<map:point var="golgotha.local.mapC" point="${home}" />
var mapTypes = {mapTypeIds: golgotha.maps.DEFAULT_TYPES};
var mapOpts = {center:golgotha.local.mapC, minZoom:2, zoom:3, scrollwheel:false, clickableIcons:false, streetViewControl:false, mapTypeControlOptions: mapTypes};

// Create the map
var map = new golgotha.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="SATELLITE" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', function() { map.infoWindow.close(); golgotha.routeMap.reset(); });

// Add the home airport
<map:marker var="airportH" point="${home}" color="white" marker="true" />
airportH.setMap(map);

golgotha.routeMap.id = '${pilot.hexID}';
google.maps.event.addListenerOnce(map, 'tilesloaded', function() { golgotha.routeMap.load(); });
</script>
<content:googleAnalytics />
</body>
</html>
