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
<title><content:airline /> Single-Leg Flight Assignment</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;

setSubmit();
disableButton('SearchButton');
disableButton('BuildButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body onload="void initLinks()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="singleassign.do" validate="return validate(this)">
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
</el:table>

<!-- Search Results -->
<el:table className="view">
<tr class="title caps">
 <td colspan="6" class="left">PROPOSED FLIGHT FROM ${criteria.airportD.name} (<fmt:airport airport="${criteria.airportD}" />)</td> 
</tr>
<c:if test="${!empty entries}">
<!-- Search Results Header Bar -->
<tr class="caps title">
 <td style="width:15%">FLIGHT NUMBER</td>
 <td style="width:10%">EQUIPMENT</td>
 <td style="width:35%">AIRPORTS</td>
 <td style="width:10%">DEPARTS</td>
 <td style="width:10%">ARRIVES</td>
 <td>DISTANCE</td>
</tr>

<c:forEach var="flight" items="${entries}">
<view:row entry="${flight}">
 <td class="pri bld">${flight.flightCode}</td>
 <td class="sec bld">${flight.equipmentType}</td>
 <td class="small">${flight.airportD.name} (<fmt:airport airport="${flight.airportD}" />) to
 ${flight.airportA.name} (<fmt:airport airport="${flight.airportA}" />)</td>
 <td><fmt:date fmt="t" t="HH:mm" tz="${flight.airportD.TZ}" date="${flight.dateTimeD.UTC}" /></td>
 <td><fmt:date fmt="t" t="HH:mm" tz="${flight.airportA.TZ}" date="${flight.dateTimeA.UTC}" /></td>
 <td class="sec"><fmt:distance value="${flight.distance}" /></td>
</view:row>
</c:forEach>
</c:if>
<c:if test="${empty entries}">
<tr>
 <td colspan="6" class="pri bld caps">NO FLIGHTS MATCHING YOUR CRITERIA WERE FOUND FROM ${criteria.airportD.name}
 (<fmt:airport airport="${criteria.airportD}" />)</td>
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
<c:if test="${innovataLink}">
<%@ include file="/jsp/schedule/innovataLink.jspf" %> 
</c:if>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
