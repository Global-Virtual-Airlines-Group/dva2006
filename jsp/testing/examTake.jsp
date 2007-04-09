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
<title>${exam.name} - ${pilot.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
var secondsLeft = ${empty timeRemaining ? 2400 : timeRemaining};

function validate(form)
{
if (!checkSubmit()) return false;

// Check if all questions were answered
var isOK = true;
var qNum = 1;
var a = getElementsById('A' + qNum);
while (isOK && (a.length > 0)) {
	if (a.length == 1) {
		isOK = (isOK && (a[0].value.length > 1));
	} else {
		var checkCount = 0;
		for (var x = 0; x < a.length; x++) {
			if (a[x].checked)
				checkCount++;
		}
		
		isOK = (isOK && (checkCount > 0));
	}
	
	qNum++;
	a = getElementsById('A' + qNum);
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
if (secondsLeft < 300)
	tr.className = 'error bld';
else if (secondsLeft < 600)
	tr.className = 'warn bld';

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

function saveAnswer(qNum, id)
{
var txtbox = getElementsById('A' + qNum);
if (!txtbox) return false;

// Create the AJAX request
var xmlreq = getXMLHttpRequest();
xmlreq.open('post', 'answer.ws?id=' + id + '&q=' + qNum);
xmlreq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');

// Save the answer
if ((txtbox.length == 1) && (txtbox[0].value.length > 1))
	xmlreq.send('answer=' + txtbox[0].value);
else if (txtbox.length > 1) {
	for (var x = 0; x < txtbox.length; x++) {
		if (txtbox[x].checked) {
			xmlreq.send('answer=' + txtbox[x].value);
			break;
		}	
	}
}

return true;
}
<c:if test="${hasQImages}">
function viewImage(id, x, y)
{
var flags = 'height=' + y + ',width=' + x + ',menubar=no,toolbar=no,status=yes,scrollbars=yes';
var w = window.open('/exam_rsrc/' + id, 'questionImage', flags);
return true;
}
</c:if>
</script>
</head>
<content:copyright visible="false" />
<body onload="void showRemaining(30)">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
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
<c:set var="hasImage" value="${q.size > 0}" scope="request"/>
<!-- Question #${q.number} -->
<tr>
 <td class="label" rowspan="${hasImage ? '2' : '1'}" valign="top">Question #<fmt:int value="${q.number}" /></td>
 <td class="data">${q.question}</td>
</tr>
<c:if test="${hasImage}">
<tr>
 <td class="data small">RESOURCE - <span class="pri bld">${q.typeName}</span> image, <fmt:int value="${q.size}" />
 bytes <span class="sec">(<fmt:int value="${q.width}" /> x <fmt:int value="${q.height}" /> pixels)
 <el:link className="pri bld" url="javascript:void viewImage('${fn:hex(q.ID)}', ${q.width}, ${q.height})">VIEW IMAGE</el:link></td>
</tr>
</c:if>

<!-- Answer# ${q.number} -->
<tr>
 <td class="label" valign="top">Answer #<fmt:int value="${q.number}" /></td>
<c:if test="${!fn:isMultiChoice(q)}">
 <td class="data"><el:textbox ID="A${q.number}" onBlur="void saveAnswer(${q.number}, ${fn:hex(exam.ID)})" name="answer${q.number}" className="small" width="90%" height="2">${q.answer}</el:textbox></td>
</c:if>
<c:if test="${fn:isMultiChoice(q)}">
 <td class="data"><el:check ID="A${q.number}" onChange="void saveAnswer(${q.number}, ${fn:hex(exam.ID)})" type="radio" name="answer${q.number}" className="small" width="400" cols="1" options="${q.choices}" value="${q.answer}" /></td>
</c:if>
</tr>
</c:forEach>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td>&nbsp;
<c:if test="${access.canSubmit}">
<el:button ID="SubmitButton" type="SUBMIT" className="BUTTON" label="SUBMIT EXAMINATION" />
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
