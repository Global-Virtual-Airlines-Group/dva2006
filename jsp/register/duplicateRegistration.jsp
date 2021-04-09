<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title>Duplicate Registration Detected</title>
<content:css name="main" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.firstName, l:3, t:'First (given) Name'});
	golgotha.form.validate({f:f.lastName, l:2, t:'Last (family) Name'});
	golgotha.form.validate({f:f.email, addr:true, t:'E-Mail Address'});
	golgotha.form.validate({f:f.msgText, l:10, t:'Message to Human Resources'});
	golgotha.form.submit(f);
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
<el:form action="dupeinfo.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">Duplicate Registration</td>
</tr>
<tr>
 <td class="pri bld left" colspan="2"><c:if test="${appSubmitted}">There is another Pilot or Applicant registered at <span class="pri bld">${airline.name}</span> with your provided name and/or e-mail 
address. </c:if>If you are an Inactive or Retired Pilot wishing to return to active status at <content:airline />, please fill in your details in the space below. These will be sent to our Human 
Resources department. You should hear back from them within 48-72 hours. Thank you for your interest in <content:airline />!</td>
</tr>
<tr>
 <td class="label">First / Last Name</td>
 <td class="data"><el:text name="firstName" className="pri bld" required="true" idx="*" size="14" max="24" value="${param.firstName}" />&nbsp;
<el:text name="lastName" className="pri bld" required="true" idx="*" size="18" max="32" value="${param.lastName}" /></td>
</tr>
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><el:addr name="email" required="true" idx="*" size="48" max="64" value="${param.email}" /></td>
</tr>
<tr>
 <td class="label top">Other Information</td>
 <td class="data"><el:textbox name="msgText" required="true" spellcheck="true" idx="*" width="90%" height="4" resize="true"></el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SUBMIT MESSAGE" /></td>
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
