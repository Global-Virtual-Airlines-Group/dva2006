<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Help Desk</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;

// Validate response
var act = form.action;
if (act.indexOf('hdissue.do') != -1) {
	if (!validateText(form.subject, 10, 'Issue Title')) return false;
	if (!validateText(form.body, 5, 'Issue Description')) return false;
	if ((form.sendIssue) && (form.sendIssue.disabled))
		form.sendIssue.checked = false;
} else {
	if (!validateCombo(form.devAssignedTo, 'Development Issue Assignee')) return false;
	if (!validateCombo(form.area, 'Development Issue Area')) return false;
	if (!validateCombo(form.type, 'Development Issue Type')) return false;
	if (!validateCombo(form.priority, 'Development Issue Priority')) return false;
}

setSubmit();
disableButton('SaveButton');
disableButton('ConvertButton');
return true;
}
<c:if test="${access.canUpdateStatus}">
function checkAssignee(combo)
{
var f = document.forms[0];
f.sendIssue.disabled = (combo.selectedIndex == document.originalAssignee);
return true;
}</c:if>
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/help/header.jspf" %> 
<%@ include file="/jsp/help/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="hdissue.do" op="save" link="${issue}" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title">
<c:if test="${!empty issue}">
 <td class="caps" colspan="2">ISSUE #${issue.ID} - ${issue.subject}</td>
</c:if>
<c:if test="${empty issue}">
 <td class="caps" colspan="2">NEW <content:airline /> HELP DESK ISSUE</td>
</c:if>
</tr>

<!-- Issue Data -->
<c:if test="${!empty issue}">
<c:set var="author" value="${pilots[issue.authorID]}" scope="request" />
<c:set var="assignee" value="${pilots[issue.assignedTo]}" scope="request" />
<tr>
 <td class="label">Reported by</td>
 <td class="data"><b>${author.name}</b> (${author.pilotCode}) on <fmt:date date="${issue.createdOn}" /></td>
</tr>
<c:if test="${access.canUpdateStatus}">
<tr>
 <td class="label">Issue Status</td>
 <td class="data"><el:combo name="status" className="bld" size="1" idx="1" options="${statuses}" value="${issue.statusName}" /></td>
</tr>
<tr>
 <td class="label">Assigned To</td>
 <td class="data"><el:combo name="assignedTo" size="1" idx="5" options="${assignees}" value="${issue.assignedTo}" onChange="void checkAssignee(this)" /></td>
</tr>
</c:if>
</c:if>
<tr>
 <td class="label">Issue Title</td>
 <td class="data"><el:text name="subject" className="pri bld req" size="64" max="128" idx="2" value="${issue.subject}" /></td>
</tr>
<tr>
 <td class="label" valign="top">Issue Description</td>
 <td class="data"><el:textbox name="body" width="80%" height="5" idx="7" className="req">${issue.body}</el:textbox></td>
</tr>
<c:if test="${access.canUpdateStatus}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isPublic" idx="*" value="true" label="This Issue is Public" checked="${issue.public}" /><br />
<el:box name="sendIssue" idx="*" value="true" checked="${true}" label="Send Notification to Assignee" /></td>
</tr>
<tr class="title">
 <td colspan="2" class="left caps">CONVERT TO DEVELOPMENT ISSUE</td>
</tr>
<tr>
 <td class="label">Assign To</td>
 <td class="data"><el:combo name="devAssignedTo" idx="*" size="1" options="${devs}" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label">Priority</td>
 <td class="data"><el:combo name="priority" idx="*" size="1" options="${priorityNames}" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label">Issue Area</td>
 <td class="data"><el:combo name="area" idx="*" size="1" options="${areaNames}" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label">Issue Type</td>
 <td class="data"><el:combo name="type" idx="*" size="1" options="${typeNames}" firstEntry="-" /></td>
</tr>
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
 <td class="data" valign="top"><fmt:msg value="${comment.body}" /></td>
</tr>
</c:forEach>
</c:if>
</c:if>
</el:table>

<!-- Button Bar -->
<c:if test="${access.canUpdateStatus || (empty issue && access.canCreate)}">
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:button ID="SaveButton" type="SUBMIT" className="BUTTON" label="${empty issue ? 'SAVE NEW' : 'UPDATE'} ISSUE" />
<c:if test="${access.canUpdateStatus}">
 <el:cmdbutton ID="ConvertButton" post="true" url="hdconvert" link="${issue}" label="CONVERT ISSUE" />
</c:if></td>
</tr>
</el:table>
</c:if>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<c:if test="${access.canUpdateStatus}">
<script language="JavaScript" type="text/javascript">
// Set original assignee
var f = document.forms[0];
document.originalAssignee = f.assignedTo.selectedIndex;
</script></c:if>
<content:googleAnalytics />
</body>
</html>
