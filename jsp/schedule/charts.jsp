<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Approach / Procedure Charts</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<content:js name="airportRefresh" />
<content:googleAnalytics eventSupport="true" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function updateAirport()
{
document.forms[0].submit();
return true;
}

function validate(form)
{
var f = document.forms[0];
return (f.id.selectedIndex > 0);
}

function updateVisibility()
{
var f = document.forms[0];
var chartTypes = [];
for (var x = 0; x < f.chartType.length; x++) {
	if (f.chartType[x].checked)
		chartTypes.push(f.chartType[x].value.toLowerCase());
}

// Hide/select rows
<fmt:jsarray var="cTypes" items="${chartTypeNames}" />
for (var x = 0; x < cTypes.length; x++) {
	var chartClass = cTypes[x].toLowerCase();
	var isDisplayed = ((chartTypes.length == 0) || (chartTypes.indexOf(chartClass) > -1));
	var rows = getElementsByClass(chartClass);
	for (var y = 0; y < rows.length; y++) {
		var row = rows[y];
		row.style.display = isDisplayed ? '' : 'none';
	}
}

return true;
}
</script>
</head>
<content:copyright visible="false" />
<body onload="updateAirports(document.forms[0].id, 'airline=charts', false, '${airport.ICAO}'); updateVisibility()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<c:set var="cspan" value="${access.canEdit ? 1 : 2}" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="charts.do" method="post" validate="return validate(this)">
<view:table className="view" pad="default" space="default" cmd="charts">

<!-- Table Header/Filter Bars-->
<tr class="title caps">
 <td colspan="5" class="left"><content:airline /> AIRPORT / APPROACH / PROCEDURE CHARTS</td>
</tr>
<tr>
 <td class="priB right" style="color: #FFFFFF; font-size: 8pt;">Filter Options</td>
 <td colspan="4" width="90%" class="left"><el:check name="chartType" className="small" idx="*" width="180" options="${chartTypes}" checked="${selectedTypes}" onChange="void updateVisibility()" /></td>
</tr>
<tr class="title">
 <td colspan="2">CHART NAME</td>
 <td width="20%">CHART TYPE</td>
 <td width="10%"><c:if test="${access.canCreate}"><el:cmdbutton url="chart" op="edit" label="NEW CHART" /></c:if> </td>
 <td class="right" width="35%">AIRPORT <el:combo name="id" onChange="void updateAirport()" size="1" idx="*" options="${emptyList}" value="${airport}" />
 <el:text name="idCode" idx="*" size="4" max="4" value="${airport.ICAO}" onBlur="setAirport(document.forms[0].id, this.value); updateAirport();" /></td>
</tr>

<!-- Table Chart Data -->
<c:forEach var="chart" items="${viewContext.results}">
<c:set var="hasPDF" value="${chart.imgTypeName == 'PDF'}" scope="page" />
<c:set var="anyPDF" value="${anyPDF || hasPDF}" scope="page" />
<view:row entry="${chart}">
<c:choose>
<c:when test="${hasPDF}">
 <td colspan="2"><el:link url="/charts/${chart.hexID}.pdf" className="bld" target="chartView">${chart.name}</el:link></td>
 <td class="sec">${chart.typeName}</td>
<c:if test="${access.canEdit}"><td><el:cmd url="chart" link="${chart}" op="edit" className="small bld">EDIT</el:cmd></td></c:if>
 <td colspan="${cspan}">Adobe PDF document, <fmt:int fmt="###,###" value="${chart.size}" /> bytes</td>
</c:when>
<c:otherwise>
 <td colspan="2"><el:cmd className="bld" url="chart" link="${chart}">${chart.name}</el:cmd></td>
 <td class="sec">${chart.typeName}</td>
<c:if test="${access.canEdit}"><td><el:cmd url="chart" link="${chart}" op="edit" className="small bld">EDIT</el:cmd></td></c:if>
 <td colspan="${cspan}">${chart.imgTypeName} image, <fmt:int fmt="###,###" value="${chart.size}" /> bytes</td>
</c:otherwise>
</c:choose>
</view:row>
</c:forEach>

<c:if test="${anyPDF}">
<!-- Download Acrobat -->
<tr valign="middle">
 <td><a href="http://www.adobe.com/products/acrobat/readstep2.html" rel="external"><el:img src="library/getacro.png" border="0" caption="Download Adobe Acrobat Reader" /></a></td>
 <td colspan="4">Some approach charts require <span class="pri bld">Adobe Acrobat Reader 6</span> or newer 
in order to be viewed. If you are having difficulties viewing our charts, please click on the link to the 
left to download the latest version of Adobe Acrobat Reader. This is a free download.</td>
</tr>
</c:if>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="5">&nbsp;</td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
