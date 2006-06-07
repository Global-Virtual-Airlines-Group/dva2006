<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
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
<map:api version="2" />
<map:vml-ie />
</head>
<content:copyright visible="false" />
<body onunload="GUnload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
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
</el:table>

<c:if test="${!empty pirep}">
<!-- ACARS PIREP data -->
<el:table className="form" space="default" pad="default">
<c:if test="${!empty pirep.remarks}">
<%@include file="/jsp/pilot/pirepACARS.jspf" %>
<tr>
 <td class="label">Comments</td>
 <td class="data"><fmt:text value="${pirep.remarks}" /></td>
</tr>
</c:if>
</el:table>
</c:if>

<c:if test="${fn:sizeof(mapRoute) > 0}">
<!-- Flight Map -->
<el:form action="acarsinfo.do" method="post" validate="return false">
<el:table className="form" space="default" pad="default">
<tr>
 <td class="label">Route Map Data</td>
 <td class="data"><span class="bld"><el:box name="showRoute" idx="*" onChange="void toggleMarkers(map, 'gRoute')" label="Route" checked="true" />
<el:box name="showFDR" idx="*" onChange="void toggleMarkers(map, 'routeMarkers')" label="Flight Data" checked="true" /> 
<el:box name="showFPlan" idx="*" onChange="void toggleMarkers(map, 'gfRoute')" label="Flight Plan" checked="true" /> 
<el:box name="showFPMarkers" idx="*" onChange="void toggleMarkers(map, 'filedMarkers')" label="Navaid Markers" checked="true" /></span></td>
</tr>
<tr>
 <td class="label" valign="top">Route Map</td>
 <td class="data"><map:div ID="googleMap" x="620" y="530" /></div>
</tr>
</el:table>
</el:form>
</c:if>

<!-- Button Bar -->
<content:filter roles="Admin"><c:if test="${empty pirep}">
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:cmdbutton url="acarsdelf" linkID="0x${info.ID}" label="DELETE FLIGHT INFORMATION ENTRY" /></td>
</tr>
</el:table>
</c:if></content:filter>
<br />
<content:copyright />
</content:region>
</content:page>
<c:if test="${fn:sizeof(mapRoute) > 0}">
<script language="JavaScript" type="text/javascript">
// Build the route line and map center
<map:point var="mapC" point="${mapCenter}" />
<map:points var="routePoints" items="${mapRoute}" />
<map:markers var="routeMarkers" items="${mapRoute}" />
<map:line var="gRoute" src="routePoints" color="#4080AF" width="3" transparency="0.85" />
<map:points var="filedPoints" items="${filedRoute}" />
<map:markers var="filedMarkers" items="${filedRoute}" />
<map:line var="gfRoute" src="filedPoints" color="#A0400F" width="2" transparency="0.75" />

// Build the map
var map = new GMap2(getElement("googleMap"), [G_MAP_TYPE, G_SATELLITE_TYPE]);
map.addControl(new GSmallZoomControl());
map.addControl(new GMapTypeControl());
map.setCenter(mapC, getDefaultZoom(${pirep.distance}));

// Add the route and markers
addMarkers(map, 'gRoute');
addMarkers(map, 'gfRoute');
addMarkers(map, 'routeMarkers');
addMarkers(map, 'filedMarkers');
</script>
</c:if>
</body>
</map:xhtml>
