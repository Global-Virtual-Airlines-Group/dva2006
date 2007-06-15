<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Flight Academy Certification Requirements - ${cert.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.name, 10, 'Certification Name')) return false;

setSubmit();
disableButton('SaveButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="certreqs.do" linkID="${cert.name}" op="save" method="post" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="2">FLIGHT ACADEMY CERTIFICATION REQUIREMENTS - ${cert.name}</td>
</tr>

<!-- Existing Requirements -->
<c:set var="reqNum" value="${0}" scope="request" />
<c:forEach var="req" items="${cert.requirements}">
<c:set var="reqNum" value="${reqNum + 1}" scope="request" />
<!-- Requirement #<fmt:int value="${reqNum}" /> -->
<tr>
 <td class="label" valign="top">Requirement #<fmt:int value="${reqNum}" /></td>
 <td class="data"><el:textbox name="reqText${reqNum}" idx="*" width="80%" height="5">${req.text}</el:textbox></td>
</tr>
</c:forEach>

<!-- Additional Requirements -->
<c:set var="reqNum" value="${reqNum + 1}" scope="request" />
<!-- Requirement #<fmt:int value="${reqNum}" /> -->
<tr>
 <td class="label" valign="top">Requirement #<fmt:int value="${reqNum}" /></td>
 <td class="data"><el:textbox name="reqText${reqNum}" idx="*" width="80%" height="5" /></td>
</tr>
<c:set var="reqNum" value="${reqNum + 1}" scope="request" />
<!-- Requirement #<fmt:int value="${reqNum}" /> -->
<tr>
 <td class="label" valign="top">Requirement #<fmt:int value="${reqNum}" /></td>
 <td class="data"><el:textbox name="reqText${reqNum}" idx="*" width="80%" height="5" /></td>
</tr>
<c:set var="reqNum" value="${reqNum + 1}" scope="request" />
<!-- Requirement #<fmt:int value="${reqNum}" /> -->
<tr>
 <td class="label" valign="top">Requirement #<fmt:int value="${reqNum}" /></td>
 <td class="data"><el:textbox name="reqText${reqNum}" idx="*" width="80%" height="5" /></td>
</tr>
<c:set var="reqNum" value="${reqNum + 1}" scope="request" />
<!-- Requirement #<fmt:int value="${reqNum}" /> -->
<tr>
 <td class="label" valign="top">Requirement #<fmt:int value="${reqNum}" /></td>
 <td class="data"><el:textbox name="reqText${reqNum}" idx="*" width="80%" height="5" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="doMore" idx="*" value="true" label="Add More Requirements" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr class="title">
 <td><el:button ID="SaveButton" type="submit" className="BUTTON" label="SAVE CERTIFICATION REQUIREMENTS" /></td>
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
