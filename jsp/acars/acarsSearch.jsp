<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> ACARS Flight Report Search</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<content:js name="datePicker" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.searchType, 'Search Type')) return false;

setSubmit();
disableButton('MapButton');
disableButton('SearchButton');
return true;
}

function showMap(form)
{
if (!checkSubmit()) return false;

// Build the URL
var isChecked = false;
var url = 'acars_earth.ws?showData=false&showRoute=' + form.showRoute.checked + '&id=';
if (form.doMap.length) {
	for (var x = 0; x < form.doMap.length; x++) {
		isChecked = isChecked || form.doMap[x].checked;
		if (form.doMap[x].checked)
			url = url + form.doMap[x].value + ',';
	}
	
	// Strip trailing comma
	url = url.substring(0, url.length - 1);
} else {
	isChecked = form.doMap.checked;
	if (form.doMap.checked)
		url = url + form.doMap.value;
}

// Check if we've selected a single flight
if (!isChecked) {
	alert('At least one Flight Report must be selected to view in Google Earth.');
	return false;
}

// Display the url
self.location = url;
return false;
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
<el:form action="acarsprsearch.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
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
 <el:button className="BUTTON" label="CALENDAR" onClick="void show_calendar('forms[0].flightDate')" /></td>
</tr>
<tr class="title">
 <td colspan="4" class="mid"><el:button ID="SearchButton" type="submit" className="BUTTON" label="SEARCH FLIGHTS" /></td>
</tr>
</el:table>
</el:form>
<c:if test="${doSearch}">
<el:form action="acarsprsearch.do" method="get" validate="return showMap(this)">
<view:table className="view" space="default" pad="default" cmd="acarsprsearch">
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
 <td colspan="4" class="left caps">SEARCH RESULTS</td>
 <td colspan="2" class="right"><el:box name="showRoute" idx="*" value="true" label="Show Filed Route" /></td>
</tr>

<!-- Table Header Data -->
<tr class="title">
 <td width="10%">DATE</td>
 <td width="5%">MAP</td>
 <td width="15%">FLIGHT NUMBER</td>
 <td width="45%">AIRPORT NAMES</td>
 <td width="10%">EQUIPMENT</td>
 <td>DURATION</td>
</tr>

<!-- Table Flight Report Data -->
<c:forEach var="pirep" items="${viewContext.results}">
<view:row entry="${pirep}">
 <td class="title"><fmt:date date="${pirep.date}" fmt="d" default="-" /></td>
 <td><el:box name="doMap" value="${fn:ACARS_ID(pirep)}" idx="*" label="" /></td>
 <td><el:cmd className="bld" url="pirep" link="${pirep}">${pirep.flightCode}</el:cmd></td>
 <td class="small">${pirep.airportD.name} (<fmt:airport airport="${pirep.airportD}" />) - 
 ${pirep.airportA.name} (<fmt:airport airport="${pirep.airportA}" />)</td>
 <td class="sec">${pirep.equipmentType}</td>
 <td><fmt:dec fmt="#0.0" value="${pirep.length / 10}" /> hours</td>
</view:row>
</c:forEach>

<!--  Scroll Bar -->
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn />&nbsp;</view:scrollbar><el:button ID="MapButton" type="submit" className="BUTTON" label="DISPLAY USING GOOGLE EARTH" /></td>
</tr>
</c:if>
</view:table>
</el:form>
</c:if>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
