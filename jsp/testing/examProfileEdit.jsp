<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<c:set var="examName" value="${empty eProfile ? 'New Examination' : eProfile.name}" scope="request" />
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>${examName}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.examName, 10, 'Examination Name')) return false;
if (!validateNumber(form.stage, 1, 'Examination Stage')) return false;
if (!validateNumber(form.minStage, 0, 'Examination Minimum Stage')) return false;
if (!validateNumber(form.questions, 1, 'Examination Size')) return false;
if (!validateNumber(form.passScore, 0, 'Passing Score')) return false;
if (!validateNumber(form.time, 5, 'Examination Duration')) return false;

setSubmit();
disableButton('SaveButton');
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
<el:form action="eprofile.do" linkID="${eProfile.name}" op="save" method="post" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<!-- Exam Title Bar -->
<tr class="title caps">
 <td colspan="2">EXAMINATION PROFILE - ${examName}</td>
</tr>
<tr>
 <td class="label">Examination Name</td>
 <td class="data"><el:text name="examName" className="pri bld req" idx="*" size="32" max="48" value="${eProfile.name}" /></td>
</tr>
<tr>
 <td class="label">Equipment Program</td>
 <td class="data"><el:combo name="eqType" idx="*" size="1" firstEntry="N/A" options="${eqTypes}" value="${eProfile.equipmentType}" /></td>
</tr>
<tr>
 <td class="label">Stage</td>
 <td class="data"><el:text name="stage" idx="*" size="1" max="1" className="req" value="${eProfile.stage}" /></td>
</tr>
<tr>
 <td class="label">Minimum Stage</td>
 <td class="data"><el:text name="minStage" idx="*" size="1" max="1" value="${eProfile.minStage}" /></td>
</tr>
<tr>
 <td class="label">Questions</td>
 <td class="data"><el:text name="size" idx="*" size="2" max="2" className="req" value="${eProfile.size}" /></td>
</tr>
<tr>
 <td class="label">Passing Score</td>
 <td class="data"><el:text name="passScore" idx="*" size="2" max="2" className="req" value="${eProfile.passScore}" /></td>
</tr>
<tr>
 <td class="label">Testing Time</td>
 <td class="data"><el:text name="time" idx="*" size="2" max="2" className="req" value="${eProfile.time}" /> minutes</td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="active" idx="*" value="true" label="Examination is Active" checked="${eProfile.active}" /><br />
<el:box name="isAcademy" className="sec" idx="*" value="true" label="This is a Fleet Academy Examination" checked="${eProfile.academy}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:button ID="SaveButton" type="SUBMIT" className="BUTTON" label="SAVE EXAMINATION PROFILE" /></td>
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
