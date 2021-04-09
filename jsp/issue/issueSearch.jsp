<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Issue Search</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.searchStr, l:5, t:'Search Term'});
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
<content:enum var="statusOpts" className="org.deltava.beans.system.IssueStatus" />
<content:enum var="areaOpts" className="org.deltava.beans.system.IssueArea" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="isearch.do" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
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
 <td colspan="2"><el:button type="submit" label="SEARCH ISSUES" /></td>
</tr>
</el:table>

<!-- Search Results Table -->
<c:if test="${doSearch}">
<view:table cmd="issues">
<tr class="title caps">
 <td colspan="6" class="left"><span class="nophone"><content:airline /> DEVELOPMENT ISSUE </span>SEARCH RESULTS</td>
 <td colspan="2">&nbsp;<c:if test="${access.canCreate}"><el:cmd url="issue" op="edit">NEW ISSUE</el:cmd></c:if></td>
</tr>
<c:if test="${empty results}">
<tr>
 <td colspan="8" class="pri bld">No Development Issues matching your search criteria were found.</td>
</tr>
</c:if>
<c:if test="${!empty results}">
<!-- Table Header Bar-->
<tr class="title">
 <td style="width:5%">ID</td>
 <td style="width:25%">TITLE</td>
 <td style="width:10%">PRIORITY</td>
  <td class="nophone" style="width:10%">AREA</td>
 <td class="nophone" style="width:10%">TYPE</td>
 <td class="nophone" style="width:10%">CREATED</td>
 <td style="width:10%">LAST COMMENT</td>
 <td class="nophone" >RESOLVED</td>
</tr>

<!-- Table Issue Data -->
<c:forEach var="issue" items="${results}">
<view:row entry="${issue}">
 <td class="sec bld"><fmt:int value="${issue.ID}" /></td>
 <td class="small"><el:cmd url="issue" link="${issue}">${issue.subject}</el:cmd></td>
  <td class="pri bld small"><fmt:defaultMethod object="${issue.priority}" method="description" /></td>
 <td class="bld small nophone"><fmt:defaultMethod object="${issue.area}" method="description" /></td>
 <td class="sec bld small nophone"><fmt:defaultMethod object="${issue.type}" method="description" /></td>
 <td class="nophone"><fmt:date fmt="d" date="${issue.createdOn}" /></td>
 <td class="sec"><fmt:date fmt="d" date="${issue.lastCommentOn}" default="-" /></td>
 <td class="bld nophone"><fmt:date fmt="d" date="${issue.resolvedOn}" default="-" /></td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="8"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn />&nbsp;</view:scrollbar>
<view:legend width="120" labels="Open,Fixed,Worked Around,Won't Fix,Deferred,Duplicate" classes="opt1, ,opt2,warn,err,opt3" /></td>
</tr>
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
