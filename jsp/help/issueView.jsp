<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Help Desk - ${issue.subject}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
<c:if test="${access.canComment || access.canUpdateStatus}">
if (!golgotha.form.check()) return false;

// Get form action
var act = f.action;
if (act.indexOf('hdcomment.do') != -1)
	golgotha.form.validate({f:f.body, l:10, t:'Issue Comments'});
else if ((f.isFAQ) && (f.isFAQ.checked) && (f.faqIDs)) {
	var isChecked = 0;
	for (x = 0; x < f.faqIDs.length; x++)
		isChecked += ((f.faqIDs[x].checked) ? 1 : 0);

	if (isChecked == 0) {
		alert('A FAQ Answer comment must be selected.');
		return false;
	} else if (isChecked > 1) {
		alert('Only one FAQ Answer comment may be selected.');
		return false;
	}
}

golgotha.form.submit(f);</c:if>
return ${access.canComment};
}
<c:if test="${access.canUseTemplate}">
golgotha.local.selectResponse = function(f)
{
if (!golgotha.form.comboSet(f.rspTemplate)) return false;
var xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'hdrsptmp.ws?id=' + escape(golgotha.form.getCombo(f.rspTemplate)));
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var xml = xmlreq.responseXML;
	if (!xml) return false;
	var xe = xml.documentElement;
	var bds = xe.getElementsByTagName("body");
	if (bds.length == 0) return false;
	var body = bds[0].firstChild.data;
	f.body.value += body;
	return true;
};

xmlreq.send(null);
return true;	
};
</c:if>
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/help/header.jspf" %> 
<%@ include file="/jsp/help/sideMenu.jspf" %>
<c:set var="author" value="${pilots[issue.authorID]}" scope="page" />
<c:set var="assignee" value="${pilots[issue.assignedTo]}" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="hdcomment.do" link="${issue}" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title">
 <td class="caps" colspan="2">ISSUE #${issue.ID} - ${issue.subject}</td>
</tr>

<!-- Issue Data -->
<tr>
 <td class="label">Reported by</td>
 <td class="data"><el:cmd url="profile" link="${author}" className="bld plain">${author.name}</el:cmd> <b>(${empty author.pilotCode ? 'NEW PILOT' : author.pilotCode})</b> on <fmt:date date="${issue.createdOn}" /></td>
</tr>
<tr>
 <td class="label">Assigned To</td>
 <td class="data bld"><el:cmd url="profile" link="${assignee}" className="plain">${assignee.name}</el:cmd> (${assignee.pilotCode})</td>
</tr>
<tr>
 <td class="label">Issue Status</td>
 <td class="data"><span class="sec bld">${issue.statusName}</span>
<c:if test="${!empty issue.resolvedOn}"> on <fmt:date date="${issue.resolvedOn}" /></c:if></td>
</tr>
<c:if test="${access.canUpdateContent && (fn:sizeof(issue.comments) > 1)}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isFAQ" idx="*" value="true" checked="${issue.FAQ}" className="sec bld" label="This Issue is part of the FAQ" /></td>
</tr>
</c:if>
<tr>
 <td class="label top">Issue Description</td>
 <td class="data"><fmt:msg value="${issue.body}" bbCode="true" /></td>
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
<c:set var="cAuthor" value="${pilots[comment.authorID]}" scope="page" />
<tr>
 <td class="label top">${cAuthor.name} (${cAuthor.pilotCode})<br />
 <fmt:date date="${comment.createdOn}" t="HH:mm" /><c:if test="${access.canUpdateContent}"><br />
<el:box name="deleteID" value="${comment.createdOn.toEpochMilli()}" checked="false" label="Delete" /><br />
<el:radio name="faqID" value="${comment.createdOn.toEpochMilli()}" checked="${comment.FAQ}" label="FAQ Answer" /></c:if></td>
 <td class="data top"><fmt:msg value="${comment.body}" bbCode="true" /></td>
</tr>
</c:forEach>
</c:if>

<c:if test="${access.canComment}">
<!-- New Comment -->
<tr>
 <td class="label top">New Comment</td>
 <td><div id="newComment" style="position:relative;"><el:textbox name="body" width="70%" height="4" idx="*" className="req" resize="true"></el:textbox>
<c:if test="${access.canUseTemplate && (!empty rspTemplates)}">
<div id="rspTemplateSelect" style="width:25%; position:absolute; top:1px; right:1px;" class="pri small bld right nophone">
Template <el:combo name="rspTemplate" className="small" firstEntry="-" options="${rspTemplates}" />
<el:button ID="TemplateButton" onClick="void golgotha.local.selectResponse(document.forms[0])" label="USE" /></div>
</c:if></div>
 </td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>
<c:if test="${access.canUpdateStatus}">
<el:cmdbutton ID="EditButton" label="EDIT ISSUE" url="hdissue" op="edit" link="${issue}" />
</c:if>
<c:if test="${access.canComment}">
 <el:button ID="CommentButton" type="submit" label="SAVE NEW COMMENT" />
</c:if>
<c:if test="${access.canUpdateContent}">
 <el:cmdbutton ID="UpdateButton" label="UPDATE ISSUE/COMMENTS" url="hdupdate" post="true" link="${issue}" />
</c:if>
<c:if test="${access.canClose}">
 <el:cmdbutton ID="CloseButton" label="CLOSE ISSUE" url="hdclose" link="${issue}" />
</c:if>
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
