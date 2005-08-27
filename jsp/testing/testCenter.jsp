<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Testing Center - ${pilot.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!form.examName) return false;
if (form.examName.selectedIndex == 0) {
	alert('Please select the Examination you wish to take.');
	form.examName.focus();
	return false;
}

// Check if we're sure
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<el:form action="newexam.do" method="POST" validate="return validate(this)">
<el:table className="view" space="default" pad="default">
<!-- Examination Title Bar -->
<tr class="title caps">
 <td>&nbsp;</td>
 <td width="30%">EXAMINATION / CHECK RIDE NAME</td>
 <td width="15%">TYPE</td>
 <td width="10%">SCORE</td>
 <td width="10%">QUESTIONS</td>
 <td width="10%">PERCENT</td>
 <td>DATE</td>
</tr>

<c:if test="${!empty exams}">
<!-- Examination Data -->
<c:forEach var="exam" items="${exams}">
<c:set var="cmdName" value="${fn:isCheckRide(exam) ? 'checkride' : 'exam'}" scope="request" />
<tr>
<c:choose>
<c:when test="${!fn:passed(exam) && !fn:failed(exam)}">
 <td><el:img caption="Not Scored" x="21" y="21" src="blank.png" /></td>
</c:when>
<c:when test="${fn:passed(exam)}">
 <td><el:img caption="Passed" x="21" y="21" src="testing/pass.png" /></td>
</c:when>
<c:when test="${fn:failed(exam)}">
 <td><el:img caption="Failed" x="21" y="21" src="testing/fail.png" /></td>
</c:when>
</c:choose>
 <td class="pri bld"><el:cmd url="${cmdName}" linkID="0x${exam.ID}">${exam.name}</el:cmd></td>
 <td class="sec">${fn:isCheckRide(exam)? 'Check Ride' : 'Examination'}</td>
 <td class="pri bld">${exam.score}</td>
 <td class="bld">${exam.size}</td>
 <td class="sec"><fmt:dec value="${exam.score / exam.size * 100.0}" />%</td>
 <td><fmt:date fmt="d" date="${exam.date}" /></td>
</tr>
</c:forEach>
</c:if>
<c:if test="${empty exams}">
<tr>
 <td colspan="7" class="pri bld">You have not completed any Pilot Examinations.</td>
</tr>
</c:if>

<!-- Document Library Section -->
<tr class="title caps">
 <td class="left" colspan="7">DOCUMENT LIBRARY</td>
</tr>
<tr>
 <td class="left" colspan="5">The <content:airline /> Document Library contains all of
 the materials needed to successfully complete written examinations, in addition to being a
 valuable resource for learning more about all aspects of our operations. Before you take
 a written examination, please make sure you've gone through the Document Library and read
 the manuals for your aircraft type. All documents require Adobe Acrobat Reader 5.0 or above.</td>
 <td colspan="2"><el:cmdbutton url="doclibrary" label="DOCUMENT LIBRARY" /></td>
</tr>

<!-- New Examination Section -->
<tr class="title caps">
 <td class="left" colspan="7">PILOT EXAMINATIONS</td>
</tr>
<tr>
<c:if test="${examActive != 0}">
 <td class="left" colspan="6">You currently are in the process of taking a Pilot Examination.
 Until this examination has been submitted and scored, you cannot take any new examinations.</td>
 <td><el:cmdbutton url="exam" linkID="0x${examActive}" label="ACTIVE EXAM" /></td>
</c:if>
<c:if test="${examActive == 0}">
 <td class="left" colspan="7">Please select a written examination to take from the list below.
 There are no restrictions on the type of tests you can take, or how often you can take each
 test, but please keep this in mind: There are only a limited number of questions for each
 test, and they are selected at random. Within two or three re-tests, you may exhaust the
 question pool. <content:airline /> staff take this into consideration when determining
 Promotions and Additional Ratings - if you take the same test on multiple occassions in a
 short period of time, the earliest test will usually be given more weight than later
 results. <span class="pri bld">Make sure that you are prepared before you take a test!</span></td>
</tr>
<tr class="title">
 <td colspan="7">SELECT EXAMINATION <el:combo name="examName" idx="1" size="1" options="${availableExams}" firstEntry="< SELECT EXAM >" />
 <el:button type="SUBMIT" className="BUTTON" label="NEW EXAMINATION" /></td>
</c:if>
</tr>
</el:table>
</el:form>
<content:copyright />
</div>
</body>
</html>
