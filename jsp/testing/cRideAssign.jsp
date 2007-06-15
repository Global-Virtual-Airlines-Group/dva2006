<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Assign Check Ride for ${pilot.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.crType, 'Aircraft Type')) return false;
if (!validateCombo(form.eqType, 'Equimpment Program')) return false;

// Validate comments
var hasScript = ((form.doScript) && (form.doScript.value == 'true'));
if (!hasScript) {
	if (!validateText(form.comments, 25, 'Check Ride Comments')) return false;
}

setSubmit();
disableButton('ProfileButton');
disableButton('AssignButton');
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
<el:form action="nakedassign.do" method="post" link="${pilot}" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="2">CHECK RIDE - ${pilot.name}</td>
</tr>
<tr>
 <td class="label">Equipment Program</td>
 <td class="data"><el:combo name="eqType" idx="*" size="1" firstEntry="-" options="${eqTypes}" value="${param.eqType}" /></td>
</tr>
<tr>
 <td class="label">Aircraft Type</td>
 <td class="data"><el:combo name="crType" idx="*" size="1" firstEntry="-" options="${empty eqType ? actypes : eqType.primaryRatings}" value="${param.crType}" /></td>
</tr>
<tr>
 <td class="label" valign="top">Comments</td>
 <td class="data"><el:textbox name="comments" idx="*" width="80%" height="4"></el:textbox></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="useScript" idx="*" value="true" label="Append Check Ride script to comments" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:cmdbutton ID="ProfileButton" url="profile" link="${pilot}" label="VIEW PROFILE" /> 
<el:button ID="AssignButton" type="submit" className="BUTTON" label="ASSIGN CHECK RIDE" /></td>
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
