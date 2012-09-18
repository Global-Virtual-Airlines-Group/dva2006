<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Flight Pre-Approval</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="airportRefresh" />
<content:googleAnalytics eventSupport="true" />
<script type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.eqType, 'Equipment Type')) return false;
if (!validateNumber(form.flight, 1, 'Flight Number')) return false;
if (!validateNumber(form.leg, 1, 'Flight Leg')) return false;
if (!validateCombo(form.airline, 'Airline')) return false;
if (!validateCombo(form.airportD, 'Departure Airport')) return false;
if (!validateCombo(form.airportA, 'Arrival Airport')) return false;

setSubmit();
disableButton('SaveButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>
<content:empty var="emptyList" />
<content:sysdata name="schedule.charter.count_days" var="countDays" default="90" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="preapprove.do" method="post" linkID="${pilotID}" validate="return validate(this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">UNSCHEDULED FLIGHT APPROVAL - ${assignPilot.name}</td>
</tr>
<c:if test="${charterFlights > 0}">
<tr>
 <td class="label">Charter Flights</td>
 <td class="data"><fmt:int value="${charterFlights}" /> total Charters flown, <fmt:int value="${charterFlightsInterval}" /> Charters
 within the past <fmt:int value="${countDays}" /> days</td>
</tr>
</c:if>
<tr>
 <td class="label">Aircraft Type</td>
 <td class="data"><el:combo name="eqType" size="1" idx="*" className="req" options="${eqTypes}" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label">Airline</td>
 <td class="data"><el:combo name="airline" size="1" idx="*" className="req" options="${airlines}" onChange="void changeAirline(this, false)" firstEntry="-" /></td>
</tr>
<c:set var="flightNumber" value="${assignPilot.pilotNumber}" scope="request" />
<c:if test="${flightNumber > 9999}"><c:set var="flightNumber" value="${flightNumber % 10000}" scope="request" /></c:if>
<tr>
 <td class="label">Flight / Leg</td>
 <td class="data"><el:text name="flight" idx="*" size="4" max="4" className="pri bld req" value="${flightNumber}" />
 <el:text name="leg" idx="*" size="1" max="1" className="req" value="1" /></td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airportD" size="1" idx="*" className="req" options="${emptyList}" firstEntry="-" onChange="void changeAirport(this)" />
 <el:text name="airportDCode" size="4" max="4" idx="*" value="" onBlur="void setAirport(document.forms[0].airportD, this.value)" /></td>
</tr>
<tr>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" size="1" idx="*" className="req" options="${emptyList}" firstEntry="-" onChange="void changeAirport(this)" />
 <el:text name="airportACode" size="4" max="4" idx="*" value="" onBlur="void setAirport(document.forms[0].airportA, this.value)" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="SAVE FLIGHT PRE-APPROVAL" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
