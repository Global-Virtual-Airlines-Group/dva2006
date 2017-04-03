<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<c:if test="${!empty template}">
<title>Message Template - ${template.name}</title>
</c:if>
<c:if test="${empty template}">
<title>New Message Template</title>
</c:if>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.name, l:4, t:'Template Name'});
golgotha.form.validate({f:f.subject, l:6, t:'E-Mail Subject'});
golgotha.form.validate({f:f.desc, l:6, t:'Template Description'});
golgotha.form.validate({f:f.body, l:15, t:'E-Mail Text'});
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

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="msgtemplate.do" linkID="${empty template ? null : template.name}" op="save" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<!-- Template Title Bar -->
<tr class="title caps">
 <td colspan="2">E-MAIL MESSAGE TEMPLATE</td>
</tr>

<!-- Message Template Data -->
<tr>
 <td class="label">Template Name</td>
<c:if test="${!empty template}">
 <td class="data pri bld">${template.name}</td>
</c:if>
<c:if test="${empty template}">
 <td class="data"><el:text name="name" className="pri bld req" idx="*" size="20" max="32" value="${template.name}" /></td>
</c:if>
</tr>
<tr>
 <td class="label">E-Mail Subject</td>
 <td class="data"><el:text name="subject" idx="*" size="48" max="64" className="req" value="${template.subject}" /></td>
</tr>
<tr>
 <td class="label">Template Description</td>
 <td class="data"><el:text name="desc" idx="*" size="64" max="128" className="req" value="${template.description}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isHTML" idx="*" value="true" checked="${template.isHTML}" label="Send E-Mail message as HTML" /></td>
</tr>
<tr>
 <td class="label top">Template Text</td>
 <td class="data"><el:textbox name="body" idx="*" width="80%" className="req" height="8" resize="true">${template.body}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>
<el:button ID="SaveButton" type="submit" label="SAVE MESSAGE TEMPLATE" />
<c:if test="${access.canDelete}">
&nbsp;<el:cmdbutton ID="DeleteButton" url="msgtemplatedelete" linkID="${template.name}" label="DELETE MESSAGE TEMPLATE" />
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
