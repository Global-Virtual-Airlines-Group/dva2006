<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Flight Academy Course - ${course.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
<c:if test="${access.canComment || access.canUpdateProgress}">
if (!checkSubmit()) return false;

// Validate response
var act = form.action;
if (act.indexOf('courseprogress.do') != -1) {

} else if (act.indexOf('courseassign.do') != -1) {

} else {
	if (!validateText(form.msgText, 5, 'Course Comments')) return false;
}

setSubmit();
disableButton('EnrollButton');
disableButton('CancelButton');
disableButton('ReturnButton');
disableButton('ApproveButton');
disableButton('DeleteButton');
disableButton('SchedButton');
disableButton('ProgressButton');
disableButton('FlightLogButton');
disableButton('CommentButton');</c:if>
return ${access.canComment || access.canUpdateProgress};
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>
<c:set var="pilot" value="${pilots[course.pilotID]}" scope="request" />
<c:set var="ins" value="${pilots[course.instructorID]}" scope="request" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="coursecomment.do" link="${course}" method="post" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="7">FLIGHT ACADEMY COURSE - ${course.name}</td>
</tr>
<tr>
 <td class="label">Pilot</td>
 <td colspan="6" class="data">${pilot.rank} <span class="pri bld">${pilot.name}</span> (${pilot.pilotCode})
 - <el:cmd url="logbook" link="${pilot}">View Log Book</el:cmd></td>
</tr>
<tr>
 <td class="label">Stage</td>
 <td colspan="6" class="data bld"><fmt:int value="${course.stage}" /></td>
</tr>
<tr>
 <td class="label">Course Status</td>
 <td colspan="6" class="data"><span class="sec bld">${course.statusName}</span>, started on 
<fmt:date fmt="d" date="${course.startDate}" /></td>
</tr>
<c:if test="${access.canAssign}">
<tr>
 <td class="label">Instructor</td>
 <td colspan="6" class="data"><el:combo name="instructor" idx="*" size="1" options="${instructors}" value="${ins}" firstEntry="-" /></td>
</tr>
</c:if>
<c:if test="${(!access.canAssign) && (!empty ins)}">
<tr>
 <td class="label">Instructor</td>
 <td colspan="6" class="data"><span class="pri bld">${ins.name}</span> (${ins.pilotCode})</td>
</tr>
</c:if>
<c:if test="${!empty course.endDate}">
<tr>
 <td class="label">Completed on</td>
 <td colspan="6" class="data"><fmt:date fmt="d" date="${course.endDate}" /></td>
</tr>
</c:if>
<c:if test="${(!empty docs) || (!empty videos)}">
<tr class="title caps">
 <td colspan="7">FLIGHT ACADEMY TRAINING MATERIALS</td>
</tr>
<c:if test="${!empty docs}">
<tr>
 <td class="label" valign="top">Study Documents</td>
 <td colspan="6" class="data"><c:forEach var="doc" items="${docs}">
<el:link url="/library/${doc.fileName}">${doc.name}</el:link><br />
</c:forEach></td>
</tr>
</c:if>
<c:if test="${!empty videos}">
<tr>
 <td class="label" valign="top">Training Videos</td>
 <td colspan="6" class="data"><c:forEach var="video" items="${videos}">
<el:link url="/video/${video.fileName}">${video.name}</el:link><br />
</c:forEach></td>
</tr>
</c:if>
</c:if>
<c:set var="cspan" value="${6}" scope="request" />
<c:set var="forceExams" value="${true}" scope="request" />
<%@ include file="/jsp/pilot/pilotExams.jspf" %>

<!-- Course Progress -->
<tr class="title caps">
 <td colspan="7">COURSE PROGRESS - <fmt:int value="${fn:sizeof(course.progress)}" /> ENTRIES</td>
</tr>
<c:forEach var="progress" items="${course.progress}">
<c:set var="lastUpd" value="${pilots[progress.authorID]}" scope="request" />
<view:row entry="${progress}">
 <td class="label" valign="top">Entry #<fmt:int value="${progress.ID}" /></td>
 <td colspan="6" class="data"><fmt:text value="${progress.text}" />
<c:if test="${progress.complete || access.canUpdateProgress}">
<br /><hr />
<c:if test="${progress.complete}">
<span class="pri bld">COMPLETED ON <fmt:date fmt="d" date="${progress.completedOn}" /> (${lastUpd.name})</span>
</c:if>
<c:if test="${access.canUpdateProgress}">
<el:box name="progress${progress.ID}" idx="*" value="true" checked="${progress.complete}" label="Completed" />
</c:if>
</c:if>
</td>
</view:row>
</c:forEach>
<c:if test="${!empty sessions}">
<!-- Instruction Sessions -->
<tr class="title caps">
 <td colspan="7">INSTRUCTION SESSION LOG - <fmt:int value="${fn:sizeof(sessions)}" /> SESSIONS</td>
</tr>
<tr class="title mid caps">
 <td>DATE</td>
 <td colspan="3">COMMENTS</td>
 <td>STATUS</td>
 <td colspan="2">INSTRUCTOR</td>
</tr>
<c:forEach var="session" items="${sessions}">
<c:set var="ins" value="${pilots[session.instructorID]}" scope="request" />
<tr class="mid">
 <td><fmt:date date="${session.date}" fmt="d" default="-" /></td>
 <td class="left small" colspan="3">${session.comments}</td>
 <td class="sec small">${session.statusName}</td>
 <td class="pri bld" colspan="2"><el:cmd url="profile" link="${ins}">${ins.name}</el:cmd></td>
</tr>
</c:forEach>
</c:if>
<c:if test="${!empty flights}">
<!-- Instruction Flights -->
<tr class="title caps">
 <td colspan="7">INSTRUCTION FLIGHT LOG - <fmt:int value="${fn:sizeof(flights)}" /> FLIGHTS</td>
</tr>
<tr class="title mid caps">
 <td>DATE</td>
 <td colspan="2">COMMENTS</td>
 <td colspan="2">INSTRUCTOR</td>
 <td width="10%">EQUIPMENT</td>
 <td>LENGTH</td>
</tr>
<c:forEach var="flight" items="${flights}">
<c:set var="ins" value="${pilots[flight.instructorID]}" scope="request" />
<tr class="mid">
 <td><fmt:date date="${flight.date}" fmt="d" default="-" /></td>
 <td class="left small" colspan="2">${flight.comments}</td>
 <td class="pri bld" colspan="2"><el:cmd url="profile" link="${ins}">${ins.name}</el:cmd></td>
 <td class="sec small">${flight.equipmentType}</td>
 <td><fmt:dec fmt="#0.0" value="${flight.length / 10}" /> hours</td>
</tr>
</c:forEach>
</c:if>
<c:if test="${!empty course.comments}">
<!-- Course Comments -->
<tr class="title caps">
 <td colspan="7">DISCUSSION - <fmt:int value="${fn:sizeof(course.comments)}" /> ENTRIES</td>
</tr>
<c:forEach var="comment" items="${course.comments}">
<c:set var="author" value="${pilots[comment.authorID]}" scope="request" />
<tr>
 <td class="label" valign="top">${author.name} (${author.pilotCode})<br />
<fmt:date date="${comment.createdOn}" /></td>
 <td colspan="6" class="data"><fmt:msg value="${comment.text}" /></td>
</tr>
</c:forEach>
</c:if>
<c:if test="${access.canComment}">
<!-- New Comment -->
<tr>
 <td class="label" valign="top">New Comment</td>
 <td colspan="6" class="data"><el:textbox name="msgText" width="80%" height="6" idx="*" /></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td> 
<c:if test="${access.canStart}">
 <el:cmdbutton ID="EnrollButton" url="coursedispose" link="${course}" op="start" label="ENROLL STUDENT" />
</c:if>
<c:if test="${access.canCancel}">
 <el:cmdbutton ID="CancelButton" url="coursedispose" link="${course}" op="abandon" label="WITHDRAW" />
</c:if>
<c:if test="${access.canRestart}">
 <el:cmdbutton ID="ReturnButton" url="coursedispose" link="${course}" op="restart" label="RETURN" />
</c:if>
<c:if test="${access.canApprove && isComplete}">
 <el:cmdbutton ID="ApproveButton" url="coursedispose" link="${course}" op="complete" label="AWARD CERTIFICATION" />
</c:if>
<c:if test="${access.canAssign}">
 <el:cmdbutton ID="AssignButton" url="courseassign" post="true" link="${course}" label="ASSIGN INSTRUCTOR" />
</c:if>
<c:if test="${access.canComment}">
 <el:button ID="CommentButton" type="SUBMIT" className="BUTTON" label="SAVE NEW COMMENT" />
</c:if>
<c:if test="${access.canUpdateProgress}">
 <el:cmdbutton ID="ProgressButton" url="courseprogress" post="true" link="${course}" label="UPDATE PROGRESS" />
 <el:cmdbutton ID="RideButton" url="courseride" link="${course}" label="ASSIGN CHECK RIDE" />
</c:if>
<c:if test="${access.canSchedule}">
 <el:cmdbutton ID="FlightLogButton" url="insflight" linkID="0&amp;courseID=${course.ID}" op="edit" label="LOG FLIGHT" />
 <el:cmdbutton ID="SchedButton" url="isession" op="edit" linkID="0&course=${fn:hex(course.ID)}" label="INSTRUCTION SESSION" />
</c:if>
<c:if test="${access.canDelete}">
 <el:cmdbutton ID="DeleteButton" url="coursedelete" link="${course}" label="DELETE COURSE" />
</c:if>
 </td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
