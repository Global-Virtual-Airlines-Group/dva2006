<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<c:set var="certName" value="${empty cert ? 'New Certification' : cert.name}" scope="request" />
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>${certName}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.name, 10, 'Certification Name')) return false;
if (!validateNumber(form.stage, 1, 'Certification Stage')) return false;
if (!validateCombo(form.preReqs, 'Examination Prerequisites')) return false;

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
<el:form action="cert.do" linkID="${cert.name}" op="save" method="post" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="2">FLIGHT ACADEMY CERTIFICATION - ${certName}</td>
</tr>
<tr>
 <td class="label">Certification Name</td>
 <td class="data"><el:text name="name" className="pri bld req" idx="*" size="32" max="32" value="${cert.name}" /></td>
</tr>
<tr>
 <td class="label">Code</td>
 <td class="data"><el:text name="code" className="bld req" idx="*" size="4" max="8" value="${cert.code}" /></td>
</tr>
<tr>
 <td class="label">Stage</td>
 <td class="data"><el:text className="bld req" name="stage" idx="*" size="1" max="1" value="${cert.stage}" /></td>
</tr>
<tr>
 <td class="label">Prerequisites</td>
 <td class="data"><el:combo name="preReqs" className="req" idx="*" size="1" value="${cert.reqName}" options="${preReqNames}" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label" valign="top">Required Examinations</td>
 <td class="data"><el:check name="reqExams" width="150" cols="4" className="small" separator="<div style=\"clear:both;\" />" checked="${cert.examNames}" options="${exams}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isActive" idx="*" value="true" label="Certification is Active" checked="${cert.active}" /><br />
<el:box name="autoEnroll" idx="*" value="true" label="Auto-Enroll students in Course" checked="${cert.autoEnroll}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr class="title">
 <td><el:button ID="SaveButton" type="submit" className="BUTTON" label="SAVE CERTIFICATION PROFILE" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
