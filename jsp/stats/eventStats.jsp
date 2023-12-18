<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Online Event Statistics</title>
<content:css name="main" />
<content:css name="view" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script async>
golgotha.local.updateGroup = function() { return document.forms[0].submit(); };
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="eventstats.do" method="post" validate="return true">
<view:table cmd="eventstats">
<tr class="title">
 <td colspan="2" class="left caps"><span class="nophone"><content:airline />&nbsp;</span>ONLINE EVENT STATISTICS</td>
 <td colspan="3" class="right caps">GROUP <el:combo name="statsType" idx="*" size="1" required="true" options="${groupTypes}" value="${param.statsType}" onChange="void golgotha.local.updateGroup()" /></td>
</tr>
<!-- Table Header Bar-->
<tr class="title caps">
 <td style="max-width:10%">#</td>
 <td>ENTRY</td>
<c:if test="${isMonthly}"> <td class="nophone">EVENTS</td></c:if>
 <td>SIGNUPS</td>
 <td>FLIGHTS</td>
</tr>

<!-- Table Statistics Data -->
<c:set var="entryNumber" value="${viewContext.start}" scope="page" />
<c:forEach var="stat" items="${viewContext.results}">
<view:row entry="${stat}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="page" />
 <td class="sec bld small">${entryNumber}</td>
 <td class="pri bld">${stat.label}</td>
<c:if test="${isMonthly}"> <td class="bld nophone"><fmt:int value="${stat.count}" /></td></c:if>
 <td class="sec bld"><fmt:int value="${stat.signups}" /><c:if test="${isMonthly}"> total, <fmt:int value="${stat.pilotSignups}" /> pilots</c:if></td>
 <td class="pri bld"><fmt:int value="${stat.flights}" /><c:if test="${isMonthly}"> total, <fmt:int value="${stat.pilotFlights}" /> pilots</c:if></td>
</view:row>
</c:forEach>

<!-- Table Footer Bar -->
<tr class="title">
 <td colspan="${isMonthly ? 5 : 4}"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
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
