<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> Pilot Map</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="googleMaps" />
<content:js name="pilotMap" />
<map:api version="2" />
<map:vml-ie />
<content:sysdata var="imgPath" name="path.img" />
<script language="JavaScript" type="text/javascript">
function reloadMap()
{
if (allMarkers.length > 0) {
	allMarkers.length = 0;
	map.clearOverlays();
}

// Load the map
addMarkers(map, 'hq');
var xmlreq = generateXMLRequest('${imgPath}');
xmlreq.send(null);
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body onunload="GUnload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="ranks" name="ranks" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="pilotboard.do" method="get" validate="return false">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> PILOT LOCATIONS<span id="isLoading" /></td>
</tr>
<tr>
 <td class="data" colspan="2"><map:div ID="googleMap" x="100%" y="625" /></td>
</tr>
<tr class="title caps">
 <td colspan="2">PILOT LOCATION FILTERING</td>
</tr>
<tr>
 <td class="label">Equipment Program</td>
 <td class="data"><el:combo name="eqType" size="1" firstEntry="ALL" options="${eqTypes}" onChange="void updateMarkers()" /></td>
</tr>
<tr>
 <td class="label">Pilot Ranks</td>
 <td class="data"><el:combo name="rank" size="1" firstEntry="ALL" options="${ranks}" onChange="void updateMarkers()" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="noFilter" value="true" checked="false" label="Disable range-based filtering (slow)" onChange="void reloadMap()" /></td>
</tr>
</el:table>

<content:filter roles="Pilot">
<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:cmdbutton url="geolocate" label="UPDATE MY LOCATION" /></td>
</tr>
</el:table>
</content:filter>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script language="JavaScript" type="text/javascript">
// Build the map
<map:point var="mapC" point="${mapCenter}" />
<map:marker var="hq" point="${hq}" />
var allMarkers = new Array();
var map = new GMap2(getElement("googleMap"), {mapTypes:[G_NORMAL_MAP, G_SATELLITE_MAP]});
map.addControl(new GLargeMapControl());
map.addControl(new GMapTypeControl());
map.setCenter(mapC, 6);
map.enableDoubleClickZoom();
map.enableContinuousZoom();
var mm = new GMarkerManager(map);
reloadMap();
</script>
<content:googleAnalytics />
</body>
</map:xhtml>
