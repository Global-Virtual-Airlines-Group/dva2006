<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Issue Report #<fmt:int value="${issue.ID}" /></title>
<content:canonical convertID="true" />
<content:css name="main" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script async>
golgotha.local.validate = function(f) {
<c:if test="${access.canComment}">
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.comment, l:10, t:'Issue Comments'});
	golgotha.form.validate({f:f.attach, ext:[], empty:true, t:'Attached File', maxSize:2048});
	golgotha.form.submit(f);</c:if>
	return ${access.canComment};
};

golgotha.local.toggleCheckbox = function() {
	const f = document.forms[0];
	f.emailAll.disabled = (!f.emailComment.checked);
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
<c:set var="author" value="${pilots[issue.authorID]}" scope="page" />
<c:set var="assignee" value="${pilots[issue.assignedTo]}" scope="page" />
<c:set var="authorLoc" value="${userData[issue.authorID]}" scope="page" />
<c:set var="assigneeLoc" value="${userData[issue.assignedTo]}" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="issuecomment.do" link="${issue}" allowUpload="true" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<!-- Title Bar -->
<tr class="title">
 <td class="caps" colspan="2">DEVELOPMENT ISSUE #<fmt:int value="${issue.ID}" /> - ${issue.subject}</td>
</tr>

<!-- Issue Data -->
<tr>
 <td class="label">Reported by</td>
 <td class="data"><el:profile location="${authorLoc}" className="bld plain">${author.name}</el:profile><c:if test="${!empty author.pilotCode}">&nbsp;<b>(${author.pilotCode})</b></c:if>
 on <fmt:date date="${issue.createdOn}" /></td>
</tr>
<tr>
 <td class="label">Assigned To</td>
 <td class="data bld"><el:profile location="${assigneeLoc}" className="plain">${assignee.name}</el:profile>&nbsp;(${assignee.pilotCode})</td>
</tr>
<tr>
 <td class="label">Issue Status</td>
 <td class="data"><span class="sec bld"><fmt:defaultMethod object="${issue.status}" method="description" /></span><c:if test="${!empty issue.resolvedOn}"> on <fmt:date date="${issue.resolvedOn}" /></c:if></td>
</tr>
<c:if test="${!empty hdIssue}">
<tr>
 <td class="label">Help Desk Issue</td>
 <td class="data">Linked to<span class="nophone"> <content:airline /> Help Desk Issue #<fmt:int className="sec bld" value="${hdIssue.ID}" /></span> <el:cmd url="hdissue" link="${hdIssue}" className="pri bld">${hdIssue.subject}</el:cmd></td>
</tr>
</c:if>
<tr>
 <td class="label">Issue Priority</td>
 <td class="data pri bld"><fmt:defaultMethod object="${issue.priority}" method="description" /></td>
</tr>
<tr>
 <td class="label">Airlines</td>
 <td class="data sec bld"><c:forEach var="ai" items="${issue.airlines}" varStatus="as">${ai.name} <c:if test="${!as.last}">, </c:if></c:forEach></td>
</tr>
<tr>
 <td class="label">Security</td>
 <td class="data"><fmt:defaultMethod object="${issue.security}" method="description" /></td>
</tr>
<tr>
 <td class="label">Area</td>
 <td class="data"><fmt:defaultMethod object="${issue.area}" method="description" /></td>
</tr>
<tr>
 <td class="label">Issue Type</td>
 <td class="data sec"><fmt:defaultMethod object="${issue.type}" method="description" /></td>
</tr>
<c:if test="${issue.majorVersion > 0}">
<tr>
 <td class="label">Target Version</td>
 <td class="data">${issue.majorVersion}.${issue.minorVersion}</td>
</tr>
</c:if>
<tr>
 <td class="label top">Issue Description</td>
 <td class="data"><fmt:msg value="${issue.description}" bbCode="true" /></td>
</tr>

<!-- Issue Comments -->
<tr class="title caps left">
 <td colspan="2"><c:if test="${issue.comments.size() > 1}"><fmt:int value="${issue.comments.size()}" />&nbsp;</c:if>ISSUE COMMENTS</td>
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
 <td class="label top">${author.name}<span class="nophone"> (${author.pilotCode})</span><br />
 <fmt:date date="${comment.createdOn}" t="HH:mm" /></td>
 <td class="data top"><fmt:msg value="${comment.body}" bbCode="true" />
<c:if test="${(!empty comment.name) && (!empty user)}">
<hr />
Attached File: <span class="pri bld">${comment.name}</span> (<fmt:fileSize value="${comment.size}" />) <a href="/attach/issue/${issue.hexID}/${comment.hexID}">Click to Download</a></c:if></td>
</tr>
</c:forEach>
</c:if>

<c:if test="${access.canComment}">
<!-- New Comment -->
<tr>
 <td class="label top">New Comment</td>
 <td><el:textbox name="comment" width="80%" height="4" idx="*" resize="true" className="req"></el:textbox></td>
</tr>
<tr>
 <td class="label">Attach File</td>
 <td><el:file name="attach" className="small" size="96" max="160" maxSize="2048" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="small sec"><el:box name="emailComment" value="true" idx="*" label="Send Comments via E-Mail" checked="true" onChange="void golgotha.local.toggleCheckbox()" />
 <el:box name="emailAll" value="true" idx="*" label="Send Comments to all Participants" checked="${multiComment}" /></td>
</tr>
</c:if>
<%@ include file="/jsp/auditLog.jspf" %>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>&nbsp;
<c:if test="${access.canEdit}"><el:cmdbutton label="EDIT ISSUE" url="issue" op="edit" link="${issue}" /></c:if>
<c:if test="${access.canComment}">&nbsp;<el:button type="submit" label="SAVE NEW COMMENT" /></c:if>
<c:if test="${access.canResolve && (issue.linkedIssueID == 0)}">&nbsp;<el:cmdbutton label="CONVERT TO HELP DESK ISSUE" url="issueconvert" link="${issue}" /></c:if>
 </td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
