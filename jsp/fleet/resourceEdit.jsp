<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Web Resource</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.url, 12, 'Resource URL')) return false;
if (!validateText(form.title, 8, 'Resource Title')) return false;
if (!validateText(form.desc, 8, 'Resource Description')) return false;
if (!validateCombo(form.category, 'Resource Category')) return false;

// Prepend a protocol to the URL
var url = form.url;
if (url.value.indexOf('://') == -1)
	url.value = 'http://' + url.value;

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
<content:sysdata var="cats" name="airline.resources.categories" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="resource.do" link="${resource}" op="save" method="post" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="2">${empty resource ? 'NEW ' : ''} <content:airline /> WEB RESOURCE</td>
</tr>
<tr>
 <td class="label">Resource URL</td>
 <td class="data"><el:text name="url" size="80" max="255" idx="*" className="small req" value="${resource.URL}" /></td>
</tr>
<tr>
 <td class="label">Resource Title</td>
 <td class="data"><el:text name="title" size="64" max="128" idx="*" className="req" value="${resource.title}" /></td>
</tr>
<tr>
 <td class="label">Category</td>
 <td class="data"><el:combo name="category" idx="*" size="1" className="req" firstEntry="-" value="${resource.category}" options="${cats}" /></td>
</tr>
<c:if test="${!empty resource}">
<c:set var="author" value="${pilots[resource.authorID]}" scope="page" />
<c:set var="lastUpd" value="${pilots[resource.lastUpdateID]}" scope="page" />
<tr>
 <td class="label">Created on</td>
 <td class="data"><span class="pri bld"><fmt:date fmt="d" date="${resource.createdOn}" /></span> by
 ${author.name} (${author.pilotCode})</td>
</tr>
<tr>
 <td class="label">Last Updated by</td>
 <td class="data">${lastUpd.name} (${lastUpd.pilotCode})</td>
</tr>
<tr>
 <td class="label">Hit Count</td>
 <td class="data bld"><fmt:int value="${resource.hits}" /> hits</td>
</tr>
</c:if>
<tr>
 <td class="label top">Description</td>
 <td class="data"><el:textbox name="desc" idx="*" width="80%" height="4" className="req">${resource.description}</el:textbox></td>
</tr>
<c:if test="${access.canEdit || (empty resource && access.canCreate)}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isPublic" idx="*" value="true" checked="${resource.public}" label="This is a public Web Resource" /></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:button ID="SaveButton" type="SUBMIT" className="BUTTON" label="SAVE WEB RESOURCE" />
<c:if test="${access.canDelete}"> <el:cmdbutton ID="DeleteButton" url="resourcedelete" link="${resource}" label="DELETE WEB RESOURCE" /></c:if></td>
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
