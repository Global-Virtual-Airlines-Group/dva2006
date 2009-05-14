<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<c:set var="author" value="${authors[entry.authorID]}" scope="page" />
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Journal - ${author.name}</title>
<content:sysdata var="airlineName" name="airline.name" />
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:js name="common" />
<content:rss title="${airlineName} Journals" path="/blog_rss.ws" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
<c:if test="${access.canComment}">
if (!checkSubmit()) return false;
if (!validateText(form.body, 8, 'Entry Feedback')) return false;

setSubmit();
disableButton('EditButton');
disableButton('CommentButton');
disableButton('DeleteButton');</c:if>
return ${access.canComment};
}
</script>
</head>
<content:copyright visible="false" />
<body onload="void initLinks()">
<content:page>
<%@ include file="/jsp/blog/header.jspf" %> 
<%@ include file="/jsp/blog/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="blogcomment.do" method="post" link="${entry}" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">${entry.title} - <fmt:date fmt="d" date="${entry.date}" /></td>
</tr>
<tr>
 <td colspan="2" class="left"><fmt:msg value="${entry.body}" /></td>
</tr>
<tr class="title caps">
 <td colspan="2">FEEDBACK - <fmt:int value="${entry.size}" /> COMMENTS</td>
</tr>
<c:forEach var="comment" items="${entry.comments}">
<tr>
 <td class="label top">${empty comment.name ? 'Anonymous' : comment.name}<br />
<fmt:date date="${comment.date}" /></td>
 <td class="data"><fmt:msg value="${comment.body}" />
<c:if test="${access.canDelete}">
<hr />
<span class="small">Posted from ${comment.remoteAddr} (${comment.remoteHost})</span>
 <el:cmd url="blogdelete" link="${entry}" op="${fn:hex(comment.date.time)}" className="pri small">DELETE COMMENT</el:cmd></c:if></td>
</tr>
</c:forEach>
<c:if test="${access.canComment}">
<tr class="title caps">
 <td colspan="2">ADD NEW COMMENT</td>
</tr>
<tr>
 <td class="label">Your Name</td>
<content:authUser anonymous="true">
 <td class="data"><el:text name="name" idx="*" className="pri bld req" size="24" max="64" value="" /></td>
</content:authUser>
<content:authUser var="user">
 <td class="data pri bld">${user.name}</td>
</content:authUser>
</tr>
<content:authUser anonymous="true">
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><el:text name="email" idx="*" size="48" max="128" value="" /></td>
</tr>
</content:authUser>
<tr>
 <td class="label top">Comments</td>
 <td class="data"><el:textbox name="body" idx="*" width="80%" height="7" className="req" /></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><c:if test="${access.canComment}">
 <el:button ID="CommentButton" type="submit" className="BUTTON" label="SUBMIT COMMENT" />
</c:if>
<c:if test="${access.canEdit}">
 <el:cmdbutton ID="EditButton" url="blogentry" op="edit" link="${entry}" label="EDIT JOURNAL ENTRY" />
</c:if>
<c:if test="${access.canDelete}">
 <el:cmdbutton ID="DeleteButton" url="blogdelete" op="true" link="${entry}" label="DELETE ENTRY" />
</c:if>
</td></tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
