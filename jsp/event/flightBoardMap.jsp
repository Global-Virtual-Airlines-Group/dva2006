<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ page buffer="none" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_mapbox.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline />&nbsp;${network} Online Flight Map</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<map:api version="3" />
<content:googleAnalytics />
<content:js name="flightBoardMap" />
<content:cspHeader />
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/event/header.jspf" %> 
<%@ include file="/jsp/event/sideMenu.jspf" %>
<content:empty var="emptyList" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="flightboard.do" method="get" validate="return false">
<el:table className="form">
<tr class="title">
 <td style="width:40%" class="left caps"><span class="nophone">${network}&nbsp;</span>ONLINE PILOTS<span id="isLoading"></span></td>
 <td style="width:15%" class="mid"><el:cmd url="flightboard" linkID="${network}">FLIGHT BOARD</el:cmd></td>
 <td class="right">SELECT NETWORK <el:combo name="networkName" size="1" idx="1" onChange="void golgotha.flightBoard.setNetwork(this)" options="${networks}" value="${network}" />
<span id="userSelect" class="nophone" style="display:none;"> ZOOM TO <el:combo ID="usrID" name="usrID" idx="*" options="${emptyList}" firstEntry="-" onChange="void golgotha.flightBoard.zoomTo(this)" /></span></td>
</tr>
<tr>
 <td colspan="2"><span class="pri bld nophone">PILOT LEGEND</span> <map:legend color="blue" className="small" legend="Member Pilot - Our Airline" />
 <map:legend color="yellow" className="small" legend="Our Airline" />
 <map:legend color="white" className="small" legend="${netInfo.network} Pilot" /></td>
 <td><span class="pri bld nophone">ATC LEGEND</span> <map:legend color="purple" className="small" legend="Oceanic" />
 <map:legend color="red" className="small" legend="Center" /> <map:legend color="green" className="small" legend="Approach / Departure" /></td>
</tr>
<tr>
 <td colspan="3"><map:div ID="mapBox" height="600" /><div id="zoomLevel" class="mapTextlabel"></div></td>
</tr>

<!-- Button Bar -->
<tr class="title">
 <td colspan="3" class="mid"><el:button onClick="void golgotha.flightBoard.updateMap(false)" label="REFRESH ${network} DATA" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script async>
<map:token />
golgotha.flightBoard.network = '${network}';

// Create the map
const mapOpts = {container:'mapBox', zoom:4, maxZoom:12, projection:'globe', center:[-93.25,38.88], style:'mapbox://styles/mapbox/outdoors-v12'};
const map = new golgotha.maps.Map(document.getElementById('mapBox'), mapOpts);
map.addControl(new mapboxgl.FullscreenControl(), 'top-right');
map.addControl(new mapboxgl.NavigationControl(), 'top-right');
map.addControl(new golgotha.maps.DIVControl('zoomLevel'), 'bottom-right');
map.on('click', golgotha.flightBoard.infoClose);
map.on('style.load', golgotha.maps.updateMapText);
map.on('zoomend', golgotha.maps.updateZoom);
map.once('load', function() { 
	map.addControl(new golgotha.maps.BaseMapControl(golgotha.maps.DEFAULT_TYPES), 'top-left');
	golgotha.flightBoard.updateMap(true);
});
</script>
</body>
</html>
