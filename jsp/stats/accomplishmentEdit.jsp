<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Pilot Accomplishment<c:if test="${!empty ap}"> - ${ap.name}</c:if></title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="jsColor" />
<script type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.name, 6, 'Accomplishment Name')) return false;
if (!validateCombo(form.units, 'Units of Measurement')) return false;
if (!validateNumber(form.value, 0, 'Number of Units')) return false;
if (!validateText(form.color, 6, 'Water Cooloer label color')) return false;

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
<el:form action="accomplishment.do" method="post" link="${ap}" op="save" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">PILOT ACCOMPLISHMENT PROFILE<c:if test="${!empty ap}"> - ${ap.name}</c:if></td>
</tr>
<tr>
 <td class="label">Accomplishment</td>
 <td class="data"><el:text name="name" idx="*" size="32" max="32" className="pri bld req" value="${ap.name}" /></td>
</tr>
<tr>
 <td class="label">Units</td>
 <td class="data"><el:combo name="units" idx="*" options="${units}" size="1" className="req" firstEntry="-" value="${ap.unit}" /></td>
</tr>
<tr>
 <td class="label">Number of Units</td>
 <td class="data"><el:text name="value" idx="*" size="5" max="8" className="bld req" value="${ap.value}" /></td>
</tr>
<tr>
 <td class="label">Water Cooler color</td>
 <td class="data"><el:text name="color" idx="*" className="color bld req" size="6" max="8" value="${ap.hexColor}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="active" idx="*" value="true" checked="${ap.active}" label="Accomplishment is Active" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SaveButton" type="submit" className="BUTTON" label="SAVE ACCOMPLISHMENT PROFILE" /></td>
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
