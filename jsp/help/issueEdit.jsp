<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Help Desk Issue<c:if test="${!empty issue}"> #<fmt:int value="${issue.ID}" /></c:if></title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script async>
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;

// Validate response
const act = f.action;
if (act.indexOf('hdissue.do') != -1) {
	golgotha.form.validate({f:f.subject, l:10, t:'Issue Title'});
	golgotha.form.validate({f:f.body, l:5, t:'Issue Description'});
	if ((f.sendIssue) && (f.sendIssue.disabled))
		f.sendIssue.checked = false;
} else {
	golgotha.form.validate({f:f.devAssignedTo, t:'Development Issue Assignee'});
	golgotha.form.validate({f:f.area, t:'Development Issue Area'});
	golgotha.form.validate({f:f.type, t:'Development Issue Type'});
	golgotha.form.validate({f:f.priority, t:'Development Issue Priority'});
}

golgotha.form.submit(f);
return true;
};
<c:if test="${access.canUpdateStatus}">
golgotha.local.checkAssignee = function(combo) {
	document.forms[0].sendIssue.disabled = (combo.selectedIndex == golgotha.local.originalAssignee);
	return true;
};</c:if>
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/help/header.jspf" %> 
<%@ include file="/jsp/help/sideMenu.jspf" %>
<content:enum var="areaOpts" className="org.deltava.beans.system.IssueArea" />
<content:enum var="typeOpts" className="org.deltava.beans.system.Issue$IssueType" />
<content:enum var="priorityOpts" className="org.deltava.beans.system.IssuePriority" />
<content:enum var="statusOpts" className="org.deltava.beans.help.IssueStatus" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="hdissue.do" allowUpload="true" op="save" link="${issue}" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title">
<c:if test="${!empty issue}">
 <td class="caps" colspan="2">ISSUE #<fmt:int value="${issue.ID}" /> - ${issue.subject}</td>
</c:if>
<c:if test="${empty issue}">
 <td class="caps" colspan="2">NEW <content:airline /> HELP DESK ISSUE</td>
</c:if>
</tr>

<!-- Issue Data -->
<c:if test="${!empty issue}">
<c:set var="author" value="${pilots[issue.authorID]}" scope="page" />
<c:set var="assignee" value="${pilots[issue.assignedTo]}" scope="page" />
<tr>
 <td class="label">Reported by</td>
 <td class="data"><span class="bld">${author.name}</span><c:if test="${!empty author.pilotCode}"> (${author.pilotCode})</c:if> on
 <fmt:date date="${issue.createdOn}" /></td>
</tr>
<c:if test="${access.canUpdateStatus}">
<tr>
 <td class="label">Issue Status</td>
 <td class="data"><el:combo name="status" className="bld" size="1" idx="1" options="${statusOpts}" value="${issue.status}" /></td>
</tr>
<tr>
 <td class="label">Assigned To</td>
 <td class="data"><el:combo name="assignedTo" size="1" idx="5" options="${assignees}" value="${issue.assignedTo}" onChange="void golgotha.local.checkAssignee(this)" /></td>
</tr>
</c:if>
</c:if>
<tr>
 <td class="label">Issue Title</td>
 <td class="data"><el:text name="subject" className="pri bld req" size="64" max="128" idx="2" value="${issue.subject}" /></td>
</tr>
<tr>
 <td class="label top">Issue Description</td>
 <td class="data"><el:textbox name="body" width="90%" height="5" idx="7" className="req" resize="true">${issue.body}</el:textbox></td>
</tr>
<c:if test="${empty issue}">
<tr>
 <td class="label">Attach File</td>
 <td><el:file name="attach" className="small" size="96" max="160" /></td>
</tr>
</c:if>
<c:if test="${access.canUpdateStatus}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isPublic" idx="*" value="true" label="This Issue is Public" checked="${issue.getPublic()}" /><br />
<el:box name="sendIssue" idx="*" value="true" checked="true" label="Send Notification to Assignee" /></td>
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
 <td class="data"><el:combo name="priority" idx="*" size="1" options="${priorityOpts}" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label">Issue Area</td>
 <td class="data"><el:combo name="area" idx="*" size="1" options="${areaOpts}" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label">Issue Type</td>
 <td class="data"><el:combo name="type" idx="*" size="1" options="${typeOpts}" firstEntry="-" /></td>
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
<c:set var="author" value="${pilots[comment.authorID]}" scope="page" />
<tr>
 <td class="label top">${author.name} (${author.pilotCode})<br />
<fmt:date date="${comment.createdOn}" /></td>
 <td class="data top"><fmt:msg value="${comment.body}" /></td>
</tr>
</c:forEach>
</c:if>
</c:if>
</el:table>

<!-- Button Bar -->
<c:if test="${access.canUpdateStatus || (empty issue && access.canCreate)}">
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="${empty issue ? 'SAVE NEW' : 'UPDATE'} ISSUE" />
<c:if test="${access.canUpdateStatus}">&nbsp;<el:cmdbutton post="true" url="hdconvert" link="${issue}" label="CONVERT ISSUE" />
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
<script async>
golgotha.local.originalAssignee = document.forms[0].assignedTo.selectedIndex;
</script></c:if>
<content:googleAnalytics />
</body>
</html>
