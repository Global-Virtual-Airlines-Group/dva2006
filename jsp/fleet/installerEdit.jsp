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
<title><content:airline /> Fleet Library - ${entry.name}</title>
</c:when>
<c:otherwise>
<title>New <content:airline /> Fleet Library Entry</title>
</c:otherwise>
</c:choose>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.title, 10, 'Installer Title')) return false;
if (!validateNumber(form.majorVersion, 1, 'Major Version Number')) return false;
if (!validateNumber(form.minorVersion, 0, 'Minor Version Number')) return false;
if (!validateNumber(form.subVersion, 0, 'Sub-Version Number')) return false;
if (!validateText(form.desc, 10, 'Description')) return false;
if (!validateText(form.code, 3, 'Installer Code')) return false;
if (!validateText(form.fileName, 8, 'Installer Filename')) return false;

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
<content:sysdata var="allEQ" name="eqtypes" sort="true" />

<!-- Main Body Frame -->
<div id="main">
<el:form action="flibsave.do" linkID="${entry.fileName}" op="save" method="POST" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
<c:choose>
<c:when test="${!empty entry}">
 <td colspan="2">FLEET LIBRARY - ${entry.name}</td>
</c:when>
<c:otherwise>
 <td colspan="2">NEW FLEET LIBRARY ENTRY</td>
</c:otherwise>
</c:choose>
</tr>
<tr>
 <td class="label">Installer Title</td>
 <td class="data"><el:text name="title" className="pri bld" idx="*" size="48" max="80" value="${entry.name}" /></td>
</tr>
<tr>
 <td class="label">Installer Filename</td>
 <td class="data"><el:text name="fileName" idx="*" size="32" max="48" value="${entry.fileName}" /></td>
</tr>
<tr>
 <td class="label">Installer Code</td>
 <td class="data bld"><el:text name="code" idx="*" size="12" max="12" value="${entry.code}" /></td>
</tr>
<tr>
 <td class="label">Fleet Library Image</td>
 <td class="data"><el:text name="img" idx="*" size="18" max="32" value="${entry.image}" /></td>
</tr>
<tr>
 <td class="label">Version Number</td>
 <td class="data"><el:text name="majorVersion" idx="*" size="1" max="2" value="${entry.majorVersion}" />.
<el:text name="minorVersion" idx="*" size="1" max="2" value="${entry.minorVersion}" />.
<el:text name="subVersion" idx="*" size="1" max="2" value="${entry.subVersion}" /></td>
</tr>
<tr>
 <td class="label">Description</td>
 <td class="data"><el:textbox name="desc" idx="*" width="120" height="3">${entry.description}</el:textbox></td>
</tr>
<tr>
 <td class="label">Installer Security</td>
 <td class="data"><el:combo name="security" idx="*" size="1" value="${fn:get(securityOptions, entry.security)}" options="${securityOptions}" /></td>
</tr>
<c:if test="${!empty entry}">
<tr>
 <td class="label">Installer Size</td>
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
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td>&nbsp;
<c:if test="${access.canEdit || access.canCreate}">
<el:button ID="SaveButton" type="SUBMIT" className="BUTTON" label="SAVE INSTALLER" />
</c:if>
 </td>
</tr>
</el:table>
</el:form>
<content:copyright />
</div>
</body>
</html>
