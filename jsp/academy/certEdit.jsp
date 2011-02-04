<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<c:set var="certName" value="${empty cert ? 'New Certification' : cert.name}" scope="page" />
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Flight Academy - ${certName}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script type="text/javascript">
function showReqCert(combo)
{
var opt = combo.options[combo.selectedIndex];
displayObject(getElement('reqCertRow'), (opt.text == 'Specific Certification'));
return true;	
}

function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.name, 10, 'Certification Name')) return false;
if (!validateNumber(form.stage, 1, 'Certification Stage')) return false;
if (!validateCombo(form.preReqs, 'Examination Prerequisites')) return false;

// Check specific cert
var reqCertRow = getElement('reqCertRow');
if (reqCertRow.style.display != 'none') {
	if (!validateCombo(form.preReqCert, 'Specific Certification Prerequisite')) return false;
}

setSubmit();
disableButton('SaveButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body onload="showReqCert(document.forms[0].preReqs)">
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>
<content:sysdata var="roles" name="security.roles" />
<content:sysdata var="airlines" name="apps" mapValues="true" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="cert.do" linkID="${cert.name}" op="save" method="post" validate="return validate(this)">
<el:table className="form">
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
 <td class="data"><el:combo name="preReqs" className="req" idx="*" size="1" value="${cert.reqName}" options="${preReqNames}" onChange="void showReqCert(this)" firstEntry="-" /></td>
</tr>
<tr id="reqCertRow" style="display:none;">
 <td class="label">Certification</td>
 <td class="data"><el:combo name="reqCert" className="req" size="1" value="${cert.reqCert}" options="${allCerts}" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label top">Enrollment Roles</td>
 <td class="data"><span class="ita">Select any Security Roles required for this Certification to be available for enrollment by a particular Pilot.</span><br />
<el:check name="enrollRoles" width="115" cols="7" newLine="true" checked="${cert.roles}" options="${roles}" /></td>
</tr>
<tr>
 <td class="label">Airlines</td>
 <td class="data"><el:check name="airlines" width="175" options="${airlines}" className="req" checked="${cert.airlines}" /></td>
</tr>
<tr>
 <td class="label top">Required Examinations</td>
 <td class="data"><el:check name="reqExams" width="220" cols="4" className="small" newLine="true" checked="${cert.examNames}" options="${exams}" /></td>
</tr>
<tr>
 <td class="label">Check Ride</td>
 <td class="data"><el:box name="hasCR" idx="*" value="true" className="sec bld" label="This Certification requires a Check Ride" checked="${cert.hasCheckRide}" /></td>
</tr>
<tr id="noScriptWarn"<c:if test="${!noScriptWarn}">style="display:none;"</c:if>>
 <td class="label">&nbsp;</td>
 <td class="data"><span class="error bld caps">This Certification requires a Check Ride, but no Check Ride Script exists</span></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isActive" idx="*" value="true" label="Certification is Active" checked="${cert.active}" /><br />
<el:box name="autoEnroll" idx="*" value="true" label="Auto-Enroll students in Course" checked="${cert.autoEnroll}" /></td>
</tr>
<tr>
 <td class="label top">Instructions</td>
 <td class="data"><el:textbox name="desc" idx="*" width="90%" height="5" resize="true">${cert.description}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr class="title">
 <td><el:button ID="SaveButton" type="submit" label="SAVE CERTIFICATION PROFILE" /></td>
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
