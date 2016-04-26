<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Route Search</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:json />
<content:js name="airportRefresh" />
<fmt:aptype var="useICAO" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.airportD, t:'Departure Airport'});
golgotha.form.validate({f:f.airportA, t:'Arrival Airport'});
golgotha.form.submit(f);
return true;
};
<c:if test="${!empty results}">
golgotha.local.validateBuild = function(f)
{
if (!golgotha.form.check()) return false;
var legNum = f.legCount.value;
for (var x = 1; x <= legNum; x++) {
	var radio = eval('f.leg' + x);
	var isOK = false;
	if (typeof radio.length != 'undefined') {
		for (var y = 0; !isOK && (y < radio.length); y++)
			isOK = radio[y].checked;
	} else
		isOK = radio.checked;
	
	if (!isOK) {
		alert('At least one flight for Leg #' + x + ' must be selected.');
		return false;
	}
}

golgotha.form.submit(f);
return true;	
};
</c:if>
golgotha.onDOMReady(function() {
	var f = document.forms[0];
	golgotha.airportLoad.config.doICAO = ${useICAO};
	golgotha.airportLoad.setHelpers(f.airportD);
	golgotha.airportLoad.setHelpers(f.airportA);
	golgotha.airportLoad.changeAirline([f.airportD, f.airportA], golgotha.airportLoad.config);	
});
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:singleton var="aD" value="${rp.airportD}" />
<content:singleton var="aA" value="${rp.airportA}" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="routeassign.do" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><span class="nophone"><content:airline /> </span>FLIGHT ASSIGNMENT ROUTE SEARCH</td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airportD" size="1" idx="*" options="${aD}" firstEntry="-" value="${rp.airportD}" onChange="void this.updateAirportCode()" />
 <el:airportCode combo="airportD" idx="*" airport="${rp.airportD}" /></td>
</tr>
<tr>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" size="1" idx="*" options="${aA}" firstEntry="-" value="${rp.airportA}" onChange="void this.updateAirportCode()" />
 <el:airportCode combo="airportA" idx="*" airport="${rp.airportA}" /></td>
</tr>

<!-- Button Bar -->
<tr class="title">
 <td colspan="2" class="mid"><el:button ID="SearchButton" type="submit" label="SEARCH FLIGHT SCHEDULE" /></td>
</tr>
</el:table>
</el:form>

<!-- Search Results -->
<c:if test="${doSearch}">
<c:if test="${!empty results}">
<br />
<el:form method="post" action="routebuild.do" validate="return golgotha.form.wrap(golgotha.local.validateBuild, this)">
<el:table className="view">
<tr class="title caps">
 <td colspan="8" class="left"><span class="nophone">SEARCH RESULTS FROM </span>${rp.airportD.name} (<fmt:airport airport="${rp.airportD}" />) to ${rp.airportA.name}
 (<fmt:airport airport="${rp.airportA}" />)</td>
</tr>
<!-- Schedule Entry Header Bar -->
<tr class="caps title">
 <td style="width:5%">&nbsp;</td>
 <td style="width:20%">FLIGHT NUMBER</td>
 <td style="width:15%">EQUIPMENT</td>
 <td class="nophone" style="width:15%">DEPARTS</td>
 <td class="nophone" style="width:15%">ARRIVES</td>
 <td class="nophone">DISPATCH ROUTES</td>
 <td class="nophone" style="width:10%">LENGTH</td>
 <td style="width:10%">DISTANCE</td>
</tr>

<c:set var="lastAirport" value="${rp.airportD}" scope="page" />
<c:set var="legNum" value="0" scope="page" />
<c:forEach var="aA" items="${fn:keys(results)}">
<c:set var="flights" value="${results[aA]}" scope="page" />
<c:set var="flightCount" value="${fn:sizeof(flights)}" scope="page" />
<c:set var="legNum" value="${legNum + 1}" scope="page" />
<tr class="title caps">
 <td colspan="8" class="left">LEG <fmt:int value="${legNum}" /> - <span class="nophone"><fmt:quantity value="${flightCount}" single="flight" /> FROM
 </span>${lastAirport.name} (<fmt:airport airport="${lastAirport}" />) TO ${aA.name} (<fmt:airport airport="${aA}" />)</td>
</tr>
<c:forEach var="flight" items="${flights}">
<view:row entry="${flight}">
 <td><el:radio name="leg${legNum}" value="${flight.flightCode}" label="" /></td>
 <td class="pri bld">${flight.flightCode}</td>
 <td class="sec bld">${flight.equipmentType}</td>
 <td class="nophone"><fmt:date fmt="t" t="HH:mm" tz="${flight.airportD.TZ}" date="${flight.timeD}" showZone="true" /></td>
 <td class="nophone"><fmt:date fmt="t" t="HH:mm" tz="${flight.airportA.TZ}" date="${flight.timeA}" showZone="true" /></td>
 <td class="pri bld nophone"><fmt:int value="${flight.dispatchRoutes}" /></td>
 <td class="nophone"><fmt:int value="${flight.length / 10}" />:<fmt:int value="${(flight.length * 6) % 60}" fmt="00" /></td>
 <td class="sec"><fmt:distance value="${flight.distance}" /></td>
</view:row>
</c:forEach>
<c:set var="lastAirport" value="${aA}" scope="page" />
</c:forEach>
<el:text name="legCount" type="hidden" value="${legNum}" />

<!-- Button Bar -->
<tr class="title">
 <td colspan="8"><c:if test="${!empty myEQ}">SET EQUIPMENT <el:combo name="eqOverride" size="1" firstEntry="-" options="${myEQ}" />&nbsp;</c:if>
<el:button ID="BuildButton" type="submit" label="BUILD FLIGHT ASSIGNMENT" /></td>
</tr>
</el:table>
</el:form>
<br />
</c:if>
<c:if test="${empty results}">
<el:table className="view">
<tr class="title caps">
 <td class="left">SEARCH RESULTS FROM ${rp.airportD.name} (<fmt:airport airport="${rp.airportD}" />) to ${rp.airportA.name}
 (<fmt:airport airport="${rp.airportA}" />)</td>
</tr>
<tr>
 <td class="pri bld">No route could be plotted between ${rp.airportD.name} and ${rp.airportA.name}.</td>
</tr>
</el:table>
</c:if>
</c:if>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics eventSupport="true" />
</body>
</html>
