<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page buffer="none" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> ${network} Online Flight Map</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<map:api version="3" />
<content:googleAnalytics eventSupport="true" />
<content:js name="flightBoardMap" />
<script type="text/javascript">
document.network = '${network}';

function setNetwork(combo)
{
var net = combo.options[combo.selectedIndex].text;
location.href = '/flightboardmap.do?id=' + net + '&op=map';
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/event/header.jspf" %> 
<%@ include file="/jsp/event/sideMenu.jspf" %>
<content:getCookie name="acarsMapType" default="map" var="gMapType" />
<content:empty var="emptyList" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="flightboard.do" method="get" validate="return false">
<el:table className="form">
<tr class="title">
 <td style="width:40%" class="left caps"><content:airline /> ${network} ONLINE PILOTS<span id="isLoading"></span></td>
 <td style="width:15%" class="mid"><el:cmd url="flightboard" linkID="${network}">FLIGHT BOARD</el:cmd></td>
 <td class="right">SELECT NETWORK <el:combo name="networkName" size="1" idx="1" onChange="void setNetwork(this)" options="${networks}" value="${network}" />
<span id="userSelect" style="display:none;"> ZOOM TO <el:combo ID="usrID" name="usrID" idx="*" options="${emptyList}" firstEntry="-" onChange="void zoomTo(this)" /></span></td>
</tr>
<tr>
 <td colspan="2"><span class="pri bld">PILOT LEGEND</span> <map:legend color="blue" className="small" legend="Member Pilot - Our Airline" />
 <map:legend color="yellow" className="small" legend="Our Airline" />
 <map:legend color="white" className="small" legend="${netInfo.network} Pilot" /></td>
 <td><span class="pri bld">ATC LEGEND</span> <map:legend color="purple" className="small" legend="Oceanic" />
 <map:legend color="red" className="small" legend="Center" /> <map:legend color="green" className="small" legend="Approach / Departure" /></td>
</tr>
<tr>
 <td colspan="3"><map:div ID="googleMap" x="100%" y="600" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr class="title">
 <td><el:button ID="RefreshButton" onClick="void updateMap(false)" label="REFRESH ${network} DATA" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script type="text/javascript">
//Create map options
var mapTypes = {mapTypeIds:golgotha.maps.DEFAULT_TYPES};
var mapOpts = {center: new google.maps.LatLng(38.88, -93.25), zoom:4, scrollwheel:false, streetViewControl:false, mapTypeControlOptions:mapTypes};

// Create the map
var map = new google.maps.Map(document.getElementById("googleMap"), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', function() { map.infoWindow.close(); });
google.maps.event.addListener(map.infoWindow, 'closeclick', infoClose);

// Add positions
updateMap(true);
</script>
</body>
</html>
