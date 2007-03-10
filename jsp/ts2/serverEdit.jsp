<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> TeamSpeak Virtual Server</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.name, 6, 'Virtual Server Name')) return false;
if (!validateNumber(form.port, 1024, 'Virtual Server UDP Port')) return false;
if (!validateNumber(form.maxUsers, 1, 'Virtual Server User Limit')) return false;
if (!validateText(form.msg, 6, 'Virtual Server Welcome Message')) return false;

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
<el:form method="post" action="ts2server.do" op="save" linkID="${fn:dbID(server)}" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="2">TEAMSPEAK 2 VIRTUAL SERVER</td>
</tr>
<tr>
 <td class="label">Server Name</td>
 <td class="data"><el:text name="name" idx="*" className="pri bld req" size="32" max="40" value="${server.name}" /></td>
</tr>
<tr>
 <td class="label">UDP Port</td>
 <td class="data"><el:text name="port" idx="*" className="req" size="4" max="5" value="${server.port}" /></td>
</tr>
<tr>
 <td class="label">Maximum Users</td>
 <td class="data"><el:text name="maxUsers" idx="*" className="req" size="4" max="4" value="${server.maxUsers}" /></td>
</tr>
<tr>
 <td class="label">Welcome Message</td>
 <td class="data"><el:text name="msg" idx="*" size="64" max="80" className="req" value="${server.welcomeMessage}" /></td>
</tr>
<tr>
 <td class="label">Server Password</td>
 <td class="data"><el:text name="pwd" idx="*" size="32" max="80" value="${server.password}" /></td>
</tr>
<tr>
 <td class="label">Description</td>
 <td class="data"><el:text name="desc" idx="*" size="64" max="80" value="${server.description}" /></td>
</tr>
<tr>
 <td class="label" valign="top">Server Options</td>
 <td class="data"><el:box name="active" idx="*" value="true" className="sec" checked="${server.active}" label="Server is Active" /><br />
<el:box name="isACARS" idx="*" value="true" checked="${server.ACARSOnly}" label="Virtual Server is accessible by logged in ACARS users only" /></td>
</tr>
<tr class="title caps">
 <td colspan="2">SECURITY ROLES</td>
</tr>
<tr>
 <td class="label" valign="top">Server Access</td>
 <td class="data"><el:check name="accessRoles" width="115" cols="7" separator="<div style=\"clear:both;\" />" checked="${server.roles['access']}" options="${roles}" /></td>
</tr>
<tr>
 <td class="label" valign="top">Auto-Voice Access</td>
 <td class="data"><el:check name="voxRoles" width="115" cols="7" separator="<div style=\"clear:both;\" />" checked="${server.roles['voice']}" options="${roles}" /></td>
</tr>
<tr>
 <td class="label" valign="top">Server Administration</td>
 <td class="data"><el:check name="adminRoles" width="115" cols="7" separator="<div style=\"clear:both;\" />" checked="${server.roles['admin']}" options="${roles}" /></td>
</tr>
<tr>
 <td class="label" valign="top">Channel Operation</td>
 <td class="data"><el:check name="opRoles" width="115" cols="7" separator="<div style=\"clear:both;\" />" checked="${server.roles['op']}" options="${roles}" /></td>
</tr>
</el:table>
 
<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:button ID="SaveButton" type="SUBMIT" className="BUTTON" label="UPDATE SERVER" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
