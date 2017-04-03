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
<title>Flight Academy Course - ${course.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
<c:if test="${access.canComment || access.canUpdateProgress}">
if (!golgotha.form.check()) return false;

// Validate response
if ((f.action.indexOf('courseprogress.do') == -1) && (f.action.indexOf('courseassign.do') == -1))
	golgotha.form.validate({f:f.msgText, l:5, t:'Course Comments'});

golgotha.form.submit(f);</c:if>
return ${access.canComment || access.canUpdateProgress};
}
<c:if test="${access.canCancel}">
golgotha.local.validateCancel = function() {
	if (confirm('Are you sure you want to withdraw?')) self.location = '/coursedispose.do?op=abandon&id=${course.hexID}';
	return true;
};
</c:if></script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>
<c:set var="pilotLoc" value="${userData[course.pilotID]}" scope="page" />
<c:set var="pilot" value="${pilots[course.pilotID]}" scope="page" />
<c:set var="ins" value="${pilots[course.instructorID]}" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="coursecomment.do" link="${course}" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="7">FLIGHT ACADEMY COURSE - ${course.name}</td>
</tr>
<tr>
 <td class="label">Pilot</td>
 <td colspan="6" class="data">${pilot.rank.name} <el:profile location="${pilotLoc}" className="pri bld">${pilot.name}</el:profile> (${pilot.pilotCode})</td>
</tr>
<tr>
 <td class="label">Stage</td>
 <td colspan="6" class="data bld"><fmt:int value="${course.stage}" /></td>
</tr>
<tr>
 <td class="label">Course Status</td>
 <td colspan="6" class="data"><span class="sec bld">${course.status.name}</span>, started on 
<fmt:date fmt="d" date="${course.startDate}" /></td>
</tr>
<c:forEach var="cr" items="${course.checkRides}">
<tr>
 <td class="label">Check Ride #<fmt:int value="${cr.index}" /></td>
 <td colspan="6" class="data"><span class="pri bld caps">${cr.status.name}</span><c:if test="${cr.flightID > 0}"> - <el:cmd url="crview" link="${cr}">VIEW FLIGHT REPORT</el:cmd></c:if></td>
</tr>
</c:forEach>
<c:choose>
<c:when test="${access.canAssignInstructor}">
<tr>
 <td class="label">Instructor</td>
 <td colspan="6" class="data"><el:combo name="instructor" idx="*" size="1" options="${instructors}" value="${ins}" firstEntry="-" /></td>
</tr>
</c:when>
<c:when test="${fn:isCourseActive(course) && (!empty ins)}">
 <td class="label">Instructor</td>
 <td colspan="6" class="data pri bld caps">Self-Directed Flight Academy Course</td>
</c:when>
<c:when test="${!empty ins}">
<tr>
 <td class="label">Instructor</td>
 <td colspan="6" class="data"><span class="pri bld">${ins.name}</span> (${ins.pilotCode})</td>
</tr>
</c:when>
</c:choose>
<c:if test="${!empty course.endDate}">
<tr>
 <td class="label">Completed on</td>
 <td colspan="6" class="data"><fmt:date fmt="d" date="${course.endDate}" /></td>
</tr>
</c:if>
<c:if test="${!empty cert.description}">
<tr class="title caps">
 <td colspan="7">COURSE INSTRUCTIONS - <span class="und" onclick="void golgotha.util.toggleExpand(this, 'courseDesc')">COLLAPSE</span></td>
</tr>
<tr class="courseDesc">
 <td class="label">&nbsp;</td>
 <td colspan="6" class="data"><fmt:msg value="${cert.description}" /></td>
</tr>
</c:if>
<c:if test="${(!empty docs) || (!empty videos)}">
<tr class="title caps">
 <td colspan="7">FLIGHT ACADEMY TRAINING MATERIALS</td>
</tr>
<c:if test="${!empty docs}">
<tr>
 <td class="label top">Study Documents</td>
 <td colspan="6" class="data"><c:forEach var="doc" items="${docs}">
<el:link target="_new" url="/library/${doc.fileName}">${doc.name}</el:link><br />
</c:forEach></td>
</tr>
</c:if>
<c:if test="${!empty videos}">
<tr>
 <td class="label top">Training Videos</td>
 <td colspan="6" class="data"><c:forEach var="video" items="${videos}">
<el:link url="/video/${video.fileName}">${video.name}</el:link><br />
</c:forEach></td>
</tr>
</c:if>
</c:if>
<c:set var="cspan" value="6" scope="page" />
<c:set var="forceExams" value="true" scope="page" />
<%@ include file="/jsp/pilot/pilotExams.jspf" %>

<!-- Course Progress -->
<tr class="title caps">
 <td colspan="7">COURSE PROGRESS - <fmt:int value="${fn:sizeof(course.progress)}" /> ENTRIES</td>
</tr>
<c:forEach var="progress" items="${course.progress}">
<c:set var="isMine" value="${course.pilotID == user.ID}" scope="page" />
<c:set var="lastUpd" value="${pilots[progress.authorID]}" scope="page" />
<view:row entry="${progress}">
 <td class="label top">Entry #<fmt:int value="${progress.ID}" /></td>
 <td colspan="6" class="data top"><fmt:msg value="${progress.text}" />
<c:if test="${(!empty progress.examName) || progress.complete || access.canUpdateProgress}">
<br /><hr />
<c:choose>
<c:when test="${progress.complete}">
<span class="pri bld">COMPLETED ON <fmt:date fmt="d" date="${progress.completedOn}" /> (${lastUpd.name})</span>
</c:when>
<c:when test="${!empty progress.examName}">
Requires the <span class="pri bld">${progress.examName}</span> examination<c:if test="${isMine}">, which can be started at the 
 <el:cmd url="academy" className="sec bld">Flight Academy</el:cmd> page</c:if>. 
</c:when>
</c:choose>
<c:if test="${access.canUpdateProgress}">
 <el:box name="progress${progress.ID}" idx="*" value="true" checked="${progress.complete}" label="Mark as Completed" /></c:if>
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
<c:set var="ins" value="${pilots[session.instructorID]}" scope="page" />
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
 <td style="width:10%">EQUIPMENT</td>
 <td>LENGTH</td>
</tr>
<c:forEach var="flight" items="${flights}">
<c:set var="ins" value="${pilots[flight.instructorID]}" scope="page" />
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
<c:set var="author" value="${pilots[comment.authorID]}" scope="page" />
<tr>
 <td class="label top">${author.name} (${author.pilotCode})<br />
<fmt:date date="${comment.createdOn}" t="HH:mm" /></td>
 <td colspan="6" class="data top"><fmt:msg value="${comment.text}" /></td>
</tr>
</c:forEach>
</c:if>
<c:if test="${access.canComment}">
<!-- New Comment -->
<tr>
 <td class="label top">New Comment</td>
 <td colspan="6" class="data"><el:textbox name="msgText" width="80%" height="6" idx="*" resize="true" /></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td> 
<c:if test="${access.canStart}">
 <el:cmdbutton ID="EnrollButton" url="coursedispose" link="${course}" op="start" label="ENROLL STUDENT" />
</c:if>
<c:if test="${access.canCancel}">
 <el:button ID="CancelButton" onClick="golgotha.local.validateCancel()" label="WITHDRAW" />
</c:if>
<c:if test="${access.canRestart}">
 <el:cmdbutton ID="ReturnButton" url="coursedispose" link="${course}" op="restart" label="RETURN" />
</c:if>
<c:if test="${access.canApprove}">
 <el:cmdbutton ID="ApproveButton" url="coursedispose" link="${course}" op="complete" label="AWARD CERTIFICATION" />
</c:if>
<c:if test="${access.canAssignInstructor}">
 <el:cmdbutton ID="AssignButton" url="courseassign" post="true" link="${course}" label="ASSIGN INSTRUCTOR" />
</c:if>
<c:if test="${access.canComment}">
 <el:button ID="CommentButton" type="submit" label="SAVE NEW COMMENT" />
</c:if>
<c:if test="${access.canUpdateProgress}">
 <el:cmdbutton ID="ProgressButton" url="courseprogress" post="true" link="${course}" label="UPDATE PROGRESS" />
</c:if>
<c:if test="${access.canAssignCheckRide}">
 <el:cmdbutton ID="RideButton" url="courseride" link="${course}" label="ASSIGN CHECK RIDE" />
</c:if>
<c:if test="${access.canSchedule}">
 <el:cmdbutton ID="FlightLogButton" url="insflight" linkID="0&amp;courseID=${course.ID}" op="edit" label="LOG FLIGHT" />
 <el:cmdbutton ID="SchedButton" url="isession" op="edit" linkID="0&course=${course.hexID}" label="INSTRUCTION SESSION" />
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
<content:googleAnalytics />
</body>
</html>
