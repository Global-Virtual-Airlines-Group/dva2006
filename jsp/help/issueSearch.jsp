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
<title><content:airline /> Help Desk Search</title>
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
<%@ include file="/jsp/help/header.jspf" %> 
<%@ include file="/jsp/help/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="hdsearch.do" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
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
 <td colspan="2"><el:button ID="SearchButton" type="submit" className="BUTTON" label="SEARCH HELP DESK" /></td>
</tr>
</el:table>

<!-- Search Results Table -->
<c:if test="${doSearch}">
<view:table className="view" pad="default" space="default" cmd="hdsearch">
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
 <td width="5%">#</td>
 <td>TITLE</td>
 <td width="10%">STATUS</td>
 <td width="30%">CREATED BY</td>
 <td width="15%">ASSIGNED TO</td>
 <td width="5%">COMMENTS</td>
</tr>

<!-- Table Issue Data -->
<c:forEach var="issue" items="${results}">
<c:set var="author" value="${pilots[issue.authorID]}" scope="page" />
<c:set var="assignee" value="${pilots[issue.assignedTo]}" scope="page" />
<view:row entry="${issue}">
 <td class="sec bld"><fmt:int value="${issue.ID}" /></td>
 <td class="pri bld"><el:cmd url="hdissue" link="${issue}" className="pri bld">${issue.subject}</el:cmd></td>
 <td class="sec bld small">${issue.statusName}</td>
 <td><el:cmd url="profile" link="${author}" className="bld">${author.name}</el:cmd> on
 <fmt:date date="${issue.createdOn}" /></td>
 <td><el:cmd url="profile" link="${assignee}" className="sec bld">${assignee.name}</el:cmd></td>
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
