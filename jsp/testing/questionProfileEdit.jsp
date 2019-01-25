<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<c:set var="isMC" value="${empty question || fn:isMultiChoice(question)}" scope="page" />
<html lang="en">
<head>
<title>Examination Question Profile</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<content:js name="examTake" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script>
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.question, l:20, t:'Question Text'});
golgotha.form.validate({f:f.correct, l:3, t:'Correct Answer to this Question'});
golgotha.form.validate({f:f.imgData, ext:['gif','jpg','png'], t:'Image Resource', empty:true});
golgotha.form.validate({f:f.owner, t:'Owner'});
golgotha.form.validate({f:f.airline, min:1, t:'Airline'});

// Validate multiple choice
<c:if test="${empty question}">
if ((f.isMultiChoice) && (f.isMultiChoice.checked))
	golgotha.form.validate({f:f.correctChoice, t:'Correct Answer to this Question'});</c:if>
<c:if test="${!empty question && isMC}">
golgotha.form.validate({f:f.correctChoice, t:'Correct Answer to this Question'});</c:if>
golgotha.form.submit(f);
return true;
};

golgotha.local.toggleAnswerBox = function()
{
var aRow = document.getElementById('answerRow');
var mcRows = golgotha.util.getElementsByClass('mcRow', 'tr');
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
};

golgotha.local.updateAnswerCombo = function()
{
var f = document.forms[0];
if ((!f.answerChoices) || (!f.correctChoice)) return false;
var oldAnswer = golgotha.form.getCombo(f.correctChoice);

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
};
</script>
</head>
<content:copyright visible="false" />
<body onload="golgotha.local.updateAnswerCombo(); golgotha.local.toggleAnswerBox()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="airlines" name="apps" mapValues="true" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="qprofile.do" link="${question}" op="save" method="post" allowUpload="true" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<!-- Question Title Bar -->
<tr class="title caps">
 <td colspan="2">EXAMINATION QUESTION PROFILE</td>
</tr>
<tr>
 <td class="label top">Question Text</td>
 <td class="data bld"><el:textbox name="question" idx="*" width="80%" height="3" className="req">${question.question}</el:textbox></td>
</tr>
<c:if test="${!isMC || (empty question)}">
<tr id="answerRow">
 <td class="label top">Correct Answer</td>
 <td class="data"><el:textbox name="correct" idx="*" width="80%" height="3" className="req">${question.correctAnswer}</el:textbox></td>
</tr>
</c:if>
<tr>
 <td class="label top">Reference</td>
 <td class="data"><el:textbox name="reference" idx="*" width="80%" height="2">${question.reference}</el:textbox></td>
</tr>
<tr>
 <td class="label">Owner Airline</td>
 <td class="data"><el:combo name="owner" idx="*" size="1" className="req" firstEntry="-" options="${airlines}" value="${question.owner}" /></td>
</tr>
<tr>
 <td class="label">Airlines</td>
 <td class="data"><el:check name="airlines" width="175" options="${airlines}" className="req" checked="${question.airlines}" /></td>
</tr>
<tr>
 <td class="label top">Pilot Examinations</td>
 <td class="data"><el:check name="examNames" idx="*" cols="5" width="225" newLine="true" className="small" checked="${question.exams}" options="${examNames}" />
<c:if test="${!empty otherExamNames}" >
<br /><br /><hr />
<span class="ita">This Examination Question is also included in the following Examinations managed by other Airlines:</span><br /><br />
<span class="ita"><fmt:list value="${otherExamNames}" delim=", " /></span></c:if></td>
</tr>
<c:if test="${!empty question}">
<tr>
 <td class="label">Statistics</td>
<c:if test="${question.totalAnswers > 0}">
 <td class="data">Answered <fmt:int value="${question.totalAnswers}" /> times, <fmt:int value="${question.correctAnswers}" /> correctly (<fmt:dec value="${question.correctAnswers / question.totalAnswers * 100}" />%)</td>
</c:if>
<c:if test="${question.totalAnswers == 0}">
 <td class="data bld">This Question has never been included in a Pilot Examination</td>
</c:if>
</tr>
<c:if test="${question.size > 0}">
<tr>
 <td class="label">Image Information</td>
 <td class="data"><span class="pri bld">${question.typeName}</span> image, <fmt:int value="${question.size}" /> bytes <span class="sec">(<fmt:int value="${question.width}" /> x <fmt:int value="${question.height}" />
 pixels)</span> <el:link className="pri bld small" url="javascript:void golgotha.exam.viewImage(${question.width},${question.height})">VIEW IMAGE</el:link></td>
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
 <td class="data"><el:box name="isMultiChoice" idx="*" value="true" label="This is a multiple choice question" checked="true" onChange="void golgotha.local.toggleAnswerBox()" /></td>
</tr>
</c:if>
<tr class="mcRow">
 <td class="label top">Answer Choices</td>
 <td class="data"><el:textbox name="answerChoices" idx="*" width="90%" height="5" onBlur="void golgotha.local.updateAnswerCombo()">${qChoices}</el:textbox></td>
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
