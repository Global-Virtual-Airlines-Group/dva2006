<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Check Ride Script</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.eqType, 'Aircraft Type')) return false;
if (!validateCombo(form.programType, 'Equipment Program')) return false;
if (!validateText(form.msgText, 15, 'Check Ride content')) return false;

setSubmit();
disableButton('SaveButton');
disableButton('DeleteButton');
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
<el:form action="crscript.do" op="save" method="post" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<!-- Title Bar -->
<tr class="title caps">
 <td colspan="2">CHECK RIDE SCRIPT</td>
</tr>
<tr>
 <td class="label">Aircraft Type</td>
 <td class="data"><el:combo name="eqType" idx="*" size="1" className="req" options="${actypes}" firstEntry="-" value="${script.equipmentType}" /></td>
</tr>
<tr>
 <td class="label">Equipment Program</td>
 <td class="data"><el:combo name="programType" idx="*" size="1" className="req" options="${eqTypes}" firstEntry="-" value="${script.program}" /></td>
</tr>
<tr>
 <td class="label top">Script Text</td>
 <td class="data"><el:textbox name="msgText" idx="*" width="90%" height="10" className="req">${script.description}</el:textbox></td>
</tr>
</el:table>

<!-- Button bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:button ID="SaveButton" type="submit" className="BUTTON" label="SAVE CHECK RIDE SCRIPT" />
<c:if test="${access.canDelete}">
 <el:cmdbutton ID="DeleteButton" url="crscriptdelete" linkID="${script.equipmentType}" label="DELETE CHECK RIDE SCRIPT" />
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
