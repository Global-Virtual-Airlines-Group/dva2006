<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Flight Report Search</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:js name="datePicker" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.searchType, t:'Search Type'});
golgotha.form.submit(f);
return true;
};

golgotha.local.showMap = function(f)
{
if (!golgotha.form.check()) return false;

// Build the URL
let isChecked = false;
let url = 'acars_earth.ws?showData=false&showRoute=' + form.showRoute.checked + '&id=';
if (f.doMap.length) {
	for (var x = 0; x < f.doMap.length; x++) {
		isChecked = isChecked || f.doMap[x].checked;
		if (f.doMap[x].checked)
			url = url + f.doMap[x].value + ',';
	}
	
	// Strip trailing comma
	url = url.substring(0, url.length - 1);
} else {
	isChecked = f.doMap.checked;
	if (f.doMap.checked)
		url = url + f.doMap.value;
}

// Check if we've selected a single flight
if (!isChecked) {
	alert('At least one Flight Report must be selected to view in Google Earth.');
	return false;
}

// Display the url
self.location = url;
return false;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="acarsprsearch.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="4">ACARS FLIGHT REPORTS</td>
</tr>
<tr>
 <td class="label">Search Type</td>
 <td class="data" colspan="3"><el:combo name="searchType" idx="*" size="1" firstEntry="-" options="${searchTypes}" value="${param.searchType}" /></td>
</tr>
<tr>
 <td class="label">Online Event</td>
 <td class="data"><c:if test="${empty events}">NONE</c:if>
<c:if test="${!empty events}"><el:combo name="eventID" idx="*" size="1" options="${events}" value="${param.eventID}" /></c:if></td>
 <td class="label">Flight Date</td>
 <td class="data"><el:text name="flightDate" idx="*" size="8" max="10" value="${param.flightDate}" />
 <el:button label="CALENDAR" onClick="void show_calendar('forms[0].flightDate')" /></td>
</tr>
<tr class="title">
 <td colspan="4" class="mid"><el:button type="submit" label="SEARCH FLIGHTS" /></td>
</tr>
</el:table>
</el:form>
<c:if test="${doSearch}">
<el:form action="acarsprsearch.do" method="get" validate="return golgotha.local.showMap(this)">
<view:table cmd="acarsprsearch">
<c:if test="${empty viewContext.results}">
<!-- Search Results -->
<tr class="title">
 <td colspan="6" class="left caps">SEARCH RESULTS</td>
</tr>
<tr>
 <td colspan="6" class="pri bld">NO ACARS FLIGHT REPORTS WERE FOUND.</td>
</tr>
</c:if>
<c:if test="${!empty viewContext.results}">
<!-- Search Results -->
<tr class="title">
 <td colspan="3" class="left caps">SEARCH RESULTS</td>
 <td colspan="3" class="right"><el:box name="showRoute" idx="*" value="true" label="Show Filed Route" /></td>
</tr>

<!-- Table Header Data -->
<tr class="title">
 <td style="width:10%">DATE</td>
 <td style="width:5%">MAP</td>
 <td style="width:15%">FLIGHT NUMBER</td>
 <td class="nophone" style="width:45%">AIRPORT NAMES</td>
 <td style="width:10%">EQUIPMENT</td>
 <td class="nophone">DURATION</td>
</tr>

<!-- Table Flight Report Data -->
<c:forEach var="pirep" items="${viewContext.results}">
<view:row entry="${pirep}">
 <td class="title"><fmt:date date="${pirep.date}" fmt="d" default="-" /></td>
 <td><el:box name="doMap" value="${fn:ACARS_ID(pirep)}" idx="*" label="" /></td>
 <td><el:cmd className="bld" url="pirep" link="${pirep}">${pirep.flightCode}</el:cmd></td>
 <td class="small nophone">${pirep.airportD.name} (<fmt:airport airport="${pirep.airportD}" />) - 
 ${pirep.airportA.name} (<fmt:airport airport="${pirep.airportA}" />)</td>
 <td class="sec">${pirep.equipmentType}</td>
 <td class="nophone"><fmt:dec fmt="#0.0" value="${pirep.length / 10}" /> hours</td>
</view:row>
</c:forEach>

<!--  Scroll Bar -->
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn />&nbsp;</view:scrollbar><el:button type="submit" label="DISPLAY USING GOOGLE EARTH" /></td>
</tr>
</c:if>
</view:table>
</el:form>
</c:if>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
