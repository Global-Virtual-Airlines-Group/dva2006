<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> Interactive Route Map</title>
<content:sysdata var="imgPath" name="path.img" />
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="googleMaps" />
<content:js name="routeMap" />
<map:api version="2" />
<map:vml-ie />
<content:sysdata var="aCode" name="airline.code" />
<content:getCookie name="acarsMapZoomLevel" default="5" var="zoomLevel" />
<content:getCookie name="acarsMapType" default="map" var="gMapType" />
</head>
<content:copyright visible="false" />
<body onunload="GUnload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<!-- Main Body Frame -->
<content:region id="main">
<el:form action="routemap.do" method="post" validate="return false">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> INTERACTIVE ROUTE MAP <span id="isLoading" /></td>
</tr>
<c:set var="alCount" value="${0}" scope="request" />
<tr>
 <td class="label">Airline</td>
 <td class="data"><el:combo ID="airlineCode" name="airline" idx="*" size="1" options="${airlines}" value="${aCode}" firstEntry="-" onChange="void updateAirports(this)" />
 <el:box ID="showInfo" name="showInfo" idx="*" value="true" className="small" label="Show Airport Information" checked="true" /></td>
</tr>
<tr>
 <td class="label" valign="top">Route Map</td>
 <td class="data"><map:div ID="googleMap" x="650" y="575" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script language="JavaScript" type="text/javascript">
<map:point var="mapC" point="${mapCenter}" />
document.imgPath = '${imgPath}';

// Create the map
var map = new GMap2(getElement('googleMap'), G_DEFAULT_MAP_TYPES);
map.addControl(new GLargeMapControl());
map.addControl(new GMapTypeControl());
map.setCenter(mapC, ${zoomLevel});
map.setMapType(${gMapType == 'map' ? 'G_MAP_TYPE' : 'G_SATELLITE_TYPE'});

// Routes placeholder
var routes;

// Save airports in JS array
var airports = new Array();
updateAirports(getElement('airlineCode'));
</script>
<content:googleAnalytics />
</body>
</map:xhtml>
