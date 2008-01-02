<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<c:choose>
<c:when test="${!empty entry}">
<title><content:airline /> Document Library - ${entry.name}</title>
</c:when>
<c:otherwise>
<title>New <content:airline /> Document Library Entry</title>
</c:otherwise>
</c:choose>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.title, 10, 'Manual Title')) return false;
if (!validateNumber(form.version, 1, 'Revision Number')) return false;
if (!validateText(form.desc, 10, 'Description')) return false;
if (!validateFile(form.file, 'pdf', 'Uploaded Manual')) return false;

setSubmit();
disableButton('SaveButton');
disableButton('DeleteButton');
return true;
}

function updateSecurity(updatedField)
{
var f = document.forms[0];
if ((!f.showRegister) || (!f.showRegister.checked))
	return true;

// Check to ensure security fields match
if ((updatedField == f.showRegister) && (f.security.selectedIndex > 0))
{
	alert('This Manual has been made available to all users, to show on the Registration page.');
	f.security.selectedIndex = 0;
} else if ((updatedField == f.security) && (f.security.selectedIndex > 0) && (f.showRegister.checked)) {
	alert('This Manual will no longer show on the Registration page.');
	f.showRegister.checked = false;
} 

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
<el:form action="doclib.do" linkID="${entry.fileName}" op="save" method="post" allowUpload="true" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
<c:choose>
<c:when test="${!empty entry}">
 <td colspan="2">DOCUMENT LIBRARY - ${entry.name}</td>
</c:when>
<c:otherwise>
 <td colspan="2">NEW DOCUMENT LIBRARY ENTRY</td>
</c:otherwise>
</c:choose>
</tr>
<tr>
 <td class="label">Document Title</td>
 <td class="data"><el:text name="title" className="pri bld req" idx="*" size="48" max="80" value="${entry.name}" /></td>
</tr>
<tr>
 <td class="label">Version Number</td>
 <td class="data"><el:text name="version" idx="*" size="1" max="2" className="req" value="${entry.version}" /></td>
</tr>
<tr>
 <td class="label" valign="top">Description</td>
 <td class="data"><el:textbox name="desc" idx="*" width="80%" height="3" className="req">${entry.description}</el:textbox></td>
</tr>
<c:if test="${!empty entry}">
<tr>
 <td class="label">Document Size</td>
<c:if test="${entry.size > 0}">
 <td class="data sec bld"><fmt:int value="${entry.size}" /> bytes</td>
</c:if>
<c:if test="${entry.size == 0}">
 <td class="data warning bld caps">FILE NOT PRESENT ON FILESYSTEM</td>
</c:if>
</tr>
<tr>
 <td class="label">Download Statistics</td>
 <td class="data">Downloaded <b><fmt:int value="${entry.downloadCount}" /></b> times</td>
</tr>
</c:if>
<content:filter roles="Instructor,HR">
<c:if test="${!empty certs}">
<tr>
 <td class="label" valign="top">Flight Academy Certifications</td>
 <td class="data"><el:check name="certNames" width="150" cols="4" className="small" newLine="true" checked="${entry.certifications}" options="${certs}" /></td>
</tr>
</c:if>
</content:filter>
<tr>
 <td class="label">Document Security</td>
 <td class="data"><el:combo name="security" idx="*" size="1" value="${fn:get(securityOptions, entry.security)}" onChange="void updateSecurity(this)" options="${securityOptions}" /></td>
</tr>
<tr>
 <td class="label">Update File</td>
 <td class="data"><el:file name="file" className="small req" size="96" max="192" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="noNotify" idx="*" value="true" label="Don't send notification e-mail" /><br />
<el:box name="showRegister" idx="*" value="true" checked="${entry.showOnRegister}" onChange="void updateSecurity(this)" label="Show Manual on Pilot Registration page" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td>&nbsp;
<c:if test="${access.canEdit || access.canCreate}">
<el:button ID="SaveButton" type="SUBMIT" className="BUTTON" label="SAVE MANUAL" />&nbsp;
</c:if>
<c:if test="${access.canDelete}">
<el:cmdbutton ID="DeleteButton" url="manualdelete" linkID="${entry.fileName}" label="DELETE MANUAL" />
</c:if>
 </td>
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
