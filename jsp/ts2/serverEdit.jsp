<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> TeamSpeak 2 Virtual Server</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script>
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.name, l:6, t:'Virtual Server Name'});
golgotha.form.validate({f:f.port, min:1024, t:'Virtual Server UDP Port'});
golgotha.form.validate({f:f.maxUsers, min:1, t:'Virtual Server User Limit'});
golgotha.form.validate({f:f.msg, l:6, t:'Virtual Server Welcome Message'});
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
<el:form method="post" action="ts2server.do" op="save" link="${server}" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
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
 <td class="label top">Server Options</td>
 <td class="data"><el:box name="active" idx="*" value="true" className="sec" checked="${server.active}" label="Server is Active" /><br />
<el:box name="isACARS" idx="*" value="true" checked="${server.ACARSOnly}" label="Virtual Server is accessible by logged in ACARS users only" /></td>
</tr>
<tr class="title caps">
 <td colspan="2">SECURITY ROLES</td>
</tr>
<tr>
 <td class="label top">Server Access</td>
 <td class="data"><el:check name="accessRoles" width="115" cols="7" newLine="true" checked="${server.roles['access']}" options="${roles}" /></td>
</tr>
<tr>
 <td class="label top">Auto-Voice Access</td>
 <td class="data"><el:check name="voxRoles" width="115" cols="7" newLine="true" checked="${server.roles['voice']}" options="${roles}" /></td>
</tr>
<tr>
 <td class="label top">Server Administration</td>
 <td class="data"><el:check name="adminRoles" width="115" cols="7" newLine="true" checked="${server.roles['admin']}" options="${roles}" /></td>
</tr>
<tr>
 <td class="label top">Channel Operation</td>
 <td class="data"><el:check name="opRoles" width="115" cols="7" newLine="true" checked="${server.roles['op']}" options="${roles}" /></td>
</tr>
</el:table>
 
<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="UPDATE SERVER" /></td>
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
