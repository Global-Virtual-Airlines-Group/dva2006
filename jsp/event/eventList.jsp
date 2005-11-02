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
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/event/header.jsp" %> 
<%@include file="/jsp/event/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<view:table className="view" space="default" pad="default" cmd="events">
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="9%">DATE</td>
 <td width="25%">EVENT NAME</td>
 <td width="8%">NETWORK</td>
 <td width="8%">STATUS</td>
 <td>AVAILABLE ROUTES</td>
</tr>

<!-- Table Event Data -->
<c:forEach var="event" items="${viewContext.results}">
<view:row entry="${event}">
 <td class="pri bld"><fmt:date fmt="d" date="${event.startTime}" /></td>
 <td><el:cmd url="event" linkID="0x${event.ID}">${event.name}</el:cmd></td>
 <td class="pri bld">${event.networkName}</td>
 <td class="sec">${event.statusName}</td>
 <td class="bld"><c:forEach var="route" items="${event.routes}">
${route.airportD.name} (<fmt:airport airport="${route.airportD}" />)${route.airportA.name} (<fmt:airport airport="${route.airportA}" />)<br />
</c:forEach></td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="5"><view:pgUp />&nbsp;<view:pgDn /></td>
</tr>
</view:table>
<content:copyright />
</div>
</body>
</html>
