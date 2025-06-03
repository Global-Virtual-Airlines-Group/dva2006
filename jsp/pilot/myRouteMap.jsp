<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_mapbox.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Route History Map - ${pilot.name}</title>
<content:expire expires="3600" />
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:googleAnalytics />
<content:js name="common" />
<map:api version="3" />
<content:js name="myRouteMap" />
<content:cspHeader />
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="myroutemap.do" method="get" validate="return false"> 
<el:table className="form">
<tr class="title caps">
 <td colspan="4"><content:airline /> ROUTE HISTORY FOR ${pilot.name}<span class="nophone"> (${pilot.pilotCode})</span><span id="isLoading"></span></td>
</tr>
<tr>
 <td class="label">Map Legend</td>
 <td class="data"><map:legend color="blue" legend="Airports" />&nbsp;<map:legend color="white" legend="Home Airport" /></td>
 <td class="label">Date Range</td>
 <td class="data"><el:combo name="days" options="${dateOptions}" size="1" idx="*" value="0" onChange="void golgotha.routeMap.updateDates(this)" /></td>
</tr>
<tr>
 <td colspan="4"><map:div ID="mapBox" height="620" /></td>
</tr>
<tr class="nophone">
 <td colspan="4" class="mid small ita">Left-click on an Airport to view all the Routes flown to/from this Airport. Right-click on an Airport to view the last flight tracks in/out.</td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script async>
<map:token />
golgotha.routeMap.id = '${pilot.hexID}';
<map:point var="golgotha.local.mapC" point="${home}" />
<map:marker var="airportH" point="${home}" color="white" marker="true" />

// Create the map
const mapOpts = {center:golgotha.local.mapC, minZoom:2, zoom:3, scrollZoom:true, projection:'globe', style:'mapbox://styles/mapbox/outdoors-v12'};
const map = new golgotha.maps.Map(document.getElementById('mapBox'), mapOpts);
map.addControl(new mapboxgl.FullscreenControl(), 'top-right');
map.addControl(new mapboxgl.NavigationControl(), 'top-right');
//map.on('click', function() { golgotha.routeMap.reset(); });
map.on('style.load', golgotha.maps.updateMapText);
map.once('load', function() {
	map.addControl(new golgotha.maps.BaseMapControl(golgotha.maps.DEFAULT_TYPES), 'top-left');
	airportH.setMap(map);
	golgotha.routeMap.load(0);
});
</script>
</body>
</html>
