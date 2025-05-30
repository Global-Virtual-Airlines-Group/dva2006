<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_mapbox.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Pilot Location - ${pilot.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<map:api version="3" />
<content:googleAnalytics />
<script async>
golgotha.maps.geoLocate = golgotha.maps.geoLocate || {usrLocation:null};
golgotha.maps.geoLocate.gpsOK = function(pos) { map.panTo({lat:pos.coords.latitude, lng:pos.coords.longitude}); map.setZoom(8); return true; };
golgotha.maps.geoLocate.gpsError = function(err) { console.log('GPS Geolocation failed - ' + err.code); return false; };
golgotha.maps.geoLocate.updateLocation = function()
{
// Calculate latitude/longitude
const f = document.forms[0];
let lat = parseInt(f.latD.value) + (parseInt(f.latM.value) / 60) + (parseInt(f.latS.value) / 3600);
lat *= (f.latDir.selectedIndex * -1);
let lng = parseInt(f.lonD.value) + (parseInt(f.lonM.value) / 60) + (parseInt(f.lonS.value) / 3600);
lng *= (f.lonDir.selectedIndex * -1);

golgotha.maps.geoLocate.usrLocation.setMap(null);
golgotha.maps.geoLocate.usrLocation = new golgotha.maps.Marker({color:'blue', info:labelText, pt:[lng,lat]});
golgotha.maps.geoLocate.usrLocation.setMap(map);
golgotha.event.beacon('Pilot Map', 'Update Location');
return true;
};

golgotha.maps.geoLocate.setLatLon = function(me)
{
const f = document.forms[0];
if (golgotha.maps.geoLocate.usrLocation != null)
	golgotha.maps.geoLocate.usrLocation.setMap(null);

// Update Latitude
const p = me.lngLat || me.target.getLngLat();
const isSouth = (p.lat < 0);
f.latD.value = Math.abs(isSouth ? Math.ceil(p.lat) : Math.floor(p.lat));
const latF = Math.abs(p.lat) - parseInt(f.latD.value);
f.latM.value = Math.floor(latF * 60);
f.latS.value = Math.floor((latF % (1/60)) * 3600);
f.latDir.selectedIndex = (isSouth) ? 1 : 0;

// Update Longitude
const isWest = (p.lng < 0);
f.lonD.value = Math.abs(isWest ? Math.ceil(p.lng) : Math.floor(p.lng));
const lonF = Math.abs(p.lng) - parseInt(f.lonD.value);
f.lonM.value = Math.floor(lonF * 60);
f.lonS.value = Math.floor((lonF % (1/60)) * 3600);
f.lonDir.selectedIndex = (isWest) ? 1 : 0;

// Build the marker
const mrk = new golgotha.maps.Marker({pt:p, color:'blue', draggable:true});
mrk.on('dragend', golgotha.maps.geoLocate.setLatLon);
golgotha.maps.geoLocate.usrLocation = mrk;
mrk.setMap(map);
return true;
};

golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.submit(f);
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="geolocate.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">PILOT LOCATION <span id="isLoading"></span></td>
</tr>
<tr>
 <td colspan="2" class="data"><c:if test="${empty location}"><span class="small">You have not selected your location. Please click on the map below to set your location. You can drag the map with your mouse and zoom in and out.</span><br />
<span class="small sec bld">To protect your privacy, the system will automatically randomize your location within a 3 mile circle each time the Pilot Location Board is displayed.</span><br /></c:if>
<map:div ID="mapBox" height="570" /></td>
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
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="UPDATE LOCATION" /><c:if test="${!empty location}">&nbsp;<el:cmdbutton url="geolocate" op="delete" label="DELETE LOCATION" /></c:if>
 </td>
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

// Build the map
const mapOpts = {container:'mapBox', zoom:golgotha.maps.util.getDefaultZoom(${!empty location ? 30 : 2000}), maxZoom:14, minZoom:4, projection:'globe', center:golgotha.local.mapC, style:'mapbox://styles/mapbox/outdoors-v12'};
const map = new golgotha.maps.Map(document.getElementById('mapBox'), mapOpts);
map.addControl(new mapboxgl.FullscreenControl(), 'top-right');
map.addControl(new mapboxgl.NavigationControl(), 'top-right');
map.addControl(new mapboxgl.GeolocateControl(), 'top-right');
map.on('style.load', golgotha.maps.updateMapText);
map.on('dblclick', golgotha.maps.geoLocate.setLatLon);
map.once('load', function() { map.addControl(new golgotha.maps.BaseMapControl(golgotha.maps.DEFAULT_TYPES), 'top-left'); });

// Add user's location
const labelText = '${empty locationText ? pageContext.request.remoteUser : locationText}';
<c:if test="${!empty location}">
<map:point var="usrLoc" point="${location}" />
golgotha.maps.geoLocate.usrLocation = new golgotha.maps.Marker({pt:usrLoc, color:'blue', draggable:true});
golgotha.maps.geoLocate.usrLocation.on('dragend', golgotha.maps.geoLocate.setLatLon);</c:if>
golgotha.maps.geoLocate.usrLocation.setMap(map);
<c:if test="${empty location}">
map.once('load', function() {
	map.addTerrain(1.5);
	if (navigator.geolocation)
		window.setTimeout(function() { navigator.geolocation.getCurrentPosition(golgotha.maps.geoLocate.gpsOK, golgotha.maps.geoLocate.gpsError,{timeout:5000}); }, 150);
});</c:if>
</script>
</body>
</html>
