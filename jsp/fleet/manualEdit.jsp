<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
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
<el:form action="dlibsave.do" linkID="${entry.fileName}" op="save" method="POST" allowUpload="true" validate="return validate(this)">
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
 <td class="data"><el:text name="title" className="pri bld" idx="*" size="48" max="80" value="${entry.name}" /></td>
</tr>
<tr>
 <td class="label">Version Number</td>
 <td class="data"><el:text name="version" idx="*" size="1" max="2" value="${entry.version}" /></td>
</tr>
<tr>
 <td class="label">Description</td>
 <td class="data"><el:textbox name="desc" idx="*" width="120" height="3">${entry.description}</el:textbox></td>
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
</c:if>
<tr>
 <td class="label">Download Statistics</td>
 <td class="data">Downloaded <b><fmt:int value="${entry.downloadCount}" /></b> times</td>
</tr>
<tr>
 <td class="label">Document Security</td>
 <td class="data"><el:combo name="security" idx="*" size="1" value="${fn:get(securityOptions, entry.security)}" options="${securityOptions}" /></td>
</tr>
<tr>
 <td class="label">Update File</td>
 <td class="data"><el:file name="file" className="small" size="96" max="192" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td>&nbsp;
<c:if test="${access.canEdit || access.canCreate}">
<el:button ID="SaveButton" type="SUBMIT" className="BUTTON" label="SAVE MANUAL" />
</c:if>
 </td>
</tr>
</el:table>
</el:form>
<content:copyright />
</div>
</body>
</html>
