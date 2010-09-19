<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<content:sysdata var="forumName" name="airline.forum" />
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> ${forumName} Channel Administration</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.newName, 5, 'Channel Name')) return false;
if (!validateText(form.desc, 15, 'Channel Description')) return false;
if (!validateCheckBox(form.airline, 1, 'Airline')) return false;

setSubmit();
disableButton('SaveButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/cooler/header.jspf" %> 
<%@ include file="/jsp/cooler/sideMenu.jspf" %>
<content:sysdata var="roles" name="security.roles" />
<content:sysdata var="airlines" name="apps" mapValues="true" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="chprofile.do" method="post" linkID="${channel.name}" op="save" validate="return validate(this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">WATER COOLER CHANNEL PROFILE</td>
</tr>
<tr>
 <td class="label">Channel Name</td>
 <td class="data"><el:text name="newName" idx="*" className="pri bld req" size="36" max="64" value="${channel.name}" /></td>
</tr>
<tr>
 <td class="label">Description</td>
 <td class="data"><el:text name="desc" idx="*" size="80" max="144" className="req" value="${channel.description}" /></td>
</tr>
<tr>
 <td class="label">Web Applications</td>
 <td class="data"><el:check name="airline" width="175" options="${airlines}" className="req" checked="${channel.airlines}" /></td>
</tr>
<tr>
 <td class="label top">Read-Access Roles</td>
 <td class="data"><el:check name="readRoles" width="115" cols="6" className="small" newLine="true" checked="${channel.readRoles}" options="${roles}" /></td>
</tr>
<tr>
 <td class="label top">Post-Access Roles</td>
 <td class="data"><el:check name="writeRoles" width="115" cols="6" className="small" newLine="true" checked="${channel.writeRoles}" options="${roles}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="active" idx="*" className="sec" value="true" label="Channel is Active" checked="${channel.active}" /><br />
<el:box name="allowNew" idx="*" value="true" label="Allow New Threads and Replies" checked="${channel.allowNewPosts}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" className="BUTTON" type="submit" label="SAVE CHANNEL PROFILE" />
<c:if test="${(!empty channel) && access.canDelete}">
 <el:cmdbutton ID="DeleteButton" url="chdelete" linkID="${channel.name}" label="DELETE CHANNEL" />
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
