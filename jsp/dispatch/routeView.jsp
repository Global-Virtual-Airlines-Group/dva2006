<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_mapbox.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Dispatcher Route #<fmt:int value="${route.ID}" /></title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<map:api version="3" />
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<tr class="title caps">
 <td colspan="4">ACARS DISPATCH ROUTE - ROUTE #<fmt:int value="${route.ID}" /></td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data">${route.airportD.name} (<fmt:airport airport="${route.airportD}" />)</td>
 <td class="label">Departure Procedure</td>
 <td class="data pri">${(empty route.SID) ? 'NONE' : route.SID}</td>
</tr>
<tr>
 <td class="label">Arriving at</td>
 <td class="data">${route.airportA.name} (<fmt:airport airport="${route.airportA}" />)</td>
 <td class="label">Arrival Route</td>
 <td class="data pri">${(empty route.STAR) ? 'NONE' : route.STAR}</td>
</tr>
<c:if test="${!empty route.airportL}">
<tr>
 <td class="label">Alternate</td>
 <td class="data" colspan="3">${route.airportL.name} (<fmt:airport airport="${route.airportL}" />)</td>
</tr>
</c:if>
<tr>
 <td class="label">Dispatcher Name</td>
 <td class="data"><span class="pri bld">${author.name}</span> (${author.pilotCode}) <span class="small">using
<c:if test="${route.dispatchBuild > 0}"> ACARS Dispatch Build <fmt:int value="${route.dispatchBuild}" /></c:if>
<c:if test="${route.dispatchBuild == 0}"> Web Application</c:if></span></td>
 <td class="label">Created on</td>
 <td class="data"><span class="bld"><fmt:date date="${route.createdOn}" d="MM/dd/yyyy" fmt="d" /></span>
<c:if test="${route.useCount > 0}"> (used <fmt:int value="${route.useCount}" /> times, last on <fmt:date date="${route.lastUsed}" t="HH:mm" />)</c:if></td>
</tr>
<c:if test="${!empty route.comments}">
<tr>
 <td class="label top">Dispatcher Comments</td>
 <td class="data" colspan="3"><fmt:text value="${route.comments}" /></td>
</tr>
</c:if>
<c:if test="${!route.active}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data" colspan="3"><span class="warn bld caps">THIS ROUTE IS CURRENTLY DISABLED</span></td>
</tr>
</c:if>
<tr>
 <td class="label top">Flight Route</td>
 <td class="data" colspan="3">${route.route}</td>
</tr>
<c:if test="${waypoints.size() > 0}">
<tr>
 <td class="data" colspan="4"><map:div ID="mapBox" height="500" /></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td>&nbsp;<c:if test="${access.canDelete}"><el:cmdbutton url="dsproutedelete" link="${route}" label="DELETE DISPATCHER ROUTE" /></c:if>
<c:if test="${access.canDisable}"> <el:cmdbutton url="dsproutetoggle" link="${route}" label="${route.active ? 'DISABLE ROUTE' : 'ENABLE ROUTE'}" /></c:if>
</td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<c:if test="${waypoints.size() > 0}">
<script async>
<map:token />

// Build the route line and map center
<map:point var="golgotha.local.mapC" point="${mapCenter}" />
<map:points var="pnts" items="${waypoints}" />
<map:markers var="golgotha.local.mrks" items="${waypoints}" />
<map:line var="route" src="pnts" color="#4080af" width="2" transparency="0.7" />
<map:bounds var="golgotha.local.bb" items="${route.airports}" />

// Build the map
const mapOpts = {center:golgotha.local.mapC, minZoom:3, maxZoom:14, bounds:golgotha.local.bb, fitBoundsOptions:{padding:48}, scrollZoom:false, projection:'globe', style:'mapbox://styles/mapbox/outdoors-v12'};
const map = new golgotha.maps.Map(document.getElementById('mapBox'), mapOpts);
map.addControl(new mapboxgl.FullscreenControl(), 'top-right');
map.addControl(new mapboxgl.NavigationControl(), 'top-right');
map.on('style.load', golgotha.maps.updateMapText);
map.once('load', function() {
	map.addControl(new golgotha.maps.BaseMapControl(golgotha.maps.DEFAULT_TYPES), 'top-left');
	
	// Add the route and markers
	map.addLine(route);
	map.addMarkers(golgotha.local.mrks);
});
</script></c:if>
<content:googleAnalytics />
</body>
</html>
