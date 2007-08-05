<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Issue Search</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.searchStr, 5, 'Search Term')) return false;

setSubmit();
disableButton('SearchButton');
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
<el:form method="post" action="isearch.do" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> DEVELOPMENT ISSUE SEARCH</td>
</tr>
<tr>
 <td class="label">Search String</td>
 <td class="data"><el:text name="searchStr" idx="*" size="32" max="48" className="req" value="${param.searchStr}" /></td>
</tr>
<tr>
 <td class="label">Issue Status</td>
 <td class="data"><el:combo name="status" idx="*" size="1" options="${statusOpts}" firstEntry="-" value="${param.status}" /></td>
</tr>
<tr>
 <td class="label">Issue Area</td>
 <td class="data"><el:combo name="area" idx="*" size="1" options="${areaOpts}" firstEntry="-" value="${param.area}" /></td>
</tr>
<tr>
 <td class="label">Maximum Results</td>
 <td class="data"><el:text name="maxResults" idx="*" size="2" max="2" value="${maxResults}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="doComments" idx="*" className="sec" value="true" label="Search Issue comments" checked="${param.doComments}" /></td>
</tr>
<tr class="title mid">
 <td colspan="2"><el:button ID="SearchButton" type="submit" className="BUTTON" label="SEARCH ISSUES" /></td>
</tr>
</el:table>

<!-- Search Results Table -->
<c:if test="${doSearch}">
<view:table className="view" pad="default" space="default" cmd="issues">
<tr class="title caps">
 <td colspan="5" class="left">SEARCH RESULTS</td>
 <td colspan="4"><c:if test="${access.canCreate}"><el:cmd url="issue" op="edit">NEW ISSUE</el:cmd></c:if>
<content:filter roles="Developer"><el:cmd url="issues" link="${pageContext.request.userPrincipal}">MY ISSUES</el:cmd></content:filter></td>
</tr>
<c:if test="${empty results}">
<tr>
 <td colspan="9" class="pri bld">No Issues matching your search criteria were found.</td>
</tr>
</c:if>
<c:if test="${!empty results}">
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
<c:forEach var="issue" items="${results}">
<view:row entry="${issue}">
 <td class="sec bld"><fmt:int value="${issue.ID}" /></td>
 <td class="small"><el:cmd url="issue" link="${issue}">${issue.subject}</el:cmd></td>
 <td class="pri bld">${issue.priorityName}</td>
 <td class="bld">${issue.areaName}</td>
 <td class="sec bld">${issue.typeName}</td>
 <td><fmt:int value="${issue.commentCount}" /></td>
 <td><fmt:date fmt="d" date="${issue.createdOn}" /></td>
 <td class="sec"><fmt:date fmt="d" date="${issue.lastCommentOn}" default="-" /></td>
 <td class="bld"><fmt:date fmt="d" date="${issue.resolvedOn}" default="-" /></td>
</view:row>
</c:forEach>
</c:if>
</view:table>
</c:if>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
