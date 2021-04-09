<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Schedule - ${empty entry ? 'New Entry' : entry.flightCode}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:json />
<content:js name="airportRefresh" />
<content:js name="datePicker" />
<content:googleAnalytics eventSupport="true" />
<script>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.airline, t:'Airline'});
	golgotha.form.validate({f:f.flightNumber, min:1, t:'Flight Number'});
	golgotha.form.validate({f:f.flightLeg, min:1, t:'Flight Leg'});
	golgotha.form.validate({f:f.eqType, t:'Equipment Type'});
	golgotha.form.validate({f:f.startDate, l:9, t:'Start Date'});
	golgotha.form.validate({f:f.endDate, l:9, t:'End Date'});
	golgotha.form.validate({f:f.timeD, l:4, t:'Departure Time'});
	golgotha.form.validate({f:f.timeA, l:4, t:'Arrival Time'});
	golgotha.form.validate({f:f.airportD, t:'Departure Airport'});
	golgotha.form.validate({f:f.airportA, t:'Arrival Airport'});
	golgotha.form.submit(f);
	return true;
};
<c:if test="${empty entry}">
golgotha.local.getAvailableFlight = function(f)
{
golgotha.form.validate({f:f.airline, t:'Airline'});
const xmlreq = new XMLHttpRequest();
xmlreq.open('get', 'next_flight.ws?start=' + f.rangeStart.value + '&end=' + f.rangeEnd.value + '&airline=' + golgotha.form.getCombo(f.airline), true);
xmlreq.onreadystatechange = function () {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	const e = JSON.parse(xmlreq.responseText);
	f.flightNumber.value = e.number;
	f.flightLeg.value = e.leg;
	golgotha.form.clear();
	return true;
};

golgotha.form.submit();
xmlreq.send(null);
return true;
};

golgotha.local.getAvailableLeg = function(f) {
	golgotha.form.validate({f:f.airline, t:'Airline'});
	const xmlreq = new XMLHttpRequest();
	xmlreq.open('get', 'next_leg.ws?flight=' + f.flightNumber.value + '&airline=' + golgotha.form.getCombo(f.airline), true);
	xmlreq.onreadystatechange = function () {
		if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
		const e = JSON.parse(xmlreq.responseText);
		f.flightNumber.value = e.number;
		f.flightLeg.value = e.leg;
		golgotha.form.clear();
		return true;
	};

	golgotha.form.submit();
	xmlreq.send(null);
	return true;
};</c:if>

golgotha.local.changeAirline = function(combo) {
	const f = document.forms[0];
	golgotha.airportLoad.changeAirline([f.airportD, f.airportA], golgotha.airportLoad.config);
	const rows = golgotha.util.getElementsByClass('airportRow');
	rows.forEach(function(r) { golgotha.util.display(r, (combo.selectedIndex > 0)); });
	f.isHistoric.checked = golgotha.local.historicAirlines[golgotha.form.getCombo(combo)];
	return true;
};

golgotha.local.historicAirlines = ${historicAL};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="airlines" name="airlines" mapValues="true" sort="true" />
<content:sysdata var="academyEnabled" name="academy.enabled" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="sched.do" method="post" linkID="${entry.flightCode}" op="save" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
<c:if test="${empty entry}"> <td colspan="2">NEW <content:airline /> SCHEDULE ENTRY</td></c:if>
<c:if test="${!empty entry}"> <td colspan="2">FLIGHT ${entry.flightCode}</td></c:if>
</tr>
<c:if test="${!empty entry}">
<tr>
 <td class="label">Source / Line</td>
 <td class="data"><span class="pri bld">${entry.source.description}</span> Line <fmt:int value="${entry.lineNumber}"  /></td>
</tr>
</c:if>
<tr>
 <td class="label">Airline Name</td>
 <td class="data"><el:combo name="airline" idx="*" required="true" size="1" options="${airlines}" value="${entry.airline}" onChange="void golgotha.local.changeAirline(this)" firstEntry="[ AIRLINE ]" /></td>
</tr>
<tr>
 <td class="label top">Flight Number / Leg</td>
 <td class="data"><el:text name="flightNumber" idx="*" required="true" size="3" max="4" value="${entry.flightNumber}" />&nbsp;<el:text name="flightLeg" idx="*" required="true" size="1" max="1" value="${empty entry ? '1' : entry.leg}" />
<c:if test="${empty entry}">
<hr />
<span class="small">You can search for an available flight number between <el:text name="rangeStart" idx="*" className="small" size="3" max="4" value="" /> and 
<el:text name="rangeEnd" idx="*" className="small" size="3" max="4" value="" />&nbsp;<el:button onClick="void golgotha.form.wrap(golgotha.local.getAvailableFlight, document.forms[0])" label="SEARCH" /><br />
You can search for the next available Flight Leg. <el:button onClick="void golgotha.form.wrap(golgotha.local.getAvailableLeg, document.forms[0])" label="SEARCH" /></span></c:if></td>
</tr>
<tr>
 <td class="label">Starts Operation on</td>
 <td class="data"><el:text name="startDate" idx="*" size="9" max="10" required="true" value="${fn:dateFmt(entry.startDate, 'MM/dd/yyyy')}" />&nbsp;<el:button label="CALENDAR" onClick="void show_calendar('forms[0].startDate')" /></td>
</tr>
<tr>
 <td class="label">Ceases Operation on</td>
 <td class="data"><el:text name="endDate" idx="*" size="9" max="10" required="true" value="${fn:dateFmt(entry.endDate, 'MM/dd/yyyy')}" />&nbsp;<el:button label="CALENDAR" onClick="void show_calendar('forms[0].endDate')" /></td>
</tr>
<tr>
 <td class="label">Days of Week</td>
 <td class="data"><el:check name="days" idx="*" className="small" cols="7" width="100" options="${daysOfWeek}" value="${entry.days}" /></td>
</tr>
<tr>
 <td class="label">Equipment Type</td>
 <td class="data"><el:combo name="eqType" idx="*" required="true" size="1" options="${eqTypes}" value="${entry.equipmentType}" firstEntry="[ EQUIPMENT ]" /></td>
</tr>
<tr class="airportRow">
 <td class="label">Departing From</td>
 <td class="data"><el:combo name="airportD" size="1" options="${airportsD}" required="true" value="${entry.airportD}" onChange="void this.updateAirportCode()" />
 <el:airportCode combo="airportD" idx="*" airport="${entry.airportD}" /> at <el:text name="timeD" idx="*" required="true" size="4" max="5" value="${fn:dateFmt(entry.timeD, 'HH:mm')}" />&nbsp;<span class="small">Local Time (Format: HH:mm)</span></td>
</tr>
<content:hasmsg>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data error bld">CANNOT SAVE SCHEDULE ENTRY - <content:sysmsg /></td>
</tr>
</content:hasmsg>
<tr class="airportRow">
 <td class="label">Arriving At</td>
 <td class="data"><el:combo name="airportA" size="1" options="${airportsA}" required="true" value="${entry.airportA}" onChange="void this.updateAirportCode()" />
 <el:airportCode combo="airportA" idx="*" airport="${entry.airportA}" /> at <el:text name="timeA" idx="*" required="true" size="4" max="5" value="${fn:dateFmt(entry.timeA, 'HH:mm')}" />&nbsp;<span class="small">Local Time (Format: HH:mm)</span></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isHistoric" className="small" idx="*" value="true" label="This is a Historic Flight" checked="${entry.historic}" /><br />
<el:box name="forceInclude" className="small" idx="*" value="true" label="Force Inclusion even if route exists" checked="${entry.forceInclude}" />
<c:if test="${academyEnabled}"><br /><el:box name="isAcademy" className="small" idx="*" value="true" label="This is a Flight Academy Flight" checked="${entry.academy}" /></c:if></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SAVE SCHEDULE ENTRY" /><c:if test="${!empty entry}">&nbsp;<el:cmdbutton url="sched_delete" linkID="${entry.source}-${entry.lineNumber}" label="DELETE ENTRY" /></c:if></td>
</tr>
</el:table>
<el:text type="hidden" name="srcLine" value="${empty entry ? 0 : entry.lineNumber}" />
<el:text type="hidden" name="src" value="${entry.source}" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<fmt:aptype var="useICAO" />
<script>
const f = document.forms[0];
const cfg = golgotha.airportLoad.config;
cfg.doICAO = ${useICAO}; cfg.useSched = false;
golgotha.airportLoad.setHelpers([f.airportD,f.airportA]);
golgotha.local.changeAirline(f.airline);
</script>
</body>
</html>
