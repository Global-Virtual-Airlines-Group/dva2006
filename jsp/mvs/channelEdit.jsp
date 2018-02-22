<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> MVS Voice Channel</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.rate, t:'Channel Bandwidth'});
golgotha.form.validate({f:f.name, l:6, t:'Channel Name'});
golgotha.form.validate({f:f.desc, l:3, t:'Channel Description'});
golgotha.form.validate({f:f.maxUsers, min:0, t:'Channel User Limit'});
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
<content:sysdata var="roles" name="security.roles" />
<content:sysdata var="airlines" name="apps" mapValues="true" />
<content:enum var="sampleRates" className="org.deltava.beans.mvs.SampleRate" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="mvschannel.do" op="save" link="${channel}" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title">
 <td class="caps" colspan="2">MODERN VOICE SERVER PERSISTENT VOICE CHANNEL</td>
</tr>
<tr>
 <td class="label">Channel Name</td>
 <td class="data"><el:text name="name" idx="*" className="pri bld req" size="26" max="40" value="${channel.name}" /></td>
</tr>
<tr>
 <td class="label">Channel Description</td>
 <td class="data"><el:text name="desc" idx="*" className="req" size="96" max="128" value="${channel.description}" /></td>
</tr>
<tr>
 <td class="label">Maximum Users</td>
 <td class="data"><el:text name="maxUsers" idx="*" size="3" max="4" className="req" value="${channel.maxUsers}" /></td>
</tr>
<tr>
 <td class="label">Sample Rate</td>
 <td class="data"><el:combo name="rate" idx="*" size="1" value="${channel.sampleRate}" options="${sampleRates}" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label">Range</td>
 <td class="data"><el:text name="range" idx="*" size="3" max="4" className="bld req" value="${channel.range}" /> miles</td>
</tr>
<tr>
 <td class="label">Web Applications</td>
 <td class="data"><el:check name="airline" width="175" options="${airlines}" className="req" checked="${channel.airlines}" /></td>
</tr>
<tr>
 <td class="label top">Join-Access Roles</td>
 <td class="data"><el:check name="joinRoles" width="115" cols="6" className="small" newLine="true" firstEntry="Pilot" checked="${channel.viewRoles}" options="${roles}" /></td>
</tr>
<tr>
 <td class="label top">Talk-Access Roles</td>
 <td class="data"><el:check name="talkRoles" width="115" cols="6" className="small" newLine="true" firstEntry="Pilot" checked="${channel.talkRoles}" options="${roles}" /></td>
</tr>
<tr>
 <td class="label top">Talk-Access if Present Roles</td>
 <td class="data"><el:check name="dynTalkRoles" width="115" cols="6" className="small" newLine="true" checked="${channel.dynTalkRoles}" options="${roles}" /></td>
</tr>
<tr>
 <td class="label top">Admin-Access Roles</td>
 <td class="data"><el:check name="adminRoles" width="115" cols="6" className="small" newLine="true" checked="${channel.adminRoles}" options="${roles}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="UPDATE CHANNEL" />
<c:if test="${!empty channel}">
 <el:cmdbutton ID="DeleteButton" url="mvschanneldelete" link="${channel}" label="DELETE CHANNEL" />
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
