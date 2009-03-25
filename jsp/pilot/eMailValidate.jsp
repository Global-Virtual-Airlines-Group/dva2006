<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> E-Mail Address Validation</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<content:sysdata var="badDomains" name="registration.reject_domain" />
<script language="JavaScript" type="text/javascript">
<fmt:jsarray var="invalidDomains" items="${badDomains}" />

function validate(form)
{
if (!checkSubmit()) return false;
var act = form.action;
if ((act.indexOf('resendvalidate.do') == -1) && (!validateText(form.code, 8, 'E-Mail Validation Code'))) return false;
if (!validateEMail(form.email, 'E-Mail Address')) return false;

// Validate e-mail domain
var eMail = form.email.value;
var usrDomain = eMail.substring(eMail.indexOf('@') + 1, eMail.length);
for (var x = 0; x < invalidDomains.length; x++) {
	if (usrDomain == invalidDomains[x]) {
		alert('Your e-mail address (' + eMail + ') contains a forbidden domain - ' + invalidDomains[x]);
		form.email.focus();
		return false;
	}
}

setSubmit();
disableButton('SubmitButton');
disableButton('ResendButton');
return true;
}

function updateAddress()
{
// Allow edits to the field and redirect
var f = document.forms[0];
enableObject(f.email, true);
f.email.readOnly = false;
enableObject(f.code, false);
disableButton('ResendButton');
f.action = '/resendvalidate.do';

// Relabel the submit button
var sb = getElement('SubmitButton');
sb.value = 'UPDATE ADDRESS';

// Hide this link
var link = getElement('updateAddrLink');
link.innerHTML = '';
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
<el:form action="validate.do" link="${person}" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<c:choose>
<c:when test="${validationFailure}">
<tr class="title caps">
 <td colspan="2">E-Mail Address Validation Failure</td>
</tr>
<tr>
 <td class="pri bld left" colspan="2">You have supplied an incorrect e-mail address validation code. Your 
e-mail address threfore cannot be validated. Please type in the validation code you received within the 
e-mail message, into the space provided below.</td>
</tr>
</c:when>
<c:otherwise>
<tr class="title caps">
 <td colspan="2">INVALID E-MAIL ADDRESS</td>
</tr>
<tr>
<c:if test="${empty addr || (!addr.isValid)}">
 <td colspan="2" class="pri bld left">Your e-mail address is currently marked as invalid. One condition for 
membership here at <content:airline /> is providing a valid e-mail address. Please provide your e-mail address 
in the space provided below.</td>
</c:if>
<c:if test="${!empty addr && (addr.isValid)}">
 <td colspan="2" class="pri bld left">Your e-mail address is currently marked as invalid. One condition for 
membership here at <content:airline /> is providing a valid e-mail address. You should have received an e-mail 
message in your mailbox at ${addr.address} with a validation code. Please provide the validation code in the 
space below.</td>
</c:if>
</tr>
</c:otherwise>
</c:choose>
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><el:text name="email" idx="*" readOnly="${addr.isValid}" size="40" max="80" className="req" value="${addr.isValid ? addr.address : ''}" />
<c:if test="${addr.isValid}"> <el:link ID="updateAddrLink" url="javascript:void updateAddress()" className="pri bld small">This isn't my correct e-mail address.</el:link></c:if></td>
</tr>
<c:if test="${!empty addr}">
<tr>
 <td class="label">Validation Code</td>
 <td class="data"><el:text name="code" idx="*" size="36" max="36" className="req" value="${param.code}" /></td>
</tr>
</c:if>
<content:hasmsg>
<tr>
 <td colspan="2" class="error mid bld"><content:sysmsg /></td>
</tr>
</content:hasmsg>
<c:if test="${resendEMail}">
<tr>
 <td colspan="2" class="ter mid bld">The validation code has been re-sent to ${addr.address}</td>
</tr>
</c:if>

<!-- Button Bar -->
<tr class="title mid">
 <td colspan="2"><el:button type="submit" ID="SubmitButton" className="BUTTON" label="VALIDATE ADDRESS" /> 
<el:cmdbutton ID="ResendButton" url="resendvalidate" post="true" label="RESEND VALIDATION E-MAIL" /></td>
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
