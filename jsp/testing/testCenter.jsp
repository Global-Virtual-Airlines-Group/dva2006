<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title>Testing Center - ${pilot.name} ($pilot.pilotCode)</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script>
golgotha.local.validate = function(f, isCR) {
	if (!golgotha.form.check()) return false;
	if (!isCR) {
		if (!f.examName) return false;
		if (!golgotha.form.comboSet(f.examName))
			throw new golgotha.event.ValidationError('Please select the Examination you wish to take.', f.examName);

		if (!confirm('Are you sure you wish to take the ' + golgotha.form.getCombo(f.examName) + ' Examination?')) return false;
	}

	golgotha.form.submit(f);
	return true;
};

golgotha.local.doCR = function() {
	var f = document.forms[0];
	if (!golgotha.local.validate(f, true)) return false;
	f.action = 'currencyassign.do';
	return f.submit();
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="examLockoutHours" name="testing.lockout" />
<content:sysdata var="currencyEnabled" name="testing.currency.enabled" />
<content:sysdata var="currencySelfEnroll" name="testing.currency.selfenroll" />
<content:sysdata var="currencyInterval" name="testing.currency.validity" />
<c:set var="cspan" value="${pilot.proficiencyCheckRides ? 8 : 7}" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="newexam.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="view">
<c:if test="${currencyEnabled && currencySelfEnroll}">
<!-- Promotion Mode Title bar -->
<tr class="title caps">
 <td colspan="${cspan}">CHECK RIDE CURRENCY</td>
</tr>
<tr>
 <td colspan="${cspan}"><content:airline /> allows its Pilots to <span class="ita">opt into</span> a recurrent certification model. Pilots will continue to require the successful completion of a written Examination
 as well as an initial Check Ride for entrance into a particular equipment program. Pilot who opt into recurrent certification will require an additional operational Check Ride every <fmt:int value="${currencyInterval}" />
 days in order to retain their type ratings.<br />
<br /> 
<c:if test="${!pilot.proficiencyCheckRides}">
You are currently enrolled in our <span class="pri bld caps">LEGACY</span> certification model. Ratings never expire, and Check Rides will remain valid permanently.<br />
<br />
To discover more about our currency-based certification model, you can <el:cmd url="currencyenable" link="${pilot}" className="pri bld">Click Here</el:cmd> to review the changes that switching to this model will
have on your existing aircraft type ratings.
</c:if>
<c:if test="${pilot.proficiencyCheckRides}">
You are currently enrolled within our <span class="ter bld caps">RECURRENT</span> certification model. Check ries are only valid for <fmt:int value="${currencyInterval}" /> days and a currency Check Ride will need to
 be performed before ratings expire.<c:if test="${!empty expiringRides}"><br />
 <br />
 <span class="pri bld">You have <fmt:int value="${expiringRides.size()}" /> Check Rides that will expire before <fmt:date fmt="d" date="${expiryDate}" />.</span></c:if></c:if>
 </td>
</tr>
</c:if>
<!-- Examination Title Bar -->
<tr class="title caps">
 <td>&nbsp;</td>
 <td style="width:30%">EXAMINATION / CHECK RIDE NAME</td>
 <td style="width:15%">TYPE</td>
 <td style="width:10%">SCORE</td>
 <td style="width:10%">QUESTIONS</td>
 <td style="width:10%">PERCENT</td>
<c:if test="${pilot.proficiencyCheckRides}"><td>EXPIRES</td></c:if>
 <td>DATE</td>
</tr>

<c:if test="${!empty exams}">
<!-- Examination Data -->
<c:forEach var="exam" items="${exams}">
<c:set var="cmdName" value="${fn:isCheckRide(exam) ? 'checkride' : 'exam'}" scope="page" />
<c:if test="${pilot.proficiencyCheckRides && fn:isCheckRide(exam)}"><c:set var="expDate" value="${exam.expirationDate}"  scope="page" />
<c:set var="isExpired" value="${(!empty expDate) && expDate.isBefore(expiryDate)}" scope="page" /></c:if>
<tr>
<c:choose>
<c:when test="${!fn:passed(exam) && !fn:failed(exam)}">
 <td><el:img caption="Not Scored" x="21" y="21" src="blank.png" /></td>
</c:when>
<c:when test="${fn:passed(exam)}">
 <td><el:img caption="Passed" x="21" y="21" src="testing/pass.png" /></td>
</c:when>
<c:when test="${fn:failed(exam)}">
 <td><el:img caption="Unsatisfactory" x="21" y="21" src="testing/fail.png" /></td>
</c:when>
</c:choose>
 <td class="pri bld"><el:cmd url="${cmdName}" link="${exam}">${exam.name}</el:cmd></td>
 <td class="sec">${fn:isCheckRide(exam)? exam.type.name : 'Examination'}</td>
<c:choose>
<c:when test="${fn:pending(exam) || (fn:isCheckRide(exam) && fn:isWaiver(exam))}">
 <td colspan="3">NOT APPLICABLE</td>
</c:when>
<c:otherwise>
 <td class="pri bld">${exam.score}</td>
 <td class="bld">${exam.size}</td>
 <td class="sec"><fmt:dec value="${exam.score * 100.0 / exam.size}" />%</td>
</c:otherwise>
</c:choose>
<c:if test="${pilot.proficiencyCheckRides}"> <td<c:if test="${isExpired}"> class="warn bld"</c:if>><fmt:date fmt="d" date="${expDate}" default="-" /></td></c:if>
 <td><fmt:date fmt="d" date="${exam.date}" /></td>
</tr>
<c:remove var="expDate" /><c:remove var="isExpired" />
</c:forEach>
</c:if>
<c:if test="${empty exams}">
<tr>
 <td colspan="${cspan}" class="pri bld">You have not completed any Pilot Examinations.</td>
</tr>
</c:if>

<!-- Document Library Section -->
<tr class="title caps">
 <td class="left" colspan="${cspan}">DOCUMENT LIBRARY</td>
</tr>
<tr>
 <td class="left" colspan="${cspan - 2}">The <content:airline /> Document Library contains all of the materials needed to successfully complete written examinations, in addition to being a
 valuable resource for learning more about all aspects of our operations. Before you take a written examination, please make sure you've gone through the Document Library and read
 the manuals for your aircraft type. All documents require Adobe Acrobat Reader 6.0 or above.</td>
 <td colspan="2"><el:cmdbutton url="doclibrary" label="DOCUMENT LIBRARY" /></td>
</tr>

<!-- New Examination Section -->
<tr class="title caps">
 <td class="left" colspan="${cspan}">PILOT EXAMINATIONS</td>
</tr>
<c:choose>
<c:when test="${examActive > 0}">
<tr>
 <td class="left" colspan="${cspan}">You currently are in the process of taking a Pilot Examination. Until it has been submitted and scored, you cannot take any new examinations.
 <el:cmd url="exam" linkID="${fn:hex(examActive)}" className="sec bld">Click Here</el:cmd> to return to the Examination.</td>
</tr>
</c:when>
<c:when test="${!empty txreq}">
<tr>
 <td class="left" colspan="${cspan}">You currently are in the process of transferring between Equipment Programs. Until your Equipment Program Transfer has been completed, you cannot take any new examinations.</td>
</tr>
</c:when>
<c:when test="${failedExam}">
<tr>
 <td class="left" colspan="${cspan}">You completed a <content:airline /> pilot Examination with an  unsatisfactory score less than <fmt:int value="${examLockoutHours}" /> hours ago, and therefore 
 cannot write a new Examination until this interval has passed.</td>
</tr>
</c:when>
<c:otherwise>
<tr>
 <td class="left" colspan="${cspan}">Please select a written examination from the list below. Make sure that you are prepared to take the exam before clicking on &quot;New Examination.&quot;<br />
<br />
Our exams are timed. You will see time remaining at the top of the examination page. After starting you have approximately 40 minutes to complete and submit the examination. <span class="bld ita">After 40 minutes the 
 examination will be automatically submitted, regardless of number of questions answered</span>.<br />
<br />
Most <content:airline /> examianations are multiple choice and automatically scored when submitted. If not, your examination will be scored within 72 hours of submission, and the results will be sent to you by e-mail.
 Until it is scored, you will not be able to take any exam again.<span class="pri bld">Make sure that you are prepared before you begin an examination!</span></td>
</tr>
<tr class="title">
 <td colspan="${cspan}">SELECT EXAMINATION <el:combo name="examName" idx="1" size="1" options="${availableExams}" firstEntry="[ SELECT EXAM ]" /> <el:button type="submit" label="NEW EXAMINATION" /></td>
</tr>
</c:otherwise>
</c:choose>
<c:if test="${pilot.proficiencyCheckRides && (!empty expiringRides) && !hasPendingCR}">
<!-- Expiring Check Ride Section -->
<tr class="title caps">
 <td class="left" colspan="${cspan}">EXPIRING CURRENCY CHECK RIDES</td>
</tr>
<tr>
 <td class="left" colspan="${cspan}">You have <fmt:int value="${expiringRides.size()}" /> currency Check Rides that have either expired or will expire before <fmt:date fmt="d" date="${expiryDate}" />. In order to maintain your existing
 equipment ratings, you will need to successfully complete a Check Ride before your ratings expire. To assign yourself a currency Check Ride, please select the equipment program below.</td>
</tr>
<tr class="title">
 <td colspan="${cspan}"><el:button onClick="void golgotha.local.doCR()" label="NEW CURRENCY CHECK RIDE" /></td>
</tr>
</c:if>
</el:table>
<el:text type="hidden" value="${pilot.hexID}" name="id" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
