<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> ACARS Flight Data - Flight <fmt:int value="${info.ID}" /></title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<map:api version="3" />
<content:js name="acarsFlightMap" />
<content:googleAnalytics eventSupport="true" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:getCookie name="acarsMapType" default="map" var="gMapType" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="acarsinfo.do" method="post" validate="return false">
<el:table className="form">
<tr class="title caps">
 <td colspan="4">ACARS FLIGHT INFORMATION - FLIGHT #<fmt:int value="${info.ID}" /></td>
</tr>
<tr>
 <td class="label">Pilot Name</td>
 <td class="data">${pilot.rank.name} <span class="pri bld">${pilot.name}</span> <span class="sec">(${pilot.pilotCode})</span></td>
 <td class="label">ACARS Client Build</td>
 <td class="data">Build <fmt:int value="${info.clientBuild}" /><c:if test="${info.beta > 0}"> (Beta ${info.beta})</c:if></td>
</tr>
<tr>
 <td class="label">Remote Address</td>
 <td class="data" colspan="3">${info.remoteAddr} (${info.remoteHost})</td>
</tr>
<tr>
 <td class="label">Equipment Type</td>
 <td class="data">${info.equipmentType} <span class="sec">(using FS${info.FSVersion})</span></td>
 <td class="label">Flight Code</td>
 <td class="data pri bld">${info.flightCode}</td>
</tr>
<tr>
 <td class="label">Flight from</td>
 <td class="data">${info.airportD.name} (<fmt:airport airport="${info.airportD}" />)</td>
 <td class="label">Flight to</td>
 <td class="data">${info.airportA.name} (<fmt:airport airport="${info.airportA}" />)</td>
</tr>
<tr>
 <td class="label">Flight started at</td>
 <td class="data"><fmt:date date="${info.startTime}" /></td>
 <td class="label">Flight ended at</td>
 <td class="data"><fmt:date date="${info.endTime}" /></td>
</tr>
<tr>
 <td class="label">Filed Route</td>
 <td class="data" colspan="3">${info.route}</td>
</tr>
<tr>
 <td class="label">Pilot Remarks</td>
 <td class="data" colspan="3">${info.remarks}</td>
</tr>
<c:if test="${(!empty dispatcher) || (!empty route)}">
<c:set var="cspan" value="${(!empty dispatcher) && (!empty route) ? 1 : 3}" scope="page" />
<!-- ACARS Dispatch Information -->
<tr class="title caps">
 <td colspan="4">ACARS DISPATCH DATA</td>
</tr>
<c:if test="${!empty dispatcher}">
<tr>
 <td class="label">Dispatcher</td>
 <td class="data" colspan="${cspan}">${dispatcher.name} <span class="pri bld">(${dispatcher.pilotCode})</span></td>
</tr>
</c:if>
<c:if test="${!empty route}">
 <td class="label">Route</td>
 <td class="data" colspan="${cspan}"><fmt:int value="${route.ID}" />, used <fmt:int value="${route.useCount}" /> times</td>
</c:if>
</c:if>
<c:if test="${!empty pirep}">
<!-- ACARS PIREP data -->
<c:set var="cspan" value="3" scope="page" />
<c:set var="flightInfo" value="${info}" scope="page" />
<%@include file="/jsp/pilot/pirepACARS.jspf" %>
<c:if test="${!empty pirep.remarks}">
<tr>
 <td class="label top">Comments</td>
 <td class="data" colspan="3"><fmt:text value="${pirep.remarks}" /></td>
</tr>
</c:if>
</c:if>

<c:if test="${fn:sizeof(mapRoute) > 0}">
<!-- Flight Map -->
<tr>
 <td class="label">Route Map Data</td>
 <td class="data" colspan="4"><span class="bld"><el:box name="showRoute" idx="*" onChange="void toggleMarkers(map, 'gRoute', this)" label="Route" checked="false" />
<el:box name="showFDR" idx="*" onChange="void toggleMarkers(map, 'routeMarkers', this)" label="Flight Data" checked="false" /> 
<el:box name="showFPlan" idx="*" onChange="void toggleMarkers(map, 'gfRoute', this)" label="Flight Plan" checked="true" /> 
<el:box name="showFPMarkers" idx="*" onChange="void toggleMarkers(map, 'filedMarkers', this)" label="Navaid Markers" checked="true" /></span>
<span id="routeProgress" class="small"></span></td>
</tr>
<tr>
 <td class="label top">Route Map</td>
 <td class="data" colspan="4"><map:div ID="googleMap" x="100%" y="530" /></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<content:filter roles="Admin"><c:if test="${empty pirep}">
<el:table className="bar">
<tr>
 <td><el:cmdbutton url="acarsdelf" link="${info}" label="DELETE FLIGHT INFORMATION ENTRY" /></td>
</tr>
</el:table>
</c:if></content:filter>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<c:if test="${fn:sizeof(mapRoute) > 0}">
<script type="text/javascript">
var gRoute;
var routePoints = [];
var routeMarkers = [];

// Build the route line and map center
<map:point var="mapC" point="${mapCenter}" />
<map:points var="filedPoints" items="${filedRoute}" />
<map:markers var="filedMarkers" items="${filedRoute}" />
<map:line var="gfRoute" src="filedPoints" color="#a0400f" width="2" transparency="0.7" geodesic="true" />

// Build the map
var mapTypes = {mapTypeIds: golgotha.maps.DEFAULT_TYPES};
var mapOpts = {center: mapC, minZoom:2, zoom: getDefaultZoom(${pirep.distance}), scrollwheel:false, streetViewControl:false, mapTypeControlOptions: mapTypes};
var map = new google.maps.Map(document.getElementById('googleMap'), mapOpts);
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', function() { map.infoWindow.close(); });
<map:type map="map" type="${gMapType}" default="TERRAIN" />
getACARSData(${info.ID});

// Add the filed route and markers
addMarkers(map, 'gfRoute');
addMarkers(map, 'filedMarkers');
</script></c:if>
</body>
</map:xhtml>
