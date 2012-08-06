<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<map:xhtml>
<head>
<title><content:airline /> Weather Center</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<map:api version="3" />
<content:googleAnalytics eventSupport="true" />
<content:sysdata var="tileHost" name="weather.tileHost" />
<c:if test="${!empty tileHost}">
<content:js name="acarsMapWX" />
<content:js name="acarsMapFF" />
<content:js name="progressBar" />
</c:if>
<script type="text/javascript">
<c:if test="${!empty jetStreamImgs}">
function setJSMapType(combo)
{
var mapType = combo.options[combo.selectedIndex].value;
var opts = jsMapTypes[mapType];

var f = document.forms[0];
var cbo = f.jsMapName;
if (opts != null) {
	cbo.options.length = opts.length;
	for (var x = 0; x < opts.length; x++)
		cbo.options[x] = opts[x];
} else {
	cbo.options.length = 1;
	cbo.options[0] = new Option('-');
}

cbo.selectedIndex = 0;
setJSMap(cbo);
var div = document.getElementById('jsURLSelect');
div.style.visibility = (combo.selectedIndex == 0) ? 'hidden' : 'visible';
return true;	
}

function setJSMap(combo)
{
var idx = combo.selectedIndex;
var row = document.getElementById('jsMapRow');
if (idx == 0) {
	row.style.display = 'none';
	return true;
} else if (row.style.display == 'none')
	row.style.display = '';

// Show the map
var opt = combo.options[idx];
var img = document.getElementById('jsImg');
img.src = opt.value;
return true;
}
</c:if>
function loadWX(code)
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
	
//Build the XML Request
var d = new Date();
var xmlreq = getXMLHttpRequest();
xmlreq.open('get', 'airportWX.ws?fa=' + useFA + '&code=' + code + '&type=METAR,TAF&time=' + d.getTime(), true);
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
		var mrk = wxMarkers[code];
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
		var p = new google.maps.LatLng(parseFloat(wx.getAttribute('lat')), parseFloat(wx.getAttribute('lng')));
		if (wx.getAttribute('pal'))
			mrk = googleIconMarker(wx.getAttribute('pal'), wx.getAttribute('icon'), p, null);
		else if (wx.getAttribute('color'))
			mrk = googleMarker(wx.getAttribute('color'), p, null);

		mrk.code = code;
		mrk.isOpen = false;
		var tabs = parseInt(wx.getAttribute('tabs'));
		if (tabs == 0) {
			var label = wx.firstChild;
			if (label)
				mrk.infoLabel = label.data;
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

		// Set the the click handlers
		google.maps.event.addListener(mrk, 'click', clickInfo);

		// Add the marker
		wxMarkers[mrk.code] = mrk;
		map.addOverlay(mrk);
	}

	var mrk = wxMarkers[code];
	if (mrk)
		google.maps.event.trigger(mrk, 'click');

	gaEvent('WeatherMap', 'Fetch TAF/METAR', code);
	return true;
}

xmlreq.send(null);
return true;
}

function closeWindow()
{
this.isOpen = false;
var f = document.forms[0];
f.wxID.value = '';
f.metarData.value = '';
f.metarData.disabled = true;
f.tafData.value = '';
f.tafData.disabled = true;
return true;
}

function clickInfo()
{
// Display the info
if (this.tabs) {
	updateTab(this, 0, new google.maps.Size(325, 100));
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
}
</script>
<map:wxList layers="radar,eurorad,sat,temp,windspeed,future_radar_ff" />
</head>
<content:copyright visible="false" />
<body>
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
 <td colspan="2" class="left"><content:airline /> WEATHER CENTER</td>
</tr>
<tr>
 <td class="data" colspan="2"><map:div ID="googleMap" x="100%" y="480" /><div id="copyright" class="small mapTextLabel" style="bottom:17px; right:2px; visibility:hidden;"></div></td>
</tr>
<tr>
 <td class="label">Airport Code</td>
 <td class="data"><el:text name="wxID" idx="*" className="bld" size="3" max="4" /> 
  <el:button ID="FetchButton" onClick="loadWX(document.forms[0].wxID.value)" label="FETCH WEATHER" />
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
<c:if test="${!empty jetStreamImgs}">
<tr class="title caps">
 <td colspan="2" class="left">JET STREAM FORECASTS</td>
</tr>
<tr>
 <td class="label">Jet Stream</td>
 <td class="data"><el:combo name="jsType" size="1" firstEntry="-" options="${jetStreamTypes}" onChange="void setJSMapType(this)" />
 <span id="jsURLSelect" style="visibility:hidden;"><el:combo name="jsMapName" size="1" firstEntry="-" options="${emptyList}" onChange="void setJSMap(this)" /></span></td>
</tr>
<tr id="jsMapRow" style="display:none;">
 <td class="label top">Jet Stream Map</td>
 <td class="data"><el:img ID="jsImg" src="blank.png" x="783" y="630" caption="Jet Stream Map" /></td>
</tr>
</c:if>
</el:table>
<div id="ffSlices" style="top:30px; right:7px; visibility:hidden;"><span id="ffLabel" class="small bld mapTextLabel">Select Time</span>
 <el:combo name="ffSlice" size="1" className="small" options="${emptyList}" onChange="void updateFF(this)" />
 <el:button ID="AnimateButton" label="ANIMATE" onClick="void animateFF()" /></div>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script type="text/javascript">
<map:point var="mapC" point="${homeAirport}" />
var mapTypes = {mapTypeIds: golgotha.maps.DEFAULT_TYPES};
var mapOpts = {center:mapC, zoom:5, minZoom:3, maxZoom:14, scrollwheel:false, streetViewControl:false, mapTypeControlOptions: mapTypes};

// Map marker codes
var wxMarkers = [];

// Create the map
var map = new google.maps.Map(document.getElementById('googleMap'), mapOpts);
map.getOptions = function() { return mapOpts; }; 
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', function () { map.infoWindow.close(); });
google.maps.event.addListener(map, 'maptypeid_changed', golgotha.maps.updateMapText);
<c:if test="${!empty tileHost}">
// Load the tile overlays
getTileOverlay('radar', 0.45);
getTileOverlay('eurorad', 0.45);
getTileOverlay('sat', 0.35);
getTileOverlay('temp', 0.25);
getTileOverlay('windspeed', 0.35);

// Load the ff tile overlays
var ffLayers = ['future_radar_ff'];
for (var i = 0; i < ffLayers.length; i++) {
	var layerName = ffLayers[i];
	var dates = getFFSlices(layerName);
	document.ffSlices[layerName] = dates;
	document.ffOptions[layerName] = getFFComboOptions(dates);
	for (var x = 0; x < dates.length; x++)
		getFFOverlay(layerName, 0.45, dates[x]);
}

// Build the layer controls
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXOverlayControl('Radar', ['radar', 'eurorad']));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXOverlayControl('Infrared', 'sat'));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXOverlayControl('Temperature', 'temp'));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new FFOverlayControl('Future Radar', 'future_radar_ff'));
map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(new WXClearControl());
google.maps.event.addListener(map, 'maptypeid_changed', hideAllSlices);

// Display the copyright notice
var d = new Date();
var cp = document.getElementById('copyright');
cp.innerHTML = 'Weather Data &copy; ' + d.getFullYear() + ' The Weather Channel.'

// Initialize FastForward elements
google.maps.event.addListenerOnce(map, 'tilesloaded', function() { 
	addOverlay(map, 'ffSlices'); 
	addOverlay(map, 'copyright'); 
	google.maps.event.trigger(this, 'maptypeid_changed');
});

// Update text color
google.maps.event.trigger(map, 'maptypeid_changed');</c:if>

// Load METAR for home airport
loadWX('${homeAirport.ICAO}');
<c:if test="${!empty jetStreamImgs}">
// Load Jet Stream map types
var jsMapTypes = [];
<c:forEach var="mapType" items="${fn:keys(jetStreamImgs)}">
var mapOptions = [];
mapOptions.push(new Option('-'));
<c:forEach var="mapURL" items="${jetStreamImgs[mapType]}">
mapOptions.push(new Option('${mapURL.comboName}', '${mapURL.comboAlias}'));</c:forEach>
jsMapTypes['${mapType}'] = mapOptions;
</c:forEach>
</c:if>
</script>
</body>
</map:xhtml>
