<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Help Desk Response Template</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.subject, 5, 'Template Title')) return false;
if (!validateText(form.body, 5, 'Template Reseponse')) return false;

setSubmit();
disableButton('SaveButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/help/header.jspf" %> 
<%@ include file="/jsp/help/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="rsptemplate.do" op="save" linkID="${template.title}" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title">
 <td class="caps" colspan="2"><content:airline /> HELP DESK RESPONSE TEMPLATE</td>
</tr>
<tr>
 <td class="label">Template Name</td>
 <td class="data"><el:text name="title" idx="*" size="32" max="48" className="pri bld req" value="${template.title}" /></td>
</tr>
<tr>
 <td class="label top">Template Body</td>
 <td class="data"><el:textbox name="body" idx="*" className="req" width="80%" height="6">${template.body}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:button ID="SaveButton" className="BUTTON" type="submit" label="UPDATE RESPONSE TEMPLATE" /></td>
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
