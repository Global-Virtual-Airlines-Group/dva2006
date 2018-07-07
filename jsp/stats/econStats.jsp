<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Load Statistics</title>
<content:css name="main" />
<content:css name="view" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script>
golgotha.local.updateSort = function() { return document.forms[0].submit(); };
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:enum var="sortTypes" className="org.deltava.beans.stats.FlightStatsSort" exclude="PIDS,OLEGS,OVLEGS,OILEGS,DSPLEGS" />
<content:enum var="groupTypes" className="org.deltava.beans.stats.FlightStatsGroup" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="econstats.do" method="post" validate="return true">
<view:table cmd="econstats">
<tr class="title">
 <td colspan="4" class="left caps"><span class="nophone"><content:airline />&nbsp;</span> PASSENGER STATISTICS</td>
 <td colspan="5" class="right">GROUP BY <el:combo name="groupType" size="1" idx="*" options="${groupTypes}" value="${groupType}" onChange="void golgotha.local.updateSort()" />
 SORT BY <el:combo name="sortType" size="1" idx="*" options="${sortTypes}" value="${viewContext.sortType}" onChange="void golgotha.local.updateSort()" /></td>
</tr>
<!-- Table Header Bar-->
<tr class="title caps">
 <td style="max-width:5%">#</td>
 <td style="max-width:25%">ENTRY</td>
 <td>PASSENGERS</td>
 <td>LOAD FACTOR</td>
 <td>HOURS</td>
 <td>LEGS</td>
 <td class="nophone">ACARS</td>
 <td class="nophone">DISTANCE</td>
 <td>AVERAGE</td>
</tr>

<!-- Table Statistics Data -->
<c:set var="entryNumber" value="${viewContext.start}" scope="page" />
<c:forEach var="stat" items="${viewContext.results}">
<view:row entry="${stat}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="page" />
 <td class="sec bld small">${entryNumber}</td>
 <td class="pri bld">${stat.label}</td>
  <td class="pri bld"><fmt:int value="${stat.pax}" /></td>
 <td class="bld"><fmt:dec value="${stat.loadFactor * 100.0}" fmt="##0.00" />%</td>
 <td class="bld" title="<fmt:distance value="${stat.distance}" longUnits="true" />"><fmt:dec value="${stat.hours}" /></td>
 <td class="sec"><fmt:int value="${stat.legs}" /></td>
 <td class="sec bld small nophone"><fmt:int value="${stat.ACARSLegs}" /> (<fmt:dec value="${stat.ACARSPercent * 100}" fmt="##0.0" />%)</td>
 <td class="small nophone"><fmt:distance value="${stat.distance}" /></td>
 <td class="small"><fmt:dec value="${stat.avgHours}" fmt="#,##0.00" /> hours, <fmt:distance value="${stat.avgDistance}" /></td>
</view:row>
</c:forEach>

<!-- Table Footer Bar -->
<tr class="title">
 <td colspan="9"><view:scrollbar><view:pgUp /> <view:pgDn /></view:scrollbar>&nbsp;</td>
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
