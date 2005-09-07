<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Issue Tracker</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function setFilter(combo, paramName)
{
newStatus = combo.options[combo.selectedIndex].value;
if (combo.selectedIndex != 0) {
	location.href = 'issues.do?' + paramName + '=' + newStatus;
} else {
	location.href = 'issues.do';
}

return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div ID="main">
<el:form action="issues.do" method="POST" validate="return false">
<view:table className="view" pad="default" space="default" cmd="issues">
<!-- Table Sort Combo Bar -->
<tr class="title">
 <td colspan="2" class="left">ISSUE LIST</td>
 <td colspan="2">BY STATUS <el:combo name="op" size="1" options="${statuses}" firstEntry="< ALL >" value="${status}" onChange="void setFilter(this, 'status')" /></td>
 <td colspan="2">SORT BY <el:combo name="sortType" size="1" options="${sortTypes}" value="${viewContext.sortType}" onChange="void setFilter(this, 'id')" /></td>
 <td colspan="3">
<c:if test="${access.canCreate}">
 <el:cmdbutton url="issue" op="edit" label="NEW ISSUE" />
</c:if>
<content:filter roles="Developer"><el:cmdbutton url="issues" linkID="0x${pageContext.request.userPrincipal.ID}" label="MY ISSUES" /></content:filter>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td width="4%">ID</td>
 <td width="25%">TITLE</td>
 <td width="8%">PRIORITY</td>
 <td width="10%">AREA</td>
 <td width="10%">TYPE</td>
 <td width="8%">COMMENTS</td>
 <td width="10%">CREATED</td>
 <td width="10%">LAST COMMENT</td>
 <td>RESOLVED</td>
</tr>

<!-- Table Issue Data -->
<c:forEach var="issue" items="${viewContext.results}">
<view:row entry="${issue}">
 <td class="sec bld"><fmt:int value="${issue.ID}" /></td>
 <td class="small"><el:cmd url="issue" linkID="0x${issue.ID}">${issue.subject}</el:cmd></td>
 <td class="pri bld">${issue.priorityName}</td>
 <td class="bld">${issue.areaName}</td>
 <td class="sec bld">${issue.typeName}</td>
 <td><fmt:int value="${issue.commentCount}" /></td>
 <td><fmt:date fmt="d" date="${issue.createdOn}" /></td>
 <td class="sec"><fmt:date fmt="d" date="${issue.lastCommentOn}" default="-" /></td>
 <td class="bld"><fmt:date fmt="d" date="${issue.resolvedOn}" default="-" /></td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="9"><view:pgUp />&nbsp;<view:pgDn />&nbsp;<view:legend width="90" labels="Open,Fixed,Workaround,Won't Fix,Deferred" classes="opt1, ,opt2,warn,err" /></td>
</tr>
</view:table>
</el:form>
<content:copyright />
</div>
</body>
</html>
