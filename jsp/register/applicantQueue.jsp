<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Pending Applicant Queue</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<script type="text/javascript">
function sort(combo)
{
if (combo.selectedIndex != -1) {
	var sortKey = combo.options[combo.selectedIndex].value;
	self.location = '/applicants.do?' + combo.name + '=' + sortKey;
}

return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="maxSize" name="users.max" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="applicants.do" method="get" validate="return false">
<view:table className="view" cmd="applicants">
<tr class="title caps">
 <td class="left" colspan="3"><content:airline /> PILOT APPLICATIONS</td>
 <td class="right" colspan="3">AIRLINE SIZE - <fmt:int value="${airlineSize}" /> PILOTS, MAX <fmt:int value="${maxSize}" /></td>
</tr>

<!-- Sort Bar -->
<tr class="title">
 <td colspan="2">STATUS <el:combo name="status" idx="*" size="1" firstEntry="" options="${statuses}" value="${param.status}" onChange="void sort(this)" /></td>
 <td colspan="2">EQUIPMENT PROGRAM <el:combo name="eqType" idx="*" size="1" firstEntry="" options="${eqTypes}" value="${param.eqType}" onChange="void sort(this)" /></td>
 <td colspan="2">LETTER <el:combo name="letter" idx="*" size="1" firstEntry="" options="${letters}" value="${param.letter}" onChange="void sort(this)" /></td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td style="width:25%">APPLICANT NAME</td>
 <td style="width:10%">REGISTERED ON</td>
 <td style="width:20%">LOCATION</td>
 <td style="width:10%">SCORE</td>
 <td>E-MAIL ADDRESS</td>
 <td style="width:10%">&nbsp;</td>
</tr>

<!-- Table Applicant Data -->
<c:forEach var="applicant" items="${viewContext.results}">
<c:set var="addrOK" value="${addrValid[applicant.ID]}" scope="page" />
<c:set var="q" value="${qMap[applicant.ID]}" scope="page" />
<tr>
 <td class="pri bld"><el:cmd url="applicant" link="${applicant}">${applicant.name}</el:cmd></td>
 <td><fmt:date fmt="d" date="${applicant.createdOn}" /></td>
 <td class="small">${applicant.location}</td>
<c:choose>
<c:when test="${empty q}">
 <td>N/A</td>
</c:when>
<c:when test="${fn:submitted(q)}">
 <td class="pri small bld">SUBMITTED</td>
</c:when>
<c:when test="${fn:pending(q)}">
 <td class="small">PENDING</td>
</c:when>
<c:otherwise>
 <td class="sec small bld"><fmt:int value="${q.score}" /> / <fmt:int value="${q.size}" /></td>
</c:otherwise>
</c:choose>
 <td><a class="small" href="mailto:${applicant.email}">${applicant.email}</a></td>
<c:if test="${addrOK}">
 <td class="ter bld small caps">VERIFIED</td>
</c:if>
<c:if test="${!addrOK}">
 <td class="small caps">UNVERIFIED</td>
</c:if>
</tr>
</c:forEach>

<!-- Scroll bar -->
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /><br /></view:scrollbar>
<view:legend width="100" labels="Pending,Approved,Rejected" classes="opt1, ,err" /></td>
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
