<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Duplicate Registration Detected</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.firstName, 3, 'First (given) Name')) return false;
if (!validateText(form.lastName, 2, 'Last (family) Name')) return false;
if (!validateText(form.email, 7, 'E-Mail Address')) return false;
if (!validateText(form.msgText, 10, 'Message to Human Resources')) return false;

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
<el:form action="dupeinfo.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">Duplicate Registration</td>
</tr>
<tr>
 <td class="pri bld left" colspan="2"><c:if test="${appSubmitted}">There is another Pilot or Applicant 
registered at <span class="pri bld">${airline.name}</span> with your provided name and/or e-mail 
address. </c:if>If you are an Inactive or Retired Pilot wishing to return to active status at 
<content:airline />, please fill in your details in the space below. These will be sent to our Human 
Resources department. You should hear back from them within 48-72 hours. Thank you for your interest 
in <content:airline />!</td>
</tr>
<tr>
 <td class="label">First / Last Name</td>
 <td class="data"><el:text name="firstName" className="pri bld req" idx="*" size="14" max="24" value="${param.firstName}" />&nbsp;
<el:text name="lastName" className="pri bld req" idx="*" size="18" max="32" value="${param.lastName}" /></td>
</tr>
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><el:text name="email" idx="*" size="48" max="64" className="req" value="${param.email}" /></td>
</tr>
<tr>
 <td class="label" valign="top">Other Information</td>
 <td class="data"><el:textbox name="msgText" idx="*" width="90%" className="req" height="7"></el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SubmitButton" type="submit" className="BUTTON" label="SUBMIT MESSAGE" /></td>
</tr>
</el:table>
<el:text name="airline" type="hidden" value="${airline.name}" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
