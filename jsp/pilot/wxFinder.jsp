<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Airport Weather Finder</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<map:api version="3" libraries="weather" />
<content:js name="googleMapsWX" />
<content:googleAnalytics eventSupport="true" />
<content:sysdata var="tileHost" name="weather.tileHost" />
<c:if test="${!empty tileHost}">
<script type="text/javascript">
var gsLoader;
gsLoader = new golgotha.maps.GinsuLoader(2);
gsLoader.setData('radar', 0.45, 'wxRadar');
gsLoader.setData('eurorad', 0.45, 'wxRadar');
gsLoader.setData('temp', 0.275, 'wxTemp');
gsLoader.setData('windspeed', 0.325, 'wxTemp');
</script>
<map:wxList layers="radar,eurorad,temp,windspeed" function="gsLoader.load" max="2" /></c:if>
<script type="text/javascript">
function filterTypes(combo)
{
var minIDX = Math.max(1, combo.selectedIndex + 1);
for (var x = 0; x < wxAirports.length; x++) {
	var mrk = wxAirports[x];
	if ((mrk.ILS < minIDX) && mrk.getVisible())
		mrk.setVisible(false);
	else if ((mrk.ILS >= minIDX) && !mrk.getVisible())
		mrk.setVisible(true);
}
	
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:getCookie name="acarsMapType" default="map" var="gMapType" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="wxfinder.do" method="get" validate="return false">
<el:table className="form">
<tr class="title caps">
 <td colspan="2" class="left"><content:airline /> AIRPORT WEATHER FINDER</td>
</tr>
<tr>
 <td class="label">ILS Category</td>
 <td class="data"><el:combo name="ils" size="1" idx="*" options="${ilsClasses}" onChange="void filterTypes(this)" /></td>
</tr>
<tr>
 <td class="label">Map Legend</td>
 <td class="data"><map:legend color="green" legend="CATI" /> <map:legend color="blue" legend="CATII" /> <map:legend color="orange" legend="CATIIIa" />
 <map:legend color="purple" legend="CATIIIb" /> <map:legend color="red" legend="CATIIIc" /></td>
</tr>
<tr>
 <td class="data" colspan="2"><map:div ID="googleMap" x="100%" y="480" /><div id="copyright" class="small mapTextLabel"></div>
<div id="mapStatus" class="small mapTextLabel"></div></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script type="text/javascript">
<map:point var="mapC" point="${mapCenter}" />
var mapTypes = {mapTypeIds:golgotha.maps.DEFAULT_TYPES};
var mapOpts = {center:mapC, zoom:4, minZoom:2, maxZoom:9, scrollwheel:false, streetViewControl:false, mapTypeControlOptions:mapTypes};

// Create the map
var map = new google.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', function () { map.infoWindow.close(); });
google.maps.event.addListener(map, 'maptypeid_changed', golgotha.maps.updateMapText);
google.maps.event.addListener(map, 'zoom_changed', golgotha.maps.updateZoom);
map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(document.getElementById('copyright'));
map.controls[google.maps.ControlPosition.LEFT_BOTTOM].push(document.getElementById('mapStatus'));

// Add airports -- don't use the standard TAG so we can add some info to the tags
var wxAirports = [];
<c:forEach var="ap" items="${metars}">
<map:marker var="mrk" marker="true" point="${ap}" color="${ap.iconColor}" label="${ap.infoBox}" />
mrk.ILS = ${ap.ILS.ordinal()};
wxAirports.push(mrk);
</c:forEach>
addMarkers(map, 'wxAirports');
<c:if test="${!empty tileHost}">
// Build the layer controls
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerSelectControl(map, 'Radar', [gsLoader.getLatest('radar'), gsLoader.getLatest('eurorad')]));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerSelectControl(map, 'Temperature', gsLoader.getLatest('temp')));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerSelectControl(map, 'Wind Speed', gsLoader.getLatest('windspeed')));
</c:if>
// Build the standard weather layers 
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerSelectControl(map, 'Clouds', new google.maps.weather.CloudLayer()));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new golgotha.maps.LayerClearControl(map));

// Update text color
google.maps.event.trigger(map, 'maptypeid_changed');
</script>
</body>
</html>
