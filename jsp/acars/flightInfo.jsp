<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Flight Data - Flight <fmt:int value="${info.ID}" /></title>
<content:expire expires="30" />
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<content:json />
<map:api version="3" />
<content:js name="acarsFlightMap" />
<content:googleAnalytics eventSupport="true" />
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
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
 <td class="data">${info.equipmentType} <span class="sec">(using ${info.simulator.name}<c:if test="${info.simMajor > 1}"> <content:simVersion sim="${info.simulator}" major ="${info.simMajor}" minor="${info.simMinor}" /></c:if>)</span></td>
 <td class="label">Flight Code</td>
 <td class="data pri bld">${info.flightCode}</td>
</tr>
<tr>
 <td class="label">Flight from</td>
 <td class="data">${info.airportD.name} (<el:cmd url="airportinfo" linkID="${info.airportD.IATA}" className="plain"><fmt:airport airport="${info.airportD}" /></el:cmd>)</td>
 <td class="label">Flight to</td>
 <td class="data">${info.airportA.name} (<el:cmd url="airportinfo" linkID="${info.airportA.IATA}" className="plain"><fmt:airport airport="${info.airportA}" /></el:cmd>)</td>
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

<c:if test="${mapRoute.size() > 0}">
<!-- Flight Map -->
<tr>
 <td class="label">Route Map Data</td>
 <td class="data" colspan="4"><span class="bld"><el:box name="showRoute" idx="*" onChange="void map.toggle(golgotha.maps.acarsFlight.gRoute, this.checked)" label="Route" checked="false" />
<el:box name="showFDR" idx="*" onChange="void map.toggle(golgotha.maps.acarsFlight.routeMarkers, this.checked)" label="Flight Data" checked="false" /> 
<el:box name="showFPlan" idx="*" onChange="void map.toggle(golgotha.maps.acarsFlight.gfRoute, this.checked)" label="Flight Plan" checked="true" /> 
<el:box name="showFPMarkers" idx="*" onChange="void map.toggle(golgotha.maps.acarsFlight.filedMarkers, this.checked)" label="Navaid Markers" checked="true" /></span>
<span id="routeProgress" class="small"></span></td>
</tr>
<tr>
 <td class="data" colspan="5"><map:div ID="googleMap" height="530" /></td>
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
<c:if test="${mapRoute.size() > 0}">
<script id="mapInit" async>
// Build the route line and map center
<map:point var="golgotha.local.mapC" point="${mapCenter}" />
<map:points var="golgotha.maps.acarsFlight.filedPoints" items="${filedRoute}" />
<map:markers var="golgotha.maps.acarsFlight.filedMarkers" items="${filedRoute}" />
<map:line var="golgotha.maps.acarsFlight.gfRoute" src="golgotha.maps.acarsFlight.filedPoints" color="#a0400f" width="2" transparency="0.7" geodesic="true" />

// Build the map
var mapTypes = {mapTypeIds:golgotha.maps.DEFAULT_TYPES};
var mapOpts = {center:golgotha.local.mapC, minZoom:2, zoom:golgotha.maps.util.getDefaultZoom(${pirep.distance}), scrollwheel:false, clickableIcons:false, streetViewControl:false, mapTypeControlOptions:mapTypes};
var map = new golgotha.maps.Map(document.getElementById('googleMap'), mapOpts);
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', map.closeWindow);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
golgotha.maps.acarsFlight.getACARSData(${info.ID});

// Add the filed route and markers
map.addMarkers(golgotha.maps.acarsFlight.gfRoute);
map.addMarkers(golgotha.maps.acarsFlight.filedMarkers);
</script></c:if>
</body>
</html>
