<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Schedule - ${empty entry ? 'New Entry' : entry.flightCode}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="airportRefresh" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.airline, 'Airline')) return false;
if (!validateNumber(form.flightNumber, 1, 'Flight Number')) return false;
if (!validateNumber(form.flightLeg, 1, 'Flight Leg')) return false;
if (!validateCombo(form.eqType, 'Equipment Type')) return false;
if (!validateCombo(form.airportD, 'Departure Airport')) return false;
if (!validateCombo(form.airportA, 'Arrival Airport')) return false;

setSubmit();
disableButton('SaveButton');
disableButton('DeleteButton');
return true;
}
<c:if test="${empty entry}">
function getAvailableFlight()
{
var f = document.forms[0];
if (!validateCombo(f.airline, 'Airline')) return false;
var aCode = f.airline[f.airline.selectedIndex].value;

// Get start/end ranges
var startF = f.rangeStart.value;
var endF = f.rangeEnd.value;

// Create the XMLHTTP request
var xmlreq = getXMLHttpRequest();
xmlreq.open("GET", "next_flight.ws?start=" + startF + "&end=" + endF + "&airline=" + aCode, true);
xmlreq.onreadystatechange = function () {
	if (xmlreq.readyState != 4) return false;
	var xmlDoc = xmlreq.responseXML;
	var e = xmlDoc.documentElement;

	// Update the flight number and leg
	var f = document.forms[0];
	f.flightNumber.value = e.getAttribute("number");
	f.flightLeg.value = e.getAttribute("leg");

	// Enable the buttons
	enableElement('LegSearchButton', true);
	enableElement('FlightSearchButton', true);
	return true;
}

disableButton('LegSearchButton');
disableButton('FlightSearchButton');
xmlreq.send(null);
return true;
}

function getAvailableLeg()
{
var f = document.forms[0];
if (!validateCombo(f.airline, 'Airline')) return false;
var aCode = f.airline[f.airline.selectedIndex].value;

// Create the XMLHTTP Request
var xmlreq = getXMLHttpRequest();
xmlreq.open("GET", "next_leg.ws?flight=" + f.flightNumber.value + "&airline=" + aCode, true);
xmlreq.onreadystatechange = function () {
	if (xmlreq.readyState != 4) return false;
	var xmlDoc = xmlreq.responseXML;
	var e = xmlDoc.documentElement;

	// Update the flight number and leg
	var f = document.forms[0];
	f.flightNumber.value = e.getAttribute("number");
	f.flightLeg.value = e.getAttribute("leg");

	// Enable the button
	enableElement('LegSearchButton', true);
	enableElement('FlightSearchButton', true);
	return true;
}

disableButton('LegSearchButton');
disableButton('FlightSearchButton');
xmlreq.send(null);
return true;
}
</c:if>
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
<el:form action="sched.do" method="post" linkID="${entry.flightCode}" op="save" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
<c:if test="${empty entry}"> <td colspan="2">NEW <content:airline /> SCHEDULE ENTRY</td></c:if>
<c:if test="${!empty entry}"> <td colspan="2">FLIGHT ${entry.flightCode}</td></c:if>
</tr>
<tr>
 <td class="label">Airline Name</td>
 <td class="data"><el:combo name="airline" idx="*" size="1" options="${airlines}" value="${entry.airline}" onChange="void changeAirline(this, false)" firstEntry="< AIRLINE >" /></td>
</tr>
<tr>
 <td class="label" valign="top">Flight Number / Leg</td>
 <td class="data"><el:text name="flightNumber" idx="*" size="3" max="4" value="${entry.flightNumber}" />
 <el:text name="flightLeg" idx="*" size="1" max="1" value="${empty entry ? '1' : entry.leg}" />
<c:if test="${empty entry}">
<hr />
<span class="small">You can search for an available flight number between 
<el:text name="rangeStart" idx="*" className="small" size="3" max="4" value="" /> and 
<el:text name="rangeEnd" idx="*" className="small" size="3" max="4" value="" />
<el:button ID="FlightSearchButton" className="BUTTON" onClick="void getAvailableFlight()" label="SEARCH" /><br />
You can search for the next available Flight Leg. <el:button ID="LegSearchButton" className="BUTTON" onClick="void getAvailableLeg()" label="SEARCH" /></c:if></td>
</tr>
<tr>
 <td class="label">Equipment Type</td>
 <td class="data"><el:combo name="eqType" idx="*" size="1" options="${eqTypes}" value="${entry.equipmentType}" firstEntry="< EQUIPMENT >" /></td>
</tr>
<tr>
 <td class="label">Departing From</td>
 <td class="data"><el:combo name="airportD" size="1" options="${airports}" value="${entry.airportD}" onChange="void changeAirport(this)" />
 <el:text ID="airportDCode" name="airportDCode" idx="*" size="3" max="4" value="${entry.airportD.ICAO}" onBlur="void setAirport(document.forms[0].airportD, this.value)" />
 at <el:text name="timeD" idx="*" size="4" max="5" value="${fn:dateFmt(entry.timeD, 'HH:mm')}" /> <span class="small">(Format: HH:mm)</span></td>
</tr>
<tr>
 <td class="label">Arriving At</td>
 <td class="data"><el:combo name="airportA" size="1" options="${airports}" value="${entry.airportA}" onChange="void changeAirport(this)" />
 <el:text ID="airportACode" name="airportACode" idx="*" size="3" max="4" value="${entry.airportA.ICAO}" onBlur="void setAirport(document.forms[0].airportA, this.value)" />
 at <el:text name="timeA" idx="*" size="4" max="5" value="${fn:dateFmt(entry.timeA, 'HH:mm')}" /> <span class="small">(Format: HH:mm)</span></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="doPurge" className="small" idx="*" value="true" label="Purge Flight on Schedule Import" checked="${entry.canPurge}" />
<el:box name="isHistoric" className="small" idx="*" value="true" label="Historic Flight" checked="${entry.historic}" />
<c:if test="${academyEnabled}"><el:box name="isAcademy" className="small" idx="*" value="true" label="Flight Academy Flight" checked="${entry.academy}" /></c:if></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SaveButton" type="submit" className="BUTTON" label="SAVE SCHEDULE ENTRY" />
<c:if test="${!empty entry}">&nbsp;<el:cmdbutton ID="DeleteButton" url="sched_delete" linkID="${entry.flightCode}" label="DELETE ENTRY" /></c:if></td>
</tr>
</el:table>
</el:form>
<br />
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
