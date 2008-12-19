<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> Weather Center</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="googleMaps" />
<content:googleAnalytics eventSupport="true" />
<content:sysdata var="imgPath" name="path.img" />
<content:sysdata var="tileHost" name="weather.tileHost" />
<map:api version="2" />
<map:vml-ie />
<c:if test="${!empty tileHost}">
<content:js name="acarsMapWX" />
<content:js name="acarsMapFF" />
</c:if>
<script language="JavaScript" type="text/javascript">
document.imgPath = '${imgPath}';
<c:if test="${!empty tileHost}">document.tileHost = '${tileHost}';</c:if>

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
var xmlreq = GXmlHttp.create();
xmlreq.open("GET", "airportWX.ws?fa=" + useFA + "&code=" + code + "&type=METAR,TAF&time=" + d.getTime(), true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	var xdoc = xmlreq.responseXML.documentElement;

	// Load the weather data
	var wc = xdoc.getElementsByTagName("wx");
	for (var i = 0; i < wc.length; i++) {
		var wx = wc[i];

		// Check for an existing marker
		var code = wx.getAttribute("icao");
		var wxType = wx.getAttribute("type");
		var mrk = wxMarkers[code];
		if (mrk) {
			if (mrk.isOpen)
				mrk.closeInfoWindow();

			var tabs = parseInt(wx.getAttribute("tabs"));
			if (tabs == 0) {
				delete mrk.tabs;
				var label = wx.firstChild;
				if (label)
					mrk.infoLabel = label.data.replace(/\n/g, "<br />");
			} else {
				mrk.tabs = new Array();
				var tbs = wx.getElementsByTagName("tab");
				for (var x = 0; x < tbs.length; x++) {
					var tab = tbs[x];
					var tabType = tab.getAttribute("type");
					var label = tab.firstChild;
					if (label) {
						eval('mrk.' + tabType + ' = label.data');
						var wxData = label.data.replace(/\n/g, "<br />");
						mrk.tabs.push(new GInfoWindowTab(tab.getAttribute("name"), wxData));
					}
				}
			}

			continue;
		}

		// Create the marker
		var p = new GLatLng(parseFloat(wx.getAttribute("lat")), parseFloat(wx.getAttribute("lng")));
		if (wx.getAttribute("pal"))
			mrk = googleIconMarker(wx.getAttribute("pal"), wx.getAttribute("icon"), p, null);
		else if (wx.getAttribute("color"))
			mrk = googleMarker(document.imgPath, wx.getAttribute("color"), p, null);

		mrk.code = code;
		mrk.isOpen = false;
		var tabs = parseInt(wx.getAttribute("tabs"));
		if (tabs == 0) {
			var label = wx.firstChild;
			if (label)
				mrk.infoLabel = label.data;
		} else {
			mrk.tabs = new Array();
			var tbs = wx.getElementsByTagName("tab");
			for (var x = 0; x < tbs.length; x++) {
				var tab = tbs[x];
				var tabType = tab.getAttribute("type");
				var label = tab.firstChild;
				if (label) {
					eval('mrk.' + tabType + ' = label.data');
					var wxData = label.data.replace(/\n/g, "<br />");
					mrk.tabs.push(new GInfoWindowTab(tab.getAttribute("name"), wxData));
				}
			}
		}

		// Set the the click handlers
		GEvent.bind(mrk, 'click', mrk, clickInfo);
		GEvent.bind(mrk, 'infowindowclose', mrk, closeWindow);

		// Add the marker
		wxMarkers[mrk.code] = mrk;
		map.addOverlay(mrk);
	}

	var mrk = wxMarkers[code];
	if (mrk)
		GEvent.trigger(mrk, 'click');

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
if (this.tabs)
	this.openInfoWindowTabsHtml(this.tabs)
else
	this.openInfoWindowHtml(this.infoLabel);

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
<c:if test="${!empty tileHost}"><script src="http://${tileHost}/TileServer/jserieslist.do?function=loadSeries&amp;id=wx&amp;type=radar,sat,temp,windspeed,future_radar_ff" type="text/javascript"></script></c:if>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:getCookie name="acarsMapType" default="map" var="gMapType" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="wxcenter.do" method="get" validate="return false">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="2" class="left"><content:airline /> WEATHER CENTER</td>
</tr>
<tr>
 <td class="label" valign="top">Weather Map</td>
 <td class="data"><map:div ID="googleMap" x="100%" y="480" /><div id="copyright" class="small"></div></td>
</tr>
<tr>
 <td class="label">Airport Code</td>
 <td class="data"><el:text name="wxID" idx="*" className="bld" size="3" max="4" /> 
  <el:button ID="FetchButton" onClick="loadWX(document.forms[0].wxID.value)" className="BUTTON" label="FETCH WEATHER" />
<content:filter roles="Route,Dispatch"> <el:box name="useFA" value="true" checked="false" label="Use FlightAware Weather" /></content:filter></td>
</tr>
<tr>
 <td class="label" valign="top">METAR Data</td>
 <td class="data"><el:textbox name="metarData" width="75%" height="2" readOnly="true" disabled="true" /></td>
</tr>
<tr>
 <td class="label" valign="top">TAF Data</td>
 <td class="data"><el:textbox name="tafData" width="75%" height="5" readOnly="true" disabled="true" /></td> 
</tr>
</el:table>
<div id="ffSlices" style="visibility:hidden;"><span id="ffLabel" class="small bld">Select Time</span>
 <el:combo name="ffSlice" size="1" className="small" options="${emptyList}" onChange="void updateFF(this)" />
 <el:button ID="AnimateButton" className="BUTTON" label="ANIMATE" onClick="void animateFF()" /></div>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script language="JavaScript" type="text/javascript">
<map:point var="mapC" point="${homeAirport}" />
// Create the map
var map = new GMap2(getElement("googleMap"), {mapTypes:[G_NORMAL_MAP, G_SATELLITE_MAP, G_PHYSICAL_MAP]});
<c:if test="${!empty tileHost}">
// Load the tile overlays
getTileOverlay("radar", 0.45);
getTileOverlay("eurorad", 0.45);
getTileOverlay("sat", 0.35);
getTileOverlay("temp", 0.25);
getTileOverlay("windspeed", 0.35);

// Load the ff tile overlays
var ffLayers = ["future_radar_ff"];
for (var i = 0; i < ffLayers.length; i++) {
	var layerName = ffLayers[i];
	var dates = getFFSlices(layerName);
	document.ffSlices[layerName] = dates;
	document.ffOptions[layerName] = getFFComboOptions(dates);
	for (var x = 0; x < dates.length; x++)
		getFFOverlay(layerName, 0.45, dates[x]);
}

// Build the layer controls
var xPos = 70;
map.addControl(new WXOverlayControl("Radar", ["radar", "eurorad"], new GSize(xPos, 7)));
map.addControl(new WXOverlayControl("Infrared", "sat", new GSize((xPos += 72), 7)));
map.addControl(new WXOverlayControl("Temperature", "temp", new GSize((xPos += 72), 7)));
map.addControl(new WXOverlayControl("Wind Speed", "windspeed", new GSize((xPos += 81), 7)));
map.addControl(new FFOverlayControl("Future Radar", "future_radar_ff", new GSize((xPos += 81), 7)));
map.addControl(new WXClearControl(new GSize((xPos += 91), 7)));
</c:if>
// Add map controls
map.addControl(new GLargeMapControl());
map.addControl(new GMapTypeControl());
map.setCenter(mapC, 5);
map.enableDoubleClickZoom();
map.enableContinuousZoom();
<map:type map="map" type="${gMapType}" default="G_PHYSICAL_MAP" />
GEvent.addListener(map, 'maptypechanged', updateMapText);
GEvent.addListener(map, 'maptypechanged', hideAllSlices);

// Map marker codes
var wxMarkers = new Array();

<c:if test="${!empty tileHost}">
// Display the copyright notice
var d = new Date();
var cp = document.getElementById("copyright");
cp.innerHTML = 'Weather Data &copy; ' + d.getFullYear() + ' The Weather Channel.'
var cpos = new GControlPosition(G_ANCHOR_BOTTOM_RIGHT, new GSize(4, 16));
cpos.apply(cp);
mapTextElements.push(cp);
map.getContainer().appendChild(cp);

// Initialize FastForward elements
var ffs = document.getElementById("ffSlices");
var ffpos = new GControlPosition(G_ANCHOR_TOP_RIGHT, new GSize(8, 30));
ffpos.apply(ffs);
map.getContainer().appendChild(ffs);
var ffl = document.getElementById("ffLabel");
mapTextElements.push(ffl);

// Update text color
GEvent.trigger(map, 'maptypechanged');</c:if>

// Load METAR for home airport
loadWX('${homeAirport.ICAO}');
</script>
</body>
</map:xhtml>
