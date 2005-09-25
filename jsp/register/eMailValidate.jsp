<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
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
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<el:form action="emailupd.do" linkID="0x${p.ID}" method="post" op="${empty addr ? 'save' : 'validate'}" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">INVALID E-MAIL ADDRESS</td>
</tr>
<tr>
<c:if test="${empty addr}">
 <td colspan="2" class="pri bld left">Your e-mail address is currently marked as invalid. One condition for 
membership here at <content:airline /> is providing a valid e-mail address. Please provide your e-mail address 
in the space provided below.</td>
</c:if>
<c:if test="${!empty addr}">
 <td colspan="2" class="pri bld left">Your e-mail address is currently marked as invalid. One condition for 
membership here at <content:airline /> is providing a valid e-mail address. You should have received an e-mail 
message in your mailbox at ${addr.address} with a validation code. Please provide the validation code in the 
space below.</td>
</c:if>
</tr>
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><el:text name="email" idx="*" size="40" max="80" value="${addr.address}" /></td>
</tr>
<c:if test="${!empty addr}">
<tr>
 <td class="label">Validation Code</td>
 <td class="data"><el:text name="code" idx="*" size="26" max="32" value="${param.code}" /></td>
</tr>
</c:if>
<c:if test="${!empty system_message}">
<tr>
 <td colspan="2" class="error mid bld">${system_message}</td>
</tr>
</c:if>

<!-- Button Bar -->
<tr class="title mid">
 <td colspan="2"><el:button type="submit" ID="SubmitButton" className="BUTTON" label="VALIDATE ADDRESS" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</div>
</body>
</html>
