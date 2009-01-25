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
<title><content:airline /> Flight Assignment</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<content:sysdata var="innovataLink" name="schedule.innovata.enabled" />
<script language="JavaScript" type="text/javascript">
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
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="4"><content:airline /> FLIGHT ASSIGNMENT SEARCH FROM ${criteria.airportD} (<fmt:airport airport="${criteria.airportD}" />)</td>
</tr>
<tr>
 <td class="label">Equipment</td>
 <td class="data"><el:combo name="eqType" size="1" idx="*" firstEntry="-" options="${pilot.ratings}" value="${criteria.equipmentType}" /></td>
 <td class="label">Airline</td>
 <td class="data"><el:combo name="airline" size="1" idx="*" firstEntry="-" options="${airlines}" value="${criteria.airline}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data" colspan="3"><el:box name="includeHistoric" idx="*" value="true" checked="${criteria.includeHistoric}" label="Include Historic Flights" /></td>
</tr>
</el:table>

<!-- Search Results -->
<el:table className="view" pad="default" space="default">
<tr class="title caps">
 <td colspan="6" class="left">PROPOSED FLIGHT FROM ${criteria.airportD.name} (<fmt:airport airport="${criteria.airportD}" />)</td> 
</tr>
<c:if test="${!empty entries}">
<!-- Search Results Header Bar -->
<tr class="caps title">
 <td width="15%">FLIGHT NUMBER</td>
 <td width="10%">EQUIPMENT</td>
 <td width="35%">AIRPORTS</td>
 <td width="10%">DEPARTS</td>
 <td width="10%">ARRIVES</td>
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
 <td colspan="6" class="pri bld caps">NO FLIGHTS MATCHING YOUR CRITERIA WERE FOUND FROM ${lastAirport.name}
 (<fmt:airport airport="${lastAirport}" />)</td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr class="title">
 <td><el:button ID="SearchButton" type="submit" className="BUTTON" label="SEARCH FOR FLIGHTS" />&nbsp;
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
