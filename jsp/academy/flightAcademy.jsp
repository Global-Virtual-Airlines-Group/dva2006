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
<title><content:airline /> Flight Academy - ${pilot.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;

// Validate response
var act = f.action;
if (act.indexOf('enroll.do') != -1)
{
	golgotha.form.validate({f:f.courseName, t:'Please select the Course you wish to enroll in.'});
	if (!confirm('Are you sure you wish to enroll in the ' + golgotha.form.getCombo(f.courseName) + ' Flight Academy Course?')) return false;
} else {
	golgotha.form.validate({f:f.examName, t:'Please select the Examination you wish to take.'});
	if (!confirm('Are you sure you wish to take the ' + golgotha.form.getCombo(f.examName) + ' Examination?')) return false;
}

golgotha.form.submit(f);
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>
<content:sysdata var="academyFlights" name="academy.minFlights" default="10" />
<content:sysdata var="isATO" name="academy.ato" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="enroll.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="view">
<!-- Course History Title Bar -->
<tr class="title caps">
 <td style="width:40%">COURSE NAME</td>
 <td class="nophone" style="width:15%">INSTRUCTOR</td>
 <td class="nophone" style="width:8%">STAGE</td>
 <td style="width:14%">STATUS</td>
 <td style="width:14%">STARTED ON</td>
 <td>COMPLETED ON</td>
</tr>

<c:if test="${!empty courses}">
<!-- Flight Academy Course Data -->
<c:forEach var="course" items="${courses}">
<c:set var="ins" value="${pilots[course.instructorID]}" scope="page" />
<view:row entry="${course}">
 <td><el:cmd url="course" link="${course}" className="pri bld">${course.name}</el:cmd></td>
 <td class="sec bld nophone">${(empty ins) ? 'Self-Directed' : ins.name}</td>
 <td class="bld nophone"><fmt:int value="${course.stage}" /></td>
 <td class="pri bld">${course.status.name}</td>
 <td><fmt:date fmt="d" date="${course.startDate}" /></td>
<c:if test="${!empty course.endDate}">
 <td><fmt:date fmt="d" date="${course.endDate}" /></td>
</c:if>
<c:if test="${empty course.endDate}">
 <td>N/A</td>
</c:if>
</view:row>
</c:forEach>
</c:if>
<c:if test="${empty courses}">
<tr>
 <td colspan="6" class="pri bld">You have not enrolled in any Flight Academy courses.</td>
</tr>
</c:if>

<!-- Flight Academy Document Library Section -->
<tr class="title caps">
 <td class="left" colspan="6">DOCUMENT LIBRARY</td>
</tr>
<tr>
 <td class="left" colspan="4">The <content:airline /> Document Library contains all of
 the materials needed to successfully complete written examinations, in addition to being a
 valuable resource for learning more about all aspects of our operations. All documents
 require Adobe Acrobat Reader 6.0 or above.</td>
 <td colspan="2"><el:cmdbutton url="doclibrary" label="DOCUMENT LIBRARY" /></td>
</tr>

<!-- Help Desk -->
<tr class="title caps">
 <td class="left" colspan="6">HELP DESK</td>
</tr>
<tr>
 <td class="left" colspan="4">The <content:airline /> Help Desk lets pilots and Flight Academy
 students communicate with our Instructors and Staff to quickly and easily resolve any issues, or
 answer questions about the Flight Academy.</td>
 <td colspan="2"><el:cmdbutton url="myhdissues" label="HELP DESK" /></td>
</tr>

<!-- New Course Section -->
<tr class="title caps">
 <td class="left" colspan="6">TRAINING COURSES</td>
</tr>
<c:choose>
<c:when test="${!empty course}">
<tr>
 <td class="left" colspan="6">You are currently enrolled in a <content:airline /> Flight Academy training
course, <span class="pri bld">${course.name}</span>. Until you have completed or withdrawn from this course, 
you may not enroll in any other <content:airline /> Flight Academy courses.</td>
</tr>
</c:when>
<c:when test="${pilot.legs < minFlights}">
<tr>
 <td class="left" colspan="6">You have completed <fmt:int value="${pilot.legs}" /> flight legs as a <content:airline />
pilot. You need to have completed <fmt:int value="${minFlights}" /> flight legs in order to enroll in a <content:airline />
Flight Academy training course.</td>
</tr>
</c:when>
<c:otherwise>
<tr>
 <td class="left" colspan="6">Please select a Flight Academy course from the list below. Make sure
 that you are prepared to enroll before clicking on &quot;Enroll.&quot;</td>
</tr>
<tr class="title">
 <td colspan="6">SELECT COURSE <el:combo name="courseName" idx="1" size="1" options="${certs}" firstEntry="[ SELECT COURSE ]" />
 <el:button ID="EnrollButton" type="submit" label="ENROLL IN COURSE" /></td>
</tr>
</c:otherwise>
</c:choose>
<c:if test="${!empty exams}">
<!-- Examination Section -->
<tr class="title caps">
 <td class="left" colspan="6">EXAMINATIONS</td>
</tr>
<c:choose>
<c:when test="${examActive > 0}">
<tr>
 <td class="left" colspan="5">You currently are in the process of taking a Pilot Examination.
 Until this examination has been submitted and scored, you cannot take any new examinations.</td>
 <td><el:cmdbutton url="exam" linkID="${fn:hex(examActive)}" label="ACTIVE EXAM" /></td>
</tr>
</c:when>
<c:otherwise>
<tr>
 <td class="left" colspan="6">Please select a written examination from the list below. Make sure that
 you are prepared to take the exam before clicking on &quot;New Examination.&quot;<br />
<br />
Our exams are timed. You will see time remaining at the top of the examianation page. After starting
 you have 40 minutes to complete and submit the examianation. <span class="ita">After 40 minutes the
 examianation will be automatically submitted, regardless of number of questions answered</span>.<br />
<br />
The <content:airline /> Flight Academy instructors score examianations within 72 hours of submission,
 and the results of your examination will be sent to you by email. Until it is scored, you will not be
 able to take any exam again.<span class="pri bld">Make sure that you are prepared before you begin
 an examination!</span></td>
</tr>
<tr class="title">
 <td colspan="6">SELECT EXAMINATION <el:combo name="examName" idx="1" size="1" options="${exams}" firstEntry="[ SELECT EXAM ]" />
 <el:cmdbutton ID="ExamButton" url="newacademyexam" post="true" label="NEW EXAMINATION" /></td>
</tr>
</c:otherwise>
</c:choose>
</c:if>
<c:if test="${isATO}">
<!-- VATSIM ATO Certificate -->
<tr class="title caps">
 <td class="left" colspan="6">VATSIM AUTHORIZED TRAINING ORGANIZATION</td>
</tr>
<tr>
 <td class="left" colspan="5"><content:airline /> is a VATSIM Authorized Training Organization, certified by VATSIM to grant Pilot
 Ratings to Flight Academy graduates upon the successful completion of certain courses.</td>
 <td><el:link url="/ATOCertificate.pdf">ATO Certificate</el:link></td>
</tr>
</c:if>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
