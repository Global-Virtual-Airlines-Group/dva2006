<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> Route Map - ${pilot.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="googleMaps" />
<content:sysdata var="imgPath" name="path.img" />
<map:api version="2" />
<map:vml-ie />
</head>
<content:copyright visible="false" />
<body onunload="GUnload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:getCookie name="acarsMapType" default="map" var="gMapType" />

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> ROUTE HISTORY FOR ${pilot.name}</td>
</tr>
<tr>
 <td class="label">Map Legend</td>
 <td class="data"><map:legend color="blue" legend="Airports" /> <map:legend color="white" legend="My Home Airport" /></td>
</tr>
<tr>
 <td colspan="2"><map:div ID="googleMap" x="100%" y="510" /></td>
</tr>
</el:table>
<br />
<content:copyright />
</content:region>
</content:page>
<script language="JavaScript" type="text/javascript">
<map:point var="mapC" point="${home}" />

// Create the map
var map = new GMap2(getElement("googleMap"), {mapTypes:[G_NORMAL_MAP, G_SATELLITE_MAP, G_PHYSICAL_MAP]});
map.addControl(new GLargeMapControl3D());
map.addControl(new GMapTypeControl());
map.addControl(new GOverviewMapControl());
map.setCenter(mapC, 3);
map.enableDoubleClickZoom();
map.enableContinuousZoom();
<map:type map="map" type="${gMapType}" default="G_PHYSICAL_MAP" />

// Create the routes
var routes = [];
<c:forEach var="route" items="${routes}">
<map:point var="aD" point="${route.airportD}" />
<map:point var="aA" point="${route.airportA}" />
var route = new GPolyline([aD, aA], '#4080AF', 1.5, 0.6, { geodesic:true });
routes.push(route);
</c:forEach>

// Add the airports
<map:markers var="airports" items="${airports}" color="blue" marker="true" />
addMarkers(map, 'routes');
addMarkers(map, 'airports');

// Add the home airport
<map:marker var="airportH" point="${home}" color="white" marker="true" />
map.addOverlay(airportH);
</script>
<content:googleAnalytics />
</body>
</map:xhtml>
