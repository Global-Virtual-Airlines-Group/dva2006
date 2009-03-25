<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> User Login</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.firstName, 2, 'First Name')) return false;
if (!validateText(form.lastName, 2, 'Last Name')) return false;
if (!validateText(form.pwd, 3, 'Password')) return false;
<c:if test="${!empty dupeUsers}">
if (!validateCheckBox(form.pilotCode, 1, 'Pilot Code')) return false;
</c:if>
if (form.jsOK.value.length == 0) {
	form.jsOK.value = 'true';
	f.screenX.value = screen.width;
	f.screenY.value = screen.height;
}

setSubmit();
disableButton('SubmitButton');
return true;
}

function setFocus()
{
var f = document.forms[0];
if (f.firstName.value.length > 0) {
	f.pwd.focus();
} else {
	f.firstName.focus();
}

// Save screen resolution
f.screenX.value = screen.width;
f.screenY.value = screen.height;

// Ensure javascript is working properly
f.jsOK.value = 'true';
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body onload="void setFocus()">
<content:page>
<%@include file="/jsp/main/header.jspf" %> 
<%@include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
Welcome to <content:airline />! In order to access the secure areas of our site, please enter 
your first and last name or your User ID and password. Your browser must be able to accept cookies 
in order to log into the site.<br />
<br />
<el:form method="post" action="login.do" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">USER LOGIN</td>
</tr>
<tr>
 <td class="label">First / Last Name</td>
 <td class="data"><el:text name="firstName" idx="*" size="10" max="16" value="${fname}" />
  <el:text name="lastName" idx="*" size="16" max="24" value="${lname}" /></td>
</tr>
<tr>
 <td class="label">Password</td>
 <td class="data"><el:text type="password" name="pwd" idx="*" size="16" max="32" value="" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data sec small"><el:box name="saveInfo" idx="*" value="true" label="Remember my User ID next time I Log in" checked="${!empty fname}" /></td>
</tr>
<c:if test="${!empty dupeUsers}">
<tr class="title caps">
 <td colspan="2">MULTIPLE USERS NAMED ${fname} ${lname} FOUND</td>
</tr>
<c:forEach var="pilot" items="${dupeUsers}">
<c:if test="${fn:isActive(pilot)}">
<c:set var="pCode" value="${empty pilot.pilotCode ? 'N/A' : pilot.pilotCode}" scope="request" />
<tr>
 <td><el:radio name="pilotCode" value="${pilot.hexID}" label="${pCode}" checked="${pilotCode == pilot.hexID}" /></td>
 <td class="data"><span class="pri bld">${pilot.name}</span> (${pilot.rank}, ${pilot.equipmentType})</td>
</tr>
</c:if></c:forEach>
</c:if>
<content:hasmsg>
<tr>
 <td colspan="2"><span class="error bld">LOGIN FAILURE - <content:sysmsg /></span></td>
</tr>
</content:hasmsg>
</el:table>
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:button ID="SubmitButton" className="BUTTON" label="LOG IN" type="submit" /></td>
</tr>
</el:table>
<el:text name="jsOK" type="hidden" value="" />
<el:text name="screenX" type="hidden" value="1024" />
<el:text name="screenY" type="hidden" value="768" />
<el:text name="redirectTo" type="hidden" value="${referTo}" />
<c:if test="${empty dupeUsers}"><el:text name="pilotCode" type="hidden" value="${pilotCode}" /></c:if>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
