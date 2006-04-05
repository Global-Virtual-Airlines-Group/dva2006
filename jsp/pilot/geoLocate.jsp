<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Pilot Location - ${pilot.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="googleMaps" />
<map:api version="2" />
<map:vml-ie />
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
return true;
}

function validate(form)
{
if (!checkSubmit()) return false;

setSubmit();
disableButton('SaveButton');
disableButton('DeleteButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body onunload="GUnload()">
<content:page>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>
<content:sysdata var="imgPath" name="path.img" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="geolocate.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">PILOT LOCATION</td>
</tr>
<tr>
 <td class="label" valign="top">Map</td>
 <td class="data"><c:if test="${empty location}"><span class="small">You have not selected your 
location. Please click on the map below to set your location. You can drag the map with your 
mouse and zoom in and out.</span><br />
<span class="small sec bld"><u>NOTE</u>: To protect your privacy, the system will automatically 
randomize your location within a 3 mile circle each time the Pilot Location Board is displayed.</span><br /></c:if>
<map:div ID="googleMap" x="640" y="570" /></td>
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
var map = new GMap2(getElement("googleMap"), [G_MAP_TYPE, G_SATELLITE_TYPE]);
map.addControl(new GLargeMapControl());
map.addControl(new GMapTypeControl());
map.setCenter(mapC, getDefaultZoom(${!empty location ? 30 : 2000}));

// Add user's location
var usrLocation;
var labelText = '${empty locationText ? pageContext.request.remoteUser : locationText}';
<c:if test="${!empty location}">
<map:marker var="usrLocation" point="${location}" />
addMarkers(map, 'usrLocation');
</c:if>

// Set onClick event for the map
GEvent.addListener(map, 'click', function setLatLon(overlay, geoPosition)
{
var f = document.forms[0];

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

map.removeOverlay(usrLocation);
usrLocation = googleMarker('${imgPath}','blue',geoPosition,labelText);
map.addOverlay(usrLocation);
return true;
} );
</script>
</body>
</html>
