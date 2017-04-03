<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Help Desk Response Template</title>
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
golgotha.form.validate({f:f.subject, l:5, t:'Template Title'});
golgotha.form.validate({f:f.body, l:5, t:'Template Reseponse'});
golgotha.form.submit(f);
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/help/header.jspf" %> 
<%@ include file="/jsp/help/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="rsptemplate.do" op="save" linkID="${template.title}" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title">
 <td class="caps" colspan="2"><content:airline /> HELP DESK RESPONSE TEMPLATE</td>
</tr>
<tr>
 <td class="label">Template Name</td>
 <td class="data"><el:text name="title" idx="*" size="32" max="48" className="pri bld req" value="${template.title}" /></td>
</tr>
<tr>
 <td class="label top">Template Body</td>
 <td class="data"><el:textbox name="body" idx="*" className="req" width="80%" height="4" resize="true">${template.body}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="UPDATE RESPONSE TEMPLATE" />
<c:if test="${(!empty template) && access.canUpdateTemplate}">
 <el:cmdbutton ID="DeleteButton" url="rsptemplatedelete" post="true" linkID="${template.title}" label="DELETE RESPONSE TEMPLATE" /></c:if></td>
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
