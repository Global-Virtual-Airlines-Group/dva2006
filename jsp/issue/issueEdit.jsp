<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Issue Report #<fmt:int value="${issue.ID}" /></title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:googleAnalytics />
<content:js name="common" />
<content:cspHeader />
<script async>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.subject, l:10, t:'Issue Title'});
	golgotha.form.validate({f:f.desc, l:5, t:'Issue Description'});
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
<content:sysdata var="versions" name="issue_track.versions" />
<content:enum var="areaOpts" className="org.deltava.beans.system.IssueArea" />
<content:enum var="typeOpts" className="org.deltava.beans.system.Issue$IssueType" />
<content:enum var="priorityOpts" className="org.deltava.beans.system.IssuePriority" />
<content:enum var="statusOpts" className="org.deltava.beans.system.IssueStatus" />
<content:enum var="securityOpts" className="org.deltava.beans.system.IssueSecurity" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="issue.do" op="save" link="${issue}" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
<c:if test="${!empty issue}">
 <td colspan="2"><content:airline /> DEVELOPMENT ISSUE #<fmt:int value="${issue.ID}" /> - ${issue.subject}</td>
</c:if>
<c:if test="${empty issue}">
 <td colspan="2">NEW <content:airline /> DEVELOPMENT ISSUE</td>
</c:if>
</tr>

<!-- Issue Data -->
<c:if test="${!empty issue}">
<c:set var="author" value="${pilots[issue.authorID]}" scope="page" />
<tr>
 <td class="label">Reported by</td>
 <td class="data"><span class="bld">${author.name}</span><c:if test="${!empty author.pilotCode}"> (${author.pilotCode})</c:if> on <fmt:date date="${issue.createdOn}" /></td>
</tr>
<tr>
 <td class="label">Issue Status</td>
<c:if test="${access.canResolve}">
 <td class="data"><el:combo name="status" className="bld" size="1" idx="1" options="${statusOpts}" value="${issue.status}" /></td>
</c:if>
<c:if test="${!access.canResolve}">
 <td class="data bld"><fmt:edesc object="${issue.status}" /></td>
</c:if>
</tr>
</c:if>
<content:filter roles="Developer">
<tr>
 <td class="label">Virtual Airlines</td>
 <td class="data"><el:check name="apps" cols="3" width="140" idx="*" className="small" checked="${issue.airlines}" options="${allApps}" /></td>
</tr>
</content:filter>
<tr>
 <td class="label">Issue Title</td>
 <td class="data"><el:text name="subject" className="pri bld req" size="64" max="128" idx="*" value="${issue.subject}" /></td>
</tr>
<content:filter roles="HR,Examination,PIREP,Developer">
<tr>
 <td class="label">Security</td>
 <td class="data"><el:combo name="security" size="1" idx="*" options="${securityOpts}" value="${issue.security}" /></td>
</tr>
</content:filter>
<tr>
 <td class="label">Issue Priority</td>
 <td class="data"><el:combo name="priority" size="1" idx="*" options="${priorityOpts}" value="${issue.priority}" /></td>
</tr>
<tr>
 <td class="label">Area</td>
 <td class="data"><el:combo name="area" size="1" idx="*" options="${areaOpts}" value="${issue.area}" /></td>
</tr>
<tr>
 <td class="label">Issue Type</td>
 <td class="data"><el:combo name="issueType" size="1" idx="*" options="${typeOpts}" value="${issue.type}" /></td>
</tr>
<c:set var="assignee" value="${pilots[issue.assignedTo]}" scope="page" />
<tr>
 <td class="label">Assigned To</td>
<c:if test="${access.canReassign}">
 <td class="data"><el:combo name="assignedTo" size="1" idx="*" options="${devs}" value="${issue.assignedTo}" /></td>
</c:if>
<c:if test="${!access.canReassign}">
 <td class="data sec bld">${assignee.name} ${asignee.pilotCode}</td>
</c:if> 
</tr>
<tr>
 <td class="label">Target Version</td>
 <td class="data"><el:combo name="version" size="1" idx="*" options="${versions}" value="${currentVersion}" /></td>
</tr>
<tr>
 <td class="label top">Issue Description</td>
 <td class="data"><el:textbox name="desc" width="80%" height="4" idx="*" className="req" resize="true">${issue.description}</el:textbox></td>
</tr>
<c:if test="${empty issue}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="small sec"><el:box name="emailIssue" value="true" checked="true" idx="*" label="Send Issue via E-Mail" /></td>
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
 <td class="data top"><fmt:msg value="${comment.body}" bbCode="true" /></td>
</tr>
</c:forEach>
</c:if>
</c:if>
</el:table>

<!-- Button Bar -->
<c:if test="${access.canEdit}">
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="${empty issue ? 'CREATE' : 'UPDATE'} ISSUE" /></td>
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
