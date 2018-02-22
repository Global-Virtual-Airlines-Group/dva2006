<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
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
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:sysdata var="badDomains" name="registration.reject_domain" />
<script>
<fmt:jsarray var="golgotha.form.invalidDomains" items="${badDomains}" />
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.email, addr:true, t:'E-Mail Address'});
golgotha.form.submit(f);
return true;
};

golgotha.local.updateAddress = function()
{
// Allow edits to the field and redirect
var f = document.forms[0];
golgotha.util.disable(f.email);
f.email.readOnly = false;
golgotha.util.disable(f.code);
golgotha.util.disable('ResendButton');
f.action = '/appresendvalidate.do';

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
<el:form action="appvalidate.do" link="${addr}" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<c:choose>
<c:when test="${validationFailure}">
<tr class="title caps">
 <td colspan="2">E-Mail Address Validation Failure</td>
</tr>
<tr>
 <td class="pri bld left" colspan="2">You have supplied an incorrect e-mail address validation code. Your e-mail address threfore cannot be validated. Please type in the validation code you received within the 
e-mail message, into the space provided below.</td>
</tr>
</c:when>
<c:otherwise>
<tr class="title caps">
 <td colspan="2">E-Mail Address Validation</td>
</tr>
<tr>
 <td class="left" colspan="2">In order for your application to <content:airline /> to be approved, you must provide us with a valid e-mail address. We have sent you an e-mail message to the address you provided when 
registering with an activation code. Please enter that application code now to validate your e-mail address.<br />
<br />
If you have provided us an incorrect e-mail address or you have not received the e-mail message, you can update your address and/or send a new validation e-mail.</td>
</tr>
</c:otherwise>
</c:choose>
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><el:text name="email" readOnly="true" idx="*" size="40" max="80" className="req" value="${addr.address}" />
 <el:link ID="updateAddrLink" url="javascript:void golgotha.local.updateAddress()" className="pri bld small">This isn't my correct e-mail address.</el:link></td>
</tr>
<tr>
 <td class="label">Validation Code</td>
 <td class="data"><el:text name="code" required="true" idx="*" size="36" max="36" value="${param.code}" /></td>
</tr>
<c:if test="${resendEMail}">
<tr>
 <td colspan="2" class="ter mid bld">The validation code has been re-sent to ${addr.address}.</td>
</tr>
</c:if>

<!-- Button Bar -->
<tr class="title mid">
 <td colspan="2"><el:button type="submit" ID="SubmitButton" label="VALIDATE ADDRESS" /> 
<el:cmdbutton ID="ResendButton" url="appresendvalidate" link="${addr}" post="true" label="RESEND VALIDATION E-MAIL" /></td>
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
