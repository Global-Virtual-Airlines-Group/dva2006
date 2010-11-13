function generateXMLRequest()
{
// Build the XMLHTTPRequest
var d = new Date();
var dtime = d.getTime() - (d.getTime() % 3000);
var xmlreq = getXMLHttpRequest();
xmlreq.open('get', 'acars_map.ws?time=' + dtime, true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	var isLoading = getElement('isLoading');
	if (isLoading && (xmlreq.status != 200)) {
		isLoading.innerHTML = ' - ERROR ' + xmlreq.status;
		return false;
	} else if (isLoading)
		isLoading.innerHTML = ' - REDRAWING...';
		
	// Clean up the map - don't strip out the weather layer
	removeMarkers('routeData');
	removeMarkers('routeWaypoints');
	removeMarkers('acPositions');
	removeMarkers('dcPositions');
	acPositions.length = 0;
	dcPositions.length = 0;
	displayObject(getElement('userSelect'), false);
	var cbo = document.forms[0].usrID;
	selectedPilot = cbo.options[cbo.selectedIndex].value;
	cbo.options.length = 1;

	// Parse the XML
	var xml = xmlreq.responseXML;
	var xe = xml.documentElement;
	var ac = xe.getElementsByTagName('aircraft');
	gaEvent('ACARS', 'Aircraft Positions');
	for (var i = 0; i < ac.length; i++) {
		var a = ac[i]; var mrk = null;
		var p = new google.maps.LatLng(parseFloat(a.getAttribute('lat')), parseFloat(a.getAttribute('lng')));
		if (a.getAttribute('pal'))
			mrk = googleIconMarker(a.getAttribute('pal'), a.getAttribute('icon'), p, null);
		else if (a.getAttribute('color'))
			mrk = googleMarker(a.getAttribute('color'), p, null);

		mrk.flight_id = a.getAttribute('flight_id');
		mrk.isBusy = (a.getAttribute('busy') == 'true');
		var tabs = parseInt(a.getAttribute('tabs'));
		if (tabs == 0) {
			var label = a.firstChild;
			mrk.infoLabel = label.data;
		} else {
			mrk.tabs = [];
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
		google.maps.event.bind(mrk, 'click', mrk, clickAircraft);
		acPositions.push(mrk);
		mrk.setMap(map);
	} // for

	var dc = xe.getElementsByTagName('dispatch');
	if (dc.length > 0)
		gaEvent('ACARS', 'Dispatch Positions');
	for (var i = 0; i < dc.length; i++) {
		var d = dc[i]; var mrk = null;
		var p = new google.maps.LatLng(parseFloat(d.getAttribute('lat')), parseFloat(d.getAttribute('lng')));
		if (d.getAttribute('pal'))
			mrk = googleIconMarker(d.getAttribute('pal'), d.getAttribute('icon'), p, null);
		else if (d.getAttribute('color'))
			mrk = googleMarker(d.getAttribute('color'), p, null);

		mrk.range = parseInt(d.getAttribute('range'));
		mrk.isBusy = (d.getAttribute('busy') == 'true');
		var tabs = parseInt(d.getAttribute('tabs'));
		if (tabs == 0) {
			var le = d.getElementsByTagName('info');
			mrk.infoLabel = le[0].firstChild.data;
		} else {
			mrk.tabs = [];
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
		google.maps.event.bind(mrk, 'click', mrk, clickDispatch);
		dcPositions.push(mrk);
		mrk.setMap(map);
	} // for

	// Enable the Google Earth button depending on if we have any aircraft
	enableElement('EarthButton', (ac.length > 0));

	// Display dispatch status
	var de = getElement('dispatchStatus');
	if ((de) && (dc.length > 0)) {
		de.className = 'ter bld caps';
		de.innerHTML = 'Dispatcher Currently Online';
	} else if (de) {
		de.className = 'bld caps';	
		de.innerHTML = 'Dispatcher Currently Offline';
	}

	// Focus on the map
	if (cbo)
		displayObject(getElement('userSelect'), (cbo.options.length > 1));
	if (isLoading)
		isLoading.innerHTML = ' - ' + (ac.length + dc.length) + ' CONNECTIONS';

	return true;
} // function

return xmlreq;
}

function clickAircraft()
{
// Check what info we display
var f = document.forms[0];
var isProgress = f.showProgress.checked;
var isRoute = f.showRoute.checked;
var isInfo = f.showInfo.checked;
gaEvent('ACARS', 'Flight Info');

// Display the info - show tab 0
if (isInfo && (this.tabs)) {
	updateTab(this, 0);
	map.infoWindow.marker = this;
	map.infoWindow.open(map, this);
} else if (isInfo) {
	map.infoWindow.setContent(this.infoLabel);
	map.infoWindow.open(map, this); 
}

// Display flight progress / route
if (isProgress || isRoute) {
	removeMarkers('routeData');
	removeMarkers('routeWaypoints');
	showFlightProgress(this, isProgress, isRoute);
}

document.pauseRefresh = true;
return true;
}

function clickDispatch()
{
// Check what info we display
var f = document.forms[0];
var isInfo = f.showInfo.checked;
gaEvent('ACARS', 'Dispatch Info');

// Display the info
if (isInfo && (this.tabs)) {
	updateTab(this, 0);
	map.infoWindow.marker = this;
	map.infoWindow.open(map, this);
} else if (isInfo) {
	map.infoWindow.setContent(this.infoLabel);
	map.infoWindow.open(map, this);
}

// Display flight progress / route
if (!this.rangeCircle) {
	this.rangeCircle = getServiceRange(this, this.range);
	if (this.rangeCircle) {
		gaEvent('ACARS', 'Dispatch Service Range');
		this.rangeCircle.setMap(map);
	}
} else
	this.rangeCircle.setMap(map);

document.pauseRefresh = true;
return true;
}

function infoClose()
{
document.pauseRefresh = false;
if ((map.infoWindow.marker) && (map.infoWindow.marker.rangeCircle))
	map.infoWindow.marker.rangeCircle.setMap(null);

removeMarkers('routeData'); 
removeMarkers('routeWaypoints');
map.infoWindow.close();
return true;	
}

function showFlightProgress(marker, doProgress, doRoute)
{
// Build the XML Requester
var d = new Date();
var xreq = getXMLHttpRequest();
xreq.open('get', 'acars_progress.ws?id=' + marker.flight_id + '&time=' + d.getTime() + '&route=' + doRoute, true);
xreq.onreadystatechange = function() {
	if ((xreq.readyState != 4) || (xreq.status != 200)) return false;

	// Load the XML
	var xdoc = xreq.responseXML;
	var wsdata = xdoc.documentElement;

	// Draw the flight route
	if (doRoute) {
		var wps = wsdata.getElementsByTagName('route');
		var waypoints = [];
		for (var i = 0; i < wps.length; i++) {
			var wp = wps[i];
			var p = new google.maps.LatLng(parseFloat(wp.getAttribute('lat')), parseFloat(wp.getAttribute('lng')));
			waypoints.push(p);
		} // for

		gaEvent('ACARS', 'Flight Route Info');
		routeWaypoints = new google.maps.Polyline({path:waypoints, strokeColor:'#af8040', strokeWeight:2, strokeOpacity:0.7, geodesic:true});
		routeWaypoints.setMap(map);
	}

	// Draw the flight progress
	if (doProgress) {
		var pos = wsdata.getElementsByTagName('pos');
		var positions = [];
		for (var i = 0; i < pos.length; i++) {
			var pe = pos[i];
			var p = new google.maps.LatLng(parseFloat(pe.getAttribute('lat')), parseFloat(pe.getAttribute('lng')));
			positions.push(p);
		} // for

		// Draw the line
		gaEvent('ACARS', 'Flight Progress Info');
		routeData = new google.maps.Polyline({path:positions, strokeColor:'#4080af', strokeWeight:2, strokeOpacity:0.8});
		routeData.setMap(map);
	}

	return true;
} // function

xreq.send(null);
return true;
}

function getServiceRange(marker, range)
{
var bColor = marker.isBusy ? '#c02020' : '#20c060';
var fColor = marker.isBusy ? '#802020' : '#208040';
var fOpacity = marker.isBusy ? 0.1 : 0.2;
return new google.maps.Circle({center:marker.getPosition(), radius:(range*1609.344), strokeColor:bColor, strokeWeight:1, strokeOpacity:0.65, fillColor:fColor, fillOpacity:fOpacity});
}

function zoomTo(combo)
{
var opt = combo.options[combo.selectedIndex];
if ((!opt) || (opt.mrk == null)) return false;

// Check if we zoom or just pan
var f = document.forms[0];
if (f.zoomToPilot.checked)
	map.setZoom(9);

// Pan to the marker
map.panTo(opt.mrk.getPosition());
google.maps.event.trigger(opt.mrk, 'click');
return true;
}
