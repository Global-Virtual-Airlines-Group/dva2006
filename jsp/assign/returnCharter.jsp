<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Return Charter Flight</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:js name="airportRefresh" />
<fmt:aptype var="useICAO" />
<script>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.eqType, t:'Equipment Type'});
	golgotha.form.validate({f:f.flight, min:1, t:'Flight Number'});
	golgotha.form.validate({f:f.airline, t:'Airline'});
	golgotha.form.validate({f:f.airportD, t:'Departure Airport'});
	golgotha.form.validate({f:f.airportA, t:'Arrival Airport'});
	golgotha.form.submit(f);
	return true;
};

golgotha.onDOMReady(function() {
	const f = document.forms[0];
	golgotha.airportLoad.config.doICAO = ${useICAO};
	golgotha.airportLoad.setHelpers([f.airportD,f.airportA]);
});
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>
<content:singleton var="aD" value="${lastFlight.airportA}" />
<content:singleton var="aA" value="${lastFlight.airportD}" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="rcharter.do" method="post" link="${assignPilot}" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">RETURN CHARTER FLIGHT APPROVAL - ${assignPilot.name} (${assignPilot.pilotCode})</td>
</tr>
<tr>
 <td class="label">Aircraft Type</td>
 <td class="data"><el:combo name="eqType" size="1" idx="*" className="req" options="${eqTypes}" value="${lastFlight.equipmentType}" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label">Airline</td>
 <td class="data"><el:combo name="airline" size="1" idx="*" className="req" options="${airlines}" value="${lastFlight.airline}" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label">Flight</td>
 <td class="data"><el:text name="flight" idx="*" size="4" max="4" className="pri bld req" readOnly="true" value="${lastFlight.flightNumber}" /></td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airportD" size="1" idx="*" className="req" options="${aD}" value="${lastFlight.airportA}" firstEntry="-" onChange="void this.updateAirportCode()" />
 <el:airportCode combo="airportD" airport="${lastFlight.airportD}" /></td>
</tr>
<tr>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" size="1" idx="*" className="req" options="${aA}" value="${lastFlight.airportD}" firstEntry="-" onChange="void this.updateAirportCode()" />
 <el:airportCode combo="airportA" airport="${lastFlight.airportA}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SAVE RETURN CHARTER FLIGHT" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
