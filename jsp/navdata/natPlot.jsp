<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_mapbox.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> North Atlantic Track Plotter</title>
<content:expire expires="3600" />
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<map:api version="3" />
<content:js name="mapBoxWX" />
<content:js name="oceanicPlot" />
<content:csp type="CONNECT" host="tilecache.rainviewer.com" />
<content:googleAnalytics />
<content:cspHeader />
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="natplot.do" method="get" validate="return false">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> NORTH ATLANTIC ROUTE PLOTTER<span id="isLoading"></span></td>
</tr>
<tr>
 <td class="label">Date</td>
 <td class="data"><el:combo name="date" idx="*" firstEntry="-" options="${dates}" value="${param.date}" onChange="void golgotha.maps.oceanic.loadTracks('NAT')" /></td>
</tr>
<tr id="fetchData" style="display:none">
 <td class="label">Retrieved on</td>
 <td class="data"><span id="fetchDate" class="pri bld"></span> from <span id="fetchSrc" class="ita"></span></td>
</tr>
<tr>
 <td class="label"><span id="trackLabel">Track Data</span></td>
 <td class="data"><span id="trackData">N/A</span>&nbsp;<span id="trackCopy" style="display:none;"><a href="javascript:void golgotha.maps.oceanic.copyRoute()"><el:img src="copyIcon.png" x="14" y="14" caption="Copy Track Waypoints" /></a></span></td>
</tr>
<tr>
 <td class="label">Map Legend</td>
 <td class="data"><map:legend color="white" legend="Eastbound" />  <map:legend color="orange" legend="Westbound" /> <map:legend color="blue" legend="Concorde" /></td>
</tr>
<tr>
 <td class="label">Display Tracks</td>
 <td class="data"><el:check name="showTracks" idx="*" options="${trackTypes}" checked="${trackTypes}" width="100" cols="3" onChange="void golgotha.maps.oceanic.updateTracks(this)" /></td>
</tr>
<tr>
 <td colspan="2" class="data"><map:div ID="mapBox" height="550" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<div id="copyright" class="mapTextLabel"></div><div id="zoomLevel" class="mapTextlabel right"></div>
<script async>
<map:token />

// Create the map
const mapOpts = {container:'mapBox', zoom:4, maxZoom:8, minZoom:3, projection:'globe', center:[-35, 52], style:'mapbox://styles/mapbox/outdoors-v12'};
const map = new golgotha.maps.Map(document.getElementById('mapBox'), mapOpts);
map.addControl(new mapboxgl.FullscreenControl(), 'top-right');
map.addControl(new mapboxgl.NavigationControl(), 'top-right');
map.on('zoomend', golgotha.maps.updateZoom);
map.on('style.load', golgotha.maps.updateMapText);

// Weather layer loader
golgotha.local.sl = new golgotha.maps.wx.SeriesLoader();
golgotha.local.sl.setData('infrared', 0.35, 'wxSat');
golgotha.local.sl.setData('radar', 0.45, 'wxRadar');
golgotha.local.sl.onload(function() {  golgotha.util.enable('#selImg'); });

// Add selection controls
golgotha.maps.wx.ctl = new golgotha.maps.wx.WXLayerControl();
golgotha.maps.wx.ctl.addLayer({name:'Radar', c:'selImg', disabled:true, f:function() { return golgotha.local.sl.getLatest('radar'); }});
golgotha.maps.wx.ctl.addLayer({name:'Satellite', c:'selImg', disabled:true, id:'infrared', f:function() { return golgotha.local.sl.getLatest('infrared'); }});
map.addControl(golgotha.maps.wx.ctl, 'bottom-left');
map.addControl(new golgotha.maps.DIVControl('copyright'), 'bottom-right');
map.addControl(new golgotha.maps.DIVControl('zoomLevel'), 'bottom-right');

/* const jsl = new golgotha.maps.ShapeLayer({maxZoom:8, nativeZoom:6, opacity:0.375, zIndex:golgotha.maps.z.OVERLAY}, 'Jet', 'wind-jet');
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Jet Stream'}, jsl)); */

// Load data async once tiles are loaded
map.once('load', function() {
	map.addControl(new golgotha.maps.BaseMapControl(golgotha.maps.DEFAULT_TYPES), 'top-left');
	golgotha.maps.oceanic.resetTracks();
	window.setTimeout(function() { golgotha.local.sl.loadRV(); }, 350);
	map.fire('zoomend');
});
</script>
</body>
</html>
