<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Issue Report #${issue.ID}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.subject, 10, 'Issue Title')) return false;
if (!validateText(form.desc, 5, 'Issue Description')) return false;

setSubmit();
disableButton('SaveButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="versions" name="issue_track.versions" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="issue.do" op="save" linkID="${fn:dbID(issue)}" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title">
 <td class="caps" colspan="2">ISSUE #${issue.ID} - ${issue.subject}</td>
</tr>

<!-- Issue Data -->
<c:if test="${!empty issue}">
<c:set var="author" value="${pilots[issue.authorID]}" scope="request" />
<tr>
 <td class="label">Reported by</td>
 <td class="data"><b>${author.name}</b> (${author.pilotCode}) on <fmt:date date="${issue.createdOn}" /></td>
</tr>
<tr>
 <td class="label">Issue Status</td>
<c:if test="${access.canResolve}">
 <td class="data"><el:combo name="status" className="bld" size="1" idx="1" options="${statuses}" value="${issue.statusName}" /></td>
</c:if>
<c:if test="${!access.canResolve}">
 <td class="data bld">${issue.statusName}</td>
</c:if>
</tr>
</c:if>
<tr>
 <td class="label">Issue Title</td>
 <td class="data"><el:text name="subject" className="pri bld req" size="64" max="128" idx="2" value="${issue.subject}" /></td>
</tr>
<tr>
 <td class="label">Issue Priority</td>
 <td class="data"><el:combo name="priority" size="1" idx="2" options="${priorities}" value="${issue.priorityName}" /></td>
</tr>
<tr>
 <td class="label">Area</td>
 <td class="data"><el:combo name="area" size="1" idx="3" options="${areas}" value="${issue.areaName}" /></td>
</tr>
<tr>
 <td class="label">Issue Type</td>
 <td class="data"><el:combo name="issueType" size="1" idx="4" options="${types}" value="${issue.typeName}" /></td>
</tr>
<c:set var="assignee" value="${pilots[issue.assignedTo]}" scope="request" />
<tr>
 <td class="label">Assigned To</td>
<c:if test="${access.canReassign}">
 <td class="data"><el:combo name="assignedTo" size="1" idx="5" options="${devs}" value="${issue.assignedTo}" /></td>
</c:if>
<c:if test="${!access.canReassign}">
 <td class="data sec bld">${assignee.name} ${asignee.pilotCode}</td>
</c:if> 
</tr>
<tr>
 <td class="label">Target Version</td>
 <td class="data"><el:combo name="version" size="1" idx="6" options="${versions}" value="${issue.majorVersion}.${issue.minorVersion}" /></td>
</tr>
<tr>
 <td class="label" valign="top">Issue Description</td>
 <td class="data"><el:textbox name="desc" width="120" height="5" idx="7" className="req">${issue.description}</el:textbox></td>
</tr>
<c:if test="${empty issue}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="small sec"><el:box name="emailIssue" value="true" idx="*" label="Send Issue via E-Mail" /></td>
</c:if>

<c:if test="${!empty issue}">
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
<c:set var="author" value="${pilots[comment.authorID]}" scope="request" />
<tr valign="top">
 <td class="label" valign="top">${author.name} ${author.pilotCode}<br />
<fmt:date date="${comment.createdOn}" /></td>
 <td class="data" valign="top"><fmt:msg value="${comment.comments}" /></td>
</tr>
</c:forEach>
</c:if>
</c:if>
</el:table>

<!-- Button Bar -->
<c:if test="${access.canEdit}">
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:button ID="SaveButton" type="SUBMIT" className="BUTTON" label="UPDATE ISSUE" /></td>
</tr>
</el:table>
</c:if>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
