<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> User Login</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1">
<content:googleAnalytics />
<content:js name="common" />
<content:captcha action="login" />
<content:newRelic>
<content:cspHeader /></content:newRelic>
<script nonce="${contentSecurity.nonce}">
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	const isOV = Boolean(f.userOV.value);
	if (!isOV) {
		golgotha.form.validate({f:f.firstName, l:2, t:'First Name'});
		golgotha.form.validate({f:f.lastName, l:2, t:'Last Name'});
	}

	golgotha.form.validate({f:f.pwd, l:3, t:'Password'});
<c:if test="${!empty dupeUsers}">
	golgotha.form.validate({f:f.pilotCode, min:1, t:'Pilot Code'});</c:if>
	if (f.jsOK.value.length == 0)
		f.jsOK.value = 'true';

	golgotha.form.submit(f);
	return true;
};

golgotha.local.setFocus = function() {
	const f = document.forms[0];
	const isOV = Boolean(f.userOV.value);
	if (isOV || (f.firstName.value.length > 0))
		f.pwd.focus();
	else
		f.firstName.focus();

	// Ensure javascript is working properly
	f.jsOK.value = 'true';
	return true;
};

golgotha.local.toggleUser = function() {
	const f = document.forms[0];
	f.userOV.value = 'false';
	f.pilotCode.value = '';
	golgotha.util.display('userLabel', false);
	golgotha.util.display('userFields', true);
	return true;
};

golgotha.onDOMReady(golgotha.local.setFocus);
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/main/header.jspf" %> 
<%@include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="domain" name="airline.domain" />

<!-- Main Body Frame -->
<content:region id="main">
Welcome to <span class="bld"><content:airline /></span>! In order to access the secure areas of our web site, please enter your first and last name <c:if test="${!empty dupeUsers}">or your User ID </c:if>and password. Your browser must be able to accept cookies from <span class="sec bld">${domain}</span> in order to log into our web site.<br>
<br>
<el:form method="post" action="login.do" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">USER LOGIN</td>
</tr>
<c:if test="${!empty userTokenData}">
<tr id="userLabel">
 <td class="label">First / Last Name</td>
 <td class="data"><span class="pri bld">${userTokenData.firstName}&nbsp;${userTokenData.lastName}</span> <a href="javascript:void golgotha.local.toggleUser()" class="small ita plain">This isn't me</a></td>
</tr>
</c:if>
<tr id="userFields"<c:if test="${!empty userTokenData}"> style="display:none;"</c:if>>
 <td class="label">First / Last Name</td>
 <td class="data"><el:text name="firstName" idx="*" size="12" max="32" className="req" autofill="given-name" value="${userTokenData.firstName}" />&nbsp;<el:text name="lastName" idx="*" size="16" max="32" className="req" autofill="family-name" value="${userTokenData.lastName}" /></td>
</tr>
<tr>
 <td class="label">Password</td>
 <td class="data"><el:text type="password" name="pwd" idx="*" size="16" max="32" required="true" autofill="current-password" value="" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data sec small"><el:box name="saveInfo" idx="*" value="true" label="Remember me next time I Log in" checked="${!empty fname || !empty userTokenData}" /></td>
</tr>
<c:if test="${!empty dupeUsers}">
<tr class="title caps">
 <td colspan="2">MULTIPLE USERS NAMED ${fname} ${lname} FOUND</td>
</tr>
<c:forEach var="pilot" items="${dupeUsers}">
<c:set var="pCode" value="${empty pilot.pilotCode ? 'N/A' : pilot.pilotCode}" scope="page" />
<tr>
 <td><el:radio name="pilotCode" value="${pilot.hexID}" label="${pCode}" checked="${pilotCode == pilot.hexID}" /></td>
 <td class="data"><span class="pri bld">${pilot.name}</span> (${pilot.rank.name}, ${pilot.equipmentType})</td>
</tr>
</c:forEach>
</c:if>
<content:hasmsg>
<tr>
 <td colspan="2"><span class="error bld">LOGIN FAILURE - <content:sysmsg /></span></td>
</tr>
</content:hasmsg>
</el:table>
<el:table className="bar">
<tr>
 <td><el:button label="LOG IN" type="submit" /></td>
</tr>
</el:table>
<el:text name="jsOK" type="hidden" value="" /><el:text name="redirectTo" type="hidden" value="${(empty referTo) ? param.redirectTo : referTo}" /><el:text name="userOV" type="hidden" value="${!empty userTokenData}" />
<c:if test="${empty dupeUsers}"><el:text name="pilotCode" type="hidden" value="${userTokenData.userID}" /></c:if>
</el:form>
<br>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
