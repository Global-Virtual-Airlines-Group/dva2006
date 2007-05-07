<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> TeamSpeak Voice Channel</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.name, 6, 'Channel Name')) return false;
if (!validateNumber(form.maxUsers, 1, 'Channel User Limit')) return false;

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
<el:form method="post" action="ts2channel.do" op="save" link="${channel}" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
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
 <td class="label" valign="top">Channel Description</td>
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
 <td class="label" valign="top">Channel Options</td>
 <td class="data"><el:box name="isModerated" idx="*" value="true" checked="${channel.moderated}" label="Channel is Moderated" /><br />
<el:box name="isDefault" idx="*" value="true" checked="${channel.default}" label="Default Channel for this Virtual Server" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:button ID="SaveButton" type="SUBMIT" className="BUTTON" label="UPDATE CHANNEL" />
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
</body>
</html>
