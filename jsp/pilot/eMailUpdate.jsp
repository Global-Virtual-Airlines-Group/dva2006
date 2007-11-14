<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Update E-Mail Address</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<content:sysdata var="badDomains" name="registration.reject_domain" />
<script language="JavaScript" type="text/javascript">
var invalidDomains = ['<fmt:list value="${badDomains}" delim="','" />'];

function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.code, 8, 'E-Mail Validation Code')) return false;
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
<el:form action="emailupd.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2" class="left">UPDATE E-MAIL ADDRESS</td>
</tr>
<tr>
 <td colspan="2" class="pri bld left">A condition of membership at <content:airline /> is providing a valid, 
verified e-mail address. If you change your e-mail address, the new address must be validated before you can 
log in again.</td>
</tr>
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><el:text name="email" idx="*" size="40" max="80" className="req" value="${user.email}" /></td>
</tr>

<!-- Button Bar -->
<tr class="title mid">
 <td colspan="2"><el:button type="submit" ID="SubmitButton" className="BUTTON" label="UPDATE ADDRESS" /></td>
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
