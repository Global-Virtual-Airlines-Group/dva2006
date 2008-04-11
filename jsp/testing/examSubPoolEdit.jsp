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
<title>${exam.name} Question Sub-Pools</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
var totalQuestions = 0;
if (!document.addPool) {
	for (var x = 1; x <= ${fn:sizeof(exam.pools)}; x++) {
		if (!validateText(eval('form.pName' + x), 3, 'Pool #' + x + ' Name')) return false;
		if (!validateNumber(eval('form.pSize' + x), 1, 'Pool #' + x + ' Size')) return false;
		totalQuestions += parseInt(eval('form.pSize' + x + '.value'));
	}
}

// Ensure that questions add up to total for exam
if (totalQuestions != ${exam.size}) {
	alert('This Examination has ${exam.size} questions. The pools must have ${exam.size} questions total, not ' + totalQuestions + '.');
	return false;
}

setSubmit();
disableButton('SaveButton');
disableButton('AddButton');
return true;
}

function addPool()
{
if (!checkSubmit()) return false;
var f = document.forms[0];
if (!validateText(form.pNameNew, 3, 'New Pool Name')) return false;
if (!validateNumber(form.pSizeNew, 1, 'New Pool Size')) return false;

// Do the submit
document.addPool = true;
f.addPool.value = 'true';
f.submit();
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<!-- Main Body Frame -->
<content:region id="main">
<el:form action="epools.do" linkID="${exam.name}" op="save" method="post" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<!-- Exam Title Bar -->
<tr class="title caps">
 <td colspan="2">EXAMINATION QUESTION SUB-POOLS - ${exam.name} (<fmt:int value="${exam.size}" /> QUESTIONS)</td>
</tr>
<c:forEach var="pool" items="${exam.pools}">
<tr>
 <td class="label" rowspan="2" valign="top">Question Pool #<fmt:int value="${pool.ID}" /></td>
 <td class="data"><el:text name="pName${pool.ID}" idx="*" className="bld req" size="24" max="24" value="${pool.name}" /></td>
</tr>
<tr>
 <td class="data small">Size: <el:text name="pSize${pool.ID}" idx="*" className="small req" size="2" max="3" value="${pool.size}" /> questions,
 <fmt:int value="${pool.poolSize}" /> possible questions</td>
</tr>
</c:forEach>
<tr class="title caps">
 <td colspan="2">ADD NEW QUESTION POOL</td>
</tr>
<tr>
 <td class="label" rowspan="2" valign="top">New Question Pool</td>
 <td class="data"><el:text name="pNameNew" idx="*" className="bld" size="24" max="24" value="" /></td>
</tr>
<tr>
 <td class="data small">Size: <el:text name="pSizeNew" idx="*" className="small" size="2" max="3" value="" /> questions</td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:button ID="SaveButton" type="SUBMIT" className="BUTTON" label="UPDATE QUESTION SUB-POOLS" />
 <el:button ID="AddButton" onClick="void addPool()" className="BUTTON" label="ADD NEW SUB-POOL" /></td>
</tr>
</el:table>
<el:text name="addPool" type="hidden" value="" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
