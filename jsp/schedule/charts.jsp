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
<content:pics />
<script language="JavaScript" type="text/javascript">
function updateAirport(combo)
{
document.forms[0].submit();
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body onload="void updateAirports(document.forms[0].airport, 'airline=charts', false, '${airport.ICAO}')">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="charts.do" method="post" validate="return true">
<view:table className="view" pad="default" space="default" cmd="charts">

<!-- Table Header/Filter Bars-->
<tr class="title caps">
 <td colspan="4" class="left"><content:airline /> AIRPORT / APPROACH / PROCEDURE CHARTS</td>
</tr>
<tr>
 <td class="priB right" style="color: #FFFFFF; font-size: 8pt;">Filter Options</td>
 <td colspan="3" width="90%" class="left"><el:check name="chartType" className="small" idx="*" width="180" options="${chartTypes}" checked="${selectedTypes}" />
 <el:button type="submit" className="BUTTON" label="UPDATE" /></td>
</tr>
<tr class="title">
 <td colspan="2">CHART NAME</td>
 <td width="25%">CHART TYPE</td>
 <td class="right" width="40%">AIRPORT <el:combo name="airport" onChange="void updateAirport(this)" size="1" idx="*" options="${emptyList}" value="${airport}" />
 <el:text name="airportDCode" idx="*" size="4" max="4" value="${airport.ICAO}" onBlur="setAirport(document.forms[0].airport, this.value); updateAirport(document.forms[0].airport);" /></td>
</tr>

<!-- Table Pilot Data -->
<c:forEach var="chart" items="${charts}">
<c:set var="hasPDF" value="${chart.imgTypeName == 'PDF'}" scope="request" />
<c:set var="anyPDF" value="${anyPDF || hasPDF}" scope="request" />
<view:row entry="${chart}">
<c:choose>
<c:when test="${hasPDF}">
 <td colspan="2"><el:link url="/charts/${chart.hexID}.pdf" className="bld" target="chartView">${chart.name}</el:link></td>
 <td class="sec">${chart.typeName}</td>
 <td>Adobe PDF document, <fmt:int fmt="###,###" value="${chart.size}" /> bytes</td>
</c:when>
<c:otherwise>
 <td colspan="2"><el:cmd className="bld" url="chart" link="${chart}">${chart.name}</el:cmd></td>
 <td class="sec">${chart.typeName}</td>
 <td>${chart.imgTypeName} image, <fmt:int fmt="###,###" value="${chart.size}" /> bytes</td>
</c:otherwise>
</c:choose>
</view:row>
</c:forEach>

<c:if test="${anyPDF}">
<!-- Download Acrobat -->
<tr valign="middle">
 <td><a href="http://www.adobe.com/products/acrobat/readstep2.html" rel="external"><el:img src="library/getacro.png" border="0" caption="Download Adobe Acrobat Reader" /></a></td>
 <td colspan="3">Some approach charts require <span class="pri bld">Adobe Acrobat Reader 6</span> or newer 
in order to be viewed. If you are having difficulties viewing our charts, please click on the link to the 
left to download the latest version of Adobe Acrobat Reader. This is a free download.</td>
</tr>
</c:if>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="4">&nbsp;</td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
