<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Approach Chart - ${chart.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function setChart(combo)
{
var id = combo.options[combo.selectedIndex].value;
self.location = '/chart.do?id=' + id;
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<c:set var="filename" value="${chart.hexID}.${fn:lower(chart.imgTypeName)}" scope="request" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="chart.do" method="get" validate="return false">
<el:table className="form" pad="default" space="default">
<tr class="title">
 <td width="50%" class="caps">${chart.name} AT ${chart.airport.name} (<fmt:airport airport="${chart.airport}" />)</td>
 <td class="right">SELECT CHART <el:combo name="chart" size="1" idx="1" options="${charts}" value="${chart}" onChange="void setChart(this)" /></td>
</tr>
<tr>
<c:choose>
<c:when test="${isPDF}">
 <td colspan="2"><object width="100%" data="/charts/${fileName}" type="application/pdf" /></td>
</c:when>
<c:otherwise>
 <td colspan="2"><img alt="${chart.name}, ${chart.size} bytes" src="/charts/${fileName}" border="0" /></td>
</c:otherwise>
</c:choose>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:cmdbutton url="chart" link="${chart}" op="print" label="VIEW PRINTER-FRIENDLY PAGE" />
<c:if test="${access.canEdit}">
<el:cmdbutton url="chart" link="${chart}" op="edit" label="EDIT CHART" />
</c:if>
 </td> 
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
