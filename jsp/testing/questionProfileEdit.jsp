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

// Copy each line in the textbox to an answer choice
var choices = f.answerChoices.value.split('\n');
f.correctChoice.options.length = 1;
f.correctChoice.options.length = choices.length + 1;
for (var x = 0; x < choices.length; x++)
	f.correctChoice.options[x + 1] = new Option(choices[x], choices[x]);

return true;
}
</script>
</head>
<content:copyright visible="false" />
<body onload="updateAnswerCombo()">
<content:page>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="qprofile.do" linkID="${fn:dbID(question)}" op="save" method="post" validate="return validate(this)">
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
 <td class="data"><el:check name="examNames" idx="*" cols="5" width="160" separator="<div style=\"clear:both;\" />" className="small" checked="${question.examNames}" options="${examNames}" /></td>
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
</c:if>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="active" className="sec" value="true" checked="${question.active}" label="Question is Available" /></td>
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
 <td class="data"><el:textbox name="answerChoices" idx="*" width="120" height="5" onBlur="void updateAnswerCombo()">${qChoices}</el:textbox></td>
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
</body>
</html>
