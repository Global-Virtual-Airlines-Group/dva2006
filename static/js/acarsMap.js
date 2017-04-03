golgotha.maps.acars = golgotha.maps.acars || {acPositions:[], dcPositions:[], routeData:null, routeWaypoints:null, tempData:null, routeMarkers:[]};
golgotha.maps.acars.generateXMLRequest = function()
{
var xmlreq = new XMLHttpRequest();
xmlreq.open('get', 'acars_map_json.ws?time=' + golgotha.util.getTimestamp(3000), true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	if (xmlreq.status != 200) {
		golgotha.util.setHTML('isLoading', ' - ERROR ' + xmlreq.status);
		return false;
	}
		golgotha.util.setHTML('isLoading', ' - REDRAWING...');
		
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

	// Parse the JSON
	var js = JSON.parse(xmlreq.responseText);
	if (js.aircraft.length > 0) golgotha.event.beacon('ACARS', 'Aircraft Positions');
	for (var i = 0; i < js.aircraft.length; i++) {
		var a = js.aircraft[i]; var mrk;
		if (a.pal)
			mrk = new golgotha.maps.IconMarker({pal:a.pal, icon:a.icon}, a.ll);
		else if (a.color)
			mrk = new golgotha.maps.Marker({color:a.color}, a.ll);

		mrk.flight_id = a.flight_id;
		mrk.isBusy = a.busy;
		if (a.tabs.length == 0)
			mrk.infoLabel = a.info;
		else {
			mrk.updateTab = golgotha.maps.util.updateTab;
			mrk.tabs = a.tabs;
		}

		// Add the user ID
		if ((a.pilot) && (cbo != null)) {
			var lbl = a.pilot.name;
			if (a.pilot.code != null)
				lbl = lbl + ' (' + a.pilot.code + ')';

			var o = new Option(lbl, a.pilot.code);
			o.mrk = mrk;
			try {
				cbo.add(o, null);
			} catch (err) {
				cbo.add(o); // IE hack
			}
			if (selectedPilot == a.pilot.code)
				cbo.selectedIndex = (cbo.options.length - 1);
		}

		// Set the the click handler
		google.maps.event.addListener(mrk, 'click', golgotha.maps.acars.clickAircraft);
		golgotha.maps.acars.acPositions.push(mrk);
		mrk.setMap(map);
	} // for

	if (js.dispatch.length > 0) golgotha.event.beacon('ACARS', 'Dispatch Positions');
	for (var i = 0; i < js.dispatch.length; i++) {
		var d = js.dispatch[i]; var mrk;
		if (d.pal)
			mrk = new golgotha.maps.IconMarker({pal:d.pal, icon:d.icon}, d.ll);
		else if (d.color)
			mrk = new golgotha.maps.Marker({color:d.color}, d.ll);

		mrk.range = d.range;
		mrk.isBusy = d.busy;
		if (d.tabs.length == 0)
			mrk.infoLabel = d.info;
		else {
			mrk.updateTab = golgotha.maps.util.updateTab;
			mrk.tabs = d.tabs;
		}

		// Add the user ID
		if ((d.pilot) && (cbo != null)) {
			var o = new Option(d.pilot.name + ' (' + d.pilot.code + '/Dispatcher)', d.pilot.code);
			o.mrk = mrk;
			try {
				cbo.add(o, null);
			} catch (err) {
				cbo.add(o); // IE hack
			}
			if (selectedPilot == d.pilot.code)
				cbo.selectedIndex = (cbo.options.length - 1);
		}

		// Set the the click handler
		google.maps.event.addListener(mrk, 'click', golgotha.maps.acars.clickDispatch);
		golgotha.maps.acars.dcPositions.push(mrk);
		mrk.setMap(map);
	} // for

	// Enable the Google Earth button depending on if we have any aircraft
	golgotha.util.disable('EarthButton', (js.aircraft.length == 0));

	// Display dispatch status
	var de = document.getElementById('dispatchStatus');
	if ((de) && (js.dispatch.length > 0)) {
		de.className = 'ter bld caps';
		de.innerHTML = 'Dispatcher Currently Online';
	} else if (de) {
		de.className = 'bld caps';	
		de.innerHTML = 'Dispatcher Currently Offline';
	}

	// Focus on the map
	golgotha.util.setHTML('isLoading', ' - ' + (js.aircraft.length + js.dispatch.length) + ' CONNECTIONS');
	if (cbo)
		golgotha.util.display('userSelect', (cbo.options.length > 1));

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
map.removeMarkers(golgotha.maps.acars.routeMarkers);
map.closeWindow();
return true;	
};

golgotha.maps.acars.showFlightProgress = function(marker, doProgress, doRoute)
{
// Build the XML Requester
var xreq = new XMLHttpRequest();
xreq.open('GET', 'acars_progress_json.ws?id=' + marker.flight_id + '&time=' + golgotha.util.getTimestamp(3000) + '&route=' + doRoute, true);
xreq.onreadystatechange = function() {
	if ((xreq.readyState != 4) || (xreq.status != 200)) return false;

	// Draw the flight route
	var js = JSON.parse(xreq.responseText);
	if (doRoute) {
		var waypoints = [];
		js.waypoints.forEach(function(wp) { 
			waypoints.push(wp.ll); var mrk;
			if (wp.pal)
				mrk = new golgotha.maps.IconMarker({map:map, pal:wp.pal, icon:wp.icon, opacity:0.5}, wp.ll);
			else if (a.color)
				mrk = new golgotha.maps.Marker({map:map, color:wp.color, opacity:0.5}, wp.ll);

			golgotha.maps.acars.routeMarkers.push(mrk)
		});

		golgotha.event.beacon('ACARS', 'Flight Route Info');
		golgotha.maps.acars.routeWaypoints = new google.maps.Polyline({map:map, path:waypoints, strokeColor:'#af8040', strokeWeight:2, strokeOpacity:0.7, geodesic:true, zIndex:golgotha.maps.z.POLYLINE});
	}

	// Draw the flight progress
	if (doProgress) {
		golgotha.event.beacon('ACARS', 'Flight Progress Info');
		golgotha.maps.acars.routeData = new google.maps.Polyline({map:map, path:js.savedPositions, strokeColor:'#4080af', strokeWeight:2, strokeOpacity:0.8, zIndex:(golgotha.maps.z.POLYLINE-1)});
		golgotha.maps.acars.tempData = new google.maps.Polyline({map:map, path:js.tempPositions, strokeColor:'#20a0bf', strokeWeight:2, strokeOpacity:0.625, zIndex:(golgotha.maps.z.POLYLINE-1)});
	}

	return true;
};

xreq.send(null);
return true;
};

golgotha.maps.acars.getServiceRange = function(marker, range) {
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
