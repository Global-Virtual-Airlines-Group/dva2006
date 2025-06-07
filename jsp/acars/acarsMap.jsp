<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_mapbox.tld" prefix="map" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Live Map</title>
<content:expire expires="3600" />
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:googleAnalytics />
<content:sysdata var="refreshInterval" name="acars.livemap.reload" />
<map:api version="3" />
<content:js name="acarsMap" />
<content:js name="mapBoxWX" />
<content:js name="wxParsers" />
<content:captcha action="acarsMap" />
<content:csp type="CONNECT" host="tilecache.rainviewer.com" />
<content:csp type="IMG" host="tilecache.rainviewer.com" />
<content:cspHeader />
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:empty var="emptyList" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="acarsMap.do" method="get" validate="return false">
<el:table className="form">
<tr class="title">
 <td colspan="2" class="caps"><content:airline /> LIVE ACARS MAP<span id="isLoading"></span></td>
 <td colspan="2" class="right nophone"><span id="userSelect" style="display:none;"> ZOOM TO <el:combo ID="usrID" name="usrID" idx="*" options="${emptyList}" firstEntry="-" onChange="void golgotha.maps.acars.zoomTo(this)" /></span></td>
</tr>
<tr>
 <td class="label">Map Options</td>
 <td class="data" colspan="3"><span class="bld"><el:box name="showProgress" idx="*" value="1" label="Show Flight Progress" checked="true" />&nbsp;
<el:box name="autoRefresh" idx="*" value="true" label="Automatically Refresh Map" checked="true" />
<el:box name="showInfo" idx="*" value="true" label="Show Flight Data" checked="true" />
<el:box name="showRoute" idx="*" value="true" label="Show Flight Plan" checked="false" />
<span class="nophone"><el:box name="zoomToPilot" idx="*" value="true" label="Zoom to Pilot" checked="false" />
<el:box name="showLegend" idx="*" value="true" label="Show Legend" checked="true" onChange="void golgotha.maps.acars.showLegend(this)" /></span></span></td>
</tr>
<tr class="nophone mapLegend">
 <td class="label" style="max-width:160px;">Aircraft Legend</td>
 <td class="data" style="width:45%;"><img height="22" width="24" alt="Cruising" src="/acicon.ws?c=blue" /> Cruise | <img height="22" width="22" alt="On Ground" src="/acicon.ws?c=white" /> On Ground | 
 <img height="22" width="22" alt="Climbing" src="/acicon.ws?c=orange" /> Climbing | <img height="22" width="22" alt="Descending" src="/acicon.ws?c=yellow" /> Descending</td>
 <td class="label">Dispatcher Legend</td>
 <td class="data"><map:legend color="green" legend="Available" /> | <map:legend color="purple" legend="Busy" /></td>
</tr>
<tr class="nophone">
 <td class="label">Dispatch Service</td>
 <td class="data"><span id="dispatchStatus" class="bld caps">DISPATCH CURRENTLY OFFLINE</span></td>
 <td class="label">Weather Layer</td>
 <td class="data"><span id="wxLoading" class="small" style="width:150px;">None</span></td>
</tr>
<tr>
 <td class="data" colspan="4"><map:div ID="mapBox" height="600" /><div id="copyright" class="small mapTextLabel right"></div><div id="mapStatus" class="small mapTextLabel right"></div>
<div id="zoomLevel" class="small mapTextLabel right"></div><div id="seriesRefresh" class="small mapTextLabel"></div></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr class="title">
 <td><el:button onClick="void golgotha.maps.acars.reloadData(false)" label="REFRESH ACARS DATA" />&nbsp;<el:button ID="EarthButton" onClick="void golgotha.maps.acars.showEarth()" label="DISPLAY IN GOOGLE EARTH" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script async>
<map:token />
golgotha.local.refresh = ${refreshInterval + 2000};
<map:point var="golgotha.local.mapC" point="${mapCenter}" />
golgotha.maps.info.ctr = golgotha.maps.info.ctr || golgotha.local.mapC;

golgotha.local.sl = new golgotha.maps.wx.SeriesLoader();
golgotha.local.sl.setData('radar', 0.45, 'wxRadar');
golgotha.local.sl.setData('infrared', 0.25, 'wxSat');
golgotha.local.sl.onload(function() { golgotha.util.enable('#selImg'); });
golgotha.local.fl = new golgotha.maps.FIRLoader();
golgotha.local.fl.onload(function() { golgotha.util.enable('wxselect-selFIR'); });

// Create the map
const map = new golgotha.maps.Map(document.getElementById('mapBox'), {center:golgotha.maps.info.ctr, minZoom:3, maxZoom:17, antiAlias:true, scrollZoom:false, zoom:golgotha.maps.info.zoom, projection:'globe', style:'mapbox://styles/mapbox/outdoors-v12'});
map.addControl(new mapboxgl.FullscreenControl(), 'top-right');
map.addControl(new mapboxgl.NavigationControl(), 'top-right');
map.on('zoomend', golgotha.maps.updateZoom);
map.on('style.load', golgotha.maps.updateMapText);
map.on('style.load', golgotha.maps.acars.updateSettings);
map.on('dragpan', golgotha.maps.acars.updateSettings);
map.on('zoomend', golgotha.maps.acars.updateSettings);

// Build the weather layer controls
golgotha.maps.wx.ctl = new golgotha.maps.wx.WXLayerControl();
golgotha.maps.wx.ctl.addLayer({name:'Radar', c:'selImg', disabled:true, f:function() { return golgotha.local.sl.getLatest('radar'); }});
golgotha.maps.wx.ctl.addLayer({name:'Satellite', c:'selImg', disabled:true, id:'infrared', f:function() { return golgotha.local.sl.getLatest('infrared'); }});
golgotha.maps.wx.ctl.addLayer({name:'FIRs', disabled:true, id:'selFIR', f:function() { return golgotha.local.fl.getFIRs(); }});
map.addControl(golgotha.maps.wx.ctl, 'bottom-left');

//const jsl = new golgotha.maps.ShapeLayer({maxZoom:8, nativeZoom:6, opacity:0.375, zIndex:golgotha.maps.z.OVERLAY}, 'Jet', 'wind-lojet');
// ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Jet Stream'}, jsl));

// Load data async once tiles are loaded
map.once('load', function() {
	map.addTerrain(1.5);
	map.addControl(new golgotha.maps.BaseMapControl(golgotha.maps.DEFAULT_TYPES), 'top-left');
	map.fire('zoomend');
	golgotha.maps.acars.reloadData(true);
	window.setTimeout(function() { golgotha.maps.reloadData(true); }, 500);
	golgotha.local.fl.load();
});

// Display the copyright notice and text boxes
map.addControl(new golgotha.maps.DIVControl('copyright'), 'bottom-right');
map.addControl(new golgotha.maps.DIVControl('mapStatus'), 'top-right');
map.addControl(new golgotha.maps.DIVControl('zoomLevel'), 'bottom-right');
map.addControl(new golgotha.maps.DIVControl('seriesRefresh'), 'bottom-left');

golgotha.maps.reloadData = function(isReload) {
	if (isReload) window.setInterval(golgotha.maps.reloadData, golgotha.maps.reload);
	const dv = document.getElementById('seriesRefresh');
	if (dv) {
		const txtDate = new Date().toString();
		dv.innerHTML = txtDate.substring(0, txtDate.indexOf('('));
	}

	golgotha.local.sl.loadRV();
	return true;
};
</script>
</body>
</html>
