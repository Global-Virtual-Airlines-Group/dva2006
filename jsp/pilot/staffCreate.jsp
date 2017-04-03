<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title>Create Staff Profile - ${pilot.name} (${pilot.pilotCode})</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.staffTitle, l:10, t:'Staff Title'});
golgotha.form.validate({f:f.staffArea, t:'Department Name'});
golgotha.form.validate({f:f.staffBody, l:30, t:'Staff Biographical Profile'});
golgotha.form.validate({f:f.staffSort, min:1, t:'Staff Profile Sort Order'});
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
<content:sysdata var="staffAreas" name="staff.departments" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="newstaff.do" link="${pilot}" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<!-- Staff Profile Title Bar -->
<tr class="title caps">
 <td colspan="2">NEW STAFF PROFILE - ${pilot.name}</td>
</tr>
<tr>
 <td class="label">Title</td>
 <td class="data"><el:text className="bld req" idx="*" name="staffTitle" value="" size="48" max="64" /></td>
</tr>
<tr>
 <td class="label">Department</td>
 <td class="data"><el:combo name="staffArea" idx="*" size="1" options="${staffAreas}" className="req" firstEntry="-" value="${staff.area}" /></td>
</tr>
<tr>
 <td class="label top">Biographical Profile</td>
 <td class="data"><el:textbox name="staffBody" idx="*" height="4" width="90%" resize="true"></el:textbox></td>
</tr>
<tr>
 <td class="label">Sort Order</td>
 <td class="data"><el:text className="req" name="staffSort" idx="*" value="6" size="1" max="1" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="SAVE STAFF PROFILE" /></td>
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
