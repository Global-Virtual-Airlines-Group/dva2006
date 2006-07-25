<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> Preferred Routes for ${airportD}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<content:js name="googleMaps" />
<map:api version="2" current="true" />
<map:vml-ie />
<content:sysdata var="imgPath" name="path.img" />
<content:getCookie name="acarsMapZoomLevel" default="12" var="zoomLevel" />
<content:getCookie name="acarsMapType" default="map" var="gMapType" />
<script language="JavaScript" type="text/javascript">
var map;

function setAirportD(combo)
{
var ad = combo.options[combo.selectedIndex].value;
self.location = '/routes.do?op=domestic&id=' + ad;
return true;
}

function setAirportA(combo)
{
var f = document.forms[0];

// Get the departure airport
var ad = f.airportD.options[f.airportD.selectedIndex].value;
if (combo.selectedIndex == 0) {
	self.location = '/routes.do?op=domestic&id=' + ad;
} else {
	var aa = combo.options[combo.selectedIndex].value;
	self.location = '/routes.do?op=domestic&id=' + ad + '&airportA=' + aa;
}

return true;
}

function showMap(route)
{
// Get the map DIV
var mapdiv = getElement('mapTable');
if (mapdiv.className == 'hidden')
	mapdiv.className = 'visible';

// Create the map
if (!map) {
	map = new GMap2(getElement('googleMap'), G_DEFAULT_MAP_TYPES);
	map.addControl(new GLargeMapControl());
	map.addControl(new GMapTypeControl());
	map.setMapType(${gMapType == 'map' ? 'G_MAP_TYPE' : 'G_SATELLITE_TYPE'});
	map.setCenter(new GLatLng(38.88, -93.25), 4);
}

// Generate an XMLHTTP request
var isLoading = getElement('isLoading');
if (isLoading)
	isLoading.innerHTML = " - LOADING...";

var xmlreq = GXmlHttp.create();
xmlreq.open("GET", "route.ws?route=" + escape(route), true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	map.clearOverlays();
	
	// Draw the markers
	var positions = new Array();
	var xmlDoc = xmlreq.responseXML;
	var waypoints = xmlDoc.documentElement.getElementsByTagName("pos");
	for (var i = 0; i < waypoints.length; i++) {
		var wp = waypoints[i];
		var label = wp.firstChild;
		var p = new GLatLng(parseFloat(wp.getAttribute("lat")), parseFloat(wp.getAttribute("lng")));
		positions.push(p);
		map.addOverlay(googleMarker('${imgPath}', wp.getAttribute('color'), p, label.data));
	} // for
	
	// Draw the route
	map.addOverlay(new GPolyline(positions, '#4080AF', 2, 0.8));
	
	// Get the midpoint and center the map
	var mps = xmlDoc.documentElement.getElementsByTagName("midpoint");
	var mpp = mps[0];
	var mp = new GLatLng(parseFloat(mpp.getAttribute("lat")), parseFloat(mpp.getAttribute("lng")));
	map.centerAndZoom(mp, getDefaultZoom(parseInt(mpp.getAttribute("distance"))));
	
	// Focus on the map
	if (isLoading)
		isLoading.innerHTML = "";
		
	return true;
}

xmlreq.send(null);
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
<el:form action="routes.do" method="get" validate="return false">
<view:table className="view" pad="default" space="default" cmd="routes">

<!-- Table Header Bar -->
<tr class="title">
 <td width="20%">DESTINATION</td>
 <td width="15%">ARTCCs</td>
 <td width="5%">&nbsp;</td>
 <td width="6%" class="left">ROUTE</td>
 <td class="right">FROM <el:combo name="airportD" idx="*" size="1" className="small" options="${airports}" value="${airportD}" onChange="void setAirportD(this)" /> TO
 <el:combo name="airportA" idx="*" size="1" className="small" firstEntry="ALL" options="${dstAP}" value="${airportA}" onChange="void setAirportA(this)" /></td>
</tr>

<!-- Table Data Section -->
<c:forEach var="route" items="${viewContext.results}">
<tr>
 <td class="pri small">${route.airportA.name} (<fmt:airport airport="${route.airportA}" />)</td>
 <td class="sec small">${route.ARTCC}</td>
 <td><el:button className="BUTTON" onClick="void showMap('${route.route}')" label="VIEW" /></td>
 <td colspan="2" class="left">${route.route}</td>
</tr>
</c:forEach>

<!-- Scroll bar -->
<tr class="title">
 <td colspan="5">
<c:if test="${access.canDelete && (!empty viewContext.results)}">
<el:cmdbutton url="routepurge" op="domestic" label="PURGE DOMESTIC ROUTES" />
</c:if>
<c:if test="${access.canImport}">
&nbsp;<el:cmdbutton url="routeimport" label="IMPORT DOMESTIC ROUTES" />
</c:if>
<view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<div id="mapTable" class="hidden">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">PREFERRED ROUTE MAP<span id="isLoading" /></td>
</tr>
<tr>
 <td class="label">Legend</td>
 <td class="data"><map:legend color="blue" legend="VOR" /> <map:legend color="orange" legend="NDB" />
 <map:legend color="green" legend="Airport" /> <map:legend color="white" legend="Intersection" /></td>
</tr>
<tr>
 <td class="label" valign="top">Route Map</td>
 <td class="data"><map:div ID="googleMap" x="100%" y="550" /></td>
</tr>
</el:table>
</div>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</map:xhtml>
