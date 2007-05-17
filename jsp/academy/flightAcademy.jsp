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
<title><content:airline /> Flight Academy - ${pilot.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;

// Validate response
var act = form.action;
if (act.indexOf('enroll.do') != -1)
{
	var c = form.courseName;
	if (!c) return false;
	if (c.selectedIndex == 0) {
		alert('Please select the Course you wish to enroll in.');
		c.focus();
		return false;
	}
	
	// Check if we're sure
	var cName = c.options[c.selectedIndex].text;
	if (!confirm('Are you sure you wish to enroll in the ' + cName + ' Flight Academy Course?')) return false;
} else {
	var c = form.examName;
	if (!c) return false;
	if (c.selectedIndex == 0) {
		alert('Please select the Examination you wish to take.');
		c.focus();
		return false;
	}

	// Check if we're sure
	var tName = c.options[c.selectedIndex].text;
	if (!confirm('Are you sure you wish to take the ' + tName + ' Examination?')) return false;
}

setSubmit();
disableButton('EnrollButton');
disableButton('ExamButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="enroll.do" method="post" validate="return validate(this)">
<el:table className="view" space="default" pad="default">
<!-- Course History Title Bar -->
<tr class="title caps">
 <td width="45%">COURSE NAME</td>
 <td width="10%">STAGE</td>
 <td width="15%">STATUS</td>
 <td width="15%">STARTED ON</td>
 <td>COMPLETED ON</td>
</tr>

<c:if test="${!empty courses}">
<!-- Flight Academy Course Data -->
<c:forEach var="course" items="${courses}">
<view:row entry="${course}">
 <td><el:cmd url="course" link="${course}" className="pri bld">${course.name}</el:cmd></td>
 <td class="bld"><fmt:int value="${course.stage}" /></td>
 <td class="pri bld">${course.statusName}</td>
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
 <td colspan="5" class="pri bld">You have not enrolled in any Flight Academy courses.</td>
</tr>
</c:if>

<!-- Flight Academy Document Library Section -->
<tr class="title caps">
 <td class="left" colspan="5">DOCUMENT LIBRARY</td>
</tr>
<tr>
 <td class="left" colspan="3">The <content:airline /> Document Library contains all of
 the materials needed to successfully complete written examinations, in addition to being a
 valuable resource for learning more about all aspects of our operations. All documents
 require Adobe Acrobat Reader 6.0 or above.</td>
 <td colspan="2"><el:cmdbutton url="doclibrary" label="DOCUMENT LIBRARY" /></td>
</tr>

<!-- Flight Academy Help Desk -->
<tr class="title caps">
 <td class="left" colspan="5">HELP DESK</td>
</tr>
<tr>
 <td class="left" colspan="3">The <content:airline /> Flight Academy Help Desk lets pilots and Flight Academy
 students communicate with our Instructors and Staff to quickly and easily resolve any issues, or
 answer questions about the Flight Academy.</td>
 <td colspan="2"><el:cmdbutton url="myhdissues" label="HELP DESK" /></td>
</tr>

<!-- New Course Section -->
<tr class="title caps">
 <td class="left" colspan="5">TRAINING COURSES</td>
</tr>
<tr>
<c:if test="${!empty course}">
 <td class="left" colspan="5">You are currently enrolled in a Flight Academy training course, 
<span class="pri bld">${course.name}</span>. Until you have completed or withdrawn from this course, 
you may not enroll in any other Flight Academy courses.</td>
</c:if>
<c:if test="${empty course}">
 <td class="left" colspan="5">Please select a Flight Academy course from the list below. Make sure
 that you are prepared to enroll before clicking on &quot;Enroll.&quot;</td>
</tr>
<tr class="title">
 <td colspan="5">SELECT COURSE <el:combo name="courseName" idx="1" size="1" options="${certs}" firstEntry="< SELECT COURSE >" />
 <el:button ID="EnrollButton" type="submit" className="BUTTON" label="ENROLL IN COURSE" /></td>
</c:if>
<c:if test="${!empty exams}">
<!-- Examination Section -->
<tr class="title caps">
 <td class="left" colspan="5">EXAMINATIONS</td>
</tr>
<c:if test="${examActive > 0}">
 <td class="left" colspan="4">You currently are in the process of taking a Pilot Examination.
 Until this examination has been submitted and scored, you cannot take any new examinations.</td>
 <td><el:cmdbutton url="exam" linkID="${fn:hex(examActive)}" label="ACTIVE EXAM" /></td>
</c:if>
<c:if test="${empty examActive}">
 <td class="left" colspan="5">Please select a written examination from the list below. Make sure that
 you are prepared to take the exam before clicking on &quot;New Examination.&quot;<br />
<br />
Our exams are timed. You will see time remaining at the top of the examianation page. After starting
 you have 40 minutes to complete and submit the examianation. <i>After 40 minutes the examianation will
 be automatically submitted, regardless of number of questions answered</i>.<br />
<br />
The <content:airline /> Flight Academy instructors score examianations within 72 hours of submission,
 and the results of your examination will be sent to you by email. Until it is scored, you will not be
 able to take any exam again.<span class="pri bld">Make sure that you are prepared before you begin
 an examination!</span></td>
</tr>
<tr class="title">
 <td colspan="5">SELECT EXAMINATION <el:combo name="examName" idx="1" size="1" options="${exams}" firstEntry="< SELECT EXAM >" />
 <el:cmdbutton ID="ExamButton" url="newacademyexam" post="true" label="NEW EXAMINATION" /></td>
</c:if>
</c:if>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>

