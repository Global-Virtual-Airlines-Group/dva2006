<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Senior Captain Nomination - ${pilot.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<script type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
var act = form.action;
if (act.indexOf('scnomdispose.do') != -1) {
	if (!confirm("Are you sure you wish to approve or reject ${pilot.name}'s nomination to Senior Captain?")) return false;
} else {
	if (!validateText(form.body, 30, 'Nomination Comments')) return false;
}

setSubmit();
disableButton('SaveButton');
disableButton('ApproveButton');
disableButton('RejectButton');
disableButton('ProfileButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<c:set var="cspan" value="7" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="scnominate.do" op="save" method="post" link="${nom}" validate="return validate(this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="${cspan + 1}"><content:airline /> SENIOR CAPTAIN NOMINATION - ${pilot.rank.name} ${pilot.name} (${pilot.pilotCode})</td>
</tr>
<tr>
 <td class="label">Current Program</td>
 <td colspan="${cspan}" class="data"><span class="bld">${eqType.name}</span> (Stage <fmt:int value="${eqType.stage}" />)</td>
</tr>
<tr>
 <td class="label">Joined on</td>
 <td colspan="${cspan}" class="data"><fmt:date date="${pilot.createdOn}" fmt="d" d="EEEE MMMM dd, yyyy" /></td>
</tr>
<tr>
 <td class="label">Total Flights</td>
 <td colspan="${cspan}" class="data"><fmt:int value="${pilot.legs}" /> legs, <fmt:dec value="${pilot.hours}" /> hours</td>
</tr>
<c:if test="${pilot.ACARSLegs > 0}">
<tr>
 <td class="label">ACARS Flights</td>
 <td colspan="${cspan}" class="data sec"><fmt:int value="${pilot.ACARSLegs}" /> legs, <fmt:dec value="${pilot.ACARSHours}" /> hours
 <el:cmd url="mystats" link="${pilot}" className="sec bld">Flight Statistics</el:cmd> | <el:cmd url="myroutemap" link="${pilot}" className="bld">Route Map</el:cmd></td>
</tr>
</c:if>
<c:if test="${pilot.onlineLegs > 0}">
<tr>
 <td class="label">Online Flights</td>
 <td colspan="${cspan}" class="data pri"><fmt:int value="${pilot.onlineLegs}" /> legs, <fmt:dec value="${pilot.onlineHours}" /> hours</td>
</tr>
</c:if>
<c:if test="${!empty nom.comments}">
<tr class="title caps">
 <td colspan="${cspan + 1}"><fmt:int value="${nom.commentCount}" /> NOMINATION COMMENTS</td>
</tr>
<c:forEach var="nc" items="${nom.comments}">
<c:set var="author" value="${authors[nc.authorID]}" scope="page" />
<tr>
 <td class="label top">${author.name} (${author.pilotCode}) <fmt:date date="${nc.createdOn}" t="HH:mm" /></td>
 <td colspan="${cspan}" class="data top">
<c:if test="${!nc.support}"><span class="small error bld">DOES NOT SUPPORT THIS NOMINATION</span><br />
<hr /></c:if>
<fmt:msg value="${nc.body}" /></td>
</tr>
</c:forEach>
</c:if>
<c:set var="showStatusToggle" value="true" scope="page" />
<c:set var="statusCollapse" value="${fn:sizeof(statusUpdates) > 10}" scope="page" />
<%@ include file="/jsp/pilot/pilotStatusUpdate.jspf" %>
<c:if test="${access.canNominate}">
<tr class="title caps">
 <td colspan="${cspan + 1}">NEW <content:airline /> SENIOR CAPTAIN NOMINATION - ${pilot.name}</td>
</tr>
<c:if test="${access.canObject}">
<tr>
 <td class="label">&nbsp;</td>
 <td colspan="${cspan}" class="data"><el:box name="support" idx="*" value="true" checked="true" label="I support this Nomination" /></td>
</tr>
</c:if>
<tr>
 <td class="label top">Comments</td>
 <td colspan="${cspan}" class="data"><el:textbox name="body" idx="*" width="90%" resize="true" height="5"></el:textbox></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>
<c:if test="${access.canNominate}"><el:button ID="SaveButton" type="submit" className="BUTTON" label="SAVE SENIOR CAPTAIN NOMINATION" /></c:if>
<c:if test="${access.canDispose}"><el:cmdbutton ID="ApproveButton" url="scnomdispose" link="${nom}" op="true" post="true" label="APPROVE NOMINATION" />
 <el:cmdbutton ID="RejectButton" url="scnomdispose" link="${nom}" op="false" post="true" label="REJECT NOMINATION" /></c:if>
 <el:cmdbutton ID="ProfileButton" url="profile" link="${pilot}" label="VIEW PROFILE" /></td>
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
