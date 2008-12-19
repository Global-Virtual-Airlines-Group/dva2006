<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> Pilot Location - ${pilot.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="googleMaps" />
<content:sysdata var="imgPath" name="path.img" />
<map:api version="2" />
<map:vml-ie />
<content:googleAnalytics eventSupport="true" />
<script language="JavaScript" type="text/javascript">
function updateLocation()
{
var f = document.forms[0];

// Calculate latitude/longitude
var lat = parseInt(f.latD.value) + (parseInt(f.latM.value) / 60) + (parseInt(f.latS.value) / 3600);
lat *= (f.latDir.selectedIndex * -1);
var lng = parseInt(f.lonD.value) + (parseInt(f.lonM.value) / 60) + (parseInt(f.lonS.value) / 3600);
lng *= (f.lonDir.selectedIndex * -1);

map.removeOverlay(usrLocation);
usrLocation = googleMarker('${imgPath}','blue',new GLatLng(lat, lng),labelText);
map.addOverlay(usrLocation);
gaEvent('Pilot Map', 'Update Location');
return true;
}

function geoLocate(addr)
{
if (addr.value.length < 3) return false;

// Do the lookup
var isLoading = getElement('isLoading');
isLoading.innerHTML = ' - SEARCHING...';
disableButton('SearchButton');
geoCoder.getLocations(addr.value, showResponse);
return true;
}

function buildIcon()
{
// Build the marker icon
var icon = new GIcon();
icon.image = '/${imgPath}/maps/point_blue.png';
icon.shadow = '/${imgPath}/maps/shadow.png';
icon.iconSize = new GSize(12, 20);
icon.shadowSize = new GSize(22, 20);
icon.iconAnchor = new GPoint(6, 20);
icon.infoWindowAnchor = new GPoint(5, 1);
return icon;
}

function showResponse(response)
{
var f = document.forms[0];
var isLoading = getElement('isLoading');
isLoading.innerHTML = '';
enableElement('SearchButton', true);

// Check for failure
if ((!response) || (response.Status.code != 200)) {
	alert('Cannot find "' + f.geoAddr.value + '"!');
	return false;
}

// Get the placemark
var pm = response.Placemark[0];
var p = point = new GLatLng(pm.Point.coordinates[1], pm.Point.coordinates[0]);
var lbl = '<span class="small"><b>' + pm.address + '<\/b><br /><br /><a href="javascript:void setLatLon(null, new GPoint('
	+ p.lng() + ',' +  p.lat() + '));">SET LOCATION</a></span>';
var mrk = googleMarker('${imgPath}', 'white', p, lbl);
GEvent.addListener(mrk, 'infowindowclose', function() { map.removeOverlay(mrk); });
map.addOverlay(mrk);
map.setCenter(p, 14);
gaEvent('Pilot Map', 'Geolocate');
return true;
}

function setLatLon(overlay, geoPosition)
{
var f = document.forms[0];
map.removeOverlay(usrLocation);

// Update Latitude
var isSouth = (geoPosition.y < 0);
f.latD.value = Math.abs((isSouth) ? Math.ceil(geoPosition.y) : Math.floor(geoPosition.y));
var latF = Math.abs(geoPosition.y) - parseInt(f.latD.value);
f.latM.value = Math.floor(latF * 60);
f.latS.value = Math.floor((latF % (1/60)) * 3600);
f.latDir.selectedIndex = (isSouth) ? 1 : 0;

// Update Longitude
var isWest = (geoPosition.x < 0);
f.lonD.value = Math.abs((isWest) ? Math.ceil(geoPosition.x) : Math.floor(geoPosition.x));
var lonF = Math.abs(geoPosition.x) - parseInt(f.lonD.value);
f.lonM.value = Math.floor(lonF * 60);
f.lonS.value = Math.floor((lonF % (1/60)) * 3600);
f.lonDir.selectedIndex = (isWest) ? 1 : 0;

// Build the marker
var icon = buildIcon();
usrLocation = new GMarker(geoPosition, {icon:icon, bouncy:true, draggable:true, bounceGravity:0.8});
GEvent.addListener(usrLocation, "dragend", function() { setLatLon(usrLocation, usrLocation.getPoint()); } );
map.addOverlay(usrLocation);
map.closeInfoWindow();
return true;
}

function validate(form)
{
if (!checkSubmit()) return false;

setSubmit();
disableButton('SaveButton');
disableButton('SearchButton');
disableButton('DeleteButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body onunload="GUnload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="geolocate.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">PILOT LOCATION <span id="isLoading" /></td>
</tr>
<tr>
 <td class="label" valign="top">Map</td>
 <td class="data"><c:if test="${empty location}"><span class="small">You have not selected your 
location. Please click on the map below to set your location. You can drag the map with your 
mouse and zoom in and out.</span><br />
<span class="small sec bld"><u>NOTE</u>: To protect your privacy, the system will automatically 
randomize your location within a 3 mile circle each time the Pilot Location Board is displayed.</span><br /></c:if>
<map:div ID="googleMap" x="100%" y="570" /></td>
</tr>
<tr>
 <td class="label">Latitude</td>
 <td class="data"><el:text name="latD" idx="*" size="2" max="2" value="${latD}" onBlur="void updateLocation()" /> degrees 
<el:text name="latM" idx="*" size="2" max="2" value="${latM}" onBlur="void updateLocation()" /> minutes 
<el:text name="latS" idx="*" size="2" max="2" value="${latS}" onBlur="void updateLocation()" /> seconds 
<el:combo name="latDir" idx="*" size="1" options="${latDir}" value="${latNS}" onChange="void updateLocation()" /></td>
</tr>
<tr>
 <td class="label">Longitude</td>
 <td class="data"><el:text name="lonD" idx="*" size="2" max="4" value="${lonD}" onBlur="void updateLocation()" /> degrees 
<el:text name="lonM" idx="*" size="2" max="2" value="${lonM}" onBlur="void updateLocation()" /> minutes 
<el:text name="lonS" idx="*" size="2" max="2" value="${lonS}" onBlur="void updateLocation()" /> seconds 
<el:combo name="lonDir" idx="*" size="1" options="${lonDir}" value="${lonEW}" onChange="void updateLocation()" /></td>
</tr>
<tr>
 <td class="label" valign="top">Zoom to</td>
 <td class="data"><el:text name="geoAddr" idx="*" size="64" max="96" value="" />
 <el:button ID="SearchButton" className="BUTTON" onClick="void geoLocate(document.forms[0].geoAddr)" label="SEARCH" /><br />
<span class="small">You can type in a location or address to zoom to. <i>To protect your privacy, no
 address data will be sent to <content:airline />.</i></span></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SaveButton" type="submit" className="BUTTON" label="UPDATE LOCATION" />
<c:if test="${!empty location}">
<el:cmdbutton ID="DeleteButton" url="geolocate" op="delete" label="DELETE LOCATION" />
</c:if>
 </td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script language="JavaScript" type="text/javascript">
// Build the map
<map:point var="mapC" point="${mapCenter}" />
var map = new GMap2(getElement("googleMap"), {mapTypes:[G_NORMAL_MAP, G_SATELLITE_MAP, G_PHYSICAL_MAP]});
map.addControl(new GLargeMapControl());
map.addControl(new GMapTypeControl());
map.setCenter(mapC, getDefaultZoom(${!empty location ? 30 : 2000}));
map.enableDoubleClickZoom();
map.enableContinuousZoom();
<map:type map="map" type="${gMapType}" default="G_PHYSICAL_MAP" />
var geoCoder = new GClientGeocoder();

// Add user's location
var usrLocation;
var labelText = '${empty locationText ? pageContext.request.remoteUser : locationText}';
<c:if test="${!empty location}">
var icon = buildIcon();
<map:point var="usrLoc" point="${location}" />
usrLocation = new GMarker(usrLoc, {icon:icon, draggable:true, bouncy:true, bounceGravity:0.8});
GEvent.addListener(usrLocation, "dragend", function() { setLatLon(usrLocation, usrLocation.getPoint()); } );
addMarkers(map, 'usrLocation');
</c:if>
// Set onClick event for the map
GEvent.addListener(map, 'click', setLatLon);
</script>
</body>
</map:xhtml>
