<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>E-Mail Address Validation Failure</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.code, 10, 'E-Mail Validation Code')) return false;
if (!validateEMail(form.email, 'E-Mail Address')) return false;

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
<el:form action="validate.do" link="${p}" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">E-Mail Address Validation Failure</td>
</tr>
<tr>
 <td class="pri bld left" colspan="2">You have supplied an incorrect e-mail address validation code. Your 
e-mail address threfore cannot be validated. Please type in the validation code you received within the 
e-mail message, into the space provided below.</td>
</tr>
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><el:text name="email" idx="*" size="32" max="80" className="req" value="${!empty addr ? addr : param.email}" /></td>
</tr>
<tr>
 <td class="label">Validation Code</td>
 <td class="data"><el:text name="code" idx="*" size="24" max="36" className="req" value="${!empty code ? code : param.code}" /></td>
</tr>
<c:if test="${invalidInfo}">
<tr>
 <td class="error bld mid" colspan="2">The specified e-mail address does not exist.</td>
</tr>
</c:if>
<tr class="title mid">
 <td colspan="2"><el:button ID="SubmitButton" type="submit" className="BUTTON" label="VALIDATE E-MAIL ADDRESS" /></td>
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
