<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Online Help</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.id, 4, 'Help Entry Title')) return false;
if (!validateText(form.subject, 8, 'Help Entry Subject')) return false;
if (!validateText(form.body, 14, 'Help Entry Text')) return false;

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

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="help.do" method="post" op="save" linkID="${help.title}" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> ONLINE HELP</td>
</tr>
<tr>
 <td class="label">Help Entry Title</td>
 <td class="data"><el:text name="id" idx="*" className="pri bld req" size="48" max="48" value="${help.title}" /></td>
</tr>
<tr>
 <td class="label">Help Entry Subject</td>
 <td class="data"><el:text name="subject" idx="*" className="req" size="64" max="144" value="${help.subject}" /></td>
</tr>
<tr>
 <td class="label" valign="top">Help Entry Text</td>
 <td class="data"><el:textbox name="body" idx="*" className="small req" width="80%" height="8">${help.body}</el:textbox></td>
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
