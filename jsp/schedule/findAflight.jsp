<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Schedule Search</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<content:js name="airportRefresh" />
<content:googleAnalytics eventSupport="true" />
<content:sysdata var="innovataLink" name="schedule.innovata.enabled" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;

// Check that at least one option was selected
var eqOK = (form.eqType.selectedIndex > 0);
var adOK = (form.airportD.selectedIndex > 0);
var aaOK = (form.airportA.selectedIndex > 0);

if (eqOK || adOK || aaOK) {
	setSubmit();
	disableButton('SearchButton');
	disableButton('BuildButton');
	disableButton('BuildResetButton');
	disableButton('SaveButton');
	disableButton('ClearButton');
	return true;
}

alert('Please select at least an Aircraft type or a Departure/Arrival Airport.');
return false;
}
<c:if test="${!empty fafResults}">
function buildValidate(form)
{
if (!checkSubmit()) return false;

var isOK = false;
if (form.addFA.length) {
	for (var x = 0; x < form.addFA.length; x++)
		isOK = isOK || form.addFA[x].checked;
} else {
	isOK = form.addFA.checked;
}

if (!isOK) {
	alert('Please select at least one Flight Leg to add.');
	return false;
}

setSubmit();
disableButton('SearchButton');
disableButton('BuildButton');
disableButton('BuildResetButton');
disableButton('SaveButton');
disableButton('ClearButton');
return true;
}
</c:if>
function updateAirline(combo)
{
var f = document.forms[0];
updateAirports(f.airportD, 'useSched=true&airline=' + getValue(combo), false, getValue(f.airportD));
updateAirports(f.airportA, 'useSched=true&dst=true&airline=' + getValue(combo), false, getValue(f.airportA));
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body onload="void initLinks()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="acarsEnabled" name="acars.enabled" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="findflight.do" op="search" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="4"><content:airline /> SCHEDULE SEARCH</td>
</tr>
<tr>
 <td class="label">Airline</td>
 <td class="data"><el:combo name="airline" size="1" idx="*" firstEntry="-" options="${airlines}" value="${fafCriteria.airline}" onChange="void updateAirline(this)" /></td>
 <td class="label">Equipment</td>
 <td class="data"><el:combo name="eqType" size="1" idx="*" firstEntry="-" options="${allEQ}" value="${fafCriteria.equipmentType}" /></td>
</tr>
<tr>
 <td class="label">Flight Number / Leg</td>
 <td class="data"><el:text name="flightNumber" idx="*" size="3" max="4" />
 <el:text name="flightLeg" idx="*" size="1" max="1" value="${fafCriteria.leg == 0 ? '' : fafCriteria.leg}" /></td>
 <td class="label">Distance</td>
 <td class="data"><el:text name="distance" idx="*" size="4" max="4" value="${fafCriteria.distance < 1 ? '' : fafCriteria.distance}" />
 +/- <el:text name="distRange" idx="*" size="4" max="4" value="${fafCriteria.distance < 1 ? '' : fafCriteria.distanceRange}" /> miles</td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airportD" idx="*" size="1" firstEntry="-" options="${airports}" value="${fafCriteria.airportD}" onChange="changeAirport(this); updateOrigin(this)" />
 <el:text ID="airportDCode" name="airportDCode" idx="*" size="3" max="4" onBlur="void setAirport(document.forms[0].airportD, this.value)" /></td>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" idx="*" size="1" firstEntry="-" options="${airportsA}" value="${fafCriteria.airportA}" onChange="void changeAirport(this)" />
 <el:text ID="airportACode" name="airportACode" idx="*" size="3" max="4" onBlur="void setAirport(document.forms[0].airportA, this.value)" /></td>
</tr>
<tr>
 <td class="label">Departure Time (+/- 2h)</td>
 <td class="data"><el:combo name="hourD" idx="*" size="1" options="${hours}" value="${fafCriteria.hourD}" /></td>
 <td class="label">Arrival Time (+/- 2h)</td>
 <td class="data"><el:combo name="hourA" idx="*" size="1" options="${hours}" value="${fafCriteria.hourA}" /></td>
</tr>
<tr>
 <td class="label">Sort Flights by</td>
 <td class="data"><el:combo name="sortType" idx="*" size="1" options="${sortTypes}" value="${param.sortType}" />
 <el:box name="sortDesc" idx="*" value="true" checked="${param.sortDesc}" label="Descending" /></td>
 <td class="label">Maximum Results</td>
 <td class="data"><el:text name="maxResults" idx="*" size="2" max="3" value="${empty fafCriteria ? 20 : fafCriteria.maxResults}" /></td>
</tr>
<c:if test="${acarsEnabled}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="includeHistoric" idx="*" value="true" checked="${fafCriteria.includeHistoric}" label="Include Historic Flights" /></td>
 <td class="label" valign="top">ACARS Dispatch</td>
 <td class="data"><el:box name="checkDispatch" idx="*" value="true" checked="${empty fafCriteria ? true : fafCriteria.checkDispatch}" label="Display Dispatch route count" /><br />
 <el:box name="dispatchOnly" idx="*" value="true" checked="${fafCriteria.dispatchOnly}" label="Flights with Dispatch routes only" /></td>
</tr>
</c:if>
<c:if test="${!acarsEnabled}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data" colspan="3"><el:box name="includeHistoric" idx="*" value="true" checked="${fafCriteria.includeHistoric}" label="Include Historic Flights" /></td>
</tr>
</c:if>
<tr class="title mid">
 <td colspan="4"><el:button ID="SearchButton" type="submit" className="BUTTON" label="SEARCH FLIGHT SCHEDULE" /></td>
</tr>
</el:table>
</el:form>
<c:if test="${!empty fafResults}">
<el:form method="post" action="buildAssign.do" validate="return buildValidate(this)">
<el:table className="view" space="default" pad="default">
<!-- Search Results Data -->
<tr class="title caps">
 <td colspan="7" class="left">SEARCH RESULTS</td>
</tr>

<!-- Search Results Header Bar -->
<tr class="caps title">
 <td width="5%">ADD</td>
 <td width="15%">FLIGHT NUMBER</td>
 <td width="10%">EQUIPMENT</td>
 <td width="35%">AIRPORTS</td>
 <td width="10%">DEPARTS</td>
 <td width="10%">ARRIVES</td>
 <td>DISTANCE</td>
</tr>

<!-- Search Results -->
<c:forEach var="flight" items="${fafResults}">
<view:row entry="${flight}">
 <td><input type="checkbox" class="check" name="addFA" value="${flight.flightCode}" /></td>
 <td class="pri bld">${flight.flightCode}</td>
 <td class="sec bld">${flight.equipmentType}</td>
 <td class="small">${flight.airportD.name} (<fmt:airport airport="${flight.airportD}" />) to
 ${flight.airportA.name} (<fmt:airport airport="${flight.airportA}" />)</td>
 <td><fmt:date fmt="t" t="HH:mm" tz="${flight.airportD.TZ}" date="${flight.dateTimeD.UTC}" /></td>
 <td><fmt:date fmt="t" t="HH:mm" tz="${flight.airportA.TZ}" date="${flight.dateTimeA.UTC}" /></td>
 <td class="sec"><fmt:int value="${flight.distance}" /> miles</td>
</view:row>
</c:forEach>

<tr class="title">
 <td colspan="7"><el:button ID="BuildButton" type="submit" className="BUTTON" label="BUILD FLIGHT ASSIGNMENT" />&nbsp;
<el:cmdbutton ID="BuildResetButton" url="buildassign" op="reset" label="RESET RESULTS" /></td>
</tr>
</el:table>
</el:form>
</c:if>
<c:if test="${!empty system_message}">
<tr class="error caps bld">
 <td colspan="5">${system_message}</td>
</tr>
</c:if>
<c:if test="${!empty buildAssign}">
<br />
<el:table className="view" space="default" pad="default">
<!-- Flight Assignment Data -->
<tr class="caps title left">
 <td colspan="5">FLIGHT ASSIGNMENT</td>
</tr>

<!-- Flight Assignment Header Bar -->
<tr class="caps title">
 <td width="20%">FLIGHT NUMBER</td>
 <td width="15%">EQUIPMENT</td>
 <td width="40%">AIRPORTS</td>
 <td width="10%">DISTANCE</td>
 <td>DURATION</td>
</tr>

<!-- Flighrt Assignment Legs -->
<c:forEach var="flight" items="${buildAssign.flights}">
<tr>
 <td class="pri bld">${flight.flightCode}</td>
 <td class="sec bld">${flight.equipmentType}</td>
 <td class="small">${flight.airportD.name} (<fmt:airport airport="${flight.airportD}" />) to
 ${flight.airportA.name} (<fmt:airport airport="${flight.airportA}" />)</td>
 <td class="sec"><fmt:int value="${flight.distance}" /> miles</td>
 <td><fmt:dec value="${flight.length / 10}" /> hours</td>
</tr>
</c:forEach>
<tr class="title">
 <td colspan="5"> <view:legend width="150" labels="Regular Flight,Historic Flight" classes=" ,opt2" />&nbsp;
<el:cmdbutton ID="SaveButton" url="assignsave" label="SAVE FLIGHT ASSIGMENT" />&nbsp;
<el:cmdbutton ID="ClearButton" url="buildassign" op="reset" label="CLEAR FLIGHT ASSIGNMENT" /></td>
</tr>
</el:table>
</c:if>

<c:if test="${doSearch && (empty fafResults)}">
<!-- No Search Results Found -->
<el:table className="view" space="default" pad="default">
<tr class="title caps">
 <td class="mid">No Flights matching your Search Criteria were found.</td>
</tr>
</el:table>
</c:if>
<c:if test="${innovataLink}">
<%@ include file="/jsp/schedule/innovataLink.jspf" %> 
</c:if>
<content:copyright />
</content:region>
</content:page>
<c:if test="${!empty fafCriteria}">
<script language="JavaScript" type="text/javascript">
changeAirport(document.forms[0].airportD);
changeAirport(document.forms[0].airportA);
</script></c:if>
</body>
</html>
