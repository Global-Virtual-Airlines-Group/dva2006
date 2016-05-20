golgotha.maps.acars = golgotha.maps.acars || {acPositions:[], dcPositions:[], routeData:null, routeWaypoints:null, tempData:null};
golgotha.maps.acars.generateXMLRequest = function()
{
var xmlreq = new XMLHttpRequest();
xmlreq.open('get', 'acars_map.ws?time=' + golgotha.util.getTimestamp(3000), true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	var isLoading = document.getElementById('isLoading');
	if (isLoading && (xmlreq.status != 200)) {
		isLoading.innerHTML = ' - ERROR ' + xmlreq.status;
		return false;
	} else if (isLoading)
		isLoading.innerHTML = ' - REDRAWING...';
		
	// Clean up the map - don't strip out the weather layer
	map.removeMarkers(golgotha.maps.acars.routeData);
	map.removeMarkers(golgotha.maps.acars.routeWaypoints);
	map.removeMarkers(golgotha.maps.acars.acPositions);
	map.removeMarkers(golgotha.maps.acars.dcPositions);
	golgotha.maps.acars.acPositions.length = 0;
	golgotha.maps.acars.dcPositions.length = 0;
	golgotha.util.display('userSelect', false);
	var cbo = document.forms[0].usrID;
	if (cbo != null) {
		selectedPilot = cbo.options[cbo.selectedIndex].value;
		cbo.options.length = 1;
	}

	// Parse the XML
	var xml = xmlreq.responseXML;
	var xe = xml.documentElement;
	var ac = xe.getElementsByTagName('aircraft');
	if (ac.length > 0) golgotha.event.beacon('ACARS', 'Aircraft Positions');
	for (var i = 0; i < ac.length; i++) {
		var a = ac[i]; var mrk = null;
		var p = {lat:parseFloat(a.getAttribute('lat')), lng:parseFloat(a.getAttribute('lng'))};
		if (a.getAttribute('pal'))
			mrk = new golgotha.maps.IconMarker({pal:a.getAttribute('pal'), icon:a.getAttribute('icon')}, p);
		else if (a.getAttribute('color'))
			mrk = new golgotha.maps.Marker({color:a.getAttribute('color')}, p);

		mrk.flight_id = a.getAttribute('flight_id');
		mrk.isBusy = (a.getAttribute('busy') == 'true');
		var tabs = parseInt(a.getAttribute('tabs'));
		if (tabs == 0) {
			var label = a.firstChild;
			mrk.infoLabel = label.data;
		} else {
			mrk.tabs = []; mrk.updateTab = golgotha.maps.util.updateTab;
			var tbs = a.getElementsByTagName('tab');
			for (var x = 0; x < tbs.length; x++) {
				var tab = tbs[x];
				var label = tab.firstChild;
				mrk.tabs.push({name:tab.getAttribute('name'), content:label.data});
			}
		}

		// Add the user ID
		var pns = a.getElementsByTagName('pilot');
		if ((pns.length > 0) && (cbo != null)) {
			var pn = pns[0];
			var lbl = pn.firstChild.data;
			var id = pn.getAttribute('id');
			if (id != null)
				lbl = lbl + ' (' + id + ')';

			var o = new Option(lbl, id);
			o.mrk = mrk;
			try {
				cbo.add(o, null);
			} catch (err) {
				cbo.add(o); // IE hack
			}
			if (selectedPilot == id)
				cbo.selectedIndex = (cbo.options.length - 1);
		}

		// Set the the click handler
		google.maps.event.addListener(mrk, 'click', golgotha.maps.acars.clickAircraft);
		golgotha.maps.acars.acPositions.push(mrk);
		mrk.setMap(map);
	} // for

	var dc = xe.getElementsByTagName('dispatch');
	if (dc.length > 0) golgotha.event.beacon('ACARS', 'Dispatch Positions');
	for (var i = 0; i < dc.length; i++) {
		var d = dc[i]; var mrk = null;
		var p = {lat:parseFloat(d.getAttribute('lat')), lng:parseFloat(d.getAttribute('lng'))};
		if (d.getAttribute('pal'))
			mrk = new golgotha.maps.IconMarker({pal:d.getAttribute('pal'), icon:d.getAttribute('icon')}, p);
		else if (d.getAttribute('color'))
			mrk = new golgotha.maps.Marker({color:d.getAttribute('color')}, p);

		mrk.range = parseInt(d.getAttribute('range'));
		mrk.isBusy = (d.getAttribute('busy') == 'true');
		var tabs = parseInt(d.getAttribute('tabs'));
		if (tabs == 0) {
			var le = d.getElementsByTagName('info');
			mrk.infoLabel = le[0].firstChild.data;
		} else {
			mrk.tabs = []; mrk.updateTab = golgotha.maps.util.updateTab;
			var tbs = d.getElementsByTagName('tab');
			for (var x = 0; x < tbs.length; x++) {
				var tab = tbs[x];
				var label = tab.firstChild;
				mrk.tabs.push({name:tab.getAttribute('name'), content:label.data});
			}
		}

		// Add the user ID
		var pns = d.getElementsByTagName('pilot');
		if ((pns.length > 0) && (cbo != null)) {
			var pn = pns[0];
			var id = pn.getAttribute('id');
			var o = new Option(pn.firstChild.data + ' (' + id + '/Dispatcher)', id);
			o.mrk = mrk;
			try {
				cbo.add(o, null);
			} catch (err) {
				cbo.add(o); // IE hack
			}
			if (selectedPilot == id)
				cbo.selectedIndex = (cbo.options.length - 1);
		}

		// Set the the click handler
		google.maps.event.addListener(mrk, 'click', golgotha.maps.acars.clickDispatch);
		golgotha.maps.acars.dcPositions.push(mrk);
		mrk.setMap(map);
	} // for

	// Enable the Google Earth button depending on if we have any aircraft
	golgotha.util.disable('EarthButton', (ac.length == 0));

	// Display dispatch status
	var de = document.getElementById('dispatchStatus');
	if ((de) && (dc.length > 0)) {
		de.className = 'ter bld caps';
		de.innerHTML = 'Dispatcher Currently Online';
	} else if (de) {
		de.className = 'bld caps';	
		de.innerHTML = 'Dispatcher Currently Offline';
	}

	// Focus on the map
	if (cbo)
		golgotha.util.display('userSelect', (cbo.options.length > 1));
	if (isLoading)
		isLoading.innerHTML = ' - ' + (ac.length + dc.length) + ' CONNECTIONS';

	return true;
};

return xmlreq;
};

golgotha.maps.acars.clickAircraft = function(e)
{
// Check what info we display
var f = document.forms[0];
var isProgress = f.showProgress.checked;
var isRoute = f.showRoute.checked;
var isInfo = f.showInfo.checked;
golgotha.event.beacon('ACARS', 'Flight Info');

// Display the info - show tab 0
if (isInfo && (this.tabs)) {
	this.updateTab(0);
	map.infoWindow.marker = this;
	map.infoWindow.open(map, this);
} else if (isInfo) {
	map.infoWindow.setContent(this.infoLabel);
	map.infoWindow.open(map, this); 
}

// Display flight progress / route
if (isProgress || isRoute) {
	map.removeMarkers(golgotha.maps.acars.routeData);
	map.removeMarkers(golgotha.maps.acars.tempData);
	map.removeMarkers(golgotha.maps.acars.routeWaypoints);
	golgotha.maps.acars.showFlightProgress(this, isProgress, isRoute);
}

document.pauseRefresh = true;
return true;
};

golgotha.maps.acars.clickDispatch = function(e)
{
// Check what info we display
var f = document.forms[0];
var isInfo = f.showInfo.checked;
golgotha.event.beacon('ACARS', 'Dispatch Info');

// Display the info
if (isInfo && (this.tabs)) {
	this.updateTab(0);
	map.infoWindow.marker = this;
	map.infoWindow.open(map, this);
} else if (isInfo) {
	map.infoWindow.marker = this;
	map.infoWindow.setContent(this.infoLabel);
	map.infoWindow.open(map, this);
}

// Display flight progress / route
if (!this.rangeCircle) {
	this.rangeCircle = golgotha.maps.acars.getServiceRange(this, this.range);
	if (this.rangeCircle) {
		golgotha.event.beacon('ACARS', 'Dispatch Service Range');
		this.rangeCircle.setMap(map);
	}
} else
	this.rangeCircle.setMap(map);

document.pauseRefresh = true;
return true;
};

golgotha.maps.acars.infoClose = function()
{
document.pauseRefresh = false;
if ((map.infoWindow.marker) && (map.infoWindow.marker.rangeCircle))
	map.infoWindow.marker.rangeCircle.setMap(null);

map.removeMarkers(golgotha.maps.acars.routeData);
map.removeMarkers(golgotha.maps.acars.tempData);
map.removeMarkers(golgotha.maps.acars.routeWaypoints);
map.closeWindow();
return true;	
};

golgotha.maps.acars.showFlightProgress = function(marker, doProgress, doRoute)
{
// Build the XML Requester
var xreq = new XMLHttpRequest();
xreq.open('GET', 'acars_progress.ws?id=' + marker.flight_id + '&time=' + golgotha.util.getTimestamp(3000) + '&route=' + doRoute, true);
xreq.onreadystatechange = function() {
	if ((xreq.readyState != 4) || (xreq.status != 200)) return false;

	// Load the XML
	var xdoc = xreq.responseXML;
	var wsdata = xdoc.documentElement;

	// Draw the flight route
	if (doRoute) {
		var wps = [].slice.call(wsdata.getElementsByTagName('route'));
		var waypoints = [];
		for (var wp = wps.pop(); (wp != null); wp = wps.pop())
			waypoints.push({lat:parseFloat(wp.getAttribute('lat')), lng:parseFloat(wp.getAttribute('lng'))});

		golgotha.event.beacon('ACARS', 'Flight Route Info');
		golgotha.maps.acars.routeWaypoints = new google.maps.Polyline({map:map, path:waypoints, strokeColor:'#af8040', strokeWeight:2, strokeOpacity:0.7, geodesic:true, zIndex:golgotha.maps.z.POLYLINE});
	}

	// Draw the flight progress
	if (doProgress) {
		var pos = [].slice.call(wsdata.getElementsByTagName('pos'));
		var positions = [];
		for (var pe = pos.pop(); (pe != null); pe = pos.pop())
			positions.push({lat:parseFloat(pe.getAttribute('lat')), lng:parseFloat(pe.getAttribute('lng'))});
		
		var tpos = [].slice.call(wsdata.getElementsByTagName('tpos'));
		var tpositions = [];
		if (positions.length > 0) tpositions.push(positions[0]);
		for (var pe = tpos.pop(); (pe != null); pe = tpos.pop())
			tpositions.push({lat:parseFloat(pe.getAttribute('lat')), lng:parseFloat(pe.getAttribute('lng'))});

		// Draw the lines
		golgotha.event.beacon('ACARS', 'Flight Progress Info');
		golgotha.maps.acars.routeData = new google.maps.Polyline({map:map, path:positions, strokeColor:'#4080af', strokeWeight:2, strokeOpacity:0.8, zIndex:(golgotha.maps.z.POLYLINE-1)});
		golgotha.maps.acars.tempData = new google.maps.Polyline({map:map, path:tpositions, strokeColor:'#20a0bf', strokeWeight:2, strokeOpacity:0.625, zIndex:(golgotha.maps.z.POLYLINE-1)});
	}

	return true;
};

xreq.send(null);
return true;
};

golgotha.maps.acars.getServiceRange = function(marker, range)
{
var bC = marker.isBusy ? '#c02020' : '#20c060';
var fC = marker.isBusy ? '#802020' : '#208040';
var fOp = marker.isBusy ? 0.1 : 0.2;
return new google.maps.Circle({center:marker.getPosition(), radius:golgotha.maps.miles2Meter(range), strokeColor:bC, strokeWeight:1, strokeOpacity:0.65, fillColor:fC, fillOpacity:fOp, zIndex:golgotha.maps.z.POLYGON});
};

golgotha.maps.acars.zoomTo = function(combo)
{
var opt = combo.options[combo.selectedIndex];
if ((!opt) || (opt.mrk == null)) return false;

// Check if we zoom or just pan
var f = document.forms[0];
if (f.zoomToPilot.checked) map.setZoom(9);

// Pan to the marker
map.panTo(opt.mrk.getPosition());
google.maps.event.trigger(opt.mrk, 'click');
return true;
};
