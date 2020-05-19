<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Client System Statistics</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script async>
golgotha.local.updateSort = function() { return document.forms[0].submit(); };
golgotha.local.validate = function(f) {
    if (!golgotha.form.check()) return false;
    golgotha.form.submit(f);
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
<el:form action="fleetstats.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> ACARS CLIENT SYSTEM STATISTICS</td>
</tr>
<tr>
 <td class="label">Sort Options</td>
 <td class="data"><el:combo name="orderBy" idx="*" size="1" options="${sortOptions}" value="${labelCode}" onChange="void golgotha.local.updateSort()" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="sortLabel" idx="*" className="sec small" value="true" label="Sort Labels instead of Totals" checked="${param.sortLabel}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SEARCH ACARS CLIENT SYSTEM STATISTICS" /></td>
</tr>
</el:table>
</el:form>

<c:if test="${!empty stats}">
<el:table className="view">
<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:60%">${labelCode}</td>
 <td>TOTAL USERS</td>
 <td>PERCENT</td>
</tr>

<!-- Table Statistics Data -->
<c:forEach var="stat" items="${stats}">
<c:set var="pct" value="${stat.count * 100.0 / total}" scope="page" />
<view:row entry="${stat}">
<c:if test="${isWindowsVersion}">
 <td class="pri bld"><fmt:windows version="${stat.label}" /></td>
</c:if>
<c:if test="${!isWindowsVersion}">
 <td class="pri bld">${stat.label}</td>
</c:if>
 <td class="bld"><fmt:int value="${stat.count}" /></td>
 <td class="sec bld"><fmt:dec value="${pct}" fmt="##0.00" />%</td>
</view:row>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title">
 <td colspan="3"><view:scrollbar><view:pgUp /> <view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</el:table>
</c:if>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
