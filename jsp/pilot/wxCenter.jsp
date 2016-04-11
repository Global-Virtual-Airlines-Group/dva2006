<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Weather Center</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<map:api version="3" />
<content:js name="progressBar" />
<content:js name="googleMapsWX" />
<content:js name="wxParsers" />
<content:googleAnalytics eventSupport="true" />
<script type="text/javascript">
var loaders = {};
loaders.series = new golgotha.maps.SeriesLoader();
loaders.fr = new golgotha.maps.LayerLoader('Fronts', golgotha.maps.fronts.FrontParser);
loaders.series.setData('radar', 0.45, 'wxRadar', 1024);
loaders.series.setData('eurorad', 0.45, 'wxRadar', 512);
loaders.series.setData('aussieradar', 0.45, 'wxRadar', 512);
loaders.series.setData('future_radar_ff', 0.45, 'radarFF', 1024);
loaders.series.setData('temp', 0.275, 'wxTemp');
loaders.series.setData('windspeed', 0.325, 'wxWind');
loaders.series.onload(function() { golgotha.util.enable('#selImg'); });
loaders.fr.onload(function() { golgotha.util.enable('selFronts'); });

golgotha.local.loadWX = function(code)
{
if (code.length < 4) {
	alert('Please provide the Airport code.');
	return false;
}

var useFA = false;
<content:filter roles="Route,Dispatch">
// Check for FlightAware Weather
var f = document.forms[0];
useFA = f.useFA.checked;</content:filter>
	
// Build the XML Request
var xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'airportWX.ws?fa=' + useFA + '&code=' + code + '&type=METAR,TAF&time=' + golgotha.util.getTimestamp(30000), true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var xdoc = xmlreq.responseXML.documentElement;

	// Load the weather data
	var wc = xdoc.getElementsByTagName('wx');
	for (var i = 0; i < wc.length; i++) {
		var wx = wc[i];

		// Check for an existing marker
		var code = wx.getAttribute('icao');
		var wxType = wx.getAttribute('type');
		var mrk = golgotha.local.wxMarkers[code];
		if (mrk) {
			if (mrk.isOpen)
				map.infoWindow.close();

			var tabs = parseInt(wx.getAttribute('tabs'));
			if (tabs == 0) {
				delete mrk.tabs;
				var label = wx.firstChild;
				if (label)
					mrk.infoLabel = label.data.replace(/\n/g, '<br />');
			} else {
				mrk.tabs = [];
				var tbs = wx.getElementsByTagName('tab');
				for (var x = 0; x < tbs.length; x++) {
					var tab = tbs[x];
					var tabType = tab.getAttribute('type');
					var label = tab.firstChild;
					if (label) {
						eval('mrk.' + tabType + ' = label.data');
						var wxData = label.data.replace(/\n/g, '<br />');
						mrk.tabs.push({name:tab.getAttribute('name'), content:wxData});
					}
				}
			}

			continue;
		}

		// Create the marker
		var p = {lat:parseFloat(wx.getAttribute('lat')), lng:parseFloat(wx.getAttribute('lng'))};
		if (wx.getAttribute('pal'))
			mrk = new golgotha.maps.IconMarker({pal:wx.getAttribute('pal'), icon:wx.getAttribute('icon')}, p);
		else if (wx.getAttribute('color'))
			mrk = new golgotha.maps.Marker({color:wx.getAttribute('color')}, p);

		mrk.code = code;
		mrk.isOpen = false;
		var tabs = parseInt(wx.getAttribute('tabs'));
		if (tabs == 0) {
			var label = wx.firstChild;
			if (label)
				mrk.infoLabel = label.data;
		} else {
			mrk.tabs = []; mrk.updateTab = golgotha.maps.util.updateTab; 
			var tbs = wx.getElementsByTagName('tab');
			for (var x = 0; x < tbs.length; x++) {
				var tab = tbs[x];
				var tabType = tab.getAttribute('type');
				var label = tab.firstChild;
				if (label) {
					eval('mrk.' + tabType + ' = label.data');
					var wxData = label.data.replace(/\n/g, '<br />');
					mrk.tabs.push({name:tab.getAttribute('name'), content:wxData});
				}
			}
		}

		// Set the the click handlers
		google.maps.event.addListener(mrk, 'click', golgotha.local.clickInfo);

		// Add the marker
		golgotha.local.wxMarkers[mrk.code] = mrk;
		mrk.setMap(map);
	}

	var mrk = golgotha.local.wxMarkers[code];
	if (mrk)
		google.maps.event.trigger(mrk, 'click');

	golgotha.event.beacon('WeatherMap', 'Fetch TAF/METAR', code);
	return true;
};

xmlreq.send(null);
return true;
};

golgotha.local.closeWindow = function()
{
this.isOpen = false;
var f = document.forms[0];
f.wxID.value = '';
f.metarData.value = '';
f.metarData.disabled = true;
f.tafData.value = '';
f.tafData.disabled = true;
map.closeWindow();
return true;
};

golgotha.local.clickInfo = function()
{
if (this.tabs) {
	this.updateTab(0, new google.maps.Size(325, 100));
	map.infoWindow.marker = this;
} else
	map.infoWindow.setContent(this.infoLabel);

map.infoWindow.open(map, this); 
	
// Copy the data to the fields
var f = document.forms[0];
f.wxID.value = this.code;
f.metarData.value = this.METAR;
f.metarData.disabled = false;
f.tafData.value = this.TAF;
f.tafData.disabled = false;
this.isOpen = true;
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:empty var="emptyList" />
<content:getCookie name="acarsMapType" default="map" var="gMapType" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="wxcenter.do" method="get" validate="return false">
<el:table className="form">
<tr class="title caps">
 <td colspan="2" class="left"><content:airline /> WEATHER CENTER <c:if test="${!empty gfsCycle}"> - GFS DATA AS OF <fmt:date date="${gfsCycle}" t="HH:mm" /></c:if></td>
</tr>
<tr>
 <td class="data" colspan="2"><map:div ID="googleMap" height="480" /></td>
</tr>
<tr>
 <td class="label">Airport Code</td>
 <td class="data"><el:text name="wxID" idx="*" className="bld" size="3" max="4" /> 
  <el:button ID="FetchButton" onClick="void golgotha.local.loadWX(document.forms[0].wxID.value)" label="FETCH WEATHER" />
<content:filter roles="Route,Dispatch"> <el:box name="useFA" value="true" checked="false" label="Use FlightAware Weather" /></content:filter></td>
</tr>
<tr>
 <td class="label top">METAR Data</td>
 <td class="data"><el:textbox name="metarData" width="75%" height="2" readOnly="true" disabled="true" /></td>
</tr>
<tr>
 <td class="label top">TAF Data</td>
 <td class="data"><el:textbox name="tafData" width="75%" height="5" readOnly="true" disabled="true" /></td> 
</tr>
</el:table>
</el:form>
<content:copyright />
</content:region>
</content:page>
<div id="copyright" class="mapTextLabel"></div>
<div id="mapStatus" class="small mapTextLabel"></div>
<div id="zoomLevel" class="mapTextLabel"></div>
<div id="seriesRefresh" class="mapTextLabel"></div>
<content:sysdata var="wuAPI" name="security.key.wunderground" />
<script id="mapInit">
<map:point var="golgotha.local.mapC" point="${homeAirport}" />
var mapOpts = {center:golgotha.local.mapC, zoom:5, minZoom:3, maxZoom:14, scrollwheel:false, streetViewControl:false, mapTypeControlOptions:{mapTypeIds:golgotha.maps.DEFAULT_TYPES}};

// Create the map
var map = new golgotha.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', golgotha.local.closeWindow);
google.maps.event.addListener(map, 'maptypeid_changed', golgotha.maps.updateMapText);
google.maps.event.addListener(map, 'zoom_changed', golgotha.maps.updateZoom);

// Add preload progress bar
map.controls[google.maps.ControlPosition.TOP_CENTER].push(golgotha.maps.util.progress.getDiv());

// Create the jetstream layers
var jsOpts = {maxZoom:8, nativeZoom:6, opacity:0.55, zIndex:golgotha.maps.z.OVERLAY};
var hjsl = new golgotha.maps.ShapeLayer(jsOpts, 'High Jet', 'wind-high');
var jsl = new golgotha.maps.ShapeLayer(jsOpts, 'Jet', 'wind-jet');
var ljsl = new golgotha.maps.ShapeLayer(jsOpts, 'Low Jet', 'wind-lojet');

// Build the layer controls
var ctls = map.controls[google.maps.ControlPosition.BOTTOM_LEFT];
var worldRadar = function() { return [loaders.series.getLatest('radar'), loaders.series.getLatest('eurorad'), loaders.series.getLatest('aussieradar')]; };
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Radar', disabled:true, c:'selImg'}, worldRadar));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Temperature', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('temp'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Wind Speed', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('windspeed'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Clouds', disabled:true, c:'selImg'}, function() { return loaders.series.getLatest('sat'); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Fronts', disabled:true, id:'selFronts'}, function() { return loaders.fr.getLayer(); }));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Low Jet'}, ljsl));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Jet Stream'}, jsl));
ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'High Jet'}, hjsl));
ctls.push(new golgotha.maps.LayerClearControl(map));

// Display the copyright notice and text boxes
map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(document.getElementById('copyright'));
map.controls[google.maps.ControlPosition.LEFT_BOTTOM].push(document.getElementById('zoomLevel'));
map.controls[google.maps.ControlPosition.RIGHT_TOP].push(document.getElementById('mapStatus'));
map.controls[google.maps.ControlPosition.LEFT_BOTTOM].push(document.getElementById('seriesRefresh'));

// Load data async once tiles are loaded
google.maps.event.addListenerOnce(map, 'tilesloaded', function() {
	golgotha.maps.reloadData(true);
	golgotha.util.createScript({id:'wuFronts', url:'//api.wunderground.com/api/${wuAPI}/fronts/view.json?callback=loaders.fr.load', async:true});
	google.maps.event.trigger(map, 'zoom_changed');
	google.maps.event.trigger(map, 'maptypeid_changed');
	golgotha.local.loadWX('${homeAirport.ICAO}');
});

golgotha.local.wxMarkers = [];
golgotha.maps.reloadData = function(isReload) {
	if (isReload) window.setInterval(golgotha.maps.reloadData, golgotha.maps.reload);
	
	// Check if we're loading/animating
	if ((map.preLoad) || (map.animator)) {
		console.log('Animating Map - reload skipped');
		return false;
	}
	
	var dv = document.getElementById('seriesRefresh');
	if (dv != null) dv.innerHTML = new Date();
	golgotha.util.createScript({id:'wxLoader', url:('//' + self.location.host + '/wx/serieslist.js?function=loaders.series.loadGinsu'), async:true});
	return true;
};
</script>
</body>
</html>
