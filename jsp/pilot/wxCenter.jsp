<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_mapbox.tld" prefix="map" %>
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
<map:api version="3" />
<content:js name="mapBoxWX" />
<content:csp type="CONNECT" host="tilecache.rainviewer.com" />
<content:googleAnalytics />
<content:cspHeader />
<script async>
golgotha.local.loadWX = function(code)
{
if (code.length < 4) {
	alert('Please provide the Airport code.');
	return false;
}

// Build the XML Request
const xmlreq = new XMLHttpRequest();
xmlreq.timeout = 4500;
xmlreq.open('GET', 'airportwx.ws?code=' + code + '&type=METAR,TAF&time=' + golgotha.util.getTimestamp(30000), true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	const js = JSON.parse(xmlreq.responseText);

	// Load the weather data
	for (var i = 0; i < js.wx.length; i++) {
		const wx = js.wx[i];

		// Check for an existing marker
		let mrk = golgotha.local.wxMarkers[wx.code];
		if (mrk) {
			const pp = mrk.getPopup();
			if (pp.isOpen()) mrk.togglePopup();
			if (wx.tabs.length == 0) {
				delete mrk.tabs;
				const label = wx.firstChild;
				if (label)
					pp.setHTML(label.data.replace(/\n/g, '<br />'));
			} else {
				mrk.tabs = [];
				for (var x = 0; x < wx.tabs.length; x++) {
					const tab = wx.tabs[x];
					eval('mrk.' + tab.type + ' = tab.content');
					const wxData = tab.content.replace(/\n/g, '<br />');
					mrk.tabs.push({name:tab.name, content:wxData});
					pp.setHTML(mrk.updateTab(0));
				}
			}

			continue;
		}

		// Create the marker
		mrk = (wx.pal) ? new golgotha.maps.IconMarker({pal:wx.pal, icon:wx.icon, pt:wx.ll, label:wx.icao}) : new golgotha.maps.Marker({color:wx.color, pt:wx.ll, label:wx.icao});
		mrk.code = wx.icao;
		const p = new mapboxgl.Popup({closeOnClick:true,focusAfterOpen:false,maxWidth:'500px'});
		if (wx.tabs.length == 0) {
			const label = wx.firstChild;
			if (label)
				p.setHTML(label.data.replace(/\n/g, '<br />'));
		} else {
			mrk.tabs = []; mrk.updateTab = golgotha.maps.util.updateTab; 
			for (var x = 0; x < wx.tabs.length; x++) {
				const tab = wx.tabs[x];
				eval('mrk.' + tab.type + ' = tab.content');
				const wxData = tab.content.replace(/\n/g, '<br />');
				mrk.tabs.push({name:tab.name, content:wxData});
				p.setHTML(mrk.updateTab(0));
			}
		}

		// Create the event handlers
		p.on('close', function() { golgotha.local.clickInfo(); });
		p.on('open', function(e) {
			golgotha.maps.selectedMarker = e.target._marker;
			golgotha.local.clickInfo(e.target._marker);
		});

		// Add the marker
		mrk.setPopup(p);
		golgotha.local.wxMarkers[mrk.code] = mrk;
		mrk.setMap(map);
	}

	const mrk = golgotha.local.wxMarkers[code];
	if (mrk) mrk.togglePopup();
	golgotha.event.beacon('WeatherMap', 'Fetch TAF/METAR', code);
	return true;
};

xmlreq.send(null);
return true;
};

golgotha.local.clickInfo = function(mrk) {
	const hasMrk = (mrk);
	const f = document.forms[0];
	f.wxID.value = hasMrk ? mrk.code : '';
	f.metarData.value = hasMrk ? mrk.METAR : '';
	f.metarData.disabled = !hasMrk;
	f.tafData.value = hasMrk ? mrk.TAF : '';
	f.tafData.disabled = !hasMrk;
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
 <td class="data" colspan="2"><map:div ID="mapBox" height="480" /></td>
</tr>
<tr>
 <td class="label">Airport Code</td>
 <td class="data"><el:text name="wxID" idx="*" className="bld" size="3" max="4" />&nbsp;<el:button onClick="void golgotha.local.loadWX(document.forms[0].wxID.value)" label="FETCH WEATHER" /></td>
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
<div id="copyright" class="small mapTextLabel"></div><div id="zoomLevel" class="small right mapTextLabel"></div><div id="seriesRefresh" class="small mapTextLabel"></div>
<script async>
<map:token />
<map:point var="golgotha.local.mapC" point="${homeAirport}" />
golgotha.local.wxMarkers = [];

// Create the map
const map = new golgotha.maps.Map(document.getElementById('mapBox'), {center:golgotha.local.mapC, zoom:5, minZoom:3, maxZoom:12, scrollZoom:false, projection:'globe', style:'mapbox://styles/mapbox/outdoors-v12'});
map.addControl(new mapboxgl.FullscreenControl(), 'top-right');
map.addControl(new mapboxgl.NavigationControl(), 'top-right');
map.on('style.load', golgotha.maps.updateMapText);
map.on('zoomend', golgotha.maps.updateZoom);

// Init weather layer loader
golgotha.local.sl = new golgotha.maps.wx.SeriesLoader();
golgotha.local.sl.setData('radar', 0.45, 'wxRadar');
golgotha.local.sl.setData('infrared', 0.35, 'wxSat');
golgotha.local.sl.onload(function() { golgotha.util.enable('#selImg'); });

// Build the layer controls
golgotha.maps.wx.ctl = new golgotha.maps.wx.WXLayerControl();
golgotha.maps.wx.ctl.addLayer({name:'Radar', c:'selImg', disabled:true, f:function() { return golgotha.local.sl.getLatest('radar'); }});
golgotha.maps.wx.ctl.addLayer({name:'Satellite', c:'selImg', disabled:true, id:'infrared', f:function() { return golgotha.local.sl.getLatest('infrared'); }});
map.addControl(golgotha.maps.wx.ctl, 'bottom-left');

// Display the copyright notice and text boxes
map.addControl(new golgotha.maps.DIVControl('copyright'), 'bottom-right');
map.addControl(new golgotha.maps.DIVControl('zoomLevel'), 'bottom-right');
map.addControl(new golgotha.maps.DIVControl('seriesRefresh'), 'bottom-left');

// Load data async once tiles are loaded
map.once('load', function() {
	map.addControl(new golgotha.maps.BaseMapControl(golgotha.maps.DEFAULT_TYPES), 'top-left');
	golgotha.local.loadWX('${homeAirport.ICAO}');
	window.setTimeout(function() { golgotha.local.sl.loadRV(); }, 500);
	map.fire('zoomend');
});
</script>
</body>
</html>
