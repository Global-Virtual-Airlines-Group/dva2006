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
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
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

setSubmit();
disableButton('SubmitButton');
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
<el:form method="POST" action="qsubmit.do" linkID="0x${exam.ID}" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<!-- Exam Title Bar -->
<tr class="title caps">
 <td colspan="2">INITIAL QUESTIONNAIRE - ${applicant.name}</td>
</tr>

<!-- Exam Questions -->
<c:forEach var="q" items="${exam.questions}">
<!-- Question #${q.number} -->
<tr>
 <td class="label">Question #<fmt:int value="${q.number}" /></td>
 <td class="data">${q.question}</td>
</tr>

<!-- Answer# ${qnum} -->
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
<el:button ID="SubmitButton" type="SUBMIT" className="BUTTON" label="SUBMIT QUESTIONNAIRE" />
</c:if>
 </td>
</tr>
</el:table>
</el:form>
<content:copyright />
</div>
</body>
</html>
