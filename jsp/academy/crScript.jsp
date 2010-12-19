<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Flight Academy Check Ride Script</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.cert, 'Flight Academy Certification')) return false;
if (!validateText(form.body, 15, 'Check Ride content')) return false;

setSubmit();
disableButton('SaveButton');
disableButton('DeleteButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body onload="void resizeAll()">
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="arscript.do" op="save" linkID="${sc.certificationName}" method="post" validate="return validate(this)">
<el:table className="form">
<!-- Title Bar -->
<tr class="title caps">
 <td colspan="2">FLIGHT ACADEMY CHECK RIDE SCRIPT</td>
</tr>
<c:if test="${empty sc}">
<tr>
 <td class="label">Certification</td>
 <td class="data"><el:combo name="cert" idx="*" size="1" className="req" options="${certs}" firstEntry="-" value="${sc.certificationName}" /></td>
</tr>
</c:if>
<tr>
 <td class="label top">Script Text</td>
 <td class="data"><el:textbox name="body" idx="*" width="90%" height="5" resize="true" className="req">${sc.description}</el:textbox></td>
</tr>
</el:table>

<!-- Button bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="SAVE CHECK RIDE SCRIPT" />
<c:if test="${access.canDelete}">
 <el:cmdbutton ID="DeleteButton" url="arscriptdelete" linkID="${sc.certificationName}" label="DELETE CHECK RIDE SCRIPT" />
</c:if></td>
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
