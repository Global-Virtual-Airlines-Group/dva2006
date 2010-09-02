// Mark each pilot/controller's position
var atc = [];
var pilots = [];
var selectedRoute;
var selectedTrack;

function updateMap(isAuto)
{
var d = new Date();
var dtime = d.getTime() - (d.getTime() % 5000);
var xmlreq = GXmlHttp.create();
xmlreq.open('get', 'si_data.ws?network=' + document.network + '&time=' + dtime + '&atc=true', true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	var isLoading = getElement('isLoading');
	if (xmlreq.status != 200) {
		isLoading.innerHTML = ' - ERROR ' + xmlreq.status;
		return false;
	}
	
	var xdoc = xmlreq.responseXML;
	var re = xdoc.documentElement;
	map.clearOverlays();
	displayObject(getElement('userSelect'), false);
	var cbo = getElement('usrID');
	var selectedATC = cbo.options[cbo.selectedIndex].value;
	cbo.options.length = 1;

	// Display effective date
	var dt = re.getAttribute('date');
	isLoading.innerHTML = ' - VALID AS OF ' + new Date(dt);

	// Display pilots
	var wps = re.getElementsByTagName('pilot'); pilots.length = 0;
	for (var i = 0; i < wps.length; i++) {
		var wp = wps[i];
		var ll = new GLatLng(parseFloat(wp.getAttribute('lat')), parseFloat(wp.getAttribute('lng')));
		var mrk = googleMarker(document.imgPath, wp.getAttribute('color'), ll, wp.firstChild.data);
		mrk.networkID = wp.getAttribute('id');
		mrk.callsign = wp.getAttribute('callsign');
		GEvent.addListener(mrk, 'click', function() { showRoute(this.networkID); });
		map.addOverlay(mrk);
		pilots[mrk.callsign] = mrk;		
	}

	// Display controllers
	var cps = re.getElementsByTagName('atc'); atc.length = 0;
	for (var i = 0; i < cps.length; i++) {
		var cp = cps[i];
		var ll = new GLatLng(parseFloat(cp.getAttribute('lat')), parseFloat(cp.getAttribute('lng')));
		var mrk = googleMarker(document.imgPath, cp.getAttribute('color'), ll, cp.firstChild.data);
		mrk.networkID = cp.getAttribute('id');
		mrk.callsign = cp.getAttribute('callsign');
		var type = cp.getAttribute('type');
		if ((type == 'CTR') || (type == 'FSS'))
			GEvent.addListener(mrk, 'click', function() { showFIR(this.callsign); });
		else if (type == 'APP')
			GEvent.addListener(mrk, 'click', function() { showAPP(this); });
		
		map.addOverlay(mrk);
		atc[mrk.callsign] = mrk;
		
		// Add to ATC list
		var o = new Overlay(mrk.callsign, mrk.callsign);
		o.mrk = mrk;
		try {
			cbo.add(o, null);
		} catch (err) {
			cbo.add(o); // IE hack
		}
		if (selectedATC == id)
			cbo.selectedIndex = (cbo.options.length - 1);
	}

	displayObject(getElement('userSelect'), (cbo.options.length > 1));
	if (isAuto)
		window.setTimeout('void updateMap()', 90000);

	return true;
} // function

xmlreq.send(null);
return true;
}

function infoClose()
{
if (selectedRoute != null) {
	removeMarkers(map, 'selectedRoute');
	delete selectedRoute;
}

if (selectedTrack != null) {
	removeMarkers(map, 'selectedTrack');
	delete selectedTrack;
}

return true;
}

function zoomTo(combo)
{
var opt = combo.options[combo.selectedIndex];
if ((!opt) || (opt.mrk == null)) return false;	

//Pan to the marker
map.panTo(opt.mrk.getLatLng());
GEvent.trigger(opt.mrk, 'click');
return true;
}

function showAPP(mrk)
{
var pts = circle(mrk, 60);
if (pts == null) return false;
selectedRoute = new GPolygon(pts, '#20c060', 1, 0.65, '#208040', 0.2); 
map.addOverlay(selectedRoute);
return true;  
}

function showFIR(code)
{
var xmlreq = GXmlHttp.create();
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
			bPts.push(new GLatLng(parseFloat(pt.getAttribute('lat')), parseFloat(pt.getAttribute('lng'))));
		}

		if (bPts.length > 0) {
			bPts.push(bPts[0]);
			var rt = new GPolygon(bPts, '#efefff', 1, 0.85, '#7f7f80', 0.25);
			selectedRoute.push(rt);
			map.addOverlay(rt);
		}
	}

	return true;
} // function

xmlreq.send(null);
return true;
}

function showRoute(pilotID)
{
var d = new Date();
var dtime = d.getTime() - (d.getTime() % 3000);
var xmlreq = GXmlHttp.create();
xmlreq.open('get', 'si_route.ws?network=' + document.network + '&id=' + pilotID + '&time=' + dtime, true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var xdoc = xmlreq.responseXML;
	var re = xdoc.documentElement;
	infoClose();

	// Display route
	var wpoints = [];
	var wps = re.getElementsByTagName("waypoint");
	for (var i = 0; i < wps.length; i++) {
		var wp = wps[i];
		wpoints.push(new GLatLng(parseFloat(wp.getAttribute("lat")), parseFloat(wp.getAttribute("lng"))));
	}

	// Display track
	var tpoints = [];
	var tps = re.getElementsByTagName("track");
	for (var i = 0; i < tps.length; i++) {
		var wp = tps[i];
		tpoints.push(new GLatLng(parseFloat(wp.getAttribute("lat")), parseFloat(wp.getAttribute("lng"))));
	}

	if (wpoints.length > 0) {
		selectedRoute = new GPolyline(wpoints, '#af8040', 2, 0.85, { geodesic:true });
		map.addOverlay(selectedRoute);
	}
	if (tpoints.length > 0) {
		selectedTrack = new GPolyline(tpoints, '#4080af', 2, 0.75, { geodesic:true });
		map.addOverlay(selectedTrack);
	}
	
	return true;
} // function

xmlreq.send(null);
return true;
}
