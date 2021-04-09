<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Flight Tour - ${tour.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<tr class="title caps">
 <td colspan="4"><content:airline /> FLIGHT TOUR - ${tour.name}</td>
</tr>
<c:if test="${!empty tour.networks}">
<tr>
 <td class="label">Network(s)</td>
 <td class="data sec bld" colspan="3"><fmt:list value="${tour.networks}" delim=", " /></td>
</tr>
</c:if>
<tr>
 <td class="label">Active between</td>
 <td class="data" colspan="3"><fmt:date date="${tour.startDate}" fmt="d" /> - <fmt:date date="${tour.endDate}" fmt="d" /><c:if test="${tour.active}"><span class="ter bld"> ACTIVE</span></c:if><c:if test="${isActiveNow}"><span class="pri bld"> CURRENTLY AVAILABLE</span></c:if></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data small caps" colspan="3"><c:if test="${tour.ACARSOnly}">
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
 <td colspan="4">BRIEFING DOCUMENTATION</td>
</tr>
<c:choose>
<c:when test="${tour.isPDF}">
<tr>
 <td class="label top">Tour Briefing</td>
 <td class="data" colspan="3"><el:link url="/tbrief/${tour.hexID}"><el:img src="library/adobe.png" className="noborder" caption="Flight Tour Briefing" x="32" y="32" /></el:link> <span class="small nophone"><fmt:int value="${tour.size}" /> bytes, Adobe PDF document</span>
</tr>
</c:when>
<c:otherwise>
<tr>
 <td class="label top">Tour Briefing</td>
 <td class="data" colspan="3"><fmt:text value="${tour.text}" /></td>
</tr>
</c:otherwise>
</c:choose>
</c:if>
<tr class="title caps">
 <td colspan="5">PILOT PROGRESS</td>
</tr>
<tr>
 <td class="label">Tour Completed</td>
 <td class="data" colspan="3"><span class="pri bld"><fmt:int value="${tour.completionIDs.size()}" /> Pilots</span><c:if test="${!empty pilots}"><span class="small">
<c:forEach var="pilotID" items="${tour.completionIDs}" varStatus="pilotNext">
<c:set var="p" value="${pilots[pilotID]}" scope="page" />
${p.name} <c:if test="${!empty p.pilotCode}" > (${p.pilotCode})</c:if><c:if test="${!pilotNext.last}">, </c:if></c:forEach></span></c:if></td>
</tr>
<tr>
 <td class="label">Tour in Progress</td>
 <td class="data"  colspan="3"><span class="bld"><fmt:int value="${tour.progressIDs.size()}" /> Pilots</span><c:if test="${!empty pilots}"><span class="small">
<c:forEach var="pilotID" items="${tour.progressIDs}" varStatus="pilotNext">
<c:set var="p" value="${pilots[pilotID]}" scope="page" />
${p.name} <c:if test="${!empty p.pilotCode}" > (${p.pilotCode})</c:if><c:if test="${!pilotNext.last}">, </c:if></c:forEach></span></c:if></td>
</tr>
<tr class="title caps">
 <td colspan="5">FLIGHT LEGS REQUIRED FOR COMPLETION</td>
</tr>
<c:set var="leg" value="0" scope="page" />
<c:forEach var="fl" items="${tour.flights}">
<c:set var="leg" value="${leg + 1}" scope="page" />
<tr class="mid">
 <td class="sec bld">Leg <fmt:int value="${leg}" /></td>
<c:if test="${tour.matchLeg}">
 <td class="pri bld" style="width:15%;">${fl.flightCode}</td>
</c:if>
 <td class="sec bld" style="width:15%">${fl.equipmentType}</td>
 <td class="small">${fl.airportD.name} (<fmt:airport airport="${fl.airportD}" />) - ${fl.airportA.name} (<fmt:airport airport="${fl.airportA}" />)</td>
 <td class="bld" colspan="${tour.matchLeg ? 1 : 2}"><fmt:date fmt="t" t="HH:mm" tz="${fl.airportD.TZ}" date="${fl.timeD}" /> - <fmt:date fmt="t" t="HH:mm" tz="${fl.airportA.TZ}" date="${fl.timeA}" /> (<fmt:int value="${fl.duration.toHoursPart()}" />h <fmt:int value="${fl.duration.toMinutesPart()}" />m)</td>
</tr>
</c:forEach>
<c:if test="${empty tour.flights}">
<tr id="tourEmpty">
 <td colspan="2" class="pri bld mid">NO FLIGHT LEGS ARE ASSOCIATED WITH THIS TOUR</td>
</tr>
</c:if>
<c:set var="auditCols" value="4" scope="request" />
<%@ include file="/jsp/auditLog.jspf" %>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>	
 <td>&nbsp;
<c:if test="${access.canEdit}">
<el:cmdbutton url="tour" link="${tour}" op="edit" label="EDIT FLIGHT TOUR" /></c:if>
<c:if test="${access.canDelete}">
&nbsp;<el:cmdbutton url="tourdelete" link="${tour}" op="edit" label="DELETE FLIGHT TOUR" /></c:if>
</td></tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
