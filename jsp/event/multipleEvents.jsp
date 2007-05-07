<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Online Events</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/event/header.jspf" %> 
<%@ include file="/jsp/event/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="view" pad="default" space="default">
<tr class="title caps">
 <td class="left" colspan="6">MULTIPLE <content:airline /> ONLINE EVENTS</td>
</tr>
<tr>
 <td class="pri bld left" colspan="6"><fmt:int value="${fn:sizeof(futureEvents)}" /> <content:airline /> 
Online Events have currently been scheduled, and are listed below. Please click on one of these
Online Event profiles to learn more about this <content:airline /> Event.</td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td width="10%">DATE</td>
 <td width="25%">EVENT NAME</td>
 <td width="8%">NETWORK</td>
 <td width="8%">STATUS</td>
 <td>AVAILABLE ROUTES</td>
</tr>

<!-- Event View data -->
<c:forEach var="event" items="${futureEvents}">
<view:row entry="${event}">
 <td class="pri bld"><fmt:date fmt="d" date="${event.startTime}" /></td>
 <td><el:cmd url="event" link="${event}">${event.name}</el:cmd></td>
 <td class="pri bld">${event.networkName}</td>
 <td class="sec">${event.statusName}</td>
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
</body>
</html>
