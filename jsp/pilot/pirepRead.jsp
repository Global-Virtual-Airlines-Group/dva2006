<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page buffer="80kb" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Flight Report - ${pirep.flightCode}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<c:set var="scoreCR" value="${access.canApprove && crAccess.canScore}" scope="request" />
<c:if test="${googleMap}">
<content:js name="googleMaps" />
<map:api version="1" />
<map:vml-ie />
</c:if>
<c:if test="${scoreCR}">
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCheckBox(form.crApprove, 1, 'Check Ride status')) return false;

setSubmit();
disableButton('CRButton');
return true;
}
</script>
</c:if>
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<c:if test="${scoreCR}">
<form method="post" action="pirepscore.do?id=${fn:hex(pirep.ID)}" onsubmit="return validate(this)">
</c:if>
<el:table className="form" pad="default" space="default">
<!-- PIREP Title Bar -->
<tr class="title">
 <td class="caps" colspan="2">FLIGHT ${pirep.flightCode} FLOWN ON 
 <fmt:date fmt="d" date="${pirep.date}" /> by ${pilot.name}</td>
</tr>

<!-- Pirep Data -->
<tr>
 <td class="label">Pilot Code / Rank</td>
 <td class="data">${pilot.pilotCode} (${pilot.rank}, ${pilot.equipmentType})</td>
</tr>
<content:filter roles="HR,PIREP">
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><a href="mailto:${pilot.email}">${pilot.email}</a></td>
</tr>
</content:filter>
<tr>
 <td class="label">Status</td>
 <td class="data bld sec">${statusMsg} <c:if test="${fn:AssignID(pirep) > 0}"><span class="ter bld">FLIGHT ASSIGNMENT</span></c:if></td>
</tr>
<tr>
 <td class="label">Airline Name</td>
 <td class="data">${pirep.airline.name}</td>
</tr>
<tr>
 <td class="label">Equipment Type</td>
 <td class="data">${pirep.equipmentType}</td>
</tr>
<tr>
 <td class="label">Departed from</td>
 <td class="data">${pirep.airportD.name} (<fmt:airport airport="${pirep.airportD}" />)</td>
</tr>
<tr>
 <td class="label">Arrived at</td>
 <td class="data">${pirep.airportA.name} (<fmt:airport airport="${pirep.airportA}" />)</td>
</tr>
<tr>
 <td class="label">Flight Simulator</td>
<c:choose>
<c:when test="${pirep.FSVersion == 0}">
 <td class="data sec bld">UNKNOWN</td>
</c:when>
<c:otherwise>
 <td class="data sec bld">Microsoft Flight Simulator ${pirep.FSVersion}</td>
</c:otherwise>
</c:choose>
</tr>
<tr>
 <td class="label">Other Information</td>
 <td class="data"><c:if test="${fn:isOnline(pirep)}">Flight Leg flown online using the ${fn:network(pirep)} network<br /></c:if>
<c:if test="${fn:isACARS(pirep)}">
<div class="sec bld caps">Flight Leg data logged using <content:airline /> ACARS</div>
</c:if>
<c:if test="${!fn:isRated(pirep)}">
<div class="warning bld caps">Flight Leg flown without Aircraft type rating</div>
</c:if>
<c:if test="${fn:routeWarn(pirep)}">
<div class="warning bld caps">Flight Route not found in <content:airline /> schedule database</div>
</c:if>
<c:if test="${fn:timeWarn(pirep)}">
<div class="warning bld caps">Flight Length outside Schedule Guidelines</div>
</c:if>
<c:if test="${!empty pirep.captEQType}">
<div class="ter bld caps">Flight Leg counts towards promotion to Captain in the <fmt:list value="${pirep.captEQType}" delim=", " /></div>
</c:if>
<c:if test="${!empty event}">
<div class="pri bld caps">Flight Leg part of the ${event.name} Online Event</div>
</c:if>
 </td>
</tr>
<tr>
 <td class="label">Flight Distance</td>
 <td class="data pri bld"><fmt:int fmt="##,##0" value="${pirep.distance}" /> miles</td>
</tr>
<tr>
 <td class="label">Logged Time</td>
 <td class="data"><fmt:dec value="${pirep.length / 10.0}" /> hours</td>
</tr>
<c:if test="${!empty pirep.remarks}">
<tr>
 <td class="label">Pilot Comments</td>
 <td class="data"><fmt:text value="${pirep.remarks}" /></td>
</tr>
</c:if>
<c:if test="${fn:isACARS(pirep)}">
<%@include file="/jsp/pilot/pirepACARS.jsp" %> 
</c:if>
<tr>
<c:if test="${googleMap}">
 <td class="label">Route Map Data</td>
 <td class="data"><span class="bld"><el:box name="showRoute" idx="*" onChange="void toggleMarkers(map, 'gRoute')" label="Route" checked="true" />
<c:if test="${fn:isACARS(pirep)}"><el:box name="showFDR" idx="*" onChange="void toggleMarkers(map, 'routeMarkers')" label="Flight Data" checked="true" /> </c:if>
<c:if test="${!empty filedRoute}"><el:box name="showFPlan" idx="*" onChange="void toggleMarkers(map, 'gfRoute')" label="Flight Plan" checked="true" /> </c:if>
<el:box name="showFPMarkers" idx="*" onChange="void toggleMarkers(map, 'filedMarkers')" label="Navaid Markers" checked="true" /></span></td>
</tr>
<tr>
 <td class="label" valign="top">Route Map</td>
 <td class="data"><div id="googleMap" style="width:640px; height:580px" /></td>
</c:if>
<c:if test="${!googleMap}">
 <td class="label" valign="top">Route Map</td>
 <td class="data"><img src="http://maps.fallingrain.com/perl/map.cgi?x=620&y=365&kind=topo&lat=${pirep.airportD.latitude}&long=${pirep.airportD.longitude}&name=${pirep.airportD.name}&c=1&lat=${pirep.airportA.latitude}&long=${pirep.airportA.longitude}&name=${pirep.airportA.name}&c=1"
alt="${pirep.airportD.name} to ${pirep.airportA.name}" width="620" height="365" /></td>
</c:if>
</tr>
</el:table>

<!-- PIREP Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td>
<c:if test="${access.canSubmit}">
 <el:cmdbutton url="submit" linkID="0x${pirep.ID}" label="SUBMIT FLIGHT REPORT" />
</c:if>
<c:if test="${access.canEdit}">
 <el:cmdbutton url="pirep" linkID="0x${pirep.ID}" op="edit" label="EDIT REPORT" />
</c:if>
<c:if test="${access.canHold}">
 <el:cmdbutton url="dispose" linkID="0x${pirep.ID}" op="hold" label="HOLD" />
</c:if>
<c:if test="${access.canApprove && (empty checkRide)}">
 <el:cmdbutton url="dispose" linkID="0x${pirep.ID}" op="approve" label="APPROVE FLIGHT" />
</c:if>
<c:if test="${access.canReject}">
 <el:cmdbutton url="dispose" linkID="0x${pirep.ID}" op="reject" label="REJECT FLIGHT" />
</c:if>
<c:if test="${access.canDelete}">
 <el:cmdbutton url="pirepdelete" linkID="0x${pirep.ID}" label="DELETE FLIGHT REPORT" />
</c:if>
 </td>
</tr>
</el:table>
<c:if test="${scoreCR}"></form></c:if>
<content:copyright />
</div>
<c:if test="${googleMap}">
<script language="JavaScript" type="text/javascript">
// Build the route line and map center
<map:point var="mapC" point="${mapCenter}" />
<map:points var="routePoints" items="${mapRoute}" />
<map:markers var="routeMarkers" items="${mapRoute}" />
<map:line var="gRoute" src="routePoints" color="#4080AF" width="3" transparency="0.85" />
<c:if test="${!empty filedRoute}">
<map:points var="filedPoints" items="${filedRoute}" />
<map:markers var="filedMarkers" items="${filedRoute}" />
<map:line var="gfRoute" src="filedPoints" color="#80800F" width="2" transparency="0.75" />
</c:if>
// Build the map
var map = new GMap(getElement("googleMap"), [G_MAP_TYPE, G_SATELLITE_TYPE]);
map.addControl(new GSmallZoomControl());
map.addControl(new GMapTypeControl());
map.centerAndZoom(mapC, getDefaultZoom(${pirep.distance}));

// Add the route and markers
addMarkers(map, 'gRoute');
addMarkers(map, 'routeMarkers');
<c:if test="${!empty filedRoute}">
addMarkers(map, 'gfRoute');
addMarkers(map, 'filedMarkers');
</c:if>
<c:if test="${empty filedRoute}">
// Airport markers
<map:marker var="gmA" point="${pirep.airportA}" />
<map:marker var="gmD" point="${pirep.airportD}" />
var filedMarkers = [gmA, gmD];
addMarkers(map, 'filedMarkers');
</c:if>
</script>
</c:if>
</body>
</html>
