// Mark each pilot/controller's position
var atc = [];
var pilots = [];
var selectedRoute;
var selectedTrack;
var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

function updateMap(isAuto)
{
var xmlreq = getXMLHttpRequest();
xmlreq.open('get', 'si_data.ws?network=' + document.network + '&time=' + golgotha.util.getTimestamp(5000) + '&atc=true', true);
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
	displayObject(document.getElementById('userSelect'), false);
	var cbo = document.getElementById('usrID');
	var selectedATC = cbo.options[cbo.selectedIndex].value;
	cbo.options.length = 1;

	// Display effective date
	var dt = new Date(parseInt(re.getAttribute('date')));
	isLoading.innerHTML = ' - VALID AS OF ' + months[dt.getMonth()] + ' ' + dt.getDate() + ' ' + dt.getFullYear() + ' ' + dt.getHours() + ':' + dt.getMinutes();

	// Display pilots
	var wps = re.getElementsByTagName('pilot'); pilots.length = 0;
	for (var i = 0; i < wps.length; i++) {
		var wp = wps[i];
		var ll = {lat:parseFloat(wp.getAttribute('lat')), lng:parseFloat(wp.getAttribute('lng'))};
		var mrk = new golgotha.maps.Marker({color:wp.getAttribute('color'), info:wp.firstChild.data, map:map}, ll);
		mrk.networkID = wp.getAttribute('id');
		mrk.callsign = wp.getAttribute('callsign');
		google.maps.event.addListener(mrk, 'click', function() { infoClose(); showRoute(this.networkID); });
		pilots[mrk.callsign] = mrk;		
	}

	// Display controllers
	var cps = re.getElementsByTagName('atc'); atc.length = 0;
	for (var i = 0; i < cps.length; i++) {
		var cp = cps[i];
		var ll = {lat:parseFloat(cp.getAttribute('lat')), lng:parseFloat(cp.getAttribute('lng'))};
		var mrk = new golgotha.maps.Marker({color:cp.getAttribute('color'), info:cp.firstChild.data, map:map}, ll);
		mrk.networkID = cp.getAttribute('id');
		mrk.callsign = cp.getAttribute('callsign');
		var type = cp.getAttribute('type');
		if ((type == 'CTR') || (type == 'FSS'))
			google.maps.event.addListener(mrk, 'click', function() { infoClose(); showFIR(this.callsign); });
		else if (type == 'APP') {
			mrk.range = parseInt(cp.getAttribute('range'));
			google.maps.event.addListener(mrk, 'click', function() { infoClose(); showAPP(this); });
		}
		
		atc[mrk.callsign] = mrk;
		
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

	displayObject(document.getElementById('userSelect'), (cbo.options.length > 1));
	if (isAuto)
		window.setTimeout('void updateMap()', 90000);

	return true;
}

xmlreq.send(null);
return true;
}

function infoClose()
{
if (selectedRoute != null) {
	removeMarkers('selectedRoute');
	delete selectedRoute;
}

if (selectedTrack != null) {
	removeMarkers('selectedTrack');
	delete selectedTrack;
}

return true;
}

function zoomTo(combo)
{
var opt = combo.options[combo.selectedIndex];
if ((!opt) || (opt.mrk == null)) return false;	

map.panTo(opt.mrk.getPosition());
google.maps.event.trigger(opt.mrk, 'click');
return true;
}

function showAPP(mrk)
{
selectedRoute = new google.maps.Circle({center: mrk.getPosition(), radius:mrk.range, strokeColor:'#20c060', strokeWeight:1, strokeOpacity:0.65, fillColor:'#208040', fillOpacity:0.2});
selectedRoute.setMap(map);
return true;  
}

function showFIR(code)
{
var xmlreq = getXMLHttpRequest();
xmlreq.open('get', 'fir.ws?id=' + code, true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var xdoc = xmlreq.responseXML;
	var re = xdoc.documentElement;
	infoClose();
	
	// Loop through the FIRs
	selectedRoute = [];
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
			selectedRoute = new google.maps.Polygon({map:map, paths:[bPts], strokeColor:'#efefff', strokeWeight:1, stokeOpacity:0.85, fillColor:'#7f7f80', fillOpacity:0.25, zIndex:golgotha.maps.z.POLYGON});
		}
	}

	return true;
} // function

xmlreq.send(null);
return true;
}

function showRoute(pilotID)
{
var xmlreq = getXMLHttpRequest();
xmlreq.open('get', 'si_route.ws?network=' + document.network + '&id=' + pilotID + '&time=' + golgotha.util.getTimestamp(5000), true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var xdoc = xmlreq.responseXML;
	var re = xdoc.documentElement;
	infoClose();

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
		selectedRoute = new google.maps.Polyline({map:map, path:wpoints, strokeColor:'#af8040', strokeWeight:2, strokeOpacity:0.85, geodesic:true, zIndex:golgotha.maps.z.POLYLINE});

	if (tpoints.length > 0)
		selectedTrack = new google.maps.Polyline({map:map, path:tpoints, strokeColor:'#4080af', strokeWeight:2, strokeOpacity:0.75, geodesic:true, zIndex:(golgotha.maps.z.POLYLINE-1)});
	
	return true;
}

xmlreq.send(null);
return true;
}
