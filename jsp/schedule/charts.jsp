<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Approach Charts</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:js name="airportRefresh" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function updateAirport(combo)
{
var ac = combo.options[combo.selectedIndex].value;
self.location = '/charts.do?id=' + ac.toUpperCase();
return true;
}

function setAirportCode(code)
{
var f = document.forms[0];
if (setAirport(f.airport, code))
	updateAirport(f.airport);

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
<el:form action="charts.do" method="get" validate="return false">
<view:table className="view" pad="default" space="default" cmd="charts">

<!-- Table Header Bar-->
<tr class="title">
 <td width="30%">CHART NAME</td>
 <td width="30%">CHART TYPE</td>
 <td class="right">AIRPORT <el:combo name="airport" onChange="void updateAirport(this)" size="1" idx="1" options="${airports}" value="${airport}" />
 <el:text name="airportDCode" idx="*" size="2" max="3" value="${airport.IATA}" onBlur="void setAirportCode(this.value)" /></td>
</tr>

<!-- Table Pilot Data -->
<c:forEach var="chart" items="${charts}">
<tr>
 <td><el:cmd className="bld" url="chart" linkID="0x${chart.ID}">${chart.name}</el:cmd></td>
 <td class="sec">${chart.typeName}</td>
 <td>${chart.imgTypeName} image, <fmt:int fmt="###,###" value="${chart.size}" /> bytes</td>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="3">&nbsp;</td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
