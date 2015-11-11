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
<title><content:airline /> Server Statistics</title>
<content:css name="main" />
<content:css name="view" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script type="text/javascript">
golgotha.local.sortBy = function(combo) {
	self.location = '/httpstats.do?sortType=' + escape(golgotha.form.getCombo(combo));
	return true;
};
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
<view:table cmd="httpstats">
<tr class="title">
 <td colspan="3" class="left"><span class="nophone"><content:airline /> </span>HTTP SERVER STATISTICS</td>
 <td colspan="3" class="right">SORT BY 
<el:combo name="sortType" size="1" idx="1" options="${sortTypes}" value="${viewContext.sortType}" onChange="void golgotha.local.sortBy(this)" /></td>
</tr>

<!-- Table Header Bar-->
<tr class="title caps">
 <td style="width:10%">#</td>
 <td>DATE</td>
 <td style="width:15%">REQUESTS</td>
 <td class="nophone" style="width:15%">HOME PAGE HITS</td>
 <td class="nophone" style="width:15%">SERVER TIME</td>
 <td>TOTAL BANDWIDTH</td>
</tr>

<!-- Table Statistics Data -->
<c:set var="entryNumber" value="${viewStart}" scope="page" />
<c:forEach var="stat" items="${viewContext.results}">
<tr>
<c:set var="entryNumber" value="${entryNumber + 1}" scope="page" />
 <td class="sec bld">${entryNumber}</td>
 <td class="pri bld"><fmt:date fmt="d" date="${stat.date}" /></td>
 <td class="bld"><fmt:int value="${stat.requests}" /></td>
 <td class="pri bld nophone"><fmt:int value="${stat.homePageHits}" /></td>
 <td class="bld nophone"><fmt:int value="${stat.executionTime / 1000}" /> s</td>
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
