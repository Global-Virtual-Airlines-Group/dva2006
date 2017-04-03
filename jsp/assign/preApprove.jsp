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
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:json />
<content:js name="airportRefresh" />
<content:googleAnalytics eventSupport="true" />
<fmt:aptype var="useICAO" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.eqType, t:'Equipment Type'});
golgotha.form.validate({f:f.flight, min:1, t:'Flight Number'});
golgotha.form.validate({f:f.leg, min:1, t:'Flight Leg'});
golgotha.form.validate({f:f.airline, t:'Airline'});
golgotha.form.validate({f:f.airportD, t:'Departure Airport'});
golgotha.form.validate({f:f.airportA, t:'Arrival Airport'});
golgotha.form.submit(f);
return true;
};

golgotha.local.changeAirline = function(combo) {
	var f = document.forms[0];
	golgotha.airportLoad.config.airline = golgotha.form.getCombo(combo);
	return golgotha.airportLoad.changeAirline([f.airportD, f.airportA], golgotha.airportLoad.config);
};

golgotha.local.changeEQ = function(combo) {
	var f = document.forms[0];
	golgotha.airportLoad.config.eqType = golgotha.form.getCombo(combo);	
	return golgotha.airportLoad.changeAirline([f.airportD, f.airportA], golgotha.airportLoad.config);
};

golgotha.onDOMReady(function() {
	var f = document.forms[0];
	var cfg = golgotha.airportLoad.config;
	cfg.useSched = false; cfg.doICAO = ${useICAO};
	golgotha.airportLoad.setHelpers(f.airportD);
	golgotha.airportLoad.setHelpers(f.airportA);	
});
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
<el:form action="preapprove.do" method="post" linkID="${pilotID}" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">UNSCHEDULED FLIGHT APPROVAL - ${assignPilot.name} (${assignPilot.pilotCode})</td>
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
 <td class="data"><el:combo name="eqType" size="1" idx="*" className="req" options="${eqTypes}" onChange="void golgotha.local.changeEQ(this)" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label">Airline</td>
 <td class="data"><el:combo name="airline" size="1" idx="*" className="req" options="${airlines}" onChange="void golgotha.local.changeAirline(this)" firstEntry="-" /></td>
</tr>
<c:set var="flightNumber" value="${assignPilot.pilotNumber % 10000}" scope="request" />
<tr>
 <td class="label">Flight / Leg</td>
 <td class="data"><el:text name="flight" idx="*" size="4" max="4" className="pri bld req" value="${flightNumber}" />
 <el:text name="leg" idx="*" size="1" max="1" className="req" value="1" /></td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airportD" size="1" idx="*" className="req" options="${emptyList}" firstEntry="-" onChange="void this.updateAirportCode()" /> <el:airportCode combo="airportD" /></td>
</tr>
<tr>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" size="1" idx="*" className="req" options="${emptyList}" firstEntry="-" onChange="void this.updateAirportCode()" /> <el:airportCode combo="airportA" /></td>
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
