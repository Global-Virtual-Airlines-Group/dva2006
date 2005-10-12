<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<%@ page import="org.deltava.beans.testing.Test" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>${exam.name} - ${pilot.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
var secondsLeft = ${empty timeRemaining ? 40000 : timeRemaining};

function validate(form)
{
if (!checkSubmit()) return false;

// Check if all questions were answered
isOK = true;
qNum = 1;
var a = getElement('A' + qNum);
while (isOK && (a != null)) {
	isOK = (isOK && (a.value.length > 2));
	qNum++;
	a = getElement('A' + qNum);
}

if ((!isOK) && (!document.isExpired)) {
	if (!confirm("You have not answered all Questions. Hit OK to submit.")) return false;
}

setSubmit();
disableButton('SubmitButton');
return true;
}

function showRemaining(interval)
{
var tr = getElement('timeRemaining');

// Update the text color
if (secondsLeft < 600) {
	tr.className = 'warn bld';
} else if (secondsLeft < 300) {
	tr.className = 'error bld';
}

// Display the text and decrement the counter
tr.innerHTML = Math.round(secondsLeft / 60) + ' minutes';
secondsLeft -= interval;

// If we're out of time, set a flag and submit
if (secondsLeft <= 0) {
	document.isExpired = true;
	document.forms[0].submit();
	return true;
}

// Fire this off again
window.setTimeout('void showRemaining(' + interval + ')', interval * 1000);
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<el:form method="post" action="examsubmit.do" linkID="0x${exam.ID}" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<!-- Exam Title Bar -->
<tr class="title caps">
 <td colspan="2">${exam.name} EXAMINATION - ${pilot.name}</td>
</tr>
<tr>
 <td class="label">Taken on</td>
 <td class="data"><fmt:date date="${exam.date}" /></td>
</tr>
<tr>
 <td class="label">Time Remaining</td>
 <td class="data"><span id="timeRemaining" class="ter bld">XX minutes</span></td>
</tr>

<!-- Exam Questions -->
<c:forEach var="q" items="${exam.questions}">
<!-- Question #${q.number} -->
<tr>
 <td class="label">Question #<fmt:int value="${q.number}" /></td>
 <td class="data">${q.question}</td>
</tr>

<!-- Answer# ${q.number} -->
<tr>
 <td class="label" valign="top">Answer #<fmt:int value="${q.number}" /></td>
 <td class="data"><el:textbox ID="A${q.number}" name="answer${q.number}" className="small" width="120" height="2">${q.answer}</el:textbox></td>
</tr>
</c:forEach>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td>&nbsp;
<c:if test="${access.canSubmit}">
<el:button ID="SubmitButton" type="SUBMIT" className="BUTTON" label="SUBMIT EXAMINATION" /></td>
</c:if>
 </td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</div>
</body>
</html>
