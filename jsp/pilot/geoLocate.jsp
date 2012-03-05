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
<map:api version="3" />
<content:googleAnalytics eventSupport="true" />
<script type="text/javascript">
function updateLocation()
{
// Calculate latitude/longitude
var f = document.forms[0];
var lat = parseInt(f.latD.value) + (parseInt(f.latM.value) / 60) + (parseInt(f.latS.value) / 3600);
lat *= (f.latDir.selectedIndex * -1);
var lng = parseInt(f.lonD.value) + (parseInt(f.lonM.value) / 60) + (parseInt(f.lonS.value) / 3600);
lng *= (f.lonDir.selectedIndex * -1);

usrLocation.setMap(null);
usrLocation = googleMarker('blue', new google.maps.LatLng(lat, lng), labelText);
usrLocation.setMap(map);
gaEvent('Pilot Map', 'Update Location');
return true;
}

function geoLocate(addr)
{
if (addr.value.length < 3) return false;

// Do the lookup
var isLoading = document.getElementById('isLoading');
isLoading.innerHTML = ' - SEARCHING...';
disableButton('SearchButton');
geoCoder.geocode({address:addr.value}, showResponse);
return true;
}

function showResponse(result, status)
{
var f = document.forms[0];
var isLoading = document.getElementById('isLoading');
isLoading.innerHTML = '';
enableElement('SearchButton', true);

// Check for failure
if ((!result.geometry) || (status != google.maps.GeocoderStatus.OK)) {
	alert('Cannot find "' + f.geoAddr.value + '" - ' + status + '!');
	return false;
}

// Get the placemark
var p = result.geometry.location;
var lbl = '<span class="small"><b>' + result.address_components[0].long_name + '<\/b><br /><br /><a href="javascript:void setLatLon({latLng:new google.maps.LatLng('
	+ p.toUrlValue() + ')});">SET LOCATION</a></span>';
var mrk = googleMarker('white', p, lbl);
mrk.setMap(map);
map.setCenter(p, 14);
gaEvent('Pilot Map', 'Geolocate');
return true;
}

function setLatLon(me)
{
var f = document.forms[0];
if (usrLocation != null)
	usrLocation.setMap(null);

// Update Latitude
var p = me.latLng;
var isSouth = (p.lat() < 0);
f.latD.value = Math.abs((isSouth) ? Math.ceil(p.lat()) : Math.floor(p.lat()));
var latF = Math.abs(p.lat()) - parseInt(f.latD.value);
f.latM.value = Math.floor(latF * 60);
f.latS.value = Math.floor((latF % (1/60)) * 3600);
f.latDir.selectedIndex = (isSouth) ? 1 : 0;

// Update Longitude
var isWest = (p.lng() < 0);
f.lonD.value = Math.abs((isWest) ? Math.ceil(p.lng()) : Math.floor(p.lng()));
var lonF = Math.abs(p.lng()) - parseInt(f.lonD.value);
f.lonM.value = Math.floor(lonF * 60);
f.lonS.value = Math.floor((lonF % (1/60)) * 3600);
f.lonDir.selectedIndex = (isWest) ? 1 : 0;

// Build the marker
usrLocation = new google.maps.Marker({position:p, icon:myIcon, draggable:true, shadow:golgotha.maps.DEFAULT_SHADOW});
google.maps.event.addListener(usrLocation, 'dragend', setLatLon);
usrLocation.setMap(map);
map.infoWindow.close();
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
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="geolocate.do" method="post" validate="return validate(this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">PILOT LOCATION <span id="isLoading" /></td>
</tr>
<tr>
 <td class="label top">Map</td>
 <td class="data"><c:if test="${empty location}"><span class="small">You have not selected your 
location. Please click on the map below to set your location. You can drag the map with your 
mouse and zoom in and out.</span><br />
<span class="small sec bld">To protect your privacy, the system will automatically randomize your 
location within a 3 mile circle each time the Pilot Location Board is displayed.</span><br /></c:if>
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
 <td class="label top">Zoom to</td>
 <td class="data"><el:text name="geoAddr" idx="*" size="64" max="96" value="" />
 <el:button ID="SearchButton" onClick="void geoLocate(document.forms[0].geoAddr)" label="SEARCH" /><br />
<span class="small">You can type in a location or address to zoom to. <span class="ita">To protect your privacy, no
 address data will be sent to <content:airline />.</span></span></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="UPDATE LOCATION" />
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
<script type="text/javascript">
<map:point var="mapC" point="${mapCenter}" />

// Create map options
var mapTypes = {mapTypeIds: golgotha.maps.DEFAULT_TYPES};
var mapOpts = {center: mapC, zoom:getDefaultZoom(${!empty location ? 30 : 2000}), scrollwheel:false, streetViewControl:false, mapTypeControlOptions: mapTypes};

// Build the map
var map = new google.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', function() { map.infoWindow.close(); });
var geoCoder = new google.maps.Geocoder();
var myIcon = new google.maps.MarkerImage('/' + golgotha.maps.IMG_PATH + '/maps/point_blue.png', null, null, null, golgotha.maps.PIN_SIZE);

// Add user's location
var usrLocation;
var labelText = '${empty locationText ? pageContext.request.remoteUser : locationText}';
<c:if test="${!empty location}">
<map:point var="usrLoc" point="${location}" />
usrLocation = new google.maps.Marker({position:usrLoc, icon:myIcon, draggable:true, shadow:golgotha.maps.DEFAULT_SHADOW});
google.maps.event.addListener(usrLocation, "dragend", setLatLon);
usrLocation.setMap(map);
</c:if>
google.maps.event.addListener(map, 'dblclick', setLatLon);
</script>
</body>
</map:xhtml>
