<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page buffer="none" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> ${network} Online Flight Map</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:sysdata var="imgPath" name="path.img" />
<content:js name="googleMaps" />
<content:googleAnalytics eventSupport="true" />
<map:api version="2" />
<map:vml-ie />
<content:js name="flightBoardMap" />
<script type="text/javascript">
document.imgPath = '${imgPath}';
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
<body onunload="GUnload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:getCookie name="acarsMapType" default="map" var="gMapType" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="flightboard.do" method="get" validate="return false">
<el:table className="form" pad="default" space="default">
<tr class="title">
 <td width="40%" class="left">ONLINE PILOTS - ${network}<span id="isLoading"> - VALID AS OF <span id="validDate"></span></span></td>
 <td width="25%" class="mid"><el:cmd url="flightboard" linkID="${network}">FLIGHT BOARD</el:cmd></td>
 <td class="right">SELECT NETWORK <el:combo name="networkName" size="1" idx="1" onChange="void setNetwork(this)" options="${networks}" value="${network}" /></td>
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
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script type="text/javascript">
// Create the map
var map = new GMap2(getElement("googleMap"), {mapTypes:[G_NORMAL_MAP, G_SATELLITE_MAP, G_PHYSICAL_MAP]});
map.addControl(new GLargeMapControl3D());
map.addControl(new GMapTypeControl());
map.addControl(new GOverviewMapControl());

// Center the map and add positions
map.setCenter(new GLatLng(38.88, -93.25), 4);
map.enableDoubleClickZoom();
map.enableContinuousZoom();
<map:type map="map" type="${gMapType}" default="G_PHYSICAL_MAP" />
GEvent.addListener(map, 'infowindowclose', infoClose);
updateMap();
</script>
</body>
</map:xhtml>
