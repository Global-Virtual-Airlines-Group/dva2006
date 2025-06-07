<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_mapbox.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Dispatch Route Plotter</title>
<content:expire expires="3600" />
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="airportRefresh" />
<map:api version="3" />
<content:js name="mapBoxWX" />
<content:js name="wxParsers" />
<content:js name="routePlot" />
<content:csp type="CONNECT" host="tilecache.rainviewer.com" />
<content:googleAnalytics />
<fmt:aptype var="useICAO" />
<content:cspHeader />
<script async>
golgotha.local.sl = new golgotha.maps.wx.SeriesLoader();
golgotha.local.sl.setData('radar', 0.45, 'wxRadar');
golgotha.local.sl.setData('infrared', 0.35, 'wxSat');
golgotha.local.sl.onload(function() { golgotha.util.enable('#selImg'); });

golgotha.local.validate = function(f)
{
// Check if we're saving an existing route
const routeID = parseInt(f.routeID.value);
if (!isNaN(routeID) && (routeID > 0))
	alert('Updating route #' + routeID);
	
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.airportD, t:'Departure Airport'});
golgotha.form.validate({f:f.airportA, t:'Arrival Airport'});
golgotha.form.validate({f:f.route, l:3, t:'Flight Route'});
golgotha.form.validate({f:f.cruiseAlt, l:4, t:'Cruising Altitude'});

golgotha.form.submit(f);
golgotha.event.beacon('Route Plotter', 'Save Route');
return true;
};

golgotha.routePlot.updateAirline = function(combo) {
	const f = document.forms[0];
	golgotha.airportLoad.changeAirline([f.airportD, f.airportA], golgotha.airportLoad.config);
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body onload="void golgotha.util.disable('RouteSaveButton')" onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="aCode" name="airline.code" />
<content:empty var="emptyList" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="dsproutesave.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> DISPATCH ROUTE PLOTTER</td>
</tr>
<tr>
 <td class="label">Airline</td>
 <td class="data"><el:combo name="airline" size="1" idx="*" options="${airlines}" value="${aCode}" onChange="void golgotha.routePlot.updateAirline(this)" /></td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airportD" size="1" idx="*" options="${airportsD}" firstEntry="-" value="${airportD}" onChange="void golgotha.routePlot.updateRoute(true, true)" />
 <el:airportCode combo="airportD" airport="${airportD}" idx="*" /><span id="runways" style="visibility:hidden;"> departing <el:combo name="runway" idx="*" size="1" options="${emptyList}" firstEntry="-" onChange="void golgotha.routePlot.updateRoute(true, false)" /></span></td>
</tr>
<tr>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" size="1" idx="*" options="${airportsA}" firstEntry="-" value="${airportA}" onChange="void golgotha.routePlot.updateRoute(true)" />
 <el:airportCode combo="airportA" airport="${airportA}" idx="*" /></td>
</tr>
<tr>
 <td class="label">Alternate</td>
 <td class="data"><el:combo name="airportL" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="golgotha.routePlot.updateRoute(); golgotha.routePlot.plotMap()" />
 <el:airportCode combo="airportL" idx="*" /></td>
</tr>
<tr id="sids" style="display:none;">
 <td class="label">Standard Departure (SID)</td>
 <td class="data"><el:combo name="sid" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="golgotha.routePlot.updateRoute(); golgotha.routePlot.plotMap()" /></td>
</tr>
<tr id="stars" style="display:none;">
 <td class="label">Terminal Arrival (STAR)</td>
 <td class="data"><el:combo name="star" size="1" idx="*" options="${emptyList}" firstEntry="-" onChange="golgotha.routePlot.updateRoute(); golgotha.routePlot.plotMap()" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="noRecenter" value="true" label="Do not move Map center on Route updates" /></td>
</tr>
<tr class="title caps">
 <td colspan="2" class="left">ROUTE SEARCH</td>
</tr>
<tr>
 <td class="label">Saved Routes</td>
 <td class="data"><el:combo name="routes" idx="*" size="1" className="small req" options="${emptyList}" firstEntry="No Routes Loaded" onChange="void golgotha.routePlot.setRoute(this)" />
 <el:box name="external" value="true" className="small" label="Search FlightAware route database" /> <el:button ID="SearchButton" onClick="void golgotha.routePlot.searchRoutes()" label="SEARCH" /></td>
</tr>
<tr class="title caps">
 <td colspan="2" class="left">PLOTTED ROUTE<span id="rtDistance"></span><span id="rtETOPS"></span></td>
</tr>
<tr id="asWarnRow" style="display:none;'">
 <td class="label">&nbsp;</td>
 <td colspan="2" class="data bld"><span class="warn small caps">Route enters the following Restricted/Prohibited Airspace: <span id="aspaceList"></span></span></td>
</tr>
<tr>
 <td colspan="2" class="data"><map:div ID="mapBox" height="580" /><div id="copyright" class="small mapTextLabel"></div><div id="zoomLevel" class="small mapTextLabel right"></div></td>
</tr>
<tr>
 <td class="label">Waypoints</td>
 <td class="data"><el:text name="route" size="80" max="320" idx="*" value="" spellcheck="false" onChange="golgotha.routePlot.updateRoute(); golgotha.routePlot.plotMap()" /></td>
</tr>
<tr>
 <td class="label">Cruising Altitude</td>
 <td class="data"><el:text name="cruiseAlt" size="5" max="5" idx="*" value="35000" spellcheck="false" onChange="void golgotha.routePlot.updateRoute();" /></td>
</tr>
<tr>
 <td class="label top">Comments</td>
 <td class="data"><el:textbox name="comments" idx="*" width="80%" height="4" onChange="void golgotha.routePlot.updateRoute();" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="UpdateButton" onClick="void plotMap()" label="UPDATE ROUTE MAP" />&nbsp;<el:button ID="RouteSaveButton" type="submit" label="SAVE DISPATCH ROUTE" /></td>
</tr>
</el:table>
<el:text name="routeID" type="hidden" value="true" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script async>
<map:token />

const f = document.forms[0];
golgotha.util.disable(f.routes);
golgotha.util.disable('SearchButton', (f.airportD.selectedIndex == 0) || (f.airportA.selectedIndex == 0));
golgotha.routePlot.etopsCheck = false;

// Load the airports
const cfg = golgotha.airportLoad.config; 
cfg.doICAO = '${useICAO}';
golgotha.airportLoad.setHelpers([f.airportD,f.airportA,f.airportL]);
const newCfg = cfg.clone();

<c:if test="${empty airportsD}">
newCfg.airline = golgotha.form.getCombo(f.airline); 
f.airportD.loadAirports(newCfg);</c:if>
<c:if test="${empty airportsA}">
window.setTimeout(function() { f.airportA.loadAirports(newCfg); }, 1050);</c:if>
window.setTimeout(function() { newCfg.airline = 'all'; f.airportL.loadAirports(newCfg); }, 1250);

// Create the map
const map = new golgotha.maps.Map(document.getElementById('mapBox'), {center:[-93.25,38.88], zoom:4, minZoom:2, maxZoom:10, scrollZoom:false, projection:'globe', style:'mapbox://styles/mapbox/outdoors-v12'});
map.addControl(new mapboxgl.FullscreenControl(), 'top-right');
map.addControl(new mapboxgl.NavigationControl(), 'top-right');
map.on('style.load', golgotha.maps.updateMapText);
map.on('zoomend', golgotha.maps.updateZoom);

// Add weather selection controls
golgotha.maps.wx.ctl = new golgotha.maps.wx.WXLayerControl();
golgotha.maps.wx.ctl.addLayer({name:'Radar', c:'selImg', disabled:true, f:function() { return golgotha.local.sl.getLatest('radar'); }});
golgotha.maps.wx.ctl.addLayer({name:'Satellite', c:'selImg', disabled:true, id:'infrared', f:function() { return golgotha.local.sl.getLatest('infrared'); }});
map.addControl(golgotha.maps.wx.ctl, 'bottom-left');

// Display the copyright notice and text boxes
map.addControl(new golgotha.maps.DIVControl('copyright'), 'bottom-right');
map.addControl(new golgotha.maps.DIVControl('zoomLevel'), 'bottom-right');

//Build gates marker managers
golgotha.routePlot.dGates = new golgotha.routePlot.GateManager(10);
golgotha.routePlot.aGates = new golgotha.routePlot.GateManager(10);

// Load data async once tiles are loaded
map.once('load', function() {
	map.addControl(new golgotha.maps.BaseMapControl(golgotha.maps.DEFAULT_TYPES), 'top-left');
	map.fire('zoomend');
	window.setTimeout(function() { golgotha.local.sl.loadRV(); }, 500);
});
<c:if test="${!empty airportD}">golgotha.routePlot.plotMap();</c:if>
</script>
</body>
</html>
