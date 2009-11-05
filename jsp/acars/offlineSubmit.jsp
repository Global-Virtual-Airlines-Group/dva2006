<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> ACARS Offline Flight Report Submission</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (form.zip.value.length > 0) {
	if (!validateFile(form.zip, 'zip', 'Offline Flight ZIP data')) return false;
} else {
	if (!validateFile(form.xml, 'xml', 'Offline Flight XML data')) return false;
	if (!validateFile(form.hashCode, 'sha', 'Offline Flight SHA-256 signature data')) return false;
}

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
<content:attr attr="isHR" value="true" roles="HR,Developer" />
<content:superUser><c:set var="isHR" value="true" scope="page" /></content:superUser>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="acarsoffline.do" method="post" allowUpload="true" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> ACARS OFFLINE FLIGHT REPORT SUBMISSION</td>
</tr>
<tr>
 <td class="label">XML File</td>
 <td class="data"><el:file name="xml" className="small" idx="*" size="96" max="144" /></td>
</tr>
<tr>
 <td class="label">SHA File</td>
 <td class="data"><el:file name="hashCode" className="small" idx="*" size="96" max="144" /></td>
</tr>
<tr>
 <td class="label top">ZIP File</td>
 <td class="data"><el:file name="zip" className="small" size="96" max="144" /><br />
<span class="small">You can submit the XML and SHA files in a ZIP archive to reduce upload times.</span></td>
</tr>
<c:if test="${isHR}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="noValidate" idx="*" className="small" value="true" label="Don't validate SHA-256 signature" /></td>
</tr>
</c:if>
<c:if test="${hashFailure}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><div class="error bld">SUBMISSION FAILURE - SHA-256 MISMATCH</div><br />
<i>The content of the <content:airline /> ACARS XML data file that you have submitted does not match the cryptographic 
signature contained in the SHA file.</i></td>
</tr>
</c:if>
<content:hasmsg>
<tr class="title caps">
 <td colspan="2">FLIGHT REPORT SUBMISSION ERROR</td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><span class="error bld"><content:sysmsg /></span>
<c:if test="${!empty error}"><br />
<br />
<pre><fmt:stack exception="${error}" /></pre></c:if></td>
</tr>
</content:hasmsg>
<c:if test="${!empty pirep}">
<tr class="title caps">
 <td colspan="2">FLIGHT REPORT SUBMITTED SUCCESSFULLY</td> 
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data">Your ACARS Offline Flight Report has been submitted. <el:cmd url="pirep" link="${pirep}" className="sec bld">Click Here</el:cmd> to view it.</td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:button ID="SubmitButton" type="SUBMIT" className="BUTTON" label="SUBMIT FLIGHT REPORT" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
