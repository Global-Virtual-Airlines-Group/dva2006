<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title>FAA Approach Chart Download</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script>
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
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>
<c:set var="noDL" value="${param.noDownload}" scope="page" />
<c:if test="${empty param.year}">
<content:sysdata var="noDL" name="schedule.chart.noDownload" /></c:if>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="faachartdl.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">FAA APPROACH CHART DOWNLOAD</td>
</tr>
<c:if test="${!empty currentCycle}">
<!-- Chart cycle data -->
<tr>
 <td class="label">&nbsp;</td>
 <td class="data">Current Approach / Procedure chart download cycle: <span class="pri bld">${currentCycle}</span>, released on <fmt:date fmt="d" date="${currentCycle.releasedOn}" d="EEEE MMMM dd, YYYY" /></td>
</tr>
</c:if>
<tr> 
 <td class="label">Chart Cycle</td>
 <td class="data"><el:combo name="month" required="true" options="${months}" value="${m}" /> <el:combo name="year" required="true" options="${years}" value="${y}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="noDownload" value="true" label="Do not download Chart images" checked="${noDL}" /></td> 
</tr>
<c:if test="${doImport}">
<tr>
 <td class="label">Status</td>
 <td class="data"><fmt:int value="${chartsAdded}" /> charts added, <fmt:int value="${chartsUpdated}" /> charts updated, <fmt:int value="${chartsDeleted}" /> charts deleted</td> 
</tr>
<c:if test="${!empty msgs}">
<tr>
 <td class="label top">Import Status</td>
 <td class="data small"><c:forEach var="msg" items="${msgs}">${msg}<br /></c:forEach></td>
</tr>
</c:if>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="DOWNLOAD FAA APPROACH CHARTS" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
