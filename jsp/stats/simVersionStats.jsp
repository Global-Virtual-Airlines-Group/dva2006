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
<content:js name="common" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script type="text/javascript">
golgotha.local.updateSort = function() { return document.forms[0].submit(); };
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:enum var="sortTypes" className="org.deltava.beans.stats.FlightStatsSort" exclude="PIDS,ALEGS,OLEGS,OVLEGS,OILEGS,HLEGS,DSPLEGS,PAX" />
<content:enum var="groupTypes" className="org.deltava.beans.stats.FlightStatsGroup" exclude="AP,AA,AD" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="simversionstats.do" method="post" validate="return true">
<view:table cmd="simversionstats">
<tr class="title">
 <td colspan="5" class="left caps"><content:airline /> FLIGHT SIMULATOR VERSION STATISTICS</td>
 <td colspan="7" class="right">GROUP BY <el:combo name="groupType" size="1" idx="*" options="${groupTypes}" value="${groupType}" onChange="void golgotha.local.updateSort()" />
 SORT BY <el:combo name="sortType" size="1" idx="*" options="${sortTypes}" value="${viewContext.sortType}" onChange="void golgotha.local.updateSort()" /></td>
</tr>
<!-- Table Header Bar-->
<tr class="title caps">
 <td style="width:6%;">#</td>
 <td style="width:21%;">ENTRY</td>
 <td style="width:10%;">HOURS</td>
 <td style="width:10%;">LEGS</td>
 <td class="nophone">DISTANCE</td>
 <td>FSX</td>
 <td>P3D</td>
 <td>FS2004</td>
 <td class="nophone">FS2002</td>
 <td class="nophone">FS2000</td>
 <td class="nophone">X-Plane</td>
 <td class="nophone">OTHER</td>
</tr>

<!-- Table Statistics Data -->
<c:set var="entryNumber" value="${viewStart}" scope="page" />
<c:forEach var="stat" items="${viewContext.results}">
<view:row entry="${stat}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="page" />
<c:set var="eLegs" value="${stat.versionLegs}" scope ="page" />
 <td class="sec bld small">${entryNumber}</td>
 <td class="pri bld">${stat.label}</td>
 <td class="bld"><fmt:dec value="${stat.hours}" /></td>
 <td class="pri bld"><fmt:int value="${stat.legs}" /></td>
 <td class="sec bld nophone"><fmt:distance value="${stat.distance}" /></td>
 <td class="small"><fmt:int value="${eLegs['FSX']}" /> (<fmt:dec value="${eLegs['FSX'] * 100.0 / stat.legs}" />%)</td>
 <td class="small"><fmt:int value="${eLegs['P3D']}" /> (<fmt:dec value="${eLegs['P3D'] * 100.0 / stat.legs}" />%)</td>
 <td class="small"><fmt:int value="${eLegs['FS9']}" /> (<fmt:dec value="${eLegs['FS9'] * 100.0 / stat.legs}" />%)</td>
 <td class="small nophone"><fmt:int value="${eLegs['FS2002']}" /> (<fmt:dec value="${eLegs['FS2002'] * 100.0 / stat.legs}" />%)</td>
 <td class="small nophone"><fmt:int value="${eLegs['FS2000']}" /> (<fmt:dec value="${eLegs['FS2000'] * 100.0 / stat.legs}" />%)</td>
 <td class="small nophone"><fmt:int value="${eLegs['XP9']}" /> (<fmt:dec value="${eLegs['XP9'] * 100.0 / stat.legs}" />%)</td>
 <td class="small nophone"><fmt:int value="${eLegs['UNKNOWN']}" /> (<fmt:dec value="${eLegs['UNKNOWN'] * 100.0 / stat.legs}" />%)</td>
</view:row>
</c:forEach>

<!-- Table Footer Bar -->
<tr class="title">
 <td colspan="12"><view:scrollbar><view:pgUp /> <view:pgDn /></view:scrollbar>&nbsp;</td>
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
