<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Approach Chart - ${chart.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script>
golgotha.local.setChart = function(combo) {
	self.location = '/chart.do?id=' + golgotha.form.getCombo(combo);
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<c:set var="fileName" value="${chart.hexID}.${fn:lower(chart.imgType)}" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="chart.do" method="get" validate="return false">
<el:table className="form">
<tr class="title">
 <td style="width:50%" class="caps">${chart.name} AT ${chart.airport.name} (<fmt:airport airport="${chart.airport}" />)</td>
 <td class="right">SELECT CHART <el:combo name="chart" size="1" idx="1" options="${charts}" value="${chart}" onChange="void golgotha.local.setChart(this)" /></td>
</tr>
<tr>
<c:choose>
<c:when test="${isPDF}">
 <td colspan="2"><object width="100%" data="/charts/${fileName}" type="application/pdf"></object></td>
</c:when>
<c:otherwise>
 <td colspan="2"><img alt="${chart.name}, ${chart.size} bytes" src="/charts/${fileName}" class="noborder" /></td>
</c:otherwise>
</c:choose>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:cmdbutton url="chart" link="${chart}" op="print" label="VIEW PRINTER-FRIENDLY PAGE" />
<c:if test="${access.canEdit}">
<el:cmdbutton url="chart" link="${chart}" op="edit" label="EDIT CHART" /></c:if>
 </td> 
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
