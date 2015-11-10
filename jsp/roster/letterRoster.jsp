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
<title><content:airline /> Pilot Roster</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
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
<view:table cmd="lroster">
<!-- Table Letter Bar -->
<tr class="title">
 <td colspan="2" class="left">PILOT LOGBOOKS</td>
 <td colspan="5" class="right">
<c:forEach var="letter" items="${letters}">
 <el:cmd url="lroster" op="${letter}">${letter}</el:cmd>&nbsp;
</c:forEach>
</td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td style="width:40%;">PILOT NAME</td>
 <td style="max-width:10%">PILOT ID</td>
 <td class="nophone">JOINED ON</td>
 <td class="nophone">LAST FLIGHT</td>
 <td style="max-width:10%">FLIGHTS</td>
 <td style="max-width:10%">HOURS</td>
 <td class="nophone">TRANSFERRED HOURS</td>
</tr>

<!-- Table Pilot Data -->
<c:choose>
<c:when test="${empty viewContext.results}">
<tr>
 <td colspan="7" class="pri bld">Please select a letter to display all the <content:airline /> pilots whose last name begins with this letter.</td>
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
 <td class="nophone"><fmt:date fmt="d" date="${pilot.createdOn}" /></td>
<c:if test="${pilot.legs > 0}">
 <td class="nophone"><fmt:date fmt="d" date="${pilot.lastFlight}" /></td>
 <td class="sec bld"><fmt:int value="${pilot.legs}" /></td>
 <td class="pri bld"><fmt:dec value="${pilot.hours}" /></td>
</c:if>
<c:if test="${pilot.legs == 0}">
 <td colspan="3" class="bld">NO FLIGHTS LOGGED</td>
</c:if>
 <td class="nophone"><fmt:dec value="${pilot.legacyHours}" /></td>
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
