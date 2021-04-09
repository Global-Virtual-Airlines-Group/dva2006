<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<c:choose>
<c:when test="${!empty entry}">
<title><content:airline /> Document Library - ${entry.name}</title>
</c:when>
<c:otherwise>
<title>New <content:airline /> Document Library Entry</title>
</c:otherwise>
</c:choose>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script>
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.title, l:10, t:'Manual Title'});
golgotha.form.validate({f:f.version, min:1, t:'Revision Number'});
golgotha.form.validate({f:f.desc, l:10, t:'Description'});
golgotha.form.validate({f:f.file, ext:['pdf','xls'], t:'Uploaded Manual', empty:${!empty entry}});
<c:if test="${empty entry}">
const fileParts = f.file.value.split('\\');
const fName = fileParts[fileParts.length - 1].toLowerCase();
if (golgotha.local.manualNames.indexOf(fName) != -1) {
	alert('A Manual named ' + fName + ' already exists.');
	form.file.focus();
	return false;
}
</c:if>
golgotha.form.submit(f);
return true;
};

golgotha.local.updateSecurity = function(updatedField)
{
const f = document.forms[0];
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
};
<c:if test="${empty entry}">
<fmt:jsarray var="golgotha.local.manualNames" items="${manualNames}" /></c:if>
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %>
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:enum var="securityOptions" className="org.deltava.beans.fleet.Security" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="doclib.do" linkID="${entry.fileName}" op="save" method="post" allowUpload="true" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
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
 <td class="label top">Description</td>
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
<content:filter roles="AcademyAdmin,Instructor,HR">
<c:if test="${!empty certs}">
<tr>
 <td class="label top">Flight Academy Certifications</td>
 <td class="data"><el:check name="certNames" width="175" cols="4" className="small" newLine="true" checked="${entry.certifications}" options="${certs}" /></td>
</tr>
<el:text type="hidden" name="hasCerts" value="true" readOnly="true" />
</c:if>
</content:filter>
<tr>
 <td class="label">Document Security</td>
 <td class="data"><el:combo name="security" idx="*" size="1" required="true" value="${entry.security}" onChange="void golgotha.local.updateSecurity(this)" options="${securityOptions}" /></td>
</tr>
<tr>
 <td class="label">Update File</td>
 <td class="data"><el:file name="file" className="small req" size="96" max="192" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="noNotify" idx="*" value="true" checked="true" label="Don't send notification e-mail" /><br />
<el:box name="showRegister" idx="*" value="true" checked="${entry.showOnRegister}" onChange="void golgotha.local.updateSecurity(this)" label="Show Manual on Pilot Registration page" /><br />
<el:box name="ignoreCerts" idx="*" value="true" checked="${entry.ignoreCertifications}" label="Make visible to Pilots not enrolled in Flight Academy" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>&nbsp;
<c:if test="${access.canEdit || access.canCreate}"><el:button type="submit" label="SAVE MANUAL" />&nbsp;</c:if>
<c:if test="${access.canDelete}"><el:cmdbutton url="manualdelete" linkID="${entry.fileName}" label="DELETE MANUAL" /></c:if> </td>
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
