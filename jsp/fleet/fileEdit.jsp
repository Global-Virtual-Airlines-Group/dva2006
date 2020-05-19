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
<title><content:airline /> File Library - ${entry.name}</title>
</c:when>
<c:otherwise>
<title>New <content:airline /> File Library Entry</title>
</c:otherwise>
</c:choose>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<script async>
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.title, l:8, t:'File Title'});
golgotha.form.validate({f:f.category, t:'File Category'});
golgotha.form.validate({f:f.desc, l:10, t:'Description'});
golgotha.form.validate({f:f.file, ext:['pdf','exe','zip','xls','doc'], t:'Uploaded File'});
golgotha.form.submit(f);
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="cats" name="airline.files.categories" />
<content:enum var="securityOptions" className="org.deltava.beans.fleet.Security" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="userfile.do" linkID="${entry.fileName}" op="save" method="post" allowUpload="true" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
<c:choose>
<c:when test="${!empty entry}">
 <td colspan="2">FILE LIBRARY - ${entry.name}</td>
</c:when>
<c:otherwise>
 <td colspan="2">NEW FILE LIBRARY ENTRY</td>
</c:otherwise>
</c:choose>
</tr>
<tr>
 <td class="label">File Title</td>
 <td class="data"><el:text name="title" className="pri bld req" idx="*" size="48" max="80" value="${entry.name}" /></td>
</tr>
<tr>
 <td class="label">Category</td>
 <td class="data"><el:combo name="category" idx="*" size="1" className="req" options="${cats}" value="${entry.category}" firstEntry="[ CATEGORY ]" /></td>
</tr>
<tr>
 <td class="label">Description</td>
 <td class="data"><el:textbox name="desc" idx="*" width="80%" height="3" className="req" >${entry.description}</el:textbox></td>
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
<tr>
 <td class="label">Document Security</td>
 <td class="data"><el:combo name="security" idx="*" size="1" required="true" value="${entry.security}" options="${securityOptions}" /></td>
</tr>
<c:if test="${(empty entry) || (entry.size == 0)}">
<tr>
 <td class="label">Update File</td>
 <td class="data"><el:file name="file" className="small" size="96" max="192" /></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SAVE FILE" /></td>
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
