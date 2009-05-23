<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> ACARS Dispatcher Route - <fmt:int value="${route.ID}" /></title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<content:js name="googleMaps" />
<map:api version="2" />
<map:vml-ie />
</head>
<content:copyright visible="false" />
<body onunload="GUnload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="imgPath" name="path.img" />
<content:getCookie name="acarsMapType" default="map" var="gMapType" />

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form" space="default" pad="default">
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
<c:if test="${fn:sizeof(waypoints) > 0}">
<tr>
 <td class="label top">Route Map</td>
 <td class="data" colspan="3"><map:div ID="googleMap" x="100%" y="500" /></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td>&nbsp;<c:if test="${access.canDelete}"><el:cmdbutton url="dsproutedelete" link="${route}" label="DELETE DISPATCHER ROUTE" /></c:if>
<c:if test="${access.canDisable}"> <el:cmdbutton url="dsproutetoggle" link="${route}" label="${route.active ? 'DISABLE ROUTE' : 'ENABLE ROUTE'}" /></c:if>
</td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<c:if test="${fn:sizeof(waypoints) > 0}">
<script language="JavaScript" type="text/javascript">
// Build the route line and map center
<map:point var="mapC" point="${mapCenter}" />
<map:points var="pnts" items="${waypoints}" />
<map:markers var="mrks" items="${waypoints}" />
<map:line var="route" src="pnts" color="#4080AF" width="2" transparency="0.7" geodesic="true" />

// Build the map
var map = new GMap2(getElement("googleMap"), {mapTypes:[G_NORMAL_MAP, G_SATELLITE_MAP, G_PHYSICAL_MAP]});
map.addControl(new GLargeMapControl3D());
map.addControl(new GMapTypeControl());
map.setCenter(mapC, getDefaultZoom(${distance}));
map.enableDoubleClickZoom();
map.enableContinuousZoom();
<map:type map="map" type="${gMapType}" default="G_PHYSICAL_MAP" />

// Add the route and markers
addMarkers(map, 'route');
addMarkers(map, 'mrks');
</script></c:if>
<content:googleAnalytics />
</body>
</map:xhtml>
