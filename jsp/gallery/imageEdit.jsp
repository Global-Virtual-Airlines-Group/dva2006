<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<c:if test="${empty img}">
<title>New <content:airline /> Image Gallery Entry</title></c:if>
<c:if test="${!empty img}">
<title><fmt:text value="${img.name}" /></title></c:if>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script>
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.title, l:6, t:'Image Title'});
golgotha.form.validate({f:f.desc, l:5, t:'Image Description'});
golgotha.form.validate({f:f.img, l:6, t:'Attached Image'});
golgotha.form.validate({f:f.img, ext:['jpg','png'], t:'Attached Image'});
golgotha.form.submit(f);
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/gallery/header.jspf" %> 
<%@ include file="/jsp/gallery/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="image.do" method="post" op="save" link="${img}" allowUpload="true" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2" class="left"><content:airline /> IMAGE GALLERY ENTRY</td>
</tr>
<tr>
 <td class="label">Image Title</td>
 <td class="data"><el:text name="title" idx="*" className="pri bld" required="true" size="48" max="96" value="${img.name}" /></td>
</tr>
<tr>
 <td class="label">Created by</td>
 <td class="data"><el:cmd className="pri bld" url="profile" link="${author}">${author.name}</el:cmd>
<c:if test="${!empty img}"> on <fmt:date fmt="d" date="${img.createdOn}" /></c:if></td>
</tr>
<tr>
 <td class="label">Image Description</td>
 <td class="data"><el:text name="desc" idx="*" size="80" max="144" value="${img.description}" required="true" /></td>
</tr>
<content:filter roles="Fleet,Gallery">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isFleet" idx="*" value="true" checked="${img.fleet}" label="Include Image in Fleet Gallery" /></td>
</tr>
</content:filter>
<c:if test="${empty img}">
<tr>
 <td class="label">Upload Image</td>
 <td class="data"><el:file name="img" className="small req" idx="*" size="64" max="144" /></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr class="title">
 <td>
&nbsp;<c:if test="${access.canEdit}"><el:button type="submit" label="SAVE IMAGE" /></c:if>
<c:if test="${access.canDelete}">&nbsp;<el:cmdbutton url="imgdelete" link="${img}" label="DELETE IMAGE" /></c:if>
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
