<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Flight Statistics</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:css name="form" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function updateSort()
{
document.forms[0].submit();
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="flightstats.do" method="post" validate="return true">
<view:table className="view" pad="default" space="default" cmd="flightstats">
<tr class="title">
 <td colspan="4" class="left caps"><content:airline /> FLIGHT STATISTICS</td>
 <td colspan="5" class="right">GROUP BY <el:combo name="groupType" size="1" idx="*" options="${groupTypes}" value="${param.groupType}" onChange="void updateSort()" />
 SORT BY <el:combo name="sortType" size="1" idx="*" options="${sortTypes}" value="${viewContext.sortType}" onChange="void updateSort()" /></td>
</tr>

<!-- Table Header Bar-->
<tr class="title caps">
 <td width="5%">#</td>
 <td width="20%">ENTRY</td>
 <td width="8%">HOURS</td>
 <td width="7%">LEGS</td>
 <td width="10%">ACARS</td>
 <td width="10%">ONLINE</td>
 <td width="10%">HISTORIC</td>
 <td width="10%">MILES</td>
 <td>AVERAGE</td>
</tr>

<!-- Table Statistics Data -->
<c:set var="entryNumber" value="${viewStart}" scope="request" />
<c:forEach var="stat" items="${viewContext.results}">
<view:row entry="${stat}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="request" />
 <td class="sec bld small">${entryNumber}</td>
 <td class="pri bld">${stat.label}</td>
 <td class="bld"><fmt:dec value="${stat.hours}" /></td>
 <td class="pri bld"><fmt:int value="${stat.legs}" /></td>
 <td class="sec bld small"><fmt:int value="${stat.ACARSLegs}" /> (<fmt:dec value="${stat.ACARSPercent * 100}" fmt="##0.0" />%)</td>
 <td class="bld small"><fmt:int value="${stat.onlineLegs}" /> (<fmt:dec value="${(stat.onlineLegs * 100.0) / stat.legs}" fmt="##0.0" />%)</td>
 <td class="sec small"><fmt:int value="${stat.historicLegs}" /> (<fmt:dec value="${(stat.historicLegs * 100.0) / stat.legs}" fmt="##0.0" />%)</td>
 <td class="small"><fmt:int value="${stat.miles}" /></td>
 <td class="bld small"><fmt:dec value="${stat.avgHours}" fmt="#,##0.00" /> Hours, 
<fmt:int value="${stat.avgMiles}" /> Miles</td>
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
