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
<title>Questionnaire - ${applicant.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
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
	a = getElement('A' + qNum);
}

if (!isOK) {
	if (!confirm("You have not answered all Questions. Hit OK to submit.")) return false;
}

setSubmit();
disableButton('SubmitButton');
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
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="qsubmit.do" linkID="0x${exam.ID}" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<!-- Exam Title Bar -->
<tr class="title caps">
 <td colspan="2">INITIAL QUESTIONNAIRE - ${applicant.name}</td>
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
 <td class="data small"><span class="pri bld">${q.typeName}</span> image, <fmt:int value="${q.size}" />
 bytes <span class="sec">(<fmt:int value="${q.width}" /> x <fmt:int value="${q.height}" /> pixels)
 <el:link className="pri bld" url="javascript:void viewImage('${fn:hex(q.ID)}', ${q.width}, ${q.height})">VIEW IMAGE</el:link></td>
</tr>
</c:if>

<!-- Answer# ${qnum} -->
<tr>
 <td class="label" valign="top">Answer #<fmt:int value="${q.number}" /></td>
 <c:if test="${!fn:isMultiChoice(q)}">
 <td class="data"><el:textbox ID="A${q.number}" name="answer${q.number}" className="small" width="90%" height="2">${q.answer}</el:textbox></td>
</c:if>
<c:if test="${fn:isMultiChoice(q)}">
 <td class="data"><el:check ID="A${q.number}" type="radio" name="answer${q.number}" className="small" width="400" cols="1" options="${q.choices}" value="${q.answer}" /></td>
</c:if>
</tr>
</c:forEach>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td>&nbsp;
<c:if test="${access.canSubmit}">
<el:button ID="SubmitButton" type="SUBMIT" className="BUTTON" label="SUBMIT QUESTIONNAIRE" />
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
