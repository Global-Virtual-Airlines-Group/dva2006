<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> Airport Weather Finder</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<map:api version="3" />
<content:googleAnalytics eventSupport="true" />
<content:sysdata var="tileHost" name="weather.tileHost" />
<c:if test="${!empty tileHost}">
<content:js name="acarsMapWX" />
<content:js name="acarsMapFF" />
<content:js name="progressBar" />
</c:if>
<script type="text/javascript">
function validate(form)
{
return (form.ils.selectedIndex > 0);
}

function updateMap()
{
return document.forms[0].submit();	
}

function updateCenter()
{
var f = document.forms[0];
f.lat.value = map.getCenter().lat();
f.lng.value = map.getCenter().lng();
return true;
}
</script>
<map:wxList layers="radar,sat,temp,windspeed" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:getCookie name="acarsMapType" default="map" var="gMapType" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="wxfinder.do" method="post" validate="return validate(this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2" class="left"><content:airline /> AIRPORT WEATHER FINDER</td>
</tr>
<tr>
 <td class="label">ILS Category</td>
 <td class="data"><el:combo name="ils" size="1" idx="*" firstEntry="-" options="${ilsClasses}" value="${ils}" onChange="void updateMap()" /></td>
</tr>
<tr>
 <td class="label">Map Legend</td>
 <td class="data"><map:legend color="blue" legend="CATI" /> <map:legend color="yellow" legend="CATII" /> <map:legend color="orange" legend="CATIIIa" />
 <map:legend color="purple" legend="CATIIIb" /> <map:legend color="red" legend="CATIIIc" /></td>
</tr>
<tr>
 <td class="data" colspan="2"><map:div ID="googleMap" x="100%" y="480" /><div id="copyright" class="small mapTextLabel" style="bottom:17px; right:2px; visibility:hidden;"></div></td>
</tr>
</el:table>
<el:text type="hidden" name="lat" value="${param.lat}" />
<el:text type="hidden" name="lng" value="${param.lng}" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script type="text/javascript">
<map:point var="mapC" point="${mapCenter}" />
var mapTypes = {mapTypeIds: golgotha.maps.DEFAULT_TYPES};
var mapOpts = {center:mapC, zoom:4, minZoom:2, maxZoom:9, scrollwheel:false, streetViewControl:false, mapTypeControlOptions: mapTypes};

// Create the map
var map = new google.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content: ''});
google.maps.event.addListener(map, 'click', function () { map.infoWindow.close(); });
google.maps.event.addListener(map, 'maptypeid_changed', updateMapText);
google.maps.event.addListener(map, 'center_changed', updateCenter);

// Add airports
<map:markers var="wxAirports" items="${metars}" />
addMarkers(map, 'wxAirports');

<c:if test="${!empty tileHost}">
// Load the tile overlays
getTileOverlay('radar', 0.45);
getTileOverlay('eurorad', 0.45);
getTileOverlay('sat', 0.35);
getTileOverlay('temp', 0.25);
getTileOverlay('windspeed', 0.35);

// Build the layer controls
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXOverlayControl('Radar', ['radar', 'eurorad']));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXOverlayControl('Infrared', 'sat'));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXOverlayControl('Temperature', 'temp'));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXOverlayControl('Wind Speed', 'windspeed'));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXClearControl());
google.maps.event.addListener(map, 'maptypeid_changed', hideAllSlices);

// Display the copyright notice
var d = new Date();
var cp = document.getElementById('copyright');
cp.innerHTML = 'Weather Data &copy; ' + d.getFullYear() + ' The Weather Channel.'

//Update text color
google.maps.event.trigger(map, 'maptypeid_changed');</c:if>
</script>
</body>
</map:xhtml>
