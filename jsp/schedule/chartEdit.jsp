<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Approach Chart</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="airportRefresh" />
<content:googleAnalytics eventSupport="true" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.airport, 'Airport')) return false;
if (!validateText(form.name, 4, 'Chart Name')) return false;
if (!validateCombo(form.chartType, 'Chart Type')) return false;
if (!validateFile(form.img, 'gif,jpg,png,pdf', 'Chart Image')) return false;

setSubmit();
disableButton('SaveButton');
return true;
}

function loadAirports()
{
var f = document.forms[0];
updateAirports(f.airport, 'airline=all', ${doICAO}, getValue(f.airport));
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body onload="void loadAirports()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="airports" name="airports" mapValues="true" sort="true" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="chart.do" method="post" link="${chart}" op="save" allowUpload="true" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
<c:if test="${!empty chart}">
 <td colspan="2">APPROACH CHART - ${chart.name} (<fmt:airport airport="${chart.airport}" />)</td>
</c:if>
<c:if test="${empty chart}">
 <td colspan="2">NEW APPROACH CHART</td>
</c:if>
</tr>
<tr>
 <td class="label">Airport</td>
 <td class="data"><el:combo className="req" name="airport" size="1" idx="*" firstEntry="< SELECT >" options="${airports}" value="${chart.airport}" />
 <el:text name="airportCode" idx="*" size="3" max="4" value="${chart.airport.ICAO}" onBlur="void setAirport(document.forms[0].airport, this.value)" /></td>
</tr>
<tr>
 <td class="label">Chart Name</td>
 <td class="data"><el:text className="pri bld req" name="name" idx="*" size="32" max="64" value="${chart.name}" /></td>
</tr>
<tr>
 <td class="label">Chart Type</td>
 <td class="data"><el:combo name="chartType" size="1" idx="*" options="${chartTypes}" className="req" firstEntry="< SELECT >" value="${chart.typeName}" /></td>
</tr>
<c:if test="${!empty chart}">
<tr>
 <td class="label">Image Properties</td>
 <td class="data sec">${chart.imgTypeName} image, <fmt:int value="${chart.size}" /> bytes</td>
</tr>
</c:if>
<tr>
 <td class="label">Upload File</td>
 <td class="data"><el:file name="img" idx="*" className="small" size="96" max="192" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SaveButton" type="submit" className="BUTTON" label="SAVE CHART" />
<c:if test="${access.canDelete && (!empty chart)}">
 <el:cmdbutton ID="DeleteButton" url="chartdelete" link="${chart}" label="DELETE CHART" />
</c:if></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
