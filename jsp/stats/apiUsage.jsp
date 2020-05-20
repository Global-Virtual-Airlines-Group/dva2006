<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> API Usage Statistics</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<content:json />
<content:googleJS module="charts" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/main/header.jspf" %> 
<%@include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="apistats.do" method="post" validate="return true">
<view:table cmd="apistats">
<tr class="title caps">
 <td colspan="2" class="left">API USAGE STATISTICS SINCE <fmt:date date="${startDate}" d="MMMM dd yyyy" fmt="d" /></td>
 <td colspan="3" class="right"><span class="nophone">PREVIOUS </span><el:text name="days" size="2" max="4" value="${daysBack}" /> DAYS</td>
</tr>

<!--  Chart Header Bar -->
<tr class="title caps">
 <td colspan="2" class="left">API USAGE CHART</td>
 <td colspan="3"><span class="und" onclick="golgotha.util.toggleExpand(this, 'chartRow'); golgotha.local.showChart()">EXPAND</span></td>
</tr>
<tr class="chartRow" style="display:none;">
 <td colspan="5"><div id="flightStats" style="height:525px;"></div></td>
</tr>

<!-- Table Header Bar-->
<tr class="title caps">
 <td style="width:20%;">DATE</td>
 <td>API / METHOD</td>
 <td>TOTAL USAGE</td>
 <td>ANONYMOUS</td>
 <td>BLOCKED</td>
</tr>

<!-- Table Statistics Data -->
<c:forEach var="apiName" items="${usage.keySet()}">
<c:set var="stats" value="${usage[apiName]}" scope="page" />
<c:set var="lastMethod" value="" scope="page" />
<c:forEach var="stat" items="${stats}">
<c:if test="${stat.name != lastMethod}">
<c:set var="lastMethod" value="${stat.name}" scope="page" />
<c:set var="p" value="${predict[lastMethod]}" scope="page" />
<c:if test="${!empty p}">
<tr class="title caps">
 <td class="left" colspan="5">${p.name} - PREDICTED USAGE <fmt:int value="${p.total}" /> (<fmt:int value="${p.anonymous}" /> ANONYMOUS) OVER <fmt:int value="${daysRemaining}" /> DAYS</td>
</tr>
</c:if>
</c:if>
<tr>
 <td class="pri bld"><fmt:date date="${stat.date}" fmt="d" /></td>
 <td class="sec bld">${stat.name}</td>
 <td class="bld"><fmt:int value="${stat.total}" /></td>
 <td><fmt:int value="${stat.anonymous}" /></td>
<c:if test="${stat.blocked > 0}">
 <td class="err bld"><fmt:int value="${stat.blocked}" /></td>
</c:if>
<c:if test="${stat.blocked == 0}">
 <td class="bld">0</td>
</c:if>
</tr>
</c:forEach>
</c:forEach>

<!-- Table Footer Bar -->
<tr class="title">
 <td colspan="5">&nbsp;</td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
