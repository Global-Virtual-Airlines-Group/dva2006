<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" xml:lang="en" lang="en">
<head>
<title><content:airline /> ACARS Live Map</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="googleMaps" />
<content:js name="acarsMap" />
<content:sysdata var="imgPath" name="path.img" />
<content:sysdata var="refreshInterval" name="acars.livemap.reload" />
<map:api version="1" />
<map:vml-ie />
<script language="JavaScript" type="text/javascript">
function reloadData(isAuto)
{
// Get auto refresh
var f = document.forms[0];
var doRefresh = f.autoRefresh.checked;
if (document.pauseRefresh) return false;

// Generate XMLHTTPRequest
var isLoading = getElement('isLoading');
isLoading.innerHTML = ' - LOADING...';
var xmlreq = generateXMLRequest('${imgPath}');
xmlreq.send(null);

// Set timer to reload the data
if (doRefresh && isAuto)
	window.setTimeout('void reloadData(true)', ${refreshInterval + 2000});

return true;
}

function saveSettings()
{
// Get the latitude, longitude and zoom level
var myLat = map.getCenterLatLng().y;
var myLng = map.getCenterLatLng().x;
var myZoom = map.getZoomLevel();
var myType = (map.getCurrentMapType() == G_SATELLITE_TYPE) ? 'sat' : 'map';

// Save the cookies
var expiryDate = new Date(2006, 11, 31);
document.cookie = 'acarsMapLat=' + myLat + '; expires=' + expiryDate.toGMTString();
document.cookie = 'acarsMapLng=' + myLng + '; expires=' + expiryDate.toGMTString();
document.cookie = 'acarsMapZoomLevel=' + myZoom + '; expires=' + expiryDate.toGMTString();
document.cookie = 'acarsMapType=' + myType + '; expires=' + expiryDate.toGMTString();

// Display confirmation message
alert('Your <content:airline /> ACARS Map preferences have been saved.');
return true;
}

function clearSettings()
{
// Clear cookies
var expiryDate = new Date();
document.cookie = 'acarsMapLat=; expires=' + expiryDate.toGMTString();
document.cookie = 'acarsMapLng=; expires=' + expiryDate.toGMTString();
document.cookie = 'acarsMapZoomLevel=; expires=' + expiryDate.toGMTString();
document.cookie = 'acarsMapType=; expires=' + expiryDate.toGMTString();

// Display confirmation message
alert('Your <content:airline /> ACARS Map preferences have been cleared.');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>
<content:getCookie name="acarsMapZoomLevel" default="12" var="zoomLevel" />
<content:getCookie name="acarsMapType" default="map" var="gMapType" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="acarsMap.do" method="post" validate="return false">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> LIVE ACARS MAP<span id="isLoading" /></td>
</tr>
<tr>
 <td class="label">Map Options</td>
 <td class="data"><span class="bld"><el:box name="showProgress" idx="*" value="1" label="Show Flight Progress" checked="true" />&nbsp;
<el:box name="autoRefresh" idx="*" value="1" label="Automatically Refresh Map" checked="true" />&nbsp;
<el:box name="showInfo" idx="*" value="1" label="Show Flight Data" checked="true" />&nbsp;
<el:box name="showRoute" idx="*" value="1" label="Show Flight Plan" checked="false" /></span></td>
</tr>
<tr>
 <td class="label">Map Legend</td>
 <td class="data"><map:legend color="blue" legend="Cruising" /> <map:legend color="white" legend="On Ground" />
 <map:legend color="orange" legend="Climbing" /> <map:legend color="yellow" legend="Descending" /></td>
</tr>
<tr>
 <td class="label" valign="top">Live Map</td>
 <td class="data"><map:div ID="googleMap" x="650" y="550" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr class="title">
 <td><el:button ID="RefreshButton" className="BUTTON" onClick="void reloadData(false)" label="REFRESH ACARS DATA" />&nbsp;
<el:button ID="SettingsButton" className="BUTTON" onClick="void saveSettings()" label="SAVE SETTINGS" />&nbsp;
<el:button ID="ClearButton" className="BUTTON" onClick="void clearSettings()" label="CLEAR SETTINGS" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script language="JavaScript" type="text/javascript">
<map:marker var="mapC" point="${mapCenter}" />

// Create the map
var map = new GMap(getElement("googleMap"), [G_MAP_TYPE, G_SATELLITE_TYPE, G_HYBRID_TYPE]);
map.addControl(new GLargeMapControl());
map.addControl(new GMapTypeControl());
map.centerAndZoom(mapC, ${zoomLevel});
map.setMapType(${gMapType == 'map' ? 'G_MAP_TYPE' : 'G_SATELLITE_TYPE'});

// Placeholder for route
var routeData;
var routeWaypoints;

// Reload ACARS data
document.doRefresh = true;
reloadData(false);
</script>
</body>
</html>
