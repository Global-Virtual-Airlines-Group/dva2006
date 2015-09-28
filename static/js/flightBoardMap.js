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
	
	var xdoc = xmlreq.responseXML;
	var re = xdoc.documentElement;
	map.clearOverlays();
	golgotha.util.display('userSelect', false);
	var cbo = document.getElementById('usrID');
	var selectedATC = cbo.options[cbo.selectedIndex].value;
	cbo.options.length = 1;

	// Display effective date
	var dt = new Date(parseInt(re.getAttribute('date')));
	isLoading.innerHTML = ' - VALID AS OF ' + golgotha.flightBoard.months[dt.getMonth()] + ' ' + dt.getDate() + ' ' + dt.getFullYear() + ' ' + dt.getHours() + ':' + dt.getMinutes();

	// Display pilots
	var wps = re.getElementsByTagName('pilot'); golgotha.flightBoard.pilots.length = 0;
	for (var i = 0; i < wps.length; i++) {
		var wp = wps[i];
		var mrk = new golgotha.maps.Marker({color:wp.getAttribute('color'), info:wp.firstChild.data, map:map}, {lat:parseFloat(wp.getAttribute('lat')), lng:parseFloat(wp.getAttribute('lng'))});
		mrk.networkID = wp.getAttribute('id');
		mrk.callsign = wp.getAttribute('callsign');
		google.maps.event.addListener(mrk, 'click', function() { golgotha.flightBoard.infoClose(); golgotha.flightBoard.showRoute(this.networkID); });
		golgotha.flightBoard.pilots[mrk.callsign] = mrk;		
	}

	// Display controllers
	var cps = re.getElementsByTagName('atc'); golgotha.flightBoard.atc.length = 0;
	for (var i = 0; i < cps.length; i++) {
		var cp = cps[i];
		var mrk = new golgotha.maps.Marker({color:cp.getAttribute('color'), info:cp.firstChild.data, map:map}, {lat:parseFloat(cp.getAttribute('lat')), lng:parseFloat(cp.getAttribute('lng'))});
		mrk.networkID = cp.getAttribute('id');
		mrk.callsign = cp.getAttribute('callsign');
		var type = cp.getAttribute('type');
		if ((type == 'CTR') || (type == 'FSS'))
			google.maps.event.addListener(mrk, 'click', function() { golgotha.flightBoard.infoClose(); golgotha.flightBoard.showFIR(this.callsign); });
		else if (type == 'APP') {
			mrk.range = parseInt(cp.getAttribute('range'));
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
	var xdoc = xmlreq.responseXML;
	var re = xdoc.documentElement;
	golgotha.flightBoard.infoClose();
	
	// Loop through the FIRs
	golgotha.flightBoard.selectedRoute = [];
	var fs = re.getElementsByTagName('fir');
	if (fs.length == 0) return true;
	for (var x = 0; x < fs.length; x++) {
		var fe = fs[x];
		var bPts = [];	

		// Display border
		var pts = fe.getElementsByTagName('pt');
		for (var i = 0; i < pts.length; i++) {
			var pt = pts[i];
			bPts.push({lat:parseFloat(pt.getAttribute('lat')), lng:parseFloat(pt.getAttribute('lng'))});
		}

		if (bPts.length > 0) {
			bPts.push(bPts[0]);
			golgotha.flightBoard.selectedRoute = new google.maps.Polygon({map:map, paths:[bPts], strokeColor:'#efefff', strokeWeight:1, stokeOpacity:0.85, fillColor:'#7f7f80', fillOpacity:0.25, zIndex:golgotha.maps.z.POLYGON});
		}
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
	var xdoc = xmlreq.responseXML;
	var re = xdoc.documentElement;
	golgotha.flightBoard.infoClose();

	// Display route
	var wpoints = [];
	var wps = re.getElementsByTagName('waypoint');
	for (var i = 0; i < wps.length; i++) {
		var wp = wps[i];
		wpoints.push({lat:parseFloat(wp.getAttribute('lat')), lng:parseFloat(wp.getAttribute('lng'))});
	}

	// Display track
	var tpoints = [];
	var tps = re.getElementsByTagName('track');
	for (var i = 0; i < tps.length; i++) {
		var wp = tps[i];
		tpoints.push({lat:parseFloat(wp.getAttribute('lat')), lng:parseFloat(wp.getAttribute('lng'))});
	}

	if (wpoints.length > 0)
		golgotha.flightBoard.selectedRoute = new google.maps.Polyline({map:map, path:wpoints, strokeColor:'#af8040', strokeWeight:2, strokeOpacity:0.85, geodesic:true, zIndex:golgotha.maps.z.POLYLINE});

	if (tpoints.length > 0)
		golgotha.flightBoard.selectedTrack = new google.maps.Polyline({map:map, path:tpoints, strokeColor:'#4080af', strokeWeight:2, strokeOpacity:0.75, geodesic:true, zIndex:(golgotha.maps.z.POLYLINE-1)});
	
	return true;
};

xmlreq.send(null);
return true;
};
