<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_mapbox.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Interactive Route Map</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<map:api version="3" />
<content:js name="routeMap" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:googleAnalytics />
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="aCode" name="airline.code" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="routemap.do" method="post" validate="return false">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> INTERACTIVE ROUTE MAP <span id="isLoading"></span></td>
</tr>
<tr>
 <td class="label">Airline</td>
 <td class="data"><el:combo ID="airlineCode" name="airline" idx="*" size="1" options="${airlines}" value="${aCode}" firstEntry="[ SELECT AIRLINE ]" onChange="void golgotha.routeMap.updateAirports(this)" />
 <el:box ID="showInfo" name="showInfo" idx="*" value="true" className="small" label="Show Airport Information" checked="true" /></td>
</tr>
<tr>
 <td class="data" colspan="2"><map:div ID="mapBox" height="530" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script async>
<map:token />
<map:point var="golgotha.local.mapC" point="${mapCenter}" />

// Create the map
const mapOpts = {center:golgotha.local.mapC, zoom:4, minZOom:2, maxZoom:11, scrollZoom:false, style:'mapbox://styles/mapbox/outdoors-v12'};
const map = new golgotha.maps.Map(document.getElementById('mapBox'), mapOpts);
map.on('click', function() { map.removeMarkers(golgotha.routeMap.routes); golgotha.routeMap.routes = []; });
map.once('load', function() {
	map.addControl(new golgotha.maps.BaseMapControl(golgotha.maps.DEFAULT_TYPES), 'top-left');
	golgotha.routeMap.updateAirports(document.forms[0].airlineCode);	
});
</script>
</body>
</html>
