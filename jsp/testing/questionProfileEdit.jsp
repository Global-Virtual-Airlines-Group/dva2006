<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<c:set var="isMC" value="${empty question || fn:isMultiChoice(question)}" scope="page" />
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Examination Question Profile</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.question, 20, 'Question Text')) return false;
if (!validateText(form.correct, 3, 'Correct Answer to this Question')) return false;
if (!validateFile(form.imgData, 'gif,jpg,png', 'Image Resource')) return false;
if (!validateCombo(form.owner, 'Owner')) return false;
if (!validateCheckBox(form.airline, 1, 'Airline')) return false;

// Validate multiple choice
<c:if test="${empty question}">
if ((form.isMultiChoice) && (form.isMultiChoice.checked)) {
	if (!validateCombo(form.correctChoice, 'Correct Answer to this Question')) return false;
}
</c:if>
<c:if test="${!empty question && isMC}">
if (!validateCombo(form.correctChoice, 'Correct Answer to this Question')) return false;
</c:if>

setSubmit();
disableButton('SaveButton');
return true;
}

function toggleAnswerBox()
{
var aRow = document.getElementById('answerRow');
var mcRows = getElementsByClass('mcRow', 'tr');
var f = document.forms[0];
if (f.isMultiChoice) {
	if (f.correct) f.correct.disabled = f.isMultiChoice.checked;
	f.answerChoices.disabled = (!f.isMultiChoice.checked);
	f.correctChoice.disabled = (!f.isMultiChoice.checked);
	aRow.style.display = f.isMultiChoice.checked ? 'none' : '';
	for (var x = 0; x < mcRows.length; x++)
		mcRows[x].style.display = f.isMultiChoice.checked ? '' : 'none';
}

return true;
}

function updateAnswerCombo()
{
var f = document.forms[0];
if ((!f.answerChoices) || (!f.correctChoice)) return false;

// Save the old answer
var oldAnswer = f.correctChoice.options[f.correctChoice.selectedIndex].text;

// Copy each line in the textbox to an answer choice
var choices = f.answerChoices.value.split('\n');
f.correctChoice.options.length = 1;
f.correctChoice.options.length = choices.length + 1;
for (var x = 0; x < choices.length; x++) {
	var c = choices[x].replace('\r','');
	f.correctChoice.options[x + 1] = new Option(c, c);
	if (c == oldAnswer)
		f.correctChoice.selectedIndex = x + 1;
}

return true;
}
<c:if test="${question.size > 0}">
function viewImage(x, y)
{
var flags = 'height=' + (y+45) + ',width=' + (x+45) + ',menubar=no,toolbar=no,status=yes,scrollbars=yes';
var w = window.open('/exam_rsrc/${question.hexID}', 'questionImage', flags);
return true;
}
</c:if>
</script>
</head>
<content:copyright visible="false" />
<body onload="updateAnswerCombo(); toggleAnswerBox()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="airlines" name="apps" mapValues="true" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="qprofile.do" link="${question}" op="save" method="post" allowUpload="true" validate="return validate(this)">
<el:table className="form">
<!-- Question Title Bar -->
<tr class="title caps">
 <td colspan="2">EXAMINATION QUESTION PROFILE</td>
</tr>
<tr>
 <td class="label">Question Text</td>
 <td class="data bld"><el:text name="question" idx="*" size="120" className="req" max="255" value="${question.question}" /></td>
</tr>
<c:if test="${!isMC || (empty question)}">
<tr id="answerRow">
 <td class="label">Correct Answer</td>
 <td class="data"><el:text name="correct" idx="*" size="120" className="req" max="255" value="${question.correctAnswer}" /></td>
</tr>
</c:if>
<tr>
 <td class="label">Owner Airline</td>
 <td class="data"><el:combo name="owner" idx="*" size="1" className="req" firstEntry="-" options="${airlines}" value="${question.owner}" /></td>
</tr>
<tr>
 <td class="label">Airlines</td>
 <td class="data"><el:check name="airline" width="175" options="${airlines}" className="req" checked="${question.airlines}" /></td>
</tr>
<tr>
 <td class="label top">Pilot Examinations</td>
 <td class="data"><el:check name="examNames" idx="*" cols="5" width="200" newLine="true" className="small" checked="${question.exams}" options="${examNames}" /></td>
</tr>
<c:if test="${!empty question}">
<tr>
 <td class="label">Statistics</td>
<c:if test="${question.totalAnswers > 0}">
 <td class="data">Answered <fmt:int value="${question.totalAnswers}" /> times,
 <fmt:int value="${question.correctAnswers}" /> correctly 
 (<fmt:dec value="${question.correctAnswers / question.totalAnswers * 100}" />%)</td>
</c:if>
<c:if test="${question.totalAnswers == 0}">
 <td class="data bld">This Question has never been included in a Pilot Examination</td>
</c:if>
</tr>
<c:if test="${question.size > 0}">
<tr>
 <td class="label">Image Information</td>
 <td class="data"><span class="pri bld">${question.typeName}</span> image, <fmt:int value="${question.size}" />
 bytes <span class="sec">(<fmt:int value="${question.width}" /> x <fmt:int value="${question.height}" />
 pixels)</span> <el:link className="pri bld small" url="javascript:void viewImage(${question.width},${question.height})">VIEW IMAGE</el:link></td>
</tr>
</c:if>
</c:if>
<tr>
 <td class="label top">Upload Image</td>
 <td class="data"><el:file name="imgData" idx="*" className="small" size="64" max="192" /><c:if test="${!empty question}"><br />
<el:box name="clearImg" className="small" idx="*" value="true" label="Clear Image Resource" /></c:if></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="active" className="small sec" value="true" checked="${question.active}" label="Question is Available" /></td>
</tr>
<c:if test="${isMC}">
<tr class="title caps">
 <td colspan="2">MULTIPLE CHOICE QUESTION</td>
</tr>
<c:if test="${empty question}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isMultiChoice" idx="*" value="true" label="This is a multiple choice question" checked="true" onChange="void toggleAnswerBox()" /></td>
</tr>
</c:if>
<tr class="mcRow">
 <td class="label top">Answer Choices</td>
 <td class="data"><el:textbox name="answerChoices" idx="*" width="90%" height="5" onBlur="void updateAnswerCombo()">${qChoices}</el:textbox></td>
</tr>
<tr class="mcRow">
 <td class="label">Correct Answer</td>
 <td class="data"><el:combo name="correctChoice" size="1" idx="*" className="req" options="${question.choices}" firstEntry="-" value="${question.correctAnswer}" /></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="SAVE QUESTION" /></td>
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
