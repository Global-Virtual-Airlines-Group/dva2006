<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Online Events</title>
<content:sysdata var="airlineName" name="airline.name" />
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:rss title="${airlineName} Online Events" path="/event_rss.ws" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/event/header.jspf" %> 
<%@ include file="/jsp/event/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="events">
<!-- Table Header Bar -->
<tr class="title caps">
 <td>DATE</td>
 <td style="max-width:30%">EVENT NAME</td>
 <td>NETWORK</td>
 <td class="nophone">STATUS</td>
 <td class="nophone">AVAILABLE ROUTES</td>
</tr>

<!-- Table Event Data -->
<c:forEach var="event" items="${viewContext.results}">
<view:row entry="${event}">
 <td class="pri bld"><fmt:date fmt="d" date="${event.startTime}" /></td>
 <td><el:cmd url="event" link="${event}">${event.name}</el:cmd></td>
 <td class="pri bld">${event.network}</td>
 <td class="sec nophone">${event.status.name}</td>
 <td class="small nophone"><c:forEach var="route" items="${event.routes}">
${route.airportD.name} (<fmt:airport airport="${route.airportD}" />) - ${route.airportA.name} (<fmt:airport airport="${route.airportA}" />)<br />
</c:forEach></td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="5"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
