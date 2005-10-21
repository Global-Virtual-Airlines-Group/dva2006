<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
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
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<el:form action="flightstats.do" method="post" validate="return true">
<view:table className="view" pad="default" space="default" cmd="flightstats">
<tr class="title">
 <td colspan="2" class="left">FLIGHT STATISTICS</td>
 <td colspan="5" class="right">GROUP BY <el:combo name="groupType" size="1" idx="*" options="${groupTypes}" value="${param.groupType}" onChange="void updateSort()" /></td>
 SORT BY <el:combo name="sortType" size="1" idx="*" options="${sortTypes}" value="${viewContext.sortType}" onChange="void updateSort()" /></td>
</tr>

<!-- Table Header Bar-->
<tr class="title caps">
 <td width="5%">#</td>
 <td width="25%">ENTRY</td>
 <td width="15%">FLIGHT HOURS</td>
 <td width="10%">FLIGHT LEGS</td>
 <td width="15%">MILES FLOWN</td>
 <td width="15%">AVERAGE HOURS</td>
 <td>AVERAGE MILES</td>
</tr>

<!-- Table Statistics Data -->
<c:set var="entryNumber" value="${viewStart}" scope="request" />
<c:forEach var="stat" items="${viewContext.results}">
<view:row entry="${stat}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="request" />
 <td class="sec bld">${entryNumber}</td>
 <td class="pri bld">${stat.label}</td>
 <td class="bld"><fmt:dec value="${stat.hours}" /></td>
 <td class="pri bld"><fmt:int value="${stat.legs}" /></td>
 <td class="bld"><fmt:int value="${stat.miles}" /></td>
 <td class="sec bld"><fmt:dec value="${stat.avgHours}" fmt="#,##0.00" /></td>
 <td class="bld"><fmt:int value="${stat.avgMiles}" /></td>
</view:row>
</c:forEach>

<!-- Table Footer Bar -->
<tr class="title">
 <td colspan="7"><view:pgUp /> <view:pgDn /></td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</div>
</body>
</html>
