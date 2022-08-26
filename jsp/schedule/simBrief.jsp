<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> SimBrief Route Plotter</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<content:js name="airportRefresh" />
<content:js name="simbrief.apiv1" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:googleAnalytics eventSupport="true" />
<script async>
golgotha.local.validate = function(f) {
    golgotha.form.validate({f:f.eqType, t:'EquipmentType'});
    golgotha.form.validate({f:f.airline, t:'Airline'});
    golgotha.form.validate({f:f.airportD, t:'Departure Airport'});
    golgotha.form.validate({f:f.airportA, t:'Arrival Airport'});
    return true;
};
</script>
</head>
<content:copyright visible="false" />
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="simBriefKey" name="security.key.simBrief" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="foo.ws" method="post" validate="return golgotha.form.wrap(golgotha.local.validate,this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> SIMBRIEF FLIGHT PLOTTER</td>
</tr>
<tr>
 <td class="label">Aircraft</td>
 <td class="data"><el:combo name="eqType" className="req" size="1" idx="*" options="${eqTypes}" firstEntry="[ AIRCRAFT ]" value="${flight.equipmentType}" /></td>
</tr>
<tr>
 <td class="label">Airline</td>
 <td class="data"><el:combo name="airline" size="1" idx="*" options="${airlines}" firstEntry="[ AIRLINE ]" value="${flight.airline}" onChange="void this.updateAirlineCode()" />
 <el:text name="airlineCode" size="2" max="3" idx="*" autoComplete="false" className="caps" onChange="void golgotha.airportLoad.setAirline(document.forms[0].airline, this, true)" /></td>
</tr>
<td class="label">Departing from</td>
<tr>
 <td class="data"><el:combo name="airportD" className="req" size="1" idx="*" options="${airportsD}" firstEntry="-" value="${flight.airportD}" onChange="void golgotha.routePlot.updateRoute(true, true)" />
 <el:airportCode combo="airportD" airport="${flight.airportD}" idx="*" /><c:if test="${!empty flight.airportD}"> <el:cmd url="airportInfo" linkID="${flight.airportD.ICAO}" className="small" target="_new">Airport Information</el:cmd></c:if></td>
</tr>
<tr>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" className="req" size="1" idx="*" options="${airportsA}" firstEntry="-" value="${flight.airportA}" onChange="void golgotha.routePlot.updateRoute(true)" />
 <el:airportCode combo="airportA" airport="${flight.airportA}" idx="*" />
<c:if test="${!empty flight.airportA}">&nbsp;<el:cmd url="airportInfo" linkID="${flight.airportA.ICAO}" className="small" target="_new">Airport Information</el:cmd></c:if></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button onClick="void golgotha.local.sbSubmit()" label="SEND TO SIMBRIEF" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<fmt:aptype var="useICAO" />
<script async>
const f = document.forms[0];
golgotha.airportLoad.config.doICAO = ${useICAO};
golgotha.airportLoad.config.airline = 'all';
golgotha.airportLoad.setHelpers([f.airportD,f.airportA]);
f.airline.updateAirlineCode = golgotha.airportLoad.updateAirlineCode;
golgotha.airportLoad.setText(f.airline);

golgotha.local.sbSubmit = function() {
	const f = document.forms[0];
	const sbf = document.getElementById('sbapiform');
	
	
	
};
</script>
<el:form ID="sbapiform" method="post" action="" validate="return true">
<el:text name="orig" type="hidden" value="" />
<el:text name="dest" type="hidden" value="" />
<el:text name="type" type="hidden" value="" />
</el:form>
</body>
</html>
