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
<title><content:airline /> ACARS Flight Data - <fmt:int value="${info.ID}" /></title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<content:js name="googleMaps" />
<content:js name="acarsFlightMap" />
<map:api version="2" />
<map:vml-ie />
</head>
<content:copyright visible="false" />
<body onunload="GUnload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="imgPath" name="path.img" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="acarsinfo.do" method="post" validate="return false">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="4">ACARS FLIGHT INFORMATION - FLIGHT #<fmt:int value="${info.ID}" /></td>
</tr>
<tr>
 <td class="label">Pilot Name</td>
 <td class="data">${pilot.rank} <span class="pri bld">${pilot.name}</span> <span class="sec">(${pilot.pilotCode})</span></td>
 <td class="label">ACARS Client Build</td>
 <td class="data">Build <fmt:int value="${conInfo.clientBuild}" /></td>
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

<c:if test="${!empty conInfo}">
<!-- ACARS Connection Information -->
<tr class="title caps">
 <td colspan="4">ACARS CONNECTION DATA</td>
</tr>
<tr>
 <td class="label">Remote Address</td>
 <td class="data" colspan="3">${conInfo.remoteAddr} (${conInfo.remoteHost})</td>
</tr>
<tr>
 <td class="label">Connection ID</td>
 <td class="data"><fmt:hex value="${conInfo.ID}" /></td>
 <td class="label">Connected at</td>
 <td class="data"><fmt:date date="${conInfo.startTime}" /></td>
</tr>
</c:if>

<c:if test="${!empty pirep}">
<!-- ACARS PIREP data -->
<c:set var="cspan" value="${3}" scope="request" />
<c:set var="flightInfo" value="${info}" scope="request" />
<%@include file="/jsp/pilot/pirepACARS.jspf" %>
<c:if test="${!empty pirep.remarks}">
<tr>
 <td class="label" valign="top">Comments</td>
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
 <td class="label" valign="top">Route Map</td>
 <td class="data" colspan="4"><map:div ID="googleMap" x="100%" y="530" /></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<content:filter roles="Admin"><c:if test="${empty pirep}">
<el:table className="bar" space="default" pad="default">
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
<script language="JavaScript" type="text/javascript">
var gRoute;
var routePoints = new Array();
var routeMarkers = new Array();
getACARSData(${info.ID}, '${imgPath}');

// Build the route line and map center
<map:point var="mapC" point="${mapCenter}" />
<map:points var="filedPoints" items="${filedRoute}" />
<map:markers var="filedMarkers" items="${filedRoute}" />
<map:line var="gfRoute" src="filedPoints" color="#A0400F" width="2" transparency="0.7" />

// Build the map
var map = new GMap2(getElement("googleMap"), G_DEFAULT_MAP_TYPES);
map.addControl(new GLargeMapControl());
map.addControl(new GMapTypeControl());
map.setCenter(mapC, getDefaultZoom(${pirep.distance}));
map.enableDoubleClickZoom();
map.enableContinuousZoom();

// Add the filed route and markers
addMarkers(map, 'gfRoute');
addMarkers(map, 'filedMarkers');
</script></c:if>
<content:googleAnalytics />
</body>
</map:xhtml>
