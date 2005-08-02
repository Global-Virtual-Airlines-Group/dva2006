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
<content:js name="common" />
<content:js name="googleMaps" />
<content:js name="acarsMap" />
<content:sysdata var="refreshInterval" name="acars.map.refresh" />
<map:api version="1" />
<map:vml-ie />
<script language="JavaScript" type="text/javascript">
function reloadData()
{
var xmlreq = generateXMLRequest();
xmlreq.send(null);

// Disable the buttons
disableButton('ToggleButton');
disableButton('RefreshButton');
disableButton('SettingsButton');

// Set timer to reload the data
if (document.doRefresh)
	window.setTimeout('void reloadData()', ${(refreshInterval * 1000) + 2500});
	
return true;
}

function saveSettings()
{
// Get the latitude, longitude and zoom level
var myLat = map.getCenterLatLng().y;
var myLng = map.getCenterLatLng().x;
var myZoom = map.getZoomLevel();

// Save the cookies
var expiryDate = new Date(2007,11,31);
document.cookie = 'acarsMapLat=' + myLat + '; expires=' + expiryDate.toGMTString();
document.cookie = 'acarsMapLng=' + myLng + '; expires=' + expiryDate.toGMTString();
document.cookie = 'acarsMapZoomLevel=' + myZoom + '; expires=' + expiryDate.toGMTString();

// Display confirmation message
alert('Your <content:airline /> ACARS Map preferences have been saved.');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>
<content:sysdata var="imgPath" name="path.img" />
<content:getCookie name="acarsMapZoomLevel" default="12" var="zoomLevel" />

<!-- Main Body Frame -->
<div id="main">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> LIVE ACARS MAP</td>
</tr>
<tr>
 <td class="label">Live Map</td>
 <td class="data"><div id="googleMap" style="width: 840px; height: 630px" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr class="title">
 <td><el:button ID="RefreshButton" className="BUTTON" onClick="void reloadData()" label="REFRESH ACARS DATA" />&nbsp;
<el:button ID="SettingsButton" className="BUTTON" onClick="void saveSettings()" label="SAVE SETTINGS" />&nbsp;
<el:button ID="ToggleButton" className="BUTTON" onClick="void toggleReload()" label="STOP AUTOMATIC REFRESH" /></td>
</tr>
</el:table>
<content:copyright />
</div>
<script language="JavaScript" type="text/javascript">
<map:marker var="mapC" point="${mapCenter}" />

// Create the map
var map = new GMap(getElement("googleMap"), [G_MAP_TYPE, G_SATELLITE_TYPE]);
map.addControl(new GSmallZoomControl());
map.addControl(new GMapTypeControl());
map.centerAndZoom(mapC, ${zoomLevel});

// Reload ACARS data
document.doRefresh = true;
reloadData();
</script>
</body>
</html>
