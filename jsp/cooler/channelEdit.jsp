<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<content:sysdata var="forumName" name="airline.forum" />
<html lang="en">
<head>
<title><content:airline /> ${forumName} Channel Administration</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.newName, l:5, t:'Channel Name'});
golgotha.form.validate({f:f.desc, l:15, t:'Channel Description'});
golgotha.form.validate({f:f.airline, min:1, t:'Airline'});
golgotha.form.submit(f);
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/cooler/header.jspf" %> 
<%@ include file="/jsp/cooler/sideMenu.jspf" %>
<content:sysdata var="airlines" name="apps" mapValues="true" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="chprofile.do" method="post" linkID="${channel.name}" op="save" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
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
 <td class="label top">Notify Roles</td>
 <td class="data"><el:check name="notifyRoles" width="115" cols="6" className="small" newLine="true" checked="${channel.notifyRoles}" options="${roles}" /></td>
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
 <td><el:button ID="SaveButton" type="submit" label="SAVE CHANNEL PROFILE" />
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
