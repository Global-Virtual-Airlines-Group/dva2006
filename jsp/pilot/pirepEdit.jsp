<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<c:if test="${!empty pirep}">
<title><content:airline /> Flight ${pirep.flightCode}</title>
</c:if>
<c:if test="${empty pirep}">
<title>New <content:airline /> Flight Report</title>
</c:if>
<c:set var="isAssign" value="{(fn:AssignID(pirep) > 0) && (!empty pirep.airportA) && (!empty pirep.airportD)}" scope="request" />
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="hourCalc" />
<c:if test="${!isAssign}">
<content:js name="airportRefresh" />
</c:if>
<content:sysdata var="networks" name="online.networks" />
<content:sysdata var="minDays" name="users.pirep.minDays" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateNumber(form.flightNumber, 1, 'Flight Number')) return false;
if (!validateNumber(form.flightLeg, 1, 'Flight Leg')) return false;
if (!validateCombo(form.eq, 'Equipment Type')) return false;
if (!validateCombo(form.flightTime, 'Logged Hours')) return false;
if (!validateCheckBox(form.network, 1, 'Online Network')) return false;
if (!validateCheckBox(form.fsVersion, 1, 'Flight Simulator Version')) return false;
if (!validateCombo(form.airline, 'Airline')) return false;
<c:if test="${!isAssign}">
if (!validateCombo(form.airportD, 'Departure Airport')) return false;
if (!validateCombo(form.airportA, 'Arrival Airport')) return false;
</c:if>

// Validate flight leg
if (parseInt(form.flightLeg.value) > 8) {
	alert('The Flight Leg must be less than 8.');
	form.flightLeg.focus();
	return false;
}

// Validate the date
<c:if test="${!empty pirep}"><content:filter roles="!PIREP">
var pY = parseInt(f.dateY.options[f.dateY.selectedIndex].text);
var pDate = new Date(pY, f.dateM.selectedIndex + 1,	f.dateD.selectedIndex + 1);
if (pDate > fwdLimit) {
	alert('You cannot file a Flight Report for a flight in the future.');
	f.dateM.focus();
	return false;
} else if (pDate < bwdLimit) {
	alert('You cannot file a Flight Report for a flight flown more than ${minDays} days ago.');
	f.dateD.focus();
	return false;
}
</content:filter></c:if>

setSubmit();
disableButton('SaveButton');
disableButton('CalcButton');
disableButton('SubmitButton');
return true;
}

function saveSubmit()
{
var f = document.forms[0];
f.doSubmit.value = 'true';
return cmdPost(f.action);
}

// Set PIREP date limitations
var fwdLimit = new Date(${forwardDateLimit});
var bwdLimit = new Date(${backwardDateLimit});
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="pirep.do" linkID="${fn:dbID(pirep)}" op="save" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<!-- PIREP Title Bar -->
<tr class="title caps">
<c:if test="${!empty pirep}">
 <td colspan="2">FLIGHT ${pirep.flightCode} FLOWN ON <fmt:date fmt="d" date="${pirep.date}" /> by ${pilot.name}</td>
</c:if>
<c:if test="${empty pirep}">
 <td colspan="2">NEW FLIGHT REPORT</td>
</c:if>
</tr>

<!-- PIREP Data -->
<tr>
 <td class="label">Pilot Code / Rank</td>
 <td class="data">${pilot.pilotCode} (${pilot.rank})</td>
</tr>
<tr>
 <td class="label">Status</td>
 <td class="data bld sec">${!empty pirep ? pirep.statusName : 'NEW'}</td>
</tr>
<tr>
 <td class="label">Airline Name</td>
 <td class="data"><el:combo name="airline" idx="*" size="1" options="${airlines}" value="${pirep.airline}" onChange="void changeAirline(this, false)" className="req" firstEntry="< AIRLINE >" /></td>
</tr>
<tr>
 <td class="label">Flight Number / Leg</td>
 <td class="data"><el:text name="flightNumber" idx="*" size="3" max="4" className="req" value="${pirep.flightNumber}" />
 <el:text name="flightLeg" idx="*" size="1" max="1" className="req" value="${pirep.leg}" /></td>
</tr>
<tr>
 <td class="label">Equipment Type</td>
 <td class="data"><el:combo name="eq" idx="*" size="1" options="${eqTypes}" value="${pirep.equipmentType}" className="req" firstEntry="< EQUIPMENT >" /></td>
</tr>
<c:choose>
<c:when test="${!isAssign}">
<tr>
 <td class="label">Departed from</td>
 <td class="data"><el:combo name="airportD" size="1" options="${emptyList}" className="req" onChange="void changeAirport(this)" />
 <el:text ID="airportDCode" name="airportDCode" idx="*" size="3" max="4" onBlur="void setAirport(document.forms[0].airportD, this.value)" /></td>
</tr>
<tr>
 <td class="label">Arrived at</td>
 <td class="data"><el:combo name="airportA" size="1" options="${emptyList}" className="req" onChange="void changeAirport(this)" />
 <el:text ID="airportACode" name="airportACode" idx="*" size="3" max="4" onBlur="void setAirport(document.forms[0].airportA, this.value)" /></td>
</tr>
</c:when>
<c:otherwise>
<tr>
 <td class="label">Departed from</td>
 <td class="data"><el:combo name="airportD" size="1" firstEntry="${pirep.airportD}" options="${emptyList}" /></td>
</tr>
<tr>
 <td class="label">Arrived at</td>
 <td class="data"><el:combo name="airportA" size="1" firstEntry="${pirep.airportA}" options="${emptyList}" /></td>
</tr>
</c:otherwise>
</c:choose>
<tr>
 <td class="label">Flown on</td>
 <td class="data"><el:combo name="dateM" idx="*" size="1" options="${months}" className="req" onChange="setDaysInMonth(this)" />
 <el:combo name="dateD" idx="*" size="1" className="req" options="${emptyList}" />&nbsp;
 <el:combo name="dateY" idx="*" size="1" value="${pirep.date.year + 1900}" className="req" options="${years}" /></td>
</tr>
<tr>
 <td class="label">Online Flight</td>
 <td class="data"><el:check type="radio" name="network" idx="*" width="70" firstEntry="Offline" options="${networks}" value="${fn:network(pirep)}" /></td>
</tr>
<tr>
 <td class="label">Flight Simulator</td>
 <td class="data"><el:check type="radio" name="fsVersion" idx="*" width="70" options="${fsVersions}" value="${pirep.FSVersionCode}" /></td>
</tr>
<c:set var="tmpH" value="${empty pirep ? '' : pirep.length / 10}" scope="request" />
<c:set var="tmpM" value="${empty pirep ? '' : (pirep.length % 10) * 6}" scope="request" />
<tr>
 <td class="label">Logged Time</td>
 <td class="data"><el:combo name="flightTime" idx="*" size="1" className="req" firstEntry="< HOURS >" options="${flightTimes}" value="${flightTime}" />&nbsp;
<el:text name="tmpHours" size="1" max="2" idx="*" value="${tmpH}" /> hours, <el:text name="tmpMinutes" size="1" max="2" idx="*" value="${tmpM}" />
 minutes&nbsp;<el:button ID="CalcButton" className="BUTTON" label="CALCULATE" onClick="void hoursCalc()" /></td>
</tr>
<c:if test="${isACARS}">
<%@ include file="/jsp/pilot/pirepACARS.jspf" %> 
</c:if>
<tr>
 <td class="label" valign="top">Remarks</td>
 <td class="data"><el:textbox idx="*" name="remarks" width="80%" height="5">${pirep.remarks}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:button ID="SaveButton" type="SUBMIT" className="BUTTON" label="SAVE FLIGHT REPORT" />
<c:if test="${access.canSubmitIfEdit}">
&nbsp;<el:button ID="SubmitButton" onClick="void saveSubmit()" className="BUTTON" label="SUBMIT FLIGHT REPORT" />
</c:if>
</td>
</tr>
</el:table>
<el:text name="doSubmit" type="HIDDEN" value="" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script language="JavaScript" type="text/javascript">
var f = document.forms[0];
var d = new Date(${pirepYear},${pirepMonth},${pirepDay},0,0,0);

initDateCombos(f.dateM, f.dateD, ((d == null) ? new Date() : d));
f.airline.focus();
if (f.airline.selectedIndex != 0) {
	var aCode = getValue(f.airline);
	updateAirports(f.airportD, 'airline=' + aCode, false, '${pirep.airportD.IATA}');
	updateAirports(f.airportA, 'airline=' + aCode, false, '${pirep.airportA.IATA}');
}
</script>
</body>
</html>
