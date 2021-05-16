<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
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
<content:favicon />
<content:js name="common" />
<content:json />
<content:js name="airportRefresh" />
<content:googleAnalytics eventSupport="true" />
<content:sysdata var="minDays" name="users.pirep.minDays" />
<content:captcha action="pirepEdit" />
<fmt:aptype var="useICAO" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script>
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
const pY = parseInt(f.dateY.options[f.dateY.selectedIndex].text);
const pD = parseInt(f.dateD.options[f.dateD.selectedIndex].text);
const pDate = new Date(pY, f.dateM.selectedIndex, pD);
if (pDate > fwdLimit)
	throw new golgotha.event.ValidationError('You cannot file a Flight Report for a flight in the future.', f.dateM);
if (pDate < bwdLimit)
	throw new golgotha.event.ValidationError('You cannot file a Flight Report for a flight flown more than ${minDays} days ago.', f.dateD);
</content:filter></content:browser>
golgotha.form.submit(f);
return true;
};

golgotha.local.saveSubmit = function() {
	const f = document.forms[0];
	f.doSubmit.value = 'true';
	return golgotha.form.post(f.action);
};

golgotha.local.initDateCombos = function(mCombo, dCombo, d) {
	mCombo.selectedIndex = d.getMonth();
	golgotha.local.setDaysInMonth(mCombo);
	dCombo.selectedIndex = d.getDate() - 1;
	return true;
};

golgotha.local.setDaysInMonth = function(combo) {
	const y = new Date().getFullYear();
	const isLeapYear = (((y % 4) == 0) && ((y % 100) != 0));
	const dCombo = document.forms[0].dateD;
	const daysInMonth = [31, (isLeapYear ? 29 : 28), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
	const dd = dCombo.selectedIndex;
	dCombo.options.length = daysInMonth[combo.selectedIndex];
	for (var x = 0; x < daysInMonth[combo.selectedIndex]; x++)
		dCombo.options[x] = new Option(x + 1);

	dCombo.selectedIndex = Math.min(dd, dCombo.options.length - 1);
	return true;
};

golgotha.local.loadAirports = function()
{
const f = document.forms[0];
if (f.airline.selectedIndex != 0) {
	golgotha.airportLoad.config.airline = golgotha.form.getCombo(f.airline);
	let cfg = golgotha.airportLoad.config.clone();
	cfg.add = golgotha.form.getCombo(f.airportD); 
	f.airportD.loadAirports(cfg);
	cfg = golgotha.airportLoad.config.clone();
	cfg.dst = true;
	cfg.add = golgotha.form.getCombo(f.airportA);
	f.airportA.loadAirports(cfg);
} else
	delete golgotha.airportLoad.config.airline;

f.airline.focus();	
return true;
};

golgotha.onDOMReady(function() {
	const f = document.forms[0];
	golgotha.airportLoad.setHelpers([f.airportD,f.airportA]);
	golgotha.airportLoad.config.doICAO = ${useICAO};
	f.airline.updateAirlineCode = golgotha.airportLoad.updateAirlineCode;

	const d = new Date(${pirepYear},${pirepMonth - 1},${pirepDay});
	golgotha.local.initDateCombos(f.dateM, f.dateD, d);
	f.tmpHours.value = Math.round(f.tmpHours.value - 0.5);
});

golgotha.local.hoursCalc = function(f)
{
const h = parseInt(f.tmpHours.value);
const m = parseInt(f.tmpMinutes.value);
if ((h == Number.NaN) || (m == Number.NaN)) {
	const fe = (h == Number.NaN) ? f.tmpHours : f.tmpMinutes;
    throw new golgotha.event.ValidationError('Please fill in both Hours and Minutes.', fe);
}
    
if ((h < 0) || (m < 0)) {
	const fe = (h < 0) ? f.tmpHours : f.tmpMinutes;
    throw new golgotha.event.ValidationError('Hours and minutes cannnot be negative.', fe);
}

// Turn into a single number
const tmpHours = (h + (m / 60));
const hrs = Math.round(tmpHours * 10) / 10;
const combo = f.flightTime;
for (x = 0; x < combo.options.length; x++) {
    const opt = combo.options[x];
    if (opt.text == hrs) {
        opt.selected = true;
        break;
    }
}

return true;
};
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
 <td class="data bld sec"><fmt:defaultMethod object="${pirep.status}" method="description" empty="NEW" /><c:if test="${fn:AssignID(pirep) > 0}">&nbsp;<span class="ter bld">FLIGHT ASSIGNMENT</span></c:if></td>
</tr>
<c:choose>
<c:when test="${!isAssign}">
<tr>
 <td class="label">Airline</td>
 <td class="data"><el:combo name="airline" idx="*" size="1" options="${airlines}" value="${pirep.airline}" onChange="this.updateAirlineCode(); golgotha.local.loadAirports()" required="true" firstEntry="[ AIRLINE ]" />
 <el:text name="airlineCode" size="2" max="3" idx="*" autoComplete="false" className="caps" onChange="void golgotha.airportLoad.setAirline(document.forms[0].airline, this, true)" /></td>
</tr>
</c:when>
<c:otherwise>
<tr>
 <td class="label">Airline</td>
 <td class="data"><el:combo name="airline" idx="*" size="1" firstEntry="${pirep.airline}" options="${emptyList}" /></td>
</tr>
</c:otherwise>
</c:choose>
<tr>
 <td class="label">Flight Number / Leg</td>
 <td class="data"><el:int name="flightNumber" idx="*" size="3" min="1" max="9999" required="true" value="${pirep.flightNumber}" />&nbsp;<el:int name="flightLeg" idx="*" size="1" min="1" max="8" required="true" value="${pirep.leg}" /></td>
</tr>
<tr>
 <td class="label">Equipment Type</td>
 <td class="data"><el:combo name="eq" idx="*" size="1" options="${eqTypes}" value="${pirep.equipmentType}" required="true" firstEntry="[ EQUIPMENT ]" /></td>
</tr>
<c:choose>
<c:when test="${!isAssign}">
<tr>
 <td class="label">Departed from</td>
 <td class="data"><el:combo name="airportD" size="1" options="${apD}" required="true" value="${pirep.airportD}" onChange="void this.updateAirportCode()" /> <el:airportCode combo="airportD" idx="*" airport="${pirep.airportD}" /></td>
</tr>
<tr>
 <td class="label">Arrived at</td>
 <td class="data"><el:combo name="airportA" size="1" options="${apA}" required="true" value="${pirep.airportA}" onChange="void this.updateAirportCode()" /> <el:airportCode combo="airportA" idx="*" airport="${pirep.airportA}" /></td>
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
 <td class="data"><el:combo name="fsVersion" idx="*" size="1" required="true" options="${fsVersions}" firstEntry="[ SIMULATOR ]" value="${pirep.simulator}" /></td>
</tr>
<c:set var="tmpH" value="${empty pirep ? '' : pirep.length / 10}" scope="page" />
<c:set var="tmpM" value="${empty pirep ? '' : (pirep.length % 10) * 6}" scope="page" />
<tr>
 <td class="label">Logged Time</td>
 <td class="data"><el:combo name="flightTime" idx="*" size="1"  required="true" firstEntry="[ HOURS ]" options="${flightTimes}" value="${flightTime}" /> <el:text name="tmpHours" size="1" max="2" idx="*" value="${tmpH}" /> hours, 
 <el:text name="tmpMinutes" size="1" max="2" idx="*" value="${tmpM}" /> minutes <el:button label="CALCULATE" onClick="void golgotha.form.wrap(golgotha.local.hoursCalc, document.forms[0])" /></td>
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
 <td><el:button type="submit" label="SAVE FLIGHT REPORT" /><c:if test="${access.canSubmitIfEdit}">&nbsp;<el:button onClick="void golgotha.local.saveSubmit()" label="SUBMIT FLIGHT REPORT" /></c:if>
</td>
</tr>
</el:table>
<el:text name="doSubmit" type="hidden" value="" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
