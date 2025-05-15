<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Flight Tour - ${tour.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:js name="common" />
<content:js name="progress" />
<content:captcha action="tour" />
<content:attr attr="tourAccess" value="true" roles="Pilot" />
<c:if test="${(tour.flights.size() > 0) && tourAccess}">
<c:set var="hasMap" value="true" scope="page" />
<map:api version="3" callback="golgotha.local.mapInit" /></c:if>
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body<c:if test="${hasMap}"> onunload="void golgotha.maps.util.unload()"</c:if>>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<tr class="title caps">
 <td colspan="6"><content:airline /> FLIGHT TOUR - ${tour.name}</td>
</tr>
<c:if test="${!empty tour.networks}">
<tr>
 <td class="label">Network(s)</td>
 <td class="data sec bld" colspan="5"><fmt:list value="${tour.networks}" delim=", " /></td>
</tr>
</c:if>
<tr>
 <td class="label">Active between</td>
 <td class="data" colspan="5"><fmt:date date="${tour.startDate}" t="HH:mm" /> - <fmt:date date="${tour.endDate}" t="HH:mm" /> <c:if test="${tour.active}"><span class="ter bld"> ENABLED</span></c:if><c:if test="${isActiveNow}"><span class="sec bld"> CURRENTLY AVAILABLE</span></c:if></td>
</tr>
<c:if test="${access.canEdit && !tour.active}">
<tr>
 <td class="label">Status</td>
 <td class="data ter bld" colspan="5">${tour.status.description}</td>
</tr>
</c:if>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data small caps" colspan="5"><c:if test="${tour.ACARSOnly}">
<div class="pri bld">Tour requires flights logged with ACARS</div></c:if>
<c:if test="${tour.allowOffline}">
<div class="bld">Tour permits Offline Flights</div></c:if>
<c:if test="${tour.matchEquipment}">
<div class="ter bld">Equipment used must match Tour definition</div></c:if>
<c:if test="${tour.matchLeg}">
<div class="bld">Flight Number must match Tour definition</div></c:if></td>
</tr>
<c:if test="${tour.size > 0}">
<tr class="title caps">
 <td colspan="6">BRIEFING DOCUMENTATION</td>
</tr>
<c:choose>
<c:when test="${tour.isPDF}">
<tr>
 <td class="label top">Tour Briefing</td>
 <td class="data" colspan="5"><el:link url="/attach/tbrief/${tour.hexID}"><el:img src="library/adobe.png" className="noborder" caption="Flight Tour Briefing" x="32" y="32" /></el:link> <span class="small nophone"><fmt:fileSize value="${tour.size}" />, Adobe PDF document</span>
</tr>
</c:when>
<c:otherwise>
<tr>
 <td class="label top">Tour Briefing</td>
 <td class="data" colspan="5"><fmt:text value="${tour.text}" /></td>
</tr>
</c:otherwise>
</c:choose>
</c:if>
<c:if test="${!empty tourProgress}">
<tr class="title caps">
 <td colspan="6">MY PROGRESS</td>
</tr>
<c:set var="pLeg" value="${tourProgress[maxLeg - 1]}" scope="page" />
<c:set var="barPct" value="${maxLeg * 100 / tour.flights.size()}" scope="page" />
<tr>
 <td class="label top">Previous Leg</td>
 <td class="data" colspan="5"><el:cmd url="pirep" link="${pLeg}" className="pri bld" authOnly="true">${pLeg.flightCode}</el:cmd>: ${pLeg.airportD.name} (<el:cmd url="airportinfo" linkID="${pLeg.airportD.IATA}"><fmt:airport airport="${pLeg.airportD}" /></el:cmd>) - 
 ${pLeg.airportA.name} (<el:cmd url="airportinfo" linkID="${pLeg.airportA.IATA}"><fmt:airport airport="${pLeg.airportA}" /></el:cmd>)<span class="nophone ita"> flown on <fmt:date date="${pLeg.date}" fmt="d" /></span><br />
 <span id="progressBar" class="bar" style="width:90%">&nbsp;</span></td>
</tr>
<c:if test="${maxLeg < tour.flights.size()}">
<c:set var="nLeg" value="${tour.flights[maxLeg]}" scope="page" />
<tr>
 <td class="label">Next Leg</td>
 <td class="data" colspan="5"><span class="sec bld">${nLeg.flightCode}</span>: ${nLeg.airportD.name} (<el:cmd url="airportinfo" linkID="${nLeg.airportD.IATA}"><fmt:airport airport="${nLeg.airportD}" /></el:cmd>) - 
 ${nLeg.airportA.name} (<el:cmd url="airportinfo" linkID="${nLeg.airportA.IATA}"><fmt:airport airport="${nLeg.airportA}" /></el:cmd>)</td>
</tr>
</c:if>
</c:if>
<tr class="title caps">
 <td colspan="6">PILOT PROGRESS</td>
</tr>
<tr>
 <td class="label">Tour Completed</td>
 <td class="data" colspan="5"><span class="pri bld"><fmt:int value="${tour.completionIDs.size()}" /> Pilots</span><c:if test="${tourAccess && (tour.completionIDs.size() > 0) && (progressIDs.size() == 0)}"> - <el:cmd url="tourprogress" link="${tour}" className="sec bld">VIEW</el:cmd><br />
 <c:if test="${!empty pilots}"><hr /></c:if></c:if>
 <c:if test="${!empty pilots}"><span class="small">
<c:forEach var="pilotID" items="${tour.completionIDs}" varStatus="pilotNext">
<c:set var="p" value="${pilots[pilotID]}" scope="page" />
${p.name} <c:if test="${!empty p.pilotCode}" > (${p.pilotCode})</c:if><c:if test="${!pilotNext.last}">, </c:if></c:forEach></span></c:if></td>
</tr>
<c:if test="${!empty tour.completionIDs && !empty avgCompletionTime}">
<tr>
 <td class="label">Average Completion Time</td>
 <td class="data" colspan="5"><fmt:duration long="true" duration="${avgCompletionTime}"  className="bld" default="N/A" /></td>
</tr>
</c:if>
<tr>
 <td class="label">Tour in Progress</td>
 <td class="data" colspan="5"><span class="bld"><fmt:int value="${progressIDs.size()}" /> Pilots</span><c:if test="${tourAccess && (progressIDs.size() > 0)}"> - <el:cmd url="tourprogress" link="${tour}" className="sec bld">VIEW</el:cmd><br />
 <c:if test="${!empty pilots}"><hr /></c:if></c:if>
 <c:if test="${!empty pilots}"><span class="small">
<c:forEach var="pilotID" items="${progressIDs}" varStatus="pilotNext">
<c:set var="p" value="${pilots[pilotID]}" scope="page" />
${p.name} <c:if test="${!empty p.pilotCode}" > (${p.pilotCode})</c:if><c:if test="${!pilotNext.last}">, </c:if></c:forEach></span></c:if></td>
</tr>
<tr class="title caps">
 <td colspan="6">FLIGHT LEGS REQUIRED FOR COMPLETION</td>
</tr>
<c:choose>
<c:when test="${!empty tour.flights}">
<c:set var="leg" value="0" scope="page" />
<c:forEach var="fl" items="${tour.flights}">
<c:set var="leg" value="${leg + 1}" scope="page" />
<tr class="mid">
 <td class="sec bld">Leg <fmt:int value="${leg}" /></td>
 <td class="pri bld" style="width:15%;">${fl.flightCode}</td>
 <td class="sec bld" style="width:15%">${fl.equipmentType}</td>
 <td class="small">${fl.airportD.name} (<el:cmd url="airportinfo" linkID="${fl.airportD.IATA}" authOnly="true"><fmt:airport airport="${fl.airportD}" /></el:cmd>) - ${fl.airportA.name} (<el:cmd url="airportinfo" linkID="${fl.airportA.IATA}" authOnly="true"><fmt:airport airport="${fl.airportA}" /></el:cmd>)</td>
 <td><fmt:distance value="${fl.distance}" /></td>
 <td class="bld" colspan="${tour.matchLeg ? 1 : 2}"><fmt:date fmt="t" t="HH:mm" tz="${fl.airportD.TZ}" date="${fl.timeD}" /> - <fmt:date fmt="t" t="HH:mm" tz="${fl.airportA.TZ}" date="${fl.timeA}" /> (<fmt:int value="${fl.duration.toHoursPart()}" />h <fmt:int value="${fl.duration.toMinutesPart()}" />m)</td>
</tr>
</c:forEach>
<c:if test="${hasMap}">
<tr class="title caps">
 <td colspan="6">FLIGHT LEG MAP<span id="historyToggle" class="toggle" onclick="void golgotha.util.toggleExpand(this, 'tourMap')">COLLAPSE</span></td>
</tr>
<tr class="tourMap">
 <td colspan="6"><map:div ID="googleMap" height="515" /></td>
</tr>
<script async>
golgotha.local.mapInit = function() {
	const lines = [];
	<map:point var="golgotha.local.mapC" point="${ctr}" />
	<map:markers var="golgotha.local.airports" items="${tourAirports}" />
	<map:points var="golgotha.local.todo" items="${tourRemaining}" />
	<map:line var="golgotha.local.todoLine" width="1" color="#a000a1" geodesic="true" src="golgotha.local.todo" transparency="0.35" />
	lines.push(golgotha.local.todoLine);
	<c:if test="${!empty myTourRoute}">
	<map:points var="golgotha.local.progress" items="${myTourRoute}" />
	<map:line var="golgotha.local.progressLine" width="2" color="#0000a1" geodesic="true" src="golgotha.local.progress" transparency="0.5" />
	lines.push(golgotha.local.progressLine);</c:if>

	// Build the map
	const mapOpts = {center:golgotha.local.mapC,minZoom:2,maxZoom:18,zoom:6,scrollwheel:false,clickableIcons:false,streetViewControl:false,mapTypeControlOptions:{mapTypeIds:golgotha.maps.DEFAULT_TYPES}};
	map = new golgotha.maps.Map(document.getElementById('googleMap'), mapOpts);
	map.setMapTypeId(golgotha.maps.info.type);
	map.infoWindow = new google.maps.InfoWindow({content:'',zIndex:golgotha.maps.z.INFOWINDOW, headerDisabled:true});
	google.maps.event.addListener(map, 'maptypeid_changed', golgotha.maps.updateMapText);
	google.maps.event.addListener(map, 'click', map.closeWindow);
	google.maps.event.addListenerOnce(map, 'tilesloaded', function() { google.maps.event.trigger(map, 'maptypeid_changed'); });
	map.addMarkers(golgotha.local.airports);
	map.addMarkers(lines);
};
</script>
</c:if>
</c:when>
<c:otherwise>
<tr id="tourEmpty">
 <td colspan="4" class="pri bld mid">NO FLIGHT LEGS ARE ASSOCIATED WITH THIS TOUR</td>
</tr>
</c:otherwise>
</c:choose>
<c:set var="auditCols" value="5" scope="request" />
<%@ include file="/jsp/auditLog.jspf" %>
</el:table>

<c:if test="${access.canProvideFeedback || access.canViewFeedback}">
<c:set var="fbCols" value="6" scope="page" />
<c:set var="fbCmd" value="tourfb" scope="page" />
<c:set var="fbName" value="Flight Tour" scope="page" />
<c:set var="fbObject" value="${tour}" scope="page" />
<%@ include file="/jsp/feedback.jspf" %>
</c:if>

<!-- Button Bar -->
<el:table className="bar">
<tr>	
 <td>&nbsp;
<c:if test="${access.canEdit}"><el:cmdbutton url="tour" link="${tour}" op="edit" label="EDIT FLIGHT TOUR" /></c:if>
<c:if test="${access.canDelete}">&nbsp;<el:cmdbutton url="tourdelete" link="${tour}" op="edit" label="DELETE FLIGHT TOUR" /></c:if>
</td></tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<c:if test="${barPct > 0}">
<script async>
const pr = golgotha.util.getStyle('main.css', '.pri') || '#0000a1'; 
golgotha.local.pb = new ProgressBar.Line('#progressBar', {color:pr, text:{value:'', className:'pri', style:{color:'#000000'}}, fill:pr});
golgotha.local.pb.setText(${Math.round(barPct * 10) / 10.0} + '% complete');
golgotha.local.pb.set(${barPct} / 100.0);
</script></c:if>
</body>
</html>
