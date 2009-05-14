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
<title><content:airline /> Flight Report - ${pirep.flightCode}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:googleAnalytics eventSupport="true" />
<c:if test="${googleMap}">
<content:os windows="true"><c:set var="showGEarth" value="false" scope="request" /></content:os>
<content:js name="googleMaps" />
<map:api version="2" />
<map:vml-ie />
</c:if>
<c:if test="${scoreCR}">
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;

// Validate form
var act = form.action;
if (act.indexOf('release.do') == -1)
	if (!validateCheckBox(form.crApprove, 1, 'Check Ride status')) return false;

setSubmit();
disableButton('CRButton');
return true;
}
</script></c:if>
<c:if test="${isACARS}">
<content:sysdata var="imgPath" name="path.img" />
<content:js name="acarsFlightMap" />
</c:if>
</head>
<content:copyright visible="false" />
<body onunload="GUnload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:getCookie name="acarsMapType" default="map" var="gMapType" />

<!-- Main Body Frame -->
<content:region id="main">
<c:choose>
<c:when test="${scoreCR && extPIREP}">
<form method="post" action="extpirepscore.do?id=${checkRide.hexID}" onsubmit="return validate(this)">
</c:when>
<c:when test="${scoreCR}">
<form method="post" action="pirepscore.do?id=${pirep.hexID}" onsubmit="return validate(this)">
</c:when>
<c:when test="${access.canDispose}">
<form method="post" action="pirep.do?id=${pirep.hexID}">
</c:when>
<c:when test="${isACARS}">
<form method="get" action="pirep.do?id=${pirep.hexID}" onsubmit="return false">
</c:when>
</c:choose>
<el:table className="form" pad="default" space="default">
<!-- PIREP Title Bar -->
<tr class="title">
 <td class="caps" colspan="2">FLIGHT ${pirep.flightCode} FLOWN ON 
 <fmt:date fmt="d" date="${pirep.date}" /> by ${pilot.name}</td>
</tr>

<!-- Pirep Data -->
<tr>
 <td class="label">Pilot Code / Rank</td>
 <td class="data"><c:if test="${!empty pilot.pilotCode}">${pilot.pilotCode} </c:if>(${pilot.rank}, ${pilot.equipmentType})</td>
</tr>
<content:filter roles="HR,PIREP,Examination">
<c:if test="${access.canApprove && (pilot.legs % 100 == 99)}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data bld caps">Flight Report Approval will update Century Club status</td>
</tr>
</c:if>
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><a href="mailto:${pilot.email}">${pilot.email}</a></td>
</tr>
</content:filter>
<tr>
 <td class="label">Status</td>
 <td class="data bld sec">${statusMsg} <c:if test="${fn:AssignID(pirep) > 0}"><span class="ter bld">FLIGHT ASSIGNMENT</span></c:if></td>
</tr>
<c:if test="${!empty pirep.submittedOn}">
<tr>
 <td class="label">Submitted on</td>
 <td class="data"><fmt:date date="${pirep.submittedOn}" /></td>
</tr>
</c:if>
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
<c:if test="${isACARS && (!empty flightInfo.SID)}">
<tr>
 <td class="label">Departure Route</td>
 <td class="data">${flightInfo.SID.name}.${flightInfo.SID.transition}</td>
</tr>
</c:if>
<c:set var="isDivert" value="${isACARS && (flightInfo.airportA.ICAO != pirep.airportA.ICAO)}" scope="page" />
<tr>
 <td class="label">Arrived at</td>
 <td class="data">${pirep.airportA.name} (<fmt:airport airport="${pirep.airportA}" />)
<c:if test="${isDivert}"> <span class="data warn caps bld">Originally filed to ${flightInfo.airportA.name} (<fmt:airport airport="${flightInfo.airportA}" />)</span></c:if></td>
</tr>
<c:if test="${isACARS && !isDivert && (!empty flightInfo.STAR)}">
<tr>
 <td class="label">Arrival Route</td>
 <td class="data">${flightInfo.STAR.name}.${flightInfo.STAR.transition}</td>
</tr>
</c:if>
<c:if test="${isACARS && (!empty flightInfo.airportL)}">
<tr>
 <td class="label">Alternate</td>
 <td class="data">${flightInfo.airportL.name} (<fmt:airport airport="${flightInfo.airportL}" />)</td>
</tr>
</c:if>
<tr>
 <td class="label">Flight Simulator</td>
<c:choose>
<c:when test="${pirep.FSVersion == 0}">
 <td class="data sec bld">UNKNOWN</td>
</c:when>
<c:when test="${pirep.FSVersion == 2006}">
 <td class="data sec bld">Microsoft Flight Simulator X</td>
</c:when>
<c:when test="${fn:isMSFS(pirep)}">
 <td class="data sec bld">Microsoft Flight Simulator ${pirep.FSVersion}</td>
</c:when>
<c:otherwise>
 <td class="data sec bld">Laminar Research X-Plane</td>
</c:otherwise>
</c:choose>
</tr>
<tr>
 <td class="label top">Other Information</td>
 <td class="data"><c:if test="${fn:isOnline(pirep)}">Flight Leg flown online using the ${fn:network(pirep)} network<br /></c:if>
<c:if test="${isACARS}">
<div class="ok bld caps">Flight Leg data logged using <content:airline /> ACARS</div>
</c:if>
<c:if test="${fn:isDispatch(pirep)}">
<div class="pri bld caps">Flight Leg planned using <content:airline /> Dispatch</div>
</c:if>
<c:if test="${!fn:isRated(pirep)}">
<div class="error bld caps">Flight Leg flown without Aircraft type rating</div>
</c:if>
<c:if test="${fn:routeWarn(pirep)}">
<div class="error bld caps">Flight Route not found in <content:airline /> schedule</div>
</c:if>
<c:if test="${fn:rangeWarn(pirep)}">
<div class="error bld caps">Flight Distance outside Aircraft Range</div>
</c:if>
<c:if test="${fn:etopsWarn(pirep)}">
<div class="error bld caps">Non-ETOPS Aircraft used on ETOPS route</div>
</c:if>
<c:if test="${fn:timeWarn(pirep)}">
<div class="warn bld caps">Flight Length outside Schedule Guidelines</div>
</c:if>
<c:if test="${fn:weightWarn(pirep)}">
<div class="warn bld caps">Excesive Aircraft Weight Detected</div>
</c:if>
<c:if test="${fn:refuelWarn(pirep)}">
<div class="warn bld caps">In-Flight Refueling Detected</div>
</c:if>
<c:if test="${fn:isCharter(pirep)}">
<div class="pri bld caps">Flight operated as a <content:airline /> Charter</div>
</c:if>
<c:if test="${fn:isHistoric(pirep)}">
<div class="ter bld caps">Flight operated as part of the <content:airline /> Historic program</div>
</c:if>
<c:if test="${fn:isPromoLeg(pirep)}">
<div class="ter bld caps">Flight Leg counts towards promotion to Captain in the <fmt:list value="${pirep.captEQType}" delim=", " /></div>
</c:if>
<c:if test="${!empty event}">
<div class="pri bld caps">Flight Leg part of the ${event.name} Online Event</div>
</c:if>
<c:if test="${fn:isAcademy(pirep)}">
<div class="pri bld caps">Flight Leg part of the <content:airline /> Flight Academy</div>
</c:if>
 </td>
</tr>
<tr>
 <td class="label">Flight Distance</td>
 <td class="data pri bld"><fmt:distance value="${pirep.distance}" longUnits="true" /></td>
</tr>
<tr>
 <td class="label">Logged Time</td>
 <td class="data"><fmt:dec value="${pirep.length / 10.0}" /> hours<c:if test="${avgTime > 0}">
 <i>(average time: <fmt:dec value="${avgTime / 10.0}" /> hours)</i></c:if></td>
</tr>
<c:if test="${!empty pirep.remarks}">
<tr>
 <td class="label top">Pilot Comments</td>
 <td class="data"><fmt:text value="${pirep.remarks}" /></td>
</tr>
</c:if>
<c:if test="${isACARS}">
<c:set var="cspan" value="${1}" scope="request" />
<%@ include file="/jsp/pilot/pirepACARS.jspf" %>
</c:if>
<tr>
<c:if test="${googleMap}">
 <td class="label">Route Map Data</td>
 <td class="data"><span class="bld"><el:box name="showRoute" idx="*" onChange="void toggleMarkers(map, 'gRoute', this)" label="Route" checked="${!isACARS}" />
<c:if test="${isACARS}"><el:box name="showFDR" idx="*" onChange="void toggleMarkers(map, 'routeMarkers', this)" label="Flight Data" checked="false" /> </c:if>
<c:if test="${!empty filedRoute}"><el:box name="showFPlan" idx="*" onChange="void toggleMarkers(map, 'gfRoute', this)" label="Flight Plan" checked="true" /> </c:if>
<el:box name="showFPMarkers" idx="*" onChange="void toggleMarkers(map, 'filedMarkers', this)" label="Navaid Markers" checked="true" />
<c:if test="${!empty onlineTrack}"> <el:box name="showOTrack" idx="*" onChange="void toggleMarkers(map, 'otRoute', this)" label="Online Track" checked="false" /></c:if>
</span></td>
</tr>
<tr>
 <td class="label top">Route Map</td>
 <td class="data"><map:div ID="googleMap" x="100%" y="550" /></td>
</c:if>
<c:if test="${!googleMap}">
 <td class="label top">Route Map</td>
 <td class="data"><img src="http://maps.fallingrain.com/perl/map.cgi?x=620&y=365&kind=topo&lat=${pirep.airportD.latitude}&long=${pirep.airportD.longitude}&name=${pirep.airportD.name}&c=1&lat=${pirep.airportA.latitude}&long=${pirep.airportA.longitude}&name=${pirep.airportA.name}&c=1"
alt="${pirep.airportD.name} to ${pirep.airportA.name}" width="620" height="365" /></td>
</c:if>
</tr>
<c:if test="${!scoreCR && (access.canDispose || access.canViewComments)}">
<tr>
 <td class="label top">Reviewer Comments</td>
<c:if test="${access.canDispose}">
 <td class="data"><textarea name="dComments" cols="100" rows="5">${pirep.comments}</textarea></td>
</c:if>
<c:if test="${!access.canDispose && access.canViewComments}">
 <td class="data"><fmt:text value="${pirep.comments}" /></td>
</c:if>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td>
<c:if test="${access.canSubmit}">
 <el:cmdbutton url="submit" link="${pirep}" label="SUBMIT FLIGHT REPORT" />
</c:if>
<c:if test="${access.canApprove && (!scoreCR)}">
 <el:cmdbutton url="dispose" link="${pirep}" op="approve" post="true" label="APPROVE FLIGHT" />
</c:if>
<c:if test="${access.canHold}">
 <el:cmdbutton url="dispose" link="${pirep}" op="hold" post="true" label="HOLD" />
</c:if>
<c:if test="${access.canRelease}">
 <el:cmdbutton url="release" link="${pirep}" post="true" label="RELEASE HOLD" />
</c:if>
<c:if test="${access.canReject}">
 <el:cmdbutton url="dispose" link="${pirep}" op="reject" post="true" label="REJECT" />
<c:if test="${isACARS && (!fn:isCheckFlight(pirep))}"><content:filter roles="HR,PIREP">
 <el:cmdbutton url="crflag" link="${pirep}" label="MARK AS CHECK RIDE" />
</content:filter></c:if>
</c:if>
<content:filter roles="HR"><c:if test="${access.canDispose && (empty checkRide)}">
<c:set var="bLabel" value="${(fn:sizeof(pirep.captEQType) == 0) ? 'SET' : 'CLEAR'}" scope="page" />
 <el:cmdbutton url="promotoggle" link="${pirep}" label="${bLabel} PROMOTION FLAG" />
</c:if></content:filter>
<c:if test="${access.canEdit}">
 <el:cmdbutton url="pirep" link="${pirep}" op="edit" label="EDIT REPORT" />
</c:if>
<c:if test="${access.canDelete}">
 <el:cmdbutton url="pirepdelete" link="${pirep}" label="DELETE REPORT" />
<c:if test="${isACARS}">
 <el:cmdbutton url="acarsdelete" link="${pirep}" label="DELETE ACARS DATA" />
</c:if> 
</c:if>
<c:if test="${fn:isDraft(pirep) && (!empty assignmentInfo) && (assignAccess.canRelease)}">
 <el:cmdbutton url="assignrelease" link="${assignmentInfo}" label="RELEASE ASSIGNMENT" />
</c:if>
 <el:cmdbutton url="profile" link="${pilot}" label="VIEW PROFILE" />
 </td>
</tr>
</el:table>
<c:if test="${scoreCR || isACARS || access.canDispose}"></form><br /></c:if>
<content:copyright />
</content:region>
</content:page>
<c:if test="${googleMap}">
<script language="JavaScript" type="text/javascript">
// Build the route line and map center
<map:point var="mapC" point="${mapCenter}" />
<c:if test="${!empty mapRoute}">
<map:points var="routePoints" items="${mapRoute}" />
<map:line var="gRoute" src="routePoints" color="#4080AF" width="3" transparency="0.75" geodesic="true" />
</c:if>
<c:if test="${empty mapRoute && isACARS}">
var gRoute;
var routePoints = new Array();
var routeMarkers = new Array();
getACARSData(${fn:ACARS_ID(pirep)}, '${imgPath}');
</c:if>
<c:if test="${!empty filedRoute}">
<map:points var="filedPoints" items="${filedRoute}" />
<map:markers var="filedMarkers" items="${filedRoute}" />
<map:line var="gfRoute" src="filedPoints" color="#80800F" width="2" transparency="0.5" geodesic="true" />
</c:if>
<c:if test="${!empty onlineTrack}">
<map:points var="onlinePoints" items="${onlineTrack}" />
<map:line var="otRoute" src="onlinePoints" color="#C09F8F" width="3" transparency="0.55" geodesic="true" />
</c:if>
// Build the map
var map = new GMap2(getElement("googleMap"), {mapTypes:[G_NORMAL_MAP, G_SATELLITE_MAP, G_PHYSICAL_MAP]});
map.addControl(new GLargeMapControl());
map.addControl(new GMapTypeControl());
map.setCenter(mapC, getDefaultZoom(${pirep.distance}));
<map:type map="map" type="${gMapType}" default="G_PHYSICAL_MAP" />
map.enableDoubleClickZoom();
map.enableContinuousZoom();

<c:if test="${!empty mapRoute}">
// Add the route and markers
addMarkers(map, 'gRoute');
</c:if>
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
<c:if test="${showGEarth}">
// Google Earth plugin support
GEvent.addListener(map, 'maptypechanged', earthToggle);
map.addMapType(G_SATELLITE_3D_MAP);
map.getEarthInstance(getEarthInstanceCB);
generateKMLRequest(${fn:ACARS_ID(pirep)}, true);
</c:if>
</script>
</c:if>
</body>
</map:xhtml>
