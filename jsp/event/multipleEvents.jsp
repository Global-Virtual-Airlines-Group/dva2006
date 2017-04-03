<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Online Events</title>
<content:css name="main" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/event/header.jspf" %> 
<%@ include file="/jsp/event/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="view">
<tr class="title caps">
 <td class="left" colspan="6">MULTIPLE <content:airline /> ONLINE EVENTS</td>
</tr>
<tr>
 <td class="pri bld left" colspan="6"><fmt:int value="${fn:sizeof(futureEvents)}" /> <content:airline /> 
Online Events have currently been scheduled, and are listed below. Please click on one of these Online Event profiles to learn more about this <content:airline /> Event.</td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:10%">DATE</td>
 <td style="width:25%">EVENT NAME</td>
 <td style="width:8%">NETWORK</td>
 <td style="width:8%">STATUS</td>
 <td>AVAILABLE ROUTES</td>
</tr>

<!-- Event View data -->
<c:forEach var="event" items="${futureEvents}">
<view:row entry="${event}">
 <td class="pri bld"><fmt:date fmt="d" date="${event.startTime}" /></td>
 <td><el:cmd url="event" link="${event}">${event.name}</el:cmd></td>
 <td class="pri bld">${event.network}</td>
 <td class="sec">${event.status.name}</td>
 <td class="small"><c:forEach var="route" items="${event.routes}">
${route.airportD.name} (<fmt:airport airport="${route.airportD}" />) - ${route.airportA.name} (<fmt:airport airport="${route.airportA}" />)<br />
</c:forEach></td>
</view:row>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title caps">
 <td colspan="5">&nbsp;</td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
