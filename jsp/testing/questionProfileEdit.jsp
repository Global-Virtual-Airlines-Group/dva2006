<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Examination Question Profile</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.question, 20, 'Question Text')) return false;
if (!validateText(form.correct, 3, 'Correct Answer to this Question')) return false;
if (!validateFile(form.imgData, 'gif,jpg,png', 'Image Resource')) return false;
if ((f.isMultiChoice) && (f.isMultiChoice.checked)) {
	if (!validateCombo(form.correctChoice, 'Correct Answer to this Question')) return false;
}

setSubmit();
disableButton('SaveButton');
return true;
}

function tooggleAnswerBox()
{
var f = document.forms[0];
if (f.isMultiChoice) {
	if (f.correct) f.correct.disabled = f.isMultiChoice.checked;
	f.answerChoices.disabled = (!f.isMultiChoice.checked);
	f.correctChoice.disabled = (!f.isMultiChoice.checked);
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
	f.correctChoice.options[x + 1] = new Option(choices[x], choices[x]);
	if (choices[x] == oldAnswer)
		f.correctChoice.selectedIndex = x + 1;
}

return true;
}
<c:if test="${question.size > 0}">
function viewImage(x, y)
{
var flags = 'height=' + y + ',width=' + x + ',menubar=no,toolbar=no,status=yes,scrollbars=yes';
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

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="qprofile.do" link="${question}" op="save" method="post" allowUpload="true" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<!-- Question Title Bar -->
<tr class="title caps">
 <td colspan="2">EXAMINATION QUESTION PROFILE</td>
</tr>
<tr>
 <td class="label">Question Text</td>
 <td class="data bld"><el:text name="question" idx="*" size="120" className="req" max="255" value="${question.question}" /></td>
</tr>
<c:if test="${(empty question) || !fn:isMultiChoice(question)}">
<tr>
 <td class="label">Correct Answer</td>
 <td class="data"><el:text name="correct" idx="*" size="120" className="req" max="255" value="${question.correctAnswer}" /></td>
</tr>
</c:if>
<tr>
 <td class="label" valign="top">Pilot Examinations</td>
 <td class="data"><el:check name="examNames" idx="*" cols="5" width="160" newLine="true" className="small" checked="${question.examNames}" options="${examNames}" /></td>
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
 pixels) <el:link className="pri bld small" url="javascript:void viewImage(${question.width},${question.height})">VIEW IMAGE</el:link></td>
</tr>
</c:if>
</c:if>
<tr>
 <td class="label">Upload Image</td>
 <td class="data"><el:file name="imgData" idx="*" className="small" size="64" max="192" /><c:if test="${!empty question}"><br />
<el:box name="clearImg" className="small" idx="*" value="true" label="Clear Image Resource" /></c:if></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="active" className="small sec" value="true" checked="${question.active}" label="Question is Available" /></td>
</tr>
<c:if test="${empty question || fn:isMultiChoice(question)}">
<tr class="title caps">
 <td colspan="2">MULTIPLE CHOICE QUESTION</td>
</tr>
<c:if test="${empty question}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isMultiChoice" idx="*" value="true" label="This is a multiple choice question" onChange="void tooggleAnswerBox()" /></td>
</tr>
</c:if>
<tr>
 <td class="label" valign="top">Answer Choices</td>
 <td class="data"><el:textbox name="answerChoices" idx="*" width="90%" height="5" onBlur="void updateAnswerCombo()">${qChoices}</el:textbox></td>
</tr>
<tr>
 <td class="label">Correct Answer</td>
 <td class="data"><el:combo name="correctChoice" size="1" idx="*" options="${question.choices}" firstEntry="-" value="${question.correctAnswer}" /></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:button ID="SaveButton" type="SUBMIT" className="BUTTON" label="SAVE QUESTION" /></td>
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
