<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Create Staff Profile - ${pilot.name} (${pilot.pilotCode})</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.staffTitle, 10, 'Staff Title')) return false;
if (!validateText(form.staffBody, 30, 'Staff Biographical Profile')) return false;
if (!validateNumber(form.staffSort, 1, 'Staff Profile Sort Order')) return false;

setSubmit();
disableButton('SaveButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="newstaff.do" linkID="0x${pilot.ID}" method="post" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<!-- Staff Profile Title Bar -->
<tr class="title caps">
 <td colspan="2">NEW STAFF PROFILE - ${pilot.name}</td>
</tr>
<tr>
 <td class="label">Title</td>
 <td class="data"><el:text className="bld" name="staffTitle" value="" size="48" max="64" /></td>
</tr>
<tr>
 <td class="label">Biographical Profile</td>
 <td class="data"><el:textbox name="staffBody" height="4" width="100"></el:textbox></td>
</tr>
<tr>
 <td class="label">Sort Order</td>
 <td class="data"><el:text name="staffSort" value="6" size="1" max="1" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:button ID="SaveButton" type="SUBMIT" className="BUTTON" label="SAVE STAFF PROFILE" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
