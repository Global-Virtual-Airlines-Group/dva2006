<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Approach Chart - ${chart.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<script language="JavaScript" type="text/javascript">
function setChart(combo)
{
var id = combo.options[combo.selectedIndex].value;
self.location = '/viewchart.do?id=' + id;
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<el:form action="viewchart.do" method="GET" validate="return false">
<el:table className="form" pad="default" space="default">
<tr class="title">
 <td width="50%" class="caps">${chart.name} AT ${chart.airport.name} (<fmt:airport airport="${chart.airport}" />)</td>
 <td class="right">SELECT CHART <el:combo name="chart" size="1" idx="1" options="${charts}" value="${chart}" onChange="void setChart(this)" /></td>
</tr>
<tr>
 <td colspan="2"><img alt="${chart.name}, ${chart.size} bytes" src="/charts/0x<fmt:hex value="${chart.ID}" />" border="0" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:cmd url="chart" linkID="0x${chart.ID}" op="print">VIEW PRINTER-FRIENDLY PAGE</el:cmd>
<c:if test="${access.canEdit}">
<el:cmd url="chart" linkID="0x${chart.ID}" op="edit">EDIT CHART</el:cmd>
</c:if>
 </td> 
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</div>
</body>
</html>
