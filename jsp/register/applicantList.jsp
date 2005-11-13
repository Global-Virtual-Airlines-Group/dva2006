<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Applicant Roster</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<script language="JavaScript" type="text/javascript">
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
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="applicants.do" method="get" validate="return false">
<view:table className="view" pad="default" space="default" cmd="applicants">
<!-- Sort Bar -->
<tr class="title">
 <td class="left">PILOT APPLICATIONS</td>
 <td>LETTER <el:combo name="letter" idx="*" size="1" firstEntry="" options="${letters}" value="${param.letter}" onChange="void sort(this)" /></td>
 <td>STATUS <el:combo name="status" idx="*" size="1" firstEntry="" options="${statuses}" value="${param.status}" onChange="void sort(this)" /></td>
 <td colspan="2">EQUIPMENT PROGRAM <el:combo name="eqType" idx="*" size="1" firstEntry="" options="${eqTypes}" value="${param.eqType}" onChange="void sort(this)" /></td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td width="25%">APPLICANT NAME</td>
 <td width="10%">REGISTERED ON</td>
 <td width="20%">HIRED AS</td>
 <td width="20%">LOCATION</td>
 <td>E-MAIL ADDRESS</td>
</tr>

<!-- Table Applicant Data -->
<c:forEach var="applicant" items="${viewContext.results}">
<view:row entry="${applicant}">
 <td class="pri bld"><el:cmd url="applicant" linkID="0x${applicant.ID}">${applicant.name}</el:cmd></td>
 <td><fmt:date fmt="d" date="${applicant.createdOn}" /></td>
<c:if test="${applicant.pilotID > 0}">
 <td class="sec small">${applicant.rank}, ${applicant.equipmentType}</td>
</c:if>
<c:if test="${applicant.pilotID == 0}">
 <td>N/A</td>
</c:if>
 <td>${applicant.location}</td>
 <td><a class="small" href="mailto:${applicant.email}">${applicant.email}</a></td>
</view:row>
</c:forEach>

<!-- Scroll bar -->
<tr class="title">
 <td colspan="5"><view:pgUp />&nbsp;<view:pgDn /><br />
<view:legend width="100" labels="Pending,Approved,Rejected" classes="opt1, ,err" /></td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
