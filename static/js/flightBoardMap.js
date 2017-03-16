golgotha.flightBoard = golgotha.flightBoard || {atc:[], pilots:[], months:['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'], network:'?'};

golgotha.flightBoard.updateMap = function(isAuto)
{
var xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'si_data.ws?network=' + golgotha.flightBoard.network + '&time=' + golgotha.util.getTimestamp(5000) + '&atc=true', true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	var isLoading = document.getElementById('isLoading');
	if (xmlreq.status != 200) {
		isLoading.innerHTML = ' - ERROR ' + xmlreq.status;
		return false;
	}

	map.clearOverlays();
	golgotha.util.display('userSelect', false);
	var cbo = document.getElementById('usrID');
	var selectedATC = cbo.options[cbo.selectedIndex].value;
	cbo.options.length = 1;
	var js = JSON.parse(xmlreq.responseText);

	// Display effective date
	var dt = new Date(js.date);
	isLoading.innerHTML = ' - VALID AS OF ' + golgotha.flightBoard.months[dt.getMonth()] + ' ' + dt.getDate() + ' ' + dt.getFullYear() + ' ' + dt.getHours() + ':' + dt.getMinutes();

	// Display pilots
	golgotha.flightBoard.pilots.length = 0;
	for (var wp = js.pilots.pop(); (wp != null); wp = js.pilots.pop()) {
		var mrk = new golgotha.maps.Marker({color:wp.color, info:wp.info, map:map}, wp.ll);
		mrk.networkID = wp.id; mrk.callsign = wp.callsign;
		google.maps.event.addListener(mrk, 'click', function() { golgotha.flightBoard.infoClose(); golgotha.flightBoard.showRoute(this.networkID); });
		golgotha.flightBoard.pilots[mrk.callsign] = mrk;		
	}

	// Display controllers
	for (var cp = js.atc.pop(); (cp != null); cp = js.atc.pop()) {
		var mrk = new golgotha.maps.Marker({color:cp.color, info:cp.info, map:map}, cp.ll);
		mrk.networkID = cp.id; mrk.callsign = cp.callsign;
		if ((cp.type == 'CTR') || (cp.type == 'FSS'))
			google.maps.event.addListener(mrk, 'click', function() { golgotha.flightBoard.infoClose(); golgotha.flightBoard.showFIR(this.callsign); });
		else if (cp.type == 'APP') {
			mrk.range = cp.range;
			google.maps.event.addListener(mrk, 'click', function() { golgotha.flightBoard.infoClose(); golgotha.flightBoard.showAPP(this); });
		}
		
		golgotha.flightBoard.atc[mrk.callsign] = mrk;
		
		// Add to ATC list
		var o = new Option(mrk.callsign, mrk.callsign);
		o.mrk = mrk;
		try {
			cbo.add(o, null);
		} catch (err) {
			cbo.add(o); // IE hack
		}
		if (selectedATC == mrk.callsign)
			cbo.selectedIndex = (cbo.options.length - 1);
	}

	golgotha.util.display('userSelect', (cbo.options.length > 1));
	if (isAuto)
		window.setTimeout(golgotha.flightBoard.updateMap, 90000);

	return true;
};

xmlreq.send(null);
return true;
};

golgotha.flightBoard.infoClose = function()
{
if (golgotha.flightBoard.selectedRoute != null) {
	map.removeMarkers(golgotha.flightBoard.selectedRoute);
	delete golgotha.flightBoard.selectedRoute;
}

if (golgotha.flightBoard.selectedTrack != null) {
	map.removeMarkers(golgotha.flightBoard.selectedTrack);
	delete golgotha.flightBoard.selectedTrack;
}

return true;
};

golgotha.flightBoard.zoomTo = function(combo)
{
var opt = combo.options[combo.selectedIndex];
if ((!opt) || (opt.mrk == null)) return false;	

map.panTo(opt.mrk.getPosition());
google.maps.event.trigger(opt.mrk, 'click');
return true;
};

golgotha.flightBoard.setNetwork = function(combo) {
	var net = combo.options[combo.selectedIndex].text;
	location.href = '/flightboardmap.do?id=' + net + '&op=map';
	return true;
};

golgotha.flightBoard.showAPP = function(mrk) {
	golgotha.flightBoard.selectedRoute = new google.maps.Circle({map:map, center: mrk.getPosition(), radius:mrk.range, strokeColor:'#20c060', strokeWeight:1, strokeOpacity:0.65, fillColor:'#208040', fillOpacity:0.2});
};

golgotha.flightBoard.showFIR = function(code)
{
var xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'fir.ws?id=' + code, true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	golgotha.flightBoard.infoClose();
	var js = JSON.parse(xmlreq.responseText);
	
	// Loop through the FIRs
	golgotha.flightBoard.selectedRoute = [];
	for (var fe = js.firs.pop(); (fe != null); fe = js.firs.pop()) {
		if (fe.border.length == 0) continue;
		fe.border.push(fe.border[0]);
		golgotha.flightBoard.selectedRoute = new google.maps.Polygon({map:map, paths:[fe.border], strokeColor:'#efefff', strokeWeight:1, stokeOpacity:0.85, fillColor:'#7f7f80', fillOpacity:0.25, zIndex:golgotha.maps.z.POLYGON});
	}

	return true;
};

xmlreq.send(null);
return true;
};

golgotha.flightBoard.showRoute = function(pilotID)
{
var xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'si_route.ws?network=' + golgotha.flightBoard.network + '&id=' + pilotID + '&time=' + golgotha.util.getTimestamp(5000), true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	golgotha.flightBoard.infoClose();
	var js = JSON.parse(xmlreq.responseText);
	if ((js.waypoints) && (js.waypoints.length > 0))
		golgotha.flightBoard.selectedRoute = new google.maps.Polyline({map:map, path:js.waypoints, strokeColor:'#af8040', strokeWeight:2, strokeOpacity:0.85, geodesic:true, zIndex:golgotha.maps.z.POLYLINE});
	if ((js.track) && (js.track.length > 0))
		golgotha.flightBoard.selectedTrack = new google.maps.Polyline({map:map, path:js.track, strokeColor:'#4080af', strokeWeight:2, strokeOpacity:0.75, geodesic:true, zIndex:(golgotha.maps.z.POLYLINE-1)});
	
	return true;
};

xmlreq.send(null);
return true;
};
