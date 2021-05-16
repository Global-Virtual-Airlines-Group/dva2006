<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Charter Flight Request</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<fmt:aptype var="useICAO" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:js name="airportRefresh" />
<script>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.airportD, t:'Departure Airport'});
	golgotha.form.validate({f:f.airportA, t:'Arrival Airport'});
	golgotha.form.validate({f:f.airline, t:'Airline'});
	golgotha.form.validate({f:f.eq, t:'Equipment Type'});
	golgotha.form.submit(f);
	return true;
};

golgotha.onDOMReady(function() {
	const f = document.forms[0];
	const cfg = golgotha.airportLoad.config;
	cfg.doICAO = ${useICAO}; cfg.useSchedule = false;
	golgotha.airportLoad.setHelpers([f.airportD,f.airportA]);
	golgotha.airportLoad.setText([f.airline,f.airportD,f.airportA]);
	f.airline.updateAirlineCode = golgotha.airportLoad.updateAirlineCode;
	return golgotha.airportLoad.changeAirline([f.airportD, f.airportA], cfg);
});
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:singleton var="apD" value="${chreq.airportD}" />
<content:singleton var="apA" value="${chreq.airportA}" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="chreq.do" link="${chreq}" op="save" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><span class="nophone"><content:airline />&nbsp;</span>CHARTER FLIGHT REQUEST</td>
</tr>
<tr>
 <td class='label'>Departing from</td>
 <td class="data"><el:combo name="airportD" size="1" options="${apD}" required="true" value="${chreq.airportD}" onChange="void this.updateAirportCode()" /> <el:airportCode combo="airportD" idx="*" airport="${chreq.airportD}" /></td>
</tr>
<tr>
 <td class='label'>Arriving at</td>
 <td class="data"><el:combo name="airportA" size="1" options="${apA}" required="true" value="${chreq.airportA}" onChange="void this.updateAirportCode()" /> <el:airportCode combo="airportA" idx="*" airport="${chreq.airportA}" /></td>
</tr>
<tr>
 <td class="label">Airline</td>
 <td class="data"><el:combo name="airline" idx="*" size="1" required="true" firstEntry="[ AIRLINE ]" options="${airlines}" value="${chreq.airline}" onChange="void this.updateAirlineCode()" />
 <el:text name="airlineCode" size="2" max="3" idx="*" autoComplete="false" className="caps" onChange="void golgotha.airportLoad.setAirline(document.forms[0].airline, this)" /></td>
</tr>
<tr>
 <td class="label">Equipment Type</td>
 <td class="data"><el:combo name="eq" idx="*" size="1" required="true" firstEntry="[ EQUIPMENT ]" options="${author.ratings}" value="${chreq.equipmentType}" /></td>
</tr>
<c:if test="${!empty chreq}">
<tr>
 <td class='label'>Created on</td>
 <td class="data bld"><fmt:date date="${chreq.createdOn}" /></td>
</tr>
</c:if>
<tr>
 <td class='label top'>Comments</td>
 <td class="data"><el:textbox name="comments" idx="*"  height="3" width="90%" resize="true">${chreq.comments}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SAVE CHARTER FLIGHT REQUEST" /></td>
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
