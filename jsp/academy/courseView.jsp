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
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<c:set var="pilot" value="${pilots[course.pilotID]}" scope="request" />
<c:set var="cspan" value="${(!empty exams) || (!empty flights) ? 6 : 1}" scope="request" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="coursecomment.do" linkID="0x${course.ID}" method="post" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="${cspan + 1}">FLIGHT ACADEMY COURSE - ${pilot.rank} ${pilot.name} (${pilot.pilotCode})</td>
</tr>
<tr>
 <td class="label">Course</td>
 <td colspan="${cspan}" class="data pri bld">${course.name}</td>
</tr>
<tr>
 <td class="label">Stage</td>
 <td colspan="${cspan}" class="data bld"><fmt:int value="${course.stage}" /></td>
</tr>
<c:if test="${!empty docs}">
<tr>
 <td class="label" valign="top">Study Documents</td>
 <td colspan="${cspan}" class="data"><c:forEach var="doc" items="${docs}">
<el:link url="/library/${doc.fileName}">${doc.name}</el:link><br />
</c:forEach></td>
</tr>
</c:if>
<tr>
 <td class="label">Course Status</td>
 <td colspan="${cspan}" class="data"><span class="sec bld">${course.statusName}</span>, started on 
<fmt:date fmt="d" date="${course.startDate}" /></td>
</tr>
<c:if test="${!empty course.endDate}">
<tr>
 <td class="label">Completed on</td>
 <td colspan="${cspan}" class="data"><fmt:date fmt="d" date="${course.endDate}" /></td>
</tr>
</c:if>
<c:if test="${!empty exams}">
<%@ include file="/jsp/pilot/pilotExams.jspf" %>
</c:if>

<!-- Course Progress -->
<tr class="title caps">
 <td colspan="${cspan + 1}">COURSE PROGRESS - <fmt:int value="${fn:sizeof(course.progress)}" /> ENTRIES</td>
</tr>
<c:forEach var="progress" items="${course.progress}">
<view:row entry="${progress}">
 <td class="label" valign="top">Entry #<fmt:int value="${progress.ID}" /></td>
 <td colspan="${cspan}" class="data"><fmt:text value="${progress.text}" />
<c:if test="${progress.complete || access.canUpdateProgress}">
<br /><hr />
<c:if test="${progress.complete}">
<span class="pri bld">COMPLETED ON <fmt:date fmt="d" date="${progress.completedOn}" /></span>
</c:if>
<c:if test="${access.canUpdateProgress}">
<el:box name="progress${progress.ID}" idx="*" value="true" checked="${progress.complete}" label="Completed" />
</c:if>
</c:if>
</td>
</view:row>
</c:forEach>
<c:if test="${!empty flights}">
<!-- Instruction Flights -->
<tr class="title caps">
 <td colspan="${cspan + 1}">INSTRUCTION FLIGHT LOG - <fmt:int value="${fn:sizeof(flights)}" /> FLIGHTS</td>
</tr>
<tr class="title mid caps">
 <td colspan="3">COMMENTS</td>
 <td colspan="2">INSTRUCTOR</td>
 <td width="10%">EQUIPMENT</td>
 <td>LENGTH</td>
</tr>
<c:forEach var="flight" items="${flights}">
<c:set var="ins" value="${pilots[flight.instructorID]}" scope="request" />
<tr>
 <td class="left small">${flight.comments}</td>
 <td class="pri bld"><el:cmd url="profile" linkID="0x${ins.ID}">${ins.name}</el:cmd></td>
 <td class="sec small">${flight.equipmentType}</td>
 <td><fmt:dec fmt="#0.0" value="${flight.length / 10}" /> hours</td>
</tr>
</c:forEach>
</c:if>
<c:if test="${!empty course.comments}">
<!-- Course Comments -->
<tr class="title caps">
 <td colspan="${cspan + 1}">DISCUSSION - <fmt:int value="${fn:sizeof(course.comments)}" /> ENTRIES</td>
</tr>
<c:forEach var="comment" items="${course.comments}">
<c:set var="author" value="${pilots[comment.authorID]}" scope="request" />
<tr>
 <td class="label" valign="top">${author.name} (${author.pilotCode})<br />
<fmt:date date="${comment.createdOn}" /></td>
 <td colspan="${cspan}" class="data"><fmt:msg value="${comment.text}" /></td>
</tr>
</c:forEach>
</c:if>
<c:if test="${access.canComment}">
<!-- New Comment -->
<tr>
 <td class="label" valign="top">New Comment</td>
 <td colspan="${cspan}" class="data"><el:textbox name="msgText" width="120" height="6" idx="*" /></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td> 
<c:if test="${access.canStart}">
 <el:cmdbutton ID="EnrollButton" url="coursedispose" linkID="0x${course.ID}" op="restart" label="ENROLL STUDENT" />
</c:if>
<c:if test="${access.canCancel}">
 <el:cmdbutton ID="CancelButton" url="coursedispose" linkID="0x${course.ID}" op="abandon" label="WITHDRAW" />
</c:if>
<c:if test="${access.canRestart}">
 <el:cmdbutton ID="ReturnButton" url="coursedispose" linkID="0x${course.ID}" op="restart" label="RETURN" />
</c:if>
<c:if test="${access.canApprove && isComplete}">
 <el:cmdbutton ID="ApproveButton" url="coursedispose" linkID="0x${course.ID}" op="complete" label="AWARD CERTIFICATION" />
</c:if>
<c:if test="${access.canComment}">
 <el:button ID="CommentButton" type="SUBMIT" className="BUTTON" label="SAVE NEW COMMENT" />
</c:if>
<c:if test="${access.canUpdateProgress}">
 <el:cmdbutton ID="ProgressButton" url="courseprogress" post="true" linkID="0x${course.ID}" label="UPDATE PROGRESS" />
 <el:cmdbutton ID="RideButton" url="courseride" linkID="0x${course.ID}" label="ASSIGN CHECK RIDE" />
</c:if>
<c:if test="${access.canSchedule}">
 <el:cmdbutton ID="FlightLogButton" url="insflight" linkID="0&amp;courseID=${course.ID}" op="edit" label="LOG FLIGHT" />
 <el:cmdbutton ID="SchedButton" url="isession" op="edit" linkID="0&course=${fn:hex(course.ID)}" label="INSTRUCTION SESSION" />
</c:if>
<c:if test="${access.canDelete}">
 <el:cmdbutton ID="DeleteButton" url="coursedelete" linkID="0x${course.ID}" label="DELETE COURSE" />
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
