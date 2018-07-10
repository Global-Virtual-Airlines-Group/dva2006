<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Single-Leg Flight Assignment</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:pics />
<content:favicon />
<content:js name="common" />
<script>
golgotha.local.validate = function(f) {
    if (!golgotha.form.check()) return false;
    golgotha.form.submit(f);
    return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="singleassign.do" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="4"><content:airline /> FLIGHT ASSIGNMENT SEARCH FROM ${criteria.airportD.name} (<fmt:airport airport="${criteria.airportD}" />)</td>
</tr>
<tr>
 <td class="label">Equipment</td>
 <td class="data"><el:combo name="eqType" size="1" idx="*" firstEntry="-" options="${pilot.ratings}" value="${criteria.equipmentType}" /></td>
 <td class="label">Flight Legs</td>
 <td class="data"><el:text name="legs" idx="*" size="1" max="1" value="${totalLegs}" /></td>
</tr>
<tr>
 <td class="label">Airline</td>
 <td class="data"><el:combo name="airline" size="1" idx="*" firstEntry="-" options="${airlines}" value="${criteria.airline}" /></td>
 <td class="label">Maximum Distnce</td>
 <td class="data"><el:text name="maxLength" idx="*" size="4" max="4" value="${(criteria.distance < 1) ? '' : criteria.distance}" />
 +/- <el:text name="maxLengthRange" idx="*" size="3" max="4" value="${(criteria.distance < 1) ? '' : criteria.distanceRange}" /> miles</td>
</tr>
<tr>
 <td class="label top">Search Options</td>
 <td class="data" colspan="3"><el:box name="avoidHistorical" idx="*" value="true" checked="${param.avoidHistorical}" label="Exclude Historic Legs" /><br />
<el:box name="avoidVisitedDestination" idx="*" value="true"  checked="${critieria.notVisitedA}" label="Exclude Visited Destination Airports" /></td>
</tr>
</el:table>

<!-- Search Results -->
<el:table className="view">
<tr class="title caps">
 <td colspan="6" class="left">PROPOSED <fmt:quantity value="${entries.size()}" single="flight" /><span class="nophone"> FROM ${criteria.airportD.name} (<fmt:airport airport="${criteria.airportD}" />)</span></td> 
</tr>
<c:if test="${!empty entries}">
<!-- Search Results Header Bar -->
<tr class="caps title">
 <td style="width:15%">FLIGHT NUMBER</td>
 <td>EQUIPMENT</td>
 <td style="width:35%">AIRPORTS</td>
 <td class="nophone" style="width:10%">DEPARTS</td>
 <td class="nophone" style="width:10%">ARRIVES</td>
 <td class="nophone">DISTANCE</td>
</tr>

<c:forEach var="flight" items="${entries}">
<view:row entry="${flight}">
 <td class="pri bld">${flight.flightCode}</td>
 <td class="sec bld">${flight.equipmentType}</td>
 <td class="small">${flight.airportD.name} (<fmt:airport airport="${flight.airportD}" />) to ${flight.airportA.name} (<fmt:airport airport="${flight.airportA}" />)</td>
 <td class="nophone"><fmt:date fmt="t" t="HH:mm" tz="${flight.airportD.TZ}" date="${flight.timeD}" /></td>
 <td class="nophone"><fmt:date fmt="t" t="HH:mm" tz="${flight.airportA.TZ}" date="${flight.timeA}" /></td>
 <td class="sec nophone"><fmt:distance value="${flight.distance}" /></td>
</view:row>
</c:forEach>
</c:if>
<c:if test="${empty entries}">
<tr>
 <td colspan="6" class="pri bld caps">NO FLIGHTS MATCHING YOUR CRITERIA WERE FOUND FROM ${criteria.airportD.name} (<fmt:airport airport="${criteria.airportD}" />)</td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr class="title">
 <td><el:button ID="SearchButton" type="submit" label="SEARCH FOR FLIGHTS" />&nbsp;
<el:cmdbutton ID="BuildButton" url="singlebuild" label="BUILD FLIGHT ASSIGNMENT" /></td>
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
