<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Video Library - ${(!empty video) ? video.name : 'New Video'}</title>
<content:css name="main" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.title, l:10, t:'Video Title'});
golgotha.form.validate({f:f.baseFile, t:'Video File'});
golgotha.form.validate({f:f.category, t:'Video Category'});
golgotha.form.validate({f:f.desc, l:10, t:'Description'});
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
<content:sysdata var="cats" name="airline.video.categories" />
<content:enum var="securityOptions" className="org.deltava.beans.fleet.Security" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="tvideo.do" linkID="${video.fileName}" op="save" method="post" allowUpload="true" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
<c:choose>
<c:when test="${!empty video}">
 <td colspan="2"><content:airline /> VIDEO LIBRARY - ${video.name}</td>
</c:when>
<c:otherwise>
 <td colspan="2">NEW <content:airline /> VIDEO LIBRARY ENTRY</td>
</c:otherwise>
</c:choose>
</tr>
<c:if test="${empty video}">
<tr>
 <td class="label">Local File</td>
 <td class="data"><el:combo name="baseFile" idx="*" required="true" size="1" options="${availableFiles}" firstEntry="[ VIDEO FILE ]" /></td>
</tr>
</c:if>
<tr>
 <td class="label">Video Title</td>
 <td class="data"><el:text name="title" className="pri bld" required="true" idx="*" size="48" max="80" value="${video.name}" /></td>
</tr>
<tr>
 <td class="label">Category</td>
 <td class="data"><el:combo name="category" idx="*" size="1" required="true" options="${cats}" value="${video.category}" firstEntry="[ CATEGORY ]" /></td>
</tr>
<tr>
 <td class="label top">Description</td>
 <td class="data"><el:textbox name="desc" idx="*" width="80%" height="3" required="true" resize="true">${video.description}</el:textbox></td>
</tr>
<c:if test="${!empty video}">
<tr>
 <td class="label">Video Information</td>
<c:if test="${video.size > 0}">
 <td class="data"><span class="pri bld">${video.type}</span>, <span class="sec bld"><fmt:int value="${video.size}" /> bytes</span></td>
</c:if>
<c:if test="${video.size == 0}">
 <td class="data warning bld caps">FILE NOT PRESENT ON FILESYSTEM</td>
</c:if>
</tr>
<tr>
 <td class="label">Statistics</td>
 <td class="data">Viewed <b><fmt:int value="${video.downloadCount}" /></b> times</td>
</tr>
</c:if>
<content:filter roles="HR,AcademyAdmin">
<tr>
 <td class="label top">Flight Academy Certifications</td>
 <td class="data"><el:check name="certNames" width="185" cols="3" className="small" newLine="true" checked="${video.certifications}" options="${certs}" /></td>
</tr>
</content:filter>
<tr>
 <td class="label">Document Security</td>
 <td class="data"><el:combo name="security" idx="*" size="1" required="true" value="${entry.security}" options="${securityOptions}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="noNotify" idx="*" value="true" label="Don't send notification e-mail" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><c:if test="${access.canEdit || access.canCreate}"><el:button ID="SaveButton" type="submit" label="SAVE VIDEO" />
<c:if test="${!empty video}"> <el:cmdbutton ID="DeleteButton" url="tvdelete" linkID="${video.fileName}" label="DELETE VIDEO" /></c:if></c:if> </td>
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
