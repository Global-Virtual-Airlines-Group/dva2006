<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_calendar.tld" prefix="calendar" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Online Event Calendar</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/event/header.jsp" %> 
<%@ include file="/jsp/event/sideMenu.jsp" %>

<!-- Main Body Frame -->
<content:region id="main">
<calendar:week startDate="${startDate}" showDaysOfWeek="true">
<calendar:entry name="event">
<el:cmd url="event" linkID="0x${event.ID}">${event.name}</el:cmd><br />
<span class="sec small bld">${event.networkName}</span> <span class="small"><fmt:date fmt="t" t="HH:mm" date="${event.startTime}" /> 
- <fmt:date fmt="t" t="HH:mm" date="${event.endTime}" /></span><br />
<span class="small"><c:forEach var="route" items="${event.routes}">
${route.airportD.name} - (<fmt:airport airport="${route.airportD}" />)${route.airportA.name} (<fmt:airport airport="${route.airportA}" />)<br />
</c:forEach></span>
</calendar:entry>
</calendar:week>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
