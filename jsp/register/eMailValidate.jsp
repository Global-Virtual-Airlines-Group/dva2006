<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> E-Mail Address Validation</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
var act = form.action;
if ((act.indexOf('resendvalidate.do') == -1) && (!validateText(form.code, 8, 'E-Mail Validation Code'))) return false;
if (!validateEMail(form.email, 'E-Mail Address')) return false;

setSubmit();
disableButton('SubmitButton');
disableButton('ResendButton');
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
<el:form action="validate.do" linkID="${fn:dbID(person)}" method="post" op="${empty addr ? 'save' : 'validate'}" validate="return validate(this)">
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
 <td class="data"><el:text name="email" idx="*" size="40" max="80" className="req" value="${addr.isValid ? addr.address : ''}" /></td>
</tr>
<c:if test="${!empty addr}">
<tr>
 <td class="label">Validation Code</td>
 <td class="data"><el:text name="code" idx="*" size="26" max="32" className="req" value="${param.code}" /></td>
</tr>
</c:if>
<c:if test="${!empty system_message}">
<tr>
 <td colspan="2" class="error mid bld">${system_message}</td>
</tr>
</c:if>
<c:if test="${resendEMail}">
<tr>
 <td colspan="2" class="ter mid bld">The validation code has been re-sent to ${emailAddr}</td>
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
</body>
</html>
