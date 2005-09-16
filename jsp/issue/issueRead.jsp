<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Issue Report #${issue.ID}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.comment, 10, 'Issue Comments')) return false;

setSubmit();
disableButton('EditButton');
disableButton('CommentButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>
<content:sysdata var="versions" name="issue_track.versions" />

<!-- Main Body Frame -->
<div ID="main">
<el:form method="POST" action="issuecomment.do" linkID="0x${issue.ID}" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<!-- PIREP Title Bar -->
<tr class="title">
 <td class="caps" colspan="2">ISSUE #${issue.ID} - ${issue.subject}</td>
</tr>

<!-- Issue Data -->
<tr>
 <td class="label">Reported by</td>
 <td class="data">${pilots[issue.createdBy].name} (${pilots[issue.createdBy].pilotCode}) on
 <fmt:date date="${issue.createdOn}" /></td>
</tr>
<tr>
 <td class="label">Assigned To</td>
 <td class="data bld">${pilots[issue.assignedTo].name} (${pilots[issue.assignedTo].pilotCode})</td>
</tr>
<tr>
 <td class="label">Issue Status</td>
 <td class="data"><span class="sec bld">${issue.statusName}</span>
<c:if test="${!empty issue.resolvedOn}"> on <fmt:date date="${issue.resolvedOn}" /></c:if></td>
</tr>
<tr>
 <td class="label">Issue Priority</td>
 <td class="data pri bld">${issue.priorityName}</td>
</tr>
<tr>
 <td class="label">Area</td>
 <td class="data">${issue.areaName}</td>
</tr>
<tr>
 <td class="label">Issue Type</td>
 <td class="data sec">${issue.typeName}</td>
</tr>
<tr>
 <td class="label">Target Version</td>
 <td class="data">${issue.majorVersion}.${issue.minorVersion}</td>
</tr>
<tr>
 <td class="label" valign="top">Issue Description</td>
 <td class="data"><fmt:text value="${issue.description}" /></td>
</tr>

<!-- Issue Comments -->
<tr class="title caps left">
 <td colspan="2">ISSUE COMMENTS</td>
</tr>
<c:if test="${empty issue.comments}">
<tr class="pri bld mid caps">
 <td colspan="2">THERE ARE NO COMMENTS FOR THIS ISSUE.</td>
</tr>
</c:if>
<c:if test="${!empty issue.comments}">
<c:forEach var="comment" items="${issue.comments}">
<c:set var="author" value="${pilots[comment.createdBy]}" scope="request" />
<tr>
 <td class="label" valign="top">${author.name} (${author.pilotCode})<br />
 <fmt:date date="${comment.createdOn}" /></td>
 <td class="data" valign="top"><fmt:text value="${comment.comments}" /></td>
</tr>
</c:forEach>
</c:if>

<c:if test="${access.canComment}">
<!-- New Comment -->
<tr>
 <td class="label" valign="top">New Comment</td>
 <td><el:textbox name="comment" width="120" height="6" idx="*"></el:textbox></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="small sec"><el:box name="emailComment" value="true" idx="*" label="Send Comments via E-Mail" checked="true" /></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td>
 <c:if test="${access.canEdit}">
 <el:cmdbutton ID="EditButton" label="EDIT ISSUE" url="issue" op="edit" linkID="0x${issue.ID}" />
 </c:if>
 <c:if test="${access.canComment}">
 <el:button ID="CommentButton" type="SUBMIT" className="BUTTON" label="SAVE NEW COMMENT" />
 </c:if>
 </td>
</tr>
</el:table>
</el:form>
<content:copyright />
</div>
</body>
</html>
