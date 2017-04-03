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
<title><content:airline /> Applicant Roster</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script type="text/javascript">
golgotha.local.sort = function(combo) {
	if (!golgotha.form.comboSet(combo)) return false;
	self.location = '/applicants.do?' + combo.name + '=' + escape(golgotha.form.getCombo(combo));
	return true;
};
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
<view:table cmd="applicants">
<tr class="title caps">
 <td class="left" colspan="3"><span class="nophone"><content:airline /> </span>PILOT APPLICATIONS</td>
 <td class="right" colspan="3">AIRLINE SIZE - <fmt:int value="${airlineSize}" /> PILOTS<span class="nophone">, MAX <fmt:int value="${maxSize}" /></span></td>
</tr>

<!-- Sort Bar -->
<tr class="title">
 <td colspan="2">STATUS <el:combo name="status" idx="*" size="1" firstEntry="" options="${statuses}" value="${param.status}" onChange="void golgotha.local.sort(this)" /></td>
 <td colspan="3">EQUIPMENT PROGRAM <el:combo name="eqType" idx="*" size="1" firstEntry="" options="${eqTypes}" value="${param.eqType}" onChange="void golgotha.local.sort(this)" /></td>
 <td class="nophone">LETTER <el:combo name="letter" idx="*" size="1" firstEntry="" options="${letters}" value="${param.letter}" onChange="void golgotha.local.sort(this)" /></td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td style="width:20%">APPLICANT NAME</td>
 <td style="width:10%">REGISTERED ON</td>
 <td class="nohphone">HIRED AS</td>
 <td style="width:17%">LOCATION</td>
 <td>SCORE</td>
 <td>E-MAIL ADDRESS</td>
</tr>

<!-- Table Applicant Data -->
<c:forEach var="applicant" items="${viewContext.results}">
<c:set var="q" value="${qMap[applicant.ID]}" scope="page" />
<c:if test="${empty q}">
<c:set var="q" value="${pqMap[applicant.pilotID]}" scope="page" />
</c:if>
<view:row entry="${applicant}">
 <td class="pri bld"><el:cmd url="applicant" link="${applicant}">${applicant.name}</el:cmd></td>
 <td><fmt:date fmt="d" date="${applicant.createdOn}" /></td>
<c:if test="${applicant.pilotID > 0}">
 <td class="sec small">${applicant.rank.name}, ${applicant.equipmentType}</td>
</c:if>
<c:if test="${applicant.pilotID == 0}">
 <td>N/A</td>
</c:if>
 <td class="small">${applicant.location}</td>
<c:if test="${!empty q}">
 <td class="sec small bld"><fmt:int value="${q.score}" /> / <fmt:int value="${q.size}" /></td>
</c:if>
<c:if test="${empty q}">
 <td>N/A</td>
</c:if>
 <td><a class="small" href="mailto:${applicant.email}">${applicant.email}</a></td>
</view:row>
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
