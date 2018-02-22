<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Web Resource</title>
<content:css name="main" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<content:favicon />
<script>
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.url, l:12, t:'Resource URL'});
golgotha.form.validate({f:f.title, l:8, t:'Resource Title'});
golgotha.form.validate({f:f.desc, l:8, t:'Resource Description'});
golgotha.form.validate({f:f.category, t:'Resource Category'});

// Prepend a protocol to the URL
if (f.url.value.indexOf('://') == -1)
	f.url.value = 'http://' + f.url.value;

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
<content:sysdata var="cats" name="airline.resources.categories" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="resource.do" link="${resource}" op="save" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
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
 <td class="data"><el:box name="isPublic" idx="*" value="true" checked="${resource.getPublic()}" label="This is a public Web Resource" /></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="SAVE WEB RESOURCE" />
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
