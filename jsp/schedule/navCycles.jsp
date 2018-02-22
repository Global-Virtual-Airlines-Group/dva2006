<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Navigation Cycle Release Dates</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="navcycles">
<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:15%">CYCLE</td>
 <td style="width:30%">RELEASE DATE</td>
 <td>&nbsp;</td>
</tr>

<content:filter roles="Schedule,Operations">
<c:choose>
<c:when test="${currentCycle.ID != nowCycle.ID}">
<!-- Data sync warning -->
<tr>
 <td colspan="3" class="error bld mid">The current Navigation Data cycle (<span class="pri bld">${currentCycle}</span>) appears to be out of 
 date. It has been superceded by Cycle <span class="pri bld">${nowCycle}</span>, released on <fmt:date fmt="d" date="${nowCycle.releasedOn}" d="EEEE MMMM dd, YYYY" />.</td> 
</tr>
<tr class="title"><td colspan="3">&nbsp;</td></tr>
</c:when>
<c:when test="${currentChartCycle.ID != currentCycle.ID}">
<!-- Chart sync warning -->
<tr>
 <td colspan="3" class="error bld mid">The current FAA Approach Charts do not appear to be in sync with the Navigation database.
 The navigation database is using Cycle <span class="pri bld">${currentCycle}</span>, and the FAA Approach Chart database is
 using cycle <span class="pri bld">${currentChartCycle}</span>.</td>
</tr>
<tr class="title"><td colspan="3">&nbsp;</td></tr>
</c:when>
</c:choose>
</content:filter>

<!-- Table Data Section -->
<c:forEach var="cycle" items="${cycles}">
<c:set var="isNow" value="${cycle.ID == nowCycle.ID}" scope="page" />
<c:set var="isCurrent" value="${cycle.ID == currentCycle.ID}" scope="page" />
<c:set var="isChart" value="${cycle.ID == currentChartCycle.ID}" scope="page" />

<view:row entry="${cycle}">
 <td class="pri bld">${cycle}</td>
 <td class="sec bld"><fmt:date fmt="d" date="${cycle.releasedOn}" d="EEEE MMMM dd, YYYY" /></td>
 <td class="left">
<c:if test="${isCurrent}">This cycle is currently loaded into the <content:airline /> navigation database</c:if>
<c:if test="${isChart}"><c:if test="${isCurrent}"><br /></c:if>
This cycle is currently loaded into the <content:airline /> FAA Approach Chart database</c:if>
<c:if test="${isNow}">


</c:if></td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="3">&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
