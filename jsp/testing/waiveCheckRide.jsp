<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Check Ride Waiver</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.eqType, 'Equipment Program')) return false;
if (!validateText(form.comments, 15, 'Check Ride Waiver comments')) return false;

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
<el:form action="waivecr.do" method="post" link="${pilot}" validate="return validate(this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">NEW <content:airline /> CHECK RIDE WAIVER FOR ${pilot.name}<c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></td>
</tr>
<tr>
 <td class="label">Pilot Name</td>
 <td class="data"><el:cmd url="profile" link="${pilot}" className="pri bld">${pilot.name}</el:cmd> (${pilot.rank.name}, ${pilot.equipmentType})</td>
</tr>
<tr>
 <td class="label">Equipment Program</td>
 <td class="data"><el:combo name="eqType" idx="*" size="1" required="true" className="bld" options="${eqTypes}" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label top">Waiver Comments</td>
 <td class="data"><el:textbox name="comments" idx="*" required="true" width="80%" height="4" resize="true" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="SAVE CHECK RIDE WAIVER" /></td>
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
