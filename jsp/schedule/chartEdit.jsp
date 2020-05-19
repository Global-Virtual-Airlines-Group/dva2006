<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Approach Chart</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<content:json />
<content:js name="airportRefresh" />
<content:googleAnalytics eventSupport="true" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script async>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.airport, t:'Airport'});
	golgotha.form.validate({f:f.name, l:4, t:'Chart Name'});
	golgotha.form.validate({f:f.chartType, t:'Chart Type'});
	golgotha.form.validate({f:f.img, ext:['gif','jpg','png','pdf'], t:'Chart Image'});
	golgotha.form.submit(f);
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:empty var="emptyList" />
<content:enum var="chartTypes" className="org.deltava.beans.schedule.Chart$Type" exclude="UNKNOWN" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="chart.do" method="post" link="${chart}" op="save" allowUpload="true" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
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
 <td class="data"><el:combo className="req" name="airport" size="1" idx="*" firstEntry="[ SELECT AIRPORT ]" options="${emptyList}" value="${chart.airport}" />
 <el:text name="airportCode" idx="*" size="3" max="4" value="${chart.airport.ICAO}" onBlur="void document.forms[0].airport.setAirport(this.value)" /></td>
</tr>
<tr>
 <td class="label">Chart Name</td>
 <td class="data"><el:text className="pri bld req" name="name" idx="*" size="40" max="64" value="${chart.name}" /></td>
</tr>
<tr>
 <td class="label">Chart Type</td>
 <td class="data"><el:combo name="chartType" size="1" idx="*" options="${chartTypes}" className="req" firstEntry="[ SELECT TYPE ]" value="${chart.type}" /></td>
</tr>
<c:if test="${!empty chart}">
<tr>
 <td class="label">Image Properties</td>
 <td class="data sec">${chart.imgType} image, <fmt:int value="${chart.size}" /> bytes</td>
</tr>
<tr>
 <td class="label">Last Modified</td>
 <td class="data"><fmt:date date="${chart.lastModified}" t="HH:mm" /></td>
</tr>
</c:if>
<tr>
 <td class="label">Upload File</td>
 <td class="data"><el:file name="img" idx="*" className="small" size="96" max="192" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SAVE CHART" /><c:if test="${access.canDelete && (!empty chart)}">&nbsp;<el:cmdbutton url="chartdelete" link="${chart}" label="DELETE CHART" /></c:if></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<fmt:aptype var="useICAO" />
<script async>
golgotha.airportLoad.config.doICAO = ${useICAO};
golgotha.airportLoad.config.airlne = 'all';
const f = document.forms[0];
golgotha.airportLoad.setHelpers(f.airport);
f.airport.loadAirports(golgotha.airportLoad.config);
</script>
</body>
</html>
