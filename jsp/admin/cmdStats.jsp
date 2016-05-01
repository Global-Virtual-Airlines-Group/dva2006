<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Command Statistics</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<script type="text/javascript">
golgotha.local.setSort = function(combo) {
	self.location = '/cmdstats.do?sortBy=' + golgotha.form.getCombo(combo);
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
<el:table className="view">
<!-- Table Header Bar -->
<tr class="title">
 <td colspan="4" class="caps">COMMAND INVOCATION STATISTICS</td>
 <td colspan="4" class="right"><el:combo name="sortBy" idx="*" size="1" options="${sortOptions}" value="${sortType}" onChange="void golgotha.local.setSort(this)" /></td>
</tr>

<!-- Table Legend Bar -->
<tr class="title caps">
 <td style="width:15%">COMMAND NAME</td>
 <td style="width:10%">INVOKED</td>
 <td style="width:10%">SUCCESSFUL</td>
 <td style="width:10%">PERCENT</td>
 <td style="width:10%">AVG. TIME</td> 
 <td style="width:14%">AVG. BACK END</td>
 <td style="width:12%">MAX TIME</td>
 <td>MAX BACK END</td>
</tr>

<!-- Table Statistics Data -->
<c:forEach var="stat" items="${stats}">
<view:row entry="${stat}">
 <td class="pri bld">${stat.name}</td>
 <td class="sec"><fmt:int value="${stat.count}" /></td>
 <td><fmt:int value="${stat.successCount}" /></td>
 <td><fmt:dec value="${stat.successCount / stat.count * 100}" />%</td>
 <td class="pri bld"><fmt:int value="${stat.avgTime}" /> ms</td>
 <td><fmt:int value="${stat.avgBackEndTime}" /> ms</td>
 <td class="bld"><fmt:int value="${stat.maxTime}" /> ms</td>
 <td><fmt:int value="${stat.maxBackEndTime}" /> ms</td>
</view:row>
</c:forEach>

<!-- Bottom Row -->
<tr class="title caps">
 <td colspan="8">&nbsp;</td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
