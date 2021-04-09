<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Tour<c:if test="${!empty tour}"> - ${tour.name}</c:if></title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<content:js name="datePicker" />
<content:js name="airportRefresh" />
<c:if test="${access.canEditLegs}">
<content:js name="tourEdit" /></c:if>
<meta name="viewport" content="width=device-width, initial-scale=1" />
<fmt:aptype var="useICAO" />
<script>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.name, l:5, t:'Tour Name'});
	golgotha.form.submit(f);
	return true;
};

golgotha.onDOMReady(function() {
	const f = document.forms[0];
	const cfg = golgotha.airportLoad.config;
	cfg.doICAO = ${useICAO};
	golgotha.airportLoad.setHelpers([f.airportD,f.airportA]);
	golgotha.airportLoad.setText([f.airportD,f.airportA]);
});
</script>
</head>
<content:copyright visible="false" />
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:enum var="networks" className="org.deltava.beans.OnlineNetwork"  exclude="INTVAS,FPI,ACARS" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="tour.do" method="post" link="${tour}" op="save" allowUpload="true" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form" ID="baseTable">
<tr class="title caps">
 <td colspan="5">FLIGHT TOUR PROFILE<c:if test="${!empty tour}"> - ${tour.name}</c:if></td>
</tr>
<tr>
 <td class="label">Name</td>
 <td class="data" colspan="4"><el:text name="name" idx="*" size="32" max="48" required="true" className="pri bld" value="${tour.name}" /></td>
</tr>
<tr>
 <td class="label">Network(s)</td>
 <td class="data" colspan="4"><el:check name="network" idx="*" cols="5" width="90" options="${networks}" checked="${tour.networks}" /></td>
</tr>
<tr>
 <td class="label">Start Date</td>
 <td class="data" colspan="4"><el:text name="startDate" required="true" idx="*" size="9" max="10" value="${fn:dateFmt(tour.startDate, 'MM/dd/yyyy')}" />&nbsp;<el:button label="CALENDAR" onClick="void show_calendar('forms[0].startDate')" /></td>
</tr>
<tr>
 <td class="label">End Date</td>
 <td class="data" colspan="4"><el:text name="endDate" required="true" idx="*" size="9" max="10" value="${fn:dateFmt(tour.endDate, 'MM/dd/yyyy')}" />&nbsp;<el:button label="CALENDAR" onClick="void show_calendar('forms[0].endDate')" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data" colspan="4"><el:box name="acarsOnly" idx="*" value="true" checked="${tour.ACARSOnly}" label="This Tour requires flights logged with ACARS" /><br />
<el:box name="allowOffline" idx="*" value="true" checked="${tour.allowOffline}" label="Allow Offline Flights" /><br />
<el:box name="matchEQ" idx="*" value="true" checked="${tour.matchEquipment}" label="Equipment used must match Tour definition" /><br />
<el:box name="matchLeg" idx="*" value="true" checked="${tour.matchLeg}" label="Flight Number must match Tour definition" /><br />
<el:box name="active" idx="*" value="true" checked="${tour.active}" className="pri" label="This Tour is Active" /></td>
</tr>
<tr class="title caps">
 <td colspan="5">BRIEFING DOCUMENTATION</td>
</tr>
<c:set var="hasTextBriefing" value="${(tour.size > 1) && !tour.isPDF}" scope="page" />
<tr>
 <td class="label top">Briefing Text</td>
 <td class="data" colspan="4"><el:textbox name="briefing" idx="*" width="90%" className="req" height="5" resize="true">${hasTextBriefing ? tour.text : ''}</el:textbox></td>
</tr>
<tr>
 <td class="label">Attach File</td>
 <td class="data" colspan="4"><el:file name="briefPDF" idx="*" className="small" size="96" max="144" /><c:if test="${tour.isPDF}"><el:box name="deleteBrief" value="true" label="Delete existing Briefing" /></c:if>
<c:if test="${hasTextBriefing}"><span class="small ita nophone"> Uploading a Briefing file will overwrite the existing Briefing!</span></c:if></td>
</tr>
<c:if test="${tour.isPDF}">
<tr>
 <td class="label top">Tour Briefing</td>
 <td class="data" colspan="4"><el:link url="/tbrief/${tour.hexID}"><el:img src="library/adobe.png" className="noborder" caption="Flight Tour Briefing" x="32" y="32" /></el:link> <span class="small nophone"><fmt:int value="${tour.size}" /> bytes, Adobe PDF document</span>
</tr>
</c:if>
<tr class="title caps">
 <td colspan="5">PILOT PROGRESS</td>
</tr>
<tr>
 <td class="label">Tour Completed</td>
 <td class="data" colspan="4"><span class="pri bld"><fmt:int value="${tour.completionIDs.size()}" /> Pilots</span><c:if test="${!empty pilots}"><span class="small">
<c:forEach var="pilotID" items="${tour.completionIDs}" varStatus="pilotNext">
<c:set var="p" value="${pilots[pilotID]}" scope="page" />
${p.name} <c:if test="${!empty p.pilotCode}" > (${p.pilotCode})</c:if><c:if test="${!pilotNext.last}">, </c:if></c:forEach></span></c:if></td>
</tr>
<tr>
 <td class="label">Tour in Progress</td>
 <td class="data" colspan="4"><span class="bld"><fmt:int value="${tour.progressIDs.size()}" /> Pilots</span><c:if test="${!empty pilots}"><span class="small">
<c:forEach var="pilotID" items="${tour.progressIDs}" varStatus="pilotNext">
<c:set var="p" value="${pilots[pilotID]}" scope="page" />
${p.name} <c:if test="${!empty p.pilotCode}" > (${p.pilotCode})</c:if><c:if test="${!pilotNext.last}">, </c:if></c:forEach></span></c:if></td>
</tr>
<tr id="legHdr" class="title caps">
 <td colspan="5">FLIGHT LEGS REQUIRED FOR COMPLETION</td>
</tr>
<c:forEach var="fl" items="${tour.flights}">
<tr class="legRow mid" id="legRow-${fl.legCode}">
 <td><c:if test="${access.canEditLegs}">&nbsp;<el:button onClick="void golgotha.tour.deleteLeg('${fl.legCode}')" label="DELETE" /></c:if>&nbsp;</td>
 <td class="pri bld" style="width:15%;">${fl.flightCode}</td>
 <td class="sec bld" style="width:10%;">${fl.equipmentType}</td>
 <td class="small">${fl.airportD.name} (<fmt:airport airport="${fl.airportD}" />) - ${fl.airportA.name} (<fmt:airport airport="${fl.airportA}" />)</td>
 <td class="small bld"><fmt:date fmt="t" t="HH:mm" tz="${fl.airportD.TZ}" date="${fl.timeD}" /> - <fmt:date fmt="t" t="HH:mm" tz="${fl.airportA.TZ}" date="${fl.timeA}" /> (<fmt:int value="${fl.duration.toHoursPart()}" />h <fmt:int value="${fl.duration.toMinutesPart()}" />m)</td>
</tr>
</c:forEach>
<c:if test="${empty tour.flights}">
<tr id="tourEmpty">
 <td colspan="5" class="pri bld mid">NO FLIGHT LEGS ARE ASSOCIATED WITH THIS TOUR</td>
</tr>
</c:if>
</el:table>
<c:if test="${access.canEditLegs}">
<!-- Search Table -->
<el:table className="form" ID="searchTable">
<tr class="title caps">
 <td colspan="4"><span class="nophone"><content:airline /> FLIGHT </span>SCHEDULE SEARCH <span id="isLoading"></span></td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airportD" size="1" firstEntry="-" options="${airports}" onChange="this.updateAirportCode()" /><span class="nophone"> <el:airportCode combo="airportD" idx="*" /></span></td>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" size="1" firstEntry="-" options="${airports}" onChange="this.updateAirportCode()" /><span class="nophone"> <el:airportCode combo="airportA" idx="*" /></span></td>
</tr>
<tr class="title">
 <td colspan="4" class="mid"><el:button onClick="golgotha.tour.search()" idx="*" label="SEARCH SCHEDULE" /></td>
</tr>
<tr class="title caps" id="searchResultHdr" style="display:none;">
 <td colspan="4">SCHEDULE SAERCH RESULTS <span id="customResultMsg"></span></td>
</tr>
<tr class="searchResultNone" style="display:none;">
 <td colspan="4" class="pri bld mid">No Flights matching your search criteria were found<span class="nophone"> in the <content:airline /> Flight Schedule</span>.</td>
</tr>
<tr class="searchResultNone title caps" style="display:none;">
 <td colspan="4">CUSTOM FLIGHT TOUR LEG</td>
</tr>
<tr class="searchResultNone" style="display:none;">
 <td class="label">Flight Number</td>
 <td class="data"><el:combo name="airline"  idx="*" size="1" options="${airlines}" firstEntry="[ AIRLINE ]" onChange="void golgotha.tour.clearCustomLeg()" /> <el:text name="flightNumber" idx="*" size="3" max="4" autoComplete="false" className="pri bld req" onChange="void golgotha.tour.clearCustomLeg()" /> Leg 
 <el:text name="flightLeg" idx="*" size="1" autoComplete="false" className="req" max="1" onChange="void golgotha.tour.clearCustomLeg()" />&nbsp;<el:combo name="eq" size="1" idx="*" firstEntry="[ EQUIPMENT TYPE ]" options="${eqTypes}" onChange="void golgotha.tour.clearCustomLeg()" /></td>
 <td class="label">Departure/Arrival Times</td>
 <td class="data"><el:text name="flightTimeD" idx="*" size="4" max="5" onChange="void golgotha.tour.clearCustomLeg()" /> - <el:text name="flightTimeA" idx="*" size="4" max="5" onChange="void golgotha.tour.clearCustomLeg()" />
&nbsp;<span id="customLeg" style="display:none;"><span id="customLegInfo" class="pri bld"></span>&nbsp;<el:button onClick="void golgotha.tour.addCustomLeg()"  label="ADD CUSTOM LEG" /></span></td>
</tr>
<tr id="legWarnRow" class="searchResultNone" style="display:none;">
 <td colspan="4" class="error mid small bld"><div id="rangeWarn">DISTANCE EXCEEDS AIRCRAFT RANGE</div> <div id="trWarn">DEPARTURE RUNWAYS TOO SHORT</div> <div id="lrWarn">LANDING RUNWAYS TOO SHORT</div>
 <div id="etopsWarn">ETOPS RATING REQUIRED</div></td>
</tr>
<tr class="searchResultNone title caps" style="display:none;">
 <td colspan="4" class="mid"><el:button onClick="golgotha.tour.searchCustom()" idx="*" label="BUILD CUSTOM FLIGHT LEG" /></td>
</tr>
</el:table>
<br />
</c:if>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" idx="*" label="SAVE FLIGHT TOUR" /></td>
</tr>
</el:table>
<el:text name="legCodes" type="hidden" value="${t.legCodes}" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<c:if test="${!empty tour.flights}">
<script>
golgotha.local.flightData = ${legData};
const rows = golgotha.util.getElementsByClass('legRow', 'tr', document.getElementById('baseTable'));
for (var x = 0; x < rows.length; x++)
	rows[x].flight = golgotha.local.flightData[x];
	
document.forms[0].legCodes.value = golgotha.tour.buildLegCodes();
</script>
</c:if>
</body>
</html>
