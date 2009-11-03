<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Aircraft<c:if test="${!empty aircraft}"> - ${aircraft.name}</c:if></title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.name, 3, 'Aircraft Name')) return false;
if (!validateText(form.fullName, 5, 'Aircraft Full Name')) return false;
if (!validateNumber(form.range, 1, 'Aircraft Range')) return false;
if (!validateNumber(form.maxWeight, 1, 'Maximum Weight')) return false;
if (!validateNumber(form.maxTWeight, 1, 'Maximum Takeoff Weight')) return false;
if (!validateNumber(form.maxLWeight, 1, 'Maximum Landing Weight')) return false;
if (!validateNumber(form.engineCount, 1, 'Engine Count')) return false;
if (!validateText(form.engineType, 4, 'Engine Count')) return false;
if (!validateNumber(form.cruiseSpeed, 40, 'Cruise Speed')) return false;
if (!validateNumber(form.fuelFlow, 100, 'Fuel Flow')) return false;
if (!validateNumber(form.baseFuel, 0, 'Base Fuel Amount')) return false;
if (!validateNumber(form.taxiFuel, 0, 'Taxi Fuel Amount')) return false;
if (!validateCheckBox(form.pTanks, 1, 'Primary Fuel Tanks')) return false;

setSubmit();
disableButton('SaveButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>
<content:sysdata var="airlines" name="apps" mapValues="true" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="aircraft.do" method="post" linkID="${aircraft.name}" op="save" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">AIRCRAFT PROFILE</td>
</tr>
<tr>
 <td class="label">Aircraft</td>
 <td class="data"><el:text name="name" idx="*" className="pri bld req" size="15" max="15" value="${aircraft.name}" /></td>
</tr>
<tr>
 <td class="label">Full Aircraft Name</td>
 <td class="data"><el:text name="fullName" idx="*" className="req" size="32" max="48" value="${aircraft.fullName}" /></td>
</tr>
<tr>
 <td class="label">Maximum Range</td>
 <td class="data"><el:text name="range" idx="*" className="req" size="4" max="5" value="${aircraft.range}" /> miles</td>
</tr>
<tr>
 <td class="label top">IATA Equipment Code(s)</td>
 <td class="data"><el:textbox name="iataCodes" idx="*" width="30" height="3">${iataCodes}</el:textbox></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isHistoric" idx="*" value="true" checked="${aircraft.historic}" label="This is a Historic Aircraft" /><br />
<el:box name="isETOPS" idx="*" value="true" checked="${aircraft.ETOPS}" label="This Aircraft is ETOPS-rated" /></td>
</tr>
<tr>
 <td class="label top">Web Applications</td>
 <td class="data"><el:check name="airlines" width="180" options="${airlines}" checked="${aircraft.apps}" /></td>
</tr>
<tr class="title caps">
 <td colspan="2">AIRCRAFT WEIGHTS</td>
</tr>
<tr>
 <td class="label">Maximum Weight</td>
 <td class="data"><el:text name="maxWeight" idx="*" size="6" max="7" value="${aircraft.maxWeight}" className="req" /> pounds</td>
</tr>
<tr>
 <td class="label">Maximum Takeoff Weight</td>
 <td class="data"><el:text name="maxTWeight" idx="*" size="6" max="7" value="${aircraft.maxTakeoffWeight}" className="req" /> pounds</td>
</tr>
<tr>
 <td class="label">Maximum Landing Weight</td>
 <td class="data"><el:text name="maxLWeight" idx="*" size="6" max="7" value="${aircraft.maxLandingWeight}" className="req" /> pounds</td>
</tr>
<tr class="title caps">
 <td colspan="2">ACARS FUEL PROFILE</td>
</tr>
<tr>
 <td class="label">Engine Information</td>
 <td class="data"><el:text name="engineCount" idx="*" size="1" max="1" value="${aircraft.engines}" className="bld req" />
 x <el:text name="engineType" idx="*" size="16" max="32" value="${aircraft.engineType}" className="req" /></td>
</tr>
<tr>
 <td class="label">Cruise Speed</td>
 <td class="data"><el:text name="cruiseSpeed" idx="*" size="3" max="4" value="${aircraft.cruiseSpeed}" className="req" /> knots</td>
</tr>
<tr>
 <td class="label">Fuel Flow</td>
 <td class="data"><el:text name="fuelFlow" idx="*" size="3" max="5" value="${aircraft.fuelFlow}" className="req" />
 pounds per engine per hour</td>
</tr>
<tr>
 <td class="label">Base Fuel</td>
 <td class="data"><el:text name="baseFuel" idx="*" size="3" max="5" value="${aircraft.baseFuel}" className="req" /> pounds</td>
</tr>
<tr>
 <td class="label">Taxi Fuel</td>
 <td class="data"><el:text name="taxiFuel" idx="*" size="3" max="5" value="${aircraft.taxiFuel}" className="req" /> pounds</td>
</tr>
<tr>
 <td class="label top">Primary Tanks</td>
 <td class="data"><el:check name="pTanks" idx="*" width="100" cols="6" newLine="true" checked="${aircraft.tankNames['Primary']}" options="${tankNames}" /></td>
</tr>
<tr>
 <td class="label">Primary Percentage</td>
 <td class="data">Fill to <el:text name="pPct" idx="*" size="2" max="3" value="${aircraft.tankPercent['Primary']}" className="req" />
 percent before filling Secondary tanks</td>
</tr>
<tr>
 <td class="label top">Secondary Tanks</td>
 <td class="data"><el:check name="sTanks" idx="*" width="100" cols="6" newLine="true" checked="${aircraft.tankNames['Secondary']}" options="${tankNames}" /></td>
</tr>
<tr>
 <td class="label">Secondary Percentage</td>
 <td class="data">Fill to <el:text name="pPct" idx="*" size="2" max="3" value="${aircraft.tankPercent['Secondary']}" className="req" />
 percent before filling Other tanks</td>
</tr>
<tr>
 <td class="label top">Other Tanks</td>
 <td class="data"><el:check name="oTanks" idx="*" width="100" cols="6" newLine="true" checked="${aircraft.tankNames['Other']}" options="${tankNames}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SaveButton" type="submit" className="BUTTON" label="SAVE AIRCRAFT PROFILE" /></td>
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
