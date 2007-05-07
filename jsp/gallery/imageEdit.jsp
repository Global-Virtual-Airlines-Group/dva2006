<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<c:if test="${empty img}">
<title>New Image Gallery Entry</title>
</c:if>
<c:if test="${!empty img}">
<title><fmt:text value="${img.name}" /></title>
</c:if>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.title, 6, 'Image Title')) return false;
if (!validateText(form.desc, 5, 'Image Description')) return false;
if (!validateText(form.img, 8, 'Attached Image')) return false;
if (!validateFile(form.img, 'jpg,png', 'Attached Image')) return false;

setSubmit();
disableButton('SaveButton');
disableButton('DeleteButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/gallery/header.jspf" %> 
<%@include file="/jsp/gallery/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="imgsave.do" method="post" link="${img}" allowUpload="true" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2" class="left"><content:airline /> IMAGE GALLERY ENTRY</td>
</tr>
<tr>
 <td class="label">Image Title</td>
 <td class="data"><el:text name="title" idx="*" className="pri bld req" size="48" max="96" value="${img.name}" /></td>
</tr>
<tr>
 <td class="label">Created by</td>
 <td class="data"><el:cmd className="pri bld" url="profile" link="${author}">${author.name}</el:cmd>
<c:if test="${!empty img}"> on <fmt:date fmt="d" date="${img.createdOn}" /></c:if></td>
</tr>
<tr>
 <td class="label">Image Description</td>
 <td class="data"><el:text name="desc" idx="*" size="80" max="144" value="${img.description}" className="req" /></td>
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
<el:table className="bar" space="default" pad="default">
<tr class="title">
 <td>
<c:if test="${access.canEdit}">
<el:button ID="SaveButton" type="submit" className="BUTTON" label="SAVE IMAGE" />
</c:if>
<c:if test="${access.canDelete}">
<el:cmdbutton ID="DeleteButton" url="imgdelete" link="${img}" label="DELETE IMAGE" />
</c:if>
 </td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
