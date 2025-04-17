<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Weather Center</title>
<content:expire expires="3600" />
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<map:api version="3" js="googleMapsWX,wxParsers" callback="golgotha.local.mapInit" />
<content:googleAnalytics eventSupport="true" />
<script async>
golgotha.local.loadWX = function(code)
{
if (code.length < 4) {
	alert('Please provide the Airport code.');
	return false;
}

golgotha.local.useFA = false;
<content:filter roles="Route,Dispatch">
// Check for FlightAware Weather
const f = document.forms[0];
golgotha.local.useFA = f.useFA.checked;</content:filter>
	
// Build the XML Request
const xmlreq = new XMLHttpRequest();
xmlreq.timeout = 4500;
xmlreq.open('GET', 'airportwx.ws?fa=' + golgotha.local.useFA + '&code=' + code + '&type=METAR,TAF&time=' + golgotha.util.getTimestamp(30000), true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	const js = JSON.parse(xmlreq.responseText);

	// Load the weather data
	for (var i = 0; i < js.wx.length; i++) {
		const wx = js.wx[i];

		// Check for an existing marker
		let mrk = golgotha.local.wxMarkers[wx.code];
		if (mrk) {
			if (mrk.isOpen) map.infoWindow.close();
			if (wx.tabs.length == 0) {
				delete mrk.tabs;
				const label = wx.firstChild;
				if (label)
					mrk.infoLabel = label.data.replace(/\n/g, '<br />');
			} else {
				mrk.tabs = [];
				for (var x = 0; x < wx.tabs.length; x++) {
					const tab = wx.tabs[x];
					eval('mrk.' + tab.type + ' = tab.content');
					const wxData = tab.content.replace(/\n/g, '<br />');
					mrk.tabs.push({name:tab.name, content:wxData});
				}
			}

			continue;
		}

		// Create the marker
		if (wx.pal)
			mrk = new golgotha.maps.IconMarker({pal:wx.pal, icon:wx.icon}, wx.ll);
		else if (wx.color)
			mrk = new golgotha.maps.Marker({color:wx.color}, wx.ll);

		mrk.code = wx.icao; mrk.isOpen = false;
		if (wx.tabs.length == 0) {
			const label = wx.firstChild;
			if (label)
				mrk.infoLabel = label.data;
		} else {
			mrk.tabs = []; mrk.updateTab = golgotha.maps.util.updateTab; 
			for (var x = 0; x < wx.tabs.length; x++) {
				const tab = wx.tabs[x];
				eval('mrk.' + tab.type + ' = tab.content');
				const wxData = tab.content.replace(/\n/g, '<br />');
				mrk.tabs.push({name:tab.name, content:wxData});
			}
		}

		// Set the the click handlers
		google.maps.event.addListener(mrk, 'click', golgotha.local.clickInfo);

		// Add the marker
		golgotha.local.wxMarkers[mrk.code] = mrk;
		mrk.setMap(map);
	}

	const mrk = golgotha.local.wxMarkers[code];
	if (mrk)
		google.maps.event.trigger(mrk, 'click');

	golgotha.event.beacon('WeatherMap', 'Fetch TAF/METAR', code);
	return true;
};

xmlreq.send(null);
return true;
};

golgotha.local.closeWindow = function() {
    this.isOpen = false;
    const f = document.forms[0];
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
const f = document.forms[0];
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
 <td class="data"><el:text name="wxID" idx="*" className="bld" size="3" max="4" />&nbsp;<el:button onClick="void golgotha.local.loadWX(document.forms[0].wxID.value)" label="FETCH WEATHER" />
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
<script async>
<map:point var="golgotha.local.mapC" point="${homeAirport}" />

golgotha.local.mapInit = function () {
	golgotha.local.wxMarkers = [];

	// Create the map
	const mapOpts = {center:golgotha.local.mapC, zoom:5, minZoom:3, maxZoom:14, scrollwheel:false, streetViewControl:false, clickableIcons:false, mapTypeControlOptions:{mapTypeIds:golgotha.maps.DEFAULT_TYPES}};
	map = new golgotha.maps.Map(document.getElementById('googleMap'), mapOpts);
	map.setMapTypeId(golgotha.maps.info.type);
	map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW, headerDisabled:true});
	google.maps.event.addListener(map, 'click', golgotha.local.closeWindow);
	google.maps.event.addListener(map, 'maptypeid_changed', golgotha.maps.updateMapText);
	google.maps.event.addListener(map, 'zoom_changed', golgotha.maps.updateZoom);

	// Init weather layer loader
	golgotha.local.sl = new golgotha.maps.SeriesLoader();
	golgotha.local.sl.setData('radar', 0.45, 'wxRadar');
	golgotha.local.sl.setData('infrared', 0.35, 'wxSat');
	golgotha.local.sl.onload(function() { golgotha.util.enable('#selImg'); });

	// Build the layer controls
	const ctls = map.controls[google.maps.ControlPosition.BOTTOM_LEFT];
	const jsl = new golgotha.maps.ShapeLayer({maxZoom:8, nativeZoom:6, opacity:0.425, zIndex:golgotha.maps.z.OVERLAY}, 'Jet', 'wind-jet');
	ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Jet Stream'}, jsl));
	ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Radar', disabled:true, c:'selImg'}, function() { return golgotha.local.sl.getLatest('radar'); }));
	ctls.push(new golgotha.maps.LayerSelectControl({map:map, title:'Satellite', disabled:true, c:'selImg'}, function() { return golgotha.local.sl.getLatest('infrared'); }));
	ctls.push(new golgotha.maps.LayerClearControl(map));

	// Display the copyright notice and text boxes
	map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(document.getElementById('copyright'));
	map.controls[google.maps.ControlPosition.LEFT_BOTTOM].push(document.getElementById('zoomLevel'));
	map.controls[google.maps.ControlPosition.RIGHT_TOP].push(document.getElementById('mapStatus'));
	map.controls[google.maps.ControlPosition.LEFT_BOTTOM].push(document.getElementById('seriesRefresh'));

	// Load data async once tiles are loaded
	google.maps.event.addListenerOnce(map, 'tilesloaded', function() {
		google.maps.event.trigger(map, 'zoom_changed');
		google.maps.event.trigger(map, 'maptypeid_changed');
		golgotha.local.loadWX('${homeAirport.ICAO}');
		window.setTimeout(function() { golgotha.local.sl.loadRV(); }, 500);
	});
};
</script>
</body>
</html>
