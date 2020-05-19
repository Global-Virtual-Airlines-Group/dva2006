<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Help Desk Search</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script async>
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
<%@ include file="/jsp/help/header.jspf" %> 
<%@ include file="/jsp/help/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="hdsearch.do" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> HELP DESK SEARCH</td>
</tr>
<tr>
 <td class="label">Search String</td>
 <td class="data"><el:text name="searchStr" idx="*" size="32" max="48" className="req" value="${param.searchStr}" /></td>
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
 <td colspan="2"><el:button type="submit" label="SEARCH HELP DESK" /></td>
</tr>
</el:table>

<!-- Search Results Table -->
<c:if test="${doSearch}">
<view:table cmd="hdsearch">
<tr class="title caps">
 <td colspan="6" class="left">SEARCH RESULTS</td>
</tr>
<c:if test="${empty results}">
<tr>
 <td colspan="6" class="pri bld">No Help Desk Issues matching your search criteria were found.</td>
</tr>
</c:if>
<c:if test="${!empty results}">
<!-- Table Header Bar-->
<tr class="title">
 <td>#</td>
 <td>TITLE</td>
 <td class="nophone" style="width:10%">STATUS</td>
 <td class="nophone" style="width:30%">CREATED BY</td>
 <td class="nophone" style="width:15%">ASSIGNED TO</td>
 <td style="width:5%">COMMENTS</td>
</tr>

<!-- Table Issue Data -->
<c:forEach var="issue" items="${results}">
<c:set var="author" value="${pilots[issue.authorID]}" scope="page" />
<c:set var="assignee" value="${pilots[issue.assignedTo]}" scope="page" />
<view:row entry="${issue}">
 <td class="sec bld"><fmt:int value="${issue.ID}" /></td>
 <td class="pri bld"><el:cmd url="hdissue" link="${issue}" className="pri bld">${issue.subject}</el:cmd></td>
 <td class="sec bld small nophone">${issue.statusName}</td>
 <td class="nophone"><el:cmd url="profile" link="${author}" className="bld">${author.name}</el:cmd> on <fmt:date date="${issue.createdOn}" t="HH:mm" /></td>
 <td class="nophone"><el:cmd url="profile" link="${assignee}" className="sec bld">${assignee.name}</el:cmd></td>
 <td><fmt:int value="${issue.commentCount}" /></td>
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
