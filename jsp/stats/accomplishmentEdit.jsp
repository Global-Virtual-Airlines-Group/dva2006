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
disableButton('DeleteButton');
return true;
}

function showChoices()
{
var f = document.forms[0];
var c = f.units.options[f.units.selectedIndex].value;
switch (c) {
case 'COUNTRIES':
	displayObject(getElement('valueCountry'), true);
	displayObject(getElement('valueState'), false);
	displayObject(getElement('valueBox'), false);
	break;

case 'STATES':
	displayObject(getElement('valueState'), true);
	displayObject(getElement('valueCountry'), false);
	displayObject(getElement('valueBox'), false);
	break;

default:
	displayObject(getElement('valueBox'), true);
	displayObject(getElement('valueState'), false);
	displayObject(getElement('valueCountry'), false);
}

return true;
}
</script>
</head>
<content:copyright visible="false" />
<body onload="void showChoices()">
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
 <td class="data"><el:combo name="units" idx="*" options="${units}" size="1" className="req" firstEntry="-" value="${ap.unit}" onChange="void showChoices()" /></td>
</tr>
<tr>
 <td class="label">Number of Units</td>
 <td class="data"><el:text name="value" idx="*" size="7" max="8" className="bld req" value="${ap.value}" /></td>
</tr>
<tr>
 <td class="label">Water Cooler color</td>
 <td class="data"><el:text name="color" idx="*" className="color bld req" size="6" max="8" value="${ap.hexColor}" />
 <span class="small">Click on the text box for a color picker.</span></td>
</tr>
<tr id="valueBox">
 <td class="label top">Valid Values</td>
 <td class="data"><el:textbox name="choices" idx="*" width="80%" height="3">${fn:splice(ap.choices, ', ')}</el:textbox></td>
</tr>
<tr id="valueCountry" style="display:none;">
 <td class="label top">Valid Countries</td>
 <td class="data"><el:check name="countries" idx="*" width="190" cols="6" className="small" newLine="true" checked="${ap.choices}" options="${countries}"/></td>
</tr>
<tr id="valueState" style="display:none;">
 <td class="label top">Valid States</td>
 <td class="data"><el:check name="states" idx="*" width="120" cols="8" newLine="true" checked="${ap.choices}" options="${states}" /></td>
</tr>
<c:if test="${!empty ap}">
<tr>
 <td class="label">Pilots</td>
 <td class="data">
<c:choose>
<c:when test="${ap.pilots == 0}"><span class="bld">No <content:airline /> Pilots have achieved this Accomplishment</span></c:when>
<c:otherwise><span class="pri bld"><fmt:int value="${ap.pilots}" /> <content:airline /> Pilots have achieved this Accomplishment</span></c:otherwise>
</c:choose></td>
</tr>
</c:if>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="active" idx="*" value="true" className="bld" checked="${ap.active}" label="Accomplishment is Active" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SaveButton" type="submit" className="BUTTON" label="SAVE ACCOMPLISHMENT PROFILE" />
<c:if test="${access.canDelete}"> <el:cmdbutton ID="DeleteButton" url="accdelete" link="${ap}" label="DELETE ACCOMPLISHMENT PROFILE" /></c:if></td>
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
