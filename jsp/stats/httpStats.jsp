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
<title><content:airline /> Server Statistics</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:css name="form" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function sortBy(combo)
{
var sortType = combo.options[combo.selectedIndex].value;
self.location = '/httpstats.do?sortType=' + sortType;
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
<el:form action="httpstats.do" method="get" validate="return false">
<view:table className="view" pad="default" space="default" cmd="httpstats">
<tr class="title">
 <td colspan="5" class="left">HTTP SERVER STATISTICS</td>
 <td colspan="2" class="right">SORT BY 
<el:combo name="sortType" size="1" idx="1" options="${sortTypes}" value="${viewContext.sortType}" onChange="void sortBy(this)" /></td>
</tr>

<!-- Table Header Bar-->
<tr class="title caps">
 <td width="10%">#</td>
 <td width="15%">DATE</td>
 <td width="15%">REQUESTS</td>
 <td width="15%">HOME PAGE HITS</td>
 <td width="15%">SERVER TIME</td>
 <td>TOTAL BANDWIDTH</td>
</tr>

<!-- Table Statistics Data -->
<c:set var="entryNumber" value="${viewStart}" scope="request" />
<c:forEach var="stat" items="${viewContext.results}">
<tr>
<c:set var="entryNumber" value="${entryNumber + 1}" scope="request" />
 <td class="sec bld">${entryNumber}</td>
 <td class="pri bld"><fmt:date fmt="d" date="${stat.date}" /></td>
 <td class="bld"><fmt:int value="${stat.requests}" /></td>
 <td class="pri bld"><fmt:int value="${stat.homePageHits}" /></td>
 <td class="bld"><fmt:int value="${stat.executionTime / 1000}" /> s</td>
 <td class="bld"><fmt:int value="${stat.bandwidth}" /> bytes</td>
</tr>
</c:forEach>

<!-- Table Footer Bar -->
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
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
