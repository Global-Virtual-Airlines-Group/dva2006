<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> ACARS Flight Log</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<content:js name="datePicker" />
<content:js name="acarsLog" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="acarslogf.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="4">ACARS FLIGHT INFORMATION LOG<c:if test="${!empty startDate}"> BETWEEN <fmt:date date="${startDate}" /> 
 - <fmt:date date="${endDate}" /></c:if></td>
</tr>
<tr>
 <td class="label">Pilot Code</td>
 <td class="data"><el:text name="pilotCode" idx="*" size="7" max="8" value="${param.pilotCode}" /></td>
 <td class="label">Maximum Results</td>
 <td class="data"><el:text name="viewCount" idx="*" size="2" max="2" value="${param.viewCount}" /></td>
</tr>
<tr>
 <td class="label">Start Date/Time</td>
 <td class="data"><el:text name="startDate" idx="*" size="10" max="10" value="${param.startDate}" />&nbsp;
<el:text name="startTime" idx="*" size="8" max="8" value="${param.startTime}" />&nbsp;
<el:button className="BUTTON" label="CALENDAR" onClick="void show_calendar('forms[0].startDate')" /></td>
 <td class="label">End Date/Time</td>
 <td class="data"><el:text name="endDate" idx="*" size="10" max="10" value="${param.endDate}" />&nbsp;
<el:text name="endTime" idx="*" size="8" max="8" value="${param.endTime}" />&nbsp;
<el:button className="BUTTON" label="CALENDAR" onClick="void show_calendar('forms[0].endDate')" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SearchButton" type="submit" className="BUTTON" label="SEARCH FLIGHT INFORMATION LOG" /></td>
</tr>
</el:table>
</el:form>

<c:choose>
<c:when test="${!empty viewContext.results}">
<!-- Table Log Results -->
<view:table className="view" space="default" pad="default" cmd="acarslogf">
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="8%">ID</td>
 <td width="15%">START/END TIME</td>
 <td width="10%">PILOT CODE</td>
 <td width="20%">PILOT NAME</td>
 <td width="10%">FLIGHT NUMBER</td>
 <td width="12%">ORIGIN</td>
 <td width="12%">DESTINATION</td>
 <td>FS VERSION</td>
</tr>

<!-- Log Entries -->
<c:forEach var="flight" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[flight.pilotID]}" scope="request" />
<c:set var="pilotLoc" value="${userData[flight.pilotID]}" scope="request" />
<view:row entry="${entry}">
 <td class="pri bld"><el:cmd url="acarsinfo" link="${flight}"><fmt:int value="${flight.ID}" /></el:cmd></td>
 <td><fmt:date t="HH:mm" date="${flight.startTime}" />
<c:if test="${!empty flight.endTime}">
<br /><fmt:date t="HH:mm" date="${flight.endTime}" />
</c:if>
</td>
 <td class="sec bld">${pilot.pilotCode}</td>
 <td class="pri bld"><el:profile location="${pilotLoc}">${pilot.name}</el:profile></td>
 <td class="bld">${flight.flightCode}</td>
 <td class="small">${flight.airportD.name} (<fmt:airport airport="${flight.airportD}" />)</td>
 <td class="small">${flight.airportA.name} (<fmt:airport airport="${flight.airportA}" />)</td>
 <td class="sec">${flight.FSVersion}</td>
</view:row>
<view:row entry="${entry}">
 <td colspan="8" class="left">Route: ${flight.route}</td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="8"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
</c:when>
<c:when test="${doSearch}">
<el:table className="view" space="default" pad="default">
<tr>
 <td class="pri bld">No Flights matching your search criteria were found in the ACARS log database.</td>
</tr>
</el:table>
</c:when>
</c:choose>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
