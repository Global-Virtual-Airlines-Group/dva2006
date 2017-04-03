<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> E-Mail Address Validation</title>
<content:css name="main" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<content:favicon />
<content:sysdata var="badDomains" name="registration.reject_domain" />
<script type="text/javascript">
<fmt:jsarray var="golgotha.form.invalidDomains" items="${badDomains}" />
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
if (f.action.indexOf('resendvalidate.do') == -1)
	golgotha.form.validate({f:f.code, l:8, t:'E-Mail Validation Code'});

golgotha.form.validate({f:f.email, addr:true, t:'E-Mail Address'});
golgotha.form.submit(f);
return true;
};

golgotha.local.updateAddress = function(f)
{
golgotha.util.disable(f.email, false);
f.email.readOnly = false;
golgotha.util.disable(f.code);
golgotha.util.disable('ResendButton');
f.action = '/resendvalidate.do';

// Relabel the submit button
var sb = document.getElementById('SubmitButton');
sb.value = 'UPDATE ADDRESS';

// Hide this link
var link = document.getElementById('updateAddrLink');
link.innerHTML = '';
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="validate.do" link="${person}" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
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
 <td class="data"><el:addr name="email" required="true" idx="*" readOnly="${addr.isValid}" size="40" max="80" value="${addr.isValid ? addr.address : ''}" />
<c:if test="${addr.isValid}"> <el:link ID="updateAddrLink" url="javascript:void golgotha.local.updateAddress(document.forms[0])" className="pri bld small">This isn't my correct e-mail address.</el:link></c:if></td>
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
 <td colspan="2"><el:button type="submit" ID="SubmitButton" label="VALIDATE ADDRESS" /> 
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
