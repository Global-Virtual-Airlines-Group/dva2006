<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<c:if test="${!empty pirep}">
<title><content:airline /> Flight ${pirep.flightCode}</title>
<c:set var="isAssign" value="${(fn:AssignID(pirep) > 0) && (!empty pirep.airportA) && (!empty pirep.airportD)}" scope="page" />
</c:if>
<c:if test="${empty pirep}">
<title>New <content:airline /> Flight Report</title></c:if>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:browser html4="true"><content:js name="hourCalc" /></content:browser>
<c:if test="${!isAssign}"><content:json />
<content:js name="airportRefresh" /></c:if>
<content:googleAnalytics eventSupport="true" />
<content:sysdata var="minDays" name="users.pirep.minDays" />
<fmt:aptype var="useICAO" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.flightNumber, min:1, t:'Flight Number'});
golgotha.form.validate({f:f.flightLeg, min:1, t:'Flight Leg'});
golgotha.form.validate({f:f.eq, t:'Equipment Type'});
golgotha.form.validate({f:f.flightTime, t:'Logged Hours'});
golgotha.form.validate({f:f.network, min:1, t:'Online Network'});
golgotha.form.validate({f:f.fsVersion, t:'Simulator Version'});
<c:if test="${!isAssign}">
golgotha.form.validate({f:f.airline, t:'Airline'});
golgotha.form.validate({f:f.airportD, t:'Departure Airport'});
golgotha.form.validate({f:f.airportA, t:'Arrival Airport'});</c:if>
if (parseInt(f.flightLeg.value) > 8)
	throw new golgotha.event.ValidationError('The Flight Leg must be equal to or less than 8.', f.flightLeg);
<content:browser html4="true">
// Validate the date
<content:filter roles="!PIREP">
var pY = parseInt(f.dateY.options[f.dateY.selectedIndex].text);
var pD = parseInt(f.dateD.options[f.dateD.selectedIndex].text);
var pDate = new Date(pY, f.dateM.selectedIndex, pD);
if (pDate > fwdLimit)
	throw new golgotha.event.ValidationError('You cannot file a Flight Report for a flight in the future.', f.dateM);
if (pDate < bwdLimit)
	throw new golgotha.event.ValidationError('You cannot file a Flight Report for a flight flown more than ${minDays} days ago.', f.dateD);
</content:filter></content:browser>
golgotha.form.submit(f);
return true;
};

golgotha.local.saveSubmit = function() {
	var f = document.forms[0];
	f.doSubmit.value = 'true';
	return golgotha.form.post(f.action);
};

golgotha.local.initDateCombos = function(mCombo, dCombo, d) {
	mCombo.selectedIndex = d.getMonth() - 1;
	golgotha.local.setDaysInMonth(mCombo);
	dCombo.selectedIndex = d.getDate() - 1;
	return true;
};

golgotha.local.setDaysInMonth = function(combo)
{
var dCombo = document.forms[0].dateD;
var daysInMonth = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
var dd = dCombo.selectedIndex;
dCombo.options.length = daysInMonth[combo.selectedIndex];
for (var x = 1; x <= daysInMonth[combo.selectedIndex]; x++)
	dCombo.options[x-1] = new Option(x);

dCombo.selectedIndex = Math.min(dd, dCombo.options.length - 1);
return true;
};

golgotha.local.loadAirports = function()
{
var f = document.forms[0];
if (f.airline.selectedIndex != 0) {
	golgotha.airportLoad.config.airline = golgotha.form.getCombo(f.airline);
	var cfg = golgotha.airportLoad.config.clone();
	cfg.add = golgotha.form.getCombo(f.airportD); 
	f.airportD.loadAirports(cfg);
	cfg = golgotha.airportLoad.config.clone();
	cfg.add = golgotha.form.getCombo(f.airportA);
	f.airportA.loadAirports(cfg);
} else
	delete golgotha.airportLoad.config.airline;

f.airline.focus();	
return true;
};

golgotha.onDOMReady(function() {
	var f = document.forms[0];
	golgotha.airportLoad.setHelpers(f.airportD);
	golgotha.airportLoad.setHelpers(f.airportA);
	golgotha.airportLoad.config.doICAO = ${useICAO};
});
<content:browser html4="true">
// Set PIREP date limitations
<fmt:jsdate var="fwdLimit" date="${forwardDateLimit}" />
<fmt:jsdate var="bwdLimit" date="${backwardDateLimit}" /></content:browser>
</script>
</head>
<content:copyright visible="false" />
<body onload="void golgotha.local.loadAirports()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:empty var="emptyList" />
<content:singleton var="apD" value="${pirep.airportD}" />
<content:singleton var="apA" value="${pirep.airportA}" />
<content:enum var="fsVersions" className="org.deltava.beans.Simulator" exclude="UNKNOWN,FS98" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="pirep.do" link="${pirep}" op="save" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
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
 <td class="data">${pilot.pilotCode} (${pilot.rank.name})</td>
</tr>
<tr>
 <td class="label">Status</td>
 <td class="data bld sec">${!empty pirep ? pirep.statusName : 'NEW'} <c:if test="${fn:AssignID(pirep) > 0}"><span class="ter bld">FLIGHT ASSIGNMENT</span></c:if></td>
</tr>
<c:choose>
<c:when test="${!isAssign}">
<tr>
 <td class="label">Airline Name</td>
 <td class="data"><el:combo name="airline" idx="*" size="1" options="${airlines}" value="${pirep.airline}" onChange="void golgotha.local.loadAirports()" className="req" firstEntry="[ AIRLINE ]" /></td>
</tr>
</c:when>
<c:otherwise>
<tr>
 <td class="label">Airline Name</td>
 <td class="data"><el:combo name="airline" idx="*" size="1" firstEntry="${pirep.airline}" options="${emptyList}" /></td>
</tr>
</c:otherwise>
</c:choose>
<tr>
 <td class="label">Flight Number / Leg</td>
 <td class="data"><el:int name="flightNumber" idx="*" size="3" min="1" max="9999" required="true" value="${pirep.flightNumber}" />
 <el:int name="flightLeg" idx="*" size="1" min="1" max="8" required="true" value="${pirep.leg}" /></td>
</tr>
<tr>
 <td class="label">Equipment Type</td>
 <td class="data"><el:combo name="eq" idx="*" size="1" options="${eqTypes}" value="${pirep.equipmentType}" required="true" firstEntry="[ EQUIPMENT ]" /></td>
</tr>
<c:choose>
<c:when test="${!isAssign}">
<tr>
 <td class="label">Departed from</td>
 <td class="data"><el:combo name="airportD" size="1" options="${apD}" required="true" value="${pirep.airportD}" onChange="void this.updateAirportCode()" />
 <el:airportCode combo="airportD" idx="*" airport="${pirep.airportD}" /></td>
</tr>
<tr>
 <td class="label">Arrived at</td>
 <td class="data"><el:combo name="airportA" size="1" options="${apA}" required="true" value="${pirep.airportA}" onChange="void this.updateAirportCode()" />
 <el:airportCode combo="airportA" idx="*" airport="${pirep.airportA}" /></td>
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
<content:browser html5="true">
 <td class="data"><el:date name="date" idx="*" size="11" required="true" min="${backwardDateLimit}" max="${forwardDateLimit}" value="${pirep.date}" /></td>
</content:browser>
<content:browser html4="true">
 <td class="data"><el:combo name="dateM" idx="*" size="1" options="${months}" required="true" onChange="void golgotha.local.setDaysInMonth(this)" />
 <el:combo name="dateD" idx="*" size="1" required="true" options="${emptyList}" /> <el:combo name="dateY" idx="*" size="1" value="${pirepYear}" required="true" options="${years}" /></td>
</content:browser>
</tr>
<tr>
 <td class="label">Online Flight</td>
 <td class="data"><el:check type="radio" name="network" idx="*" width="70" firstEntry="Offline" options="${networks}" value="${fn:network(pirep)}" /></td>
</tr>
<tr>
 <td class="label">Simulator</td>
 <td class="data"><el:combo name="fsVersion" idx="*" size="1" required="true" options="${fsVersions}" firstEntry="[ SIMULATOR ]" value="${pirep.FSVersion}" /></td>
</tr>
<c:set var="tmpH" value="${empty pirep ? '' : pirep.length / 10}" scope="page" />
<c:set var="tmpM" value="${empty pirep ? '' : (pirep.length % 10) * 6}" scope="page" />
<tr>
 <td class="label">Logged Time</td>
<content:browser html4="true">
 <td class="data"><el:combo name="flightTime" idx="*" size="1"  required="true" firstEntry="[ HOURS ]" options="${flightTimes}" value="${flightTime}" />&nbsp;
<el:text name="tmpHours" size="1" max="2" idx="*" value="${tmpH}" /> hours, <el:text name="tmpMinutes" size="1" max="2" idx="*" value="${tmpM}" />
 minutes&nbsp;<el:button ID="CalcButton" label="CALCULATE" onClick="void golgotha.form.wrap(golgotha.util.hoursCalc, document.forms[0])" /></td>
</content:browser>
<content:browser html5="true">
 <td class="data"><el:float name="flightTime" idx="*" size="3" max="18.9" min="0" step="0.1" value="${flightTime}" required="true" /> hours</td>
</content:browser>
</tr>
<c:if test="${!isACARS}">
<tr>
 <td class="label top">Flight Route</td>
 <td class="data"><el:textbox idx="*" name="route" width="80%" height="3" resize="true">${pirep.route}</el:textbox></td>
</tr>
</c:if>
<c:if test="${isACARS}">
<%@ include file="/jsp/pilot/pirepACARS.jspf" %></c:if>
<tr>
 <td class="label top">Remarks</td>
 <td class="data"><el:textbox idx="*" name="remarks" width="80%" height="3" resize="true">${pirep.remarks}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="SAVE FLIGHT REPORT" />
<c:if test="${access.canSubmitIfEdit}">&nbsp;<el:button ID="SubmitButton" onClick="void golgotha.local.saveSubmit()" label="SUBMIT FLIGHT REPORT" /></c:if>
</td>
</tr>
</el:table>
<el:text name="doSubmit" type="hidden" value="" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:browser html4="true">
<script id="dateInit" async>
var f = document.forms[0];
var d = new Date(${pirepYear},${pirepMonth},${pirepDay},0,0,0);
golgotha.local.initDateCombos(f.dateM, f.dateD, d);
f.tmpHours.value = Math.round(f.tmpHours.value - 0.5);
</script></content:browser>
</body>
</html>
