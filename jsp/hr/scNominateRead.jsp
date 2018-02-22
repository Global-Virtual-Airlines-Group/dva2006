<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Senior Captain Nomination - ${pilot.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script>
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
var act = f.action;
if (act.indexOf('scnomdispose.do') != -1)
	if (!confirm("Are you sure you wish to approve or reject ${pilot.name}'s nomination to Senior Captain?")) return false;
else
	golgotha.form.validate({f:f.body, l:30, t:'Nomination Comments'});

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
<c:set var="cspan" value="7" scope="page" />
<content:attr attr="isHR" value="true" roles="HR" /> 

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="scnominate.do" op="save" method="post" link="${nom}" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="${cspan + 1}"><content:airline /> SENIOR CAPTAIN NOMINATION - ${pilot.rank.name} ${pilot.name} (${pilot.pilotCode}) - ${nom.quarter}</td>
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
<c:if test="${!fn:hasRole('HR', author) || nc.support || isHR}">
<tr>
 <td class="label top">${author.name} (${author.pilotCode}) <fmt:date date="${nc.createdOn}" t="HH:mm" /></td>
 <td colspan="${cspan}" class="data top">
<c:if test="${!nc.support}"><span class="small error bld">DOES NOT SUPPORT THIS NOMINATION</span><br />
<hr /></c:if>
<fmt:msg value="${nc.body}" /></td>
</tr>
</c:if>
</c:forEach>
</c:if>
<c:set var="showExamToggle" value="true" scope="page" />
<c:set var="examCollapse" value="${fn:sizeof(exams) >= 10}" scope="page" />
<%@ include file="/jsp/pilot/pilotExams.jspf" %>
<c:set var="showCourseToggle" value="true" scope="page" />
<c:set var="courseCollapse" value="${fn:sizeof(courses) >= 10}" scope="page" />
<%@ include file="/jsp/pilot/pilotCourses.jspf" %>
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
<c:if test="${access.canNominate}"><el:button ID="SaveButton" type="submit" label="SAVE SENIOR CAPTAIN NOMINATION" /></c:if>
<c:if test="${access.canDispose}"><el:cmdbutton ID="ApproveButton" url="scnomdispose" link="${nom}" op="true" post="true" label="PROMOTE TO SENIOR CAPTAIN" />
 <el:cmdbutton ID="RejectButton" url="scnomdispose" link="${nom}" op="false" post="true" label="REJECT NOMINATION" /></c:if>
 <el:cmdbutton ID="ProfileButton" url="profile" link="${pilot}" key="V" label="VIEW PROFILE" /></td>
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
