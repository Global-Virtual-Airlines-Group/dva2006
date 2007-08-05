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
<title><content:airline /> Pilot Roster</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="lroster.do" method="GET" validate="return false;">
<view:table className="view" pad="default" space="default" cmd="lroster">
<!-- Table Letter Bar -->
<tr class="title">
 <td class="left">PILOT LOGBOOKS</td>
 <td colspan="6" class="right">
<c:forEach var="letter" items="${letters}">
 <el:cmd url="lroster" op="${letter}">${letter}</el:cmd>&nbsp;
</c:forEach>
</td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td width="30%">PILOT NAME</td>
 <td width="10%">PILOT ID</td>
 <td width="10%">JOINED ON</td>
 <td width="15%">LAST FLIGHT</td>
 <td width="10%">FLIGHTS</td>
 <td width="10%">HOURS</td>
 <td>TRANSFERRED HOURS</td>
</tr>

<!-- Table Pilot Data -->
<c:choose>
<c:when test="${empty viewContext}">
<tr>
 <td colspan="7" class="pri bld">Please select a letter to display all the Delta 
 Virtual Airlines pilots whose last name begins with this letter.</td>
</tr>
</c:when>
<c:otherwise>
<c:forEach var="pilot" items="${viewContext.results}">
<view:row entry="${pilot}">
<c:choose>
<c:when test="${pilot.legs > 0}">
 <td><el:cmd className="bld" url="logbook" op="log" link="${pilot}">${pilot.name}</el:cmd></td>
</c:when>
<c:otherwise>
 <td class="pri bld">${pilot.name}</td>
</c:otherwise>
</c:choose>
 <td class="pri bld">${pilot.pilotCode}</td>
 <td><fmt:date fmt="d" date="${pilot.createdOn}" /></td>
<c:if test="${pilot.legs > 0}">
 <td><fmt:date fmt="d" date="${pilot.lastFlight}" /></td>
 <td class="sec bld"><fmt:int value="${pilot.legs}" /></td>
 <td class="pri bld"><fmt:dec value="${pilot.hours}" /></td>
</c:if>
<c:if test="${pilot.legs == 0}">
 <td colspan="3" class="bld">NO FLIGHTS LOGGED</td>
</c:if>
 <td><fmt:dec value="${pilot.legacyHours}" /></td>
</view:row>
</c:forEach>
</c:otherwise>
</c:choose>

<!-- Table Scroll/Bottom/Legend Bar -->
<tr class="title">
 <td colspan="7"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /><br /></view:scrollbar>
<view:legend width="100" labels="Active,Inactive,Retired,On Leave" classes=" ,opt2,opt3,warn" /></td>
</tr>
</view:table>
</el:form>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
