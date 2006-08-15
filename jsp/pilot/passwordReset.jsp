<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Password Reset</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.fName, 2, 'First Name')) return false;
if (!validateText(form.lName, 2, 'Last Name')) return false;
<content:filter roles="!HR">
if (!validateText(form.eMail, 10, 'E-Mail Address')) return false;
</content:filter>

setSubmit();
disableButton('SaveButton');
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
<el:form method="post" action="pwdreset.do" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title">
 <td colspan="2" class="left">PASSWORD RESET</td>
</tr>
<tr>
 <td class="label">First / Last Name</td>
 <td class="data"><el:text name="fName" idx="1" size="10" max="16" className="req" />
 <el:text name="lName" idx="2" size="16" max="14" className="req" /></td>
</tr>
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><el:text name="eMail" idx="3" size="32" max="80" /><br />
 <span class="small">(We need your e-mail address to verify it's really you.)</span></td>
</tr>
<c:if test="${!empty system_message}">
<tr>
 <td colspan="2" class="error bld">PASSWORD RESET FAILURE - ${system_message}</td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:button ID="SaveButton" className="BUTTON" label="RESET PASSWORD" type="submit" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
