<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> TeamSpeak 2 Voice Channel</title>
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
golgotha.form.validate({f:f.server, t:'TeamSpeak Server'});
golgotha.form.validate({f:f.codec, t:'Channel Bandwidth'});
golgotha.form.validate({f:f.name, l:6, t:'Channel Name'});
golgotha.form.validate({f:f.maxUsers, min:1, t:'Channel User Limit'});
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
<el:form method="post" action="ts2channel.do" op="save" link="${channel}" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title">
 <td class="caps" colspan="2">TEAMSPEAK 2 VOICE CHANNEL</td>
</tr>
<tr>
 <td class="label">Virtual Server</td>
 <td class="data"><el:combo name="server" idx="*" size="1" value="${server}" options="${servers}" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label">Channel Name</td>
 <td class="data"><el:text name="name" idx="*" className="pri bld req" size="26" max="40" value="${channel.name}" /></td>
</tr>
<tr>
 <td class="label">Channel Topic</td>
 <td class="data"><el:text name="topic" idx="*" size="24" max="40" value="${channel.topic}" /></td>
</tr>
<tr>
 <td class="label top">Channel Description</td>
 <td class="data"><el:textbox name="desc" idx="*" width="120" height="4">${channel.description}</el:textbox></td>
</tr>
<tr>
 <td class="label">Channel Password</td>
 <td class="data"><el:text name="pwd" idx="*" size="24" max="80" value="${channel.password}" /></td>
</tr>
<tr>
 <td class="label">Maximum Users</td>
 <td class="data"><el:text name="maxUsers" idx="*" size="3" max="4" className="req" value="${channel.maxUsers}" /></td>
</tr>
<tr>
 <td class="label">Channel Codec</td>
 <td class="data"><el:combo name="codec" idx="*" size="1" value="${channel.codec}" options="${codecs}" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label top">Channel Options</td>
 <td class="data"><el:box name="isModerated" idx="*" value="true" checked="${channel.moderated}" label="Channel is Moderated" /><br />
<el:box name="isDefault" idx="*" value="true" checked="${channel.getDefault()}" label="Default Channel for this Virtual Server" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="UPDATE CHANNEL" />
<c:if test="${!empty channel}">
 <el:cmdbutton ID="DeleteButton" url="ts2channeldelete" link="${channel}" label="DELETE CHANNEL" />
</c:if></td>
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
