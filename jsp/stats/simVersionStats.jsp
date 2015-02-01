<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Flight Simulator Version Statistics</title>
<content:css name="main" />
<content:css name="view" />
<content:css name="form" />
<content:pics />
<script type="text/javascript">
golgotha.local.updateSort = function() { return document.forms[0].submit(); };
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="simversionstats.do" method="post" validate="return true">
<view:table cmd="simversionstats">
<tr class="title">
 <td colspan="5" class="left caps"><content:airline /> FLIGHT SIMULATOR VERSION STATISTICS</td>
 <td colspan="5" class="right">GROUP BY <el:combo name="groupType" size="1" idx="*" options="${groupTypes}" value="${param.groupType}" onChange="void golgotha.local.updateSort()" />
 SORT BY <el:combo name="sortType" size="1" idx="*" options="${sortTypes}" value="${viewContext.sortType}" onChange="void golgotha.local.updateSort()" /></td>
</tr>
<!-- Table Header Bar-->
<tr class="title caps">
 <td style="width:5%">#</td>
 <td style="width:23%">ENTRY</td>
 <td style="width:9%">HOURS</td>
 <td style="width:8%">LEGS</td>
 <td style="width:9%">FSX</td>
 <td style="width:9%">FS2004</td>
 <td style="width:8%">FS2002</td>
 <td style="width:8%">FS2000</td>
 <td style="width:8%">P3D</td>
 <td>OTHER</td>
</tr>

<!-- Table Statistics Data -->
<c:set var="entryNumber" value="${viewStart}" scope="page" />
<c:forEach var="stat" items="${viewContext.results}">
<view:row entry="${stat}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="page" />
 <td class="sec bld small">${entryNumber}</td>
 <td class="pri bld">${stat.label}</td>
 <td class="bld"><fmt:dec value="${stat.hours}" /></td>
 <td class="pri bld"><fmt:int value="${stat.legs}" /></td>
 <td class="small"><fmt:int value="${stat.versionLegs[10]}" /> (<fmt:dec value="${stat.versionLegs[10] * 100.0 / stat.legs}" />%)</td>
 <td class="small"><fmt:int value="${stat.versionLegs[9]}" /> (<fmt:dec value="${stat.versionLegs[9] * 100.0 / stat.legs}" />%)</td>
 <td class="small"><fmt:int value="${stat.versionLegs[8]}" /> (<fmt:dec value="${stat.versionLegs[8] * 100.0 / stat.legs}" />%)</td>
 <td class="small"><fmt:int value="${stat.versionLegs[7]}" /> (<fmt:dec value="${stat.versionLegs[7] * 100.0 / stat.legs}" />%)</td>
 <td class="small"><fmt:int value="${stat.versionLegs[11]}" /> (<fmt:dec value="${stat.versionLegs[11] * 100.0 / stat.legs}" />%)</td>
 <td class="small"><fmt:int value="${stat.versionLegs[0]}" /> (<fmt:dec value="${stat.versionLegs[0] * 100.0 / stat.legs}" />%)</td>
</view:row>
</c:forEach>

<!-- Table Footer Bar -->
<tr class="title">
 <td colspan="10"><view:scrollbar><view:pgUp /> <view:pgDn /></view:scrollbar>&nbsp;</td>
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
