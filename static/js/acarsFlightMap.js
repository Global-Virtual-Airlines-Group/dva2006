golgotha.maps.acarsFlight = golgotha.maps.acarsFlight || {selectedFIRs:[], routePoints:[], routeMarkers:[], airspace:[]}; 
golgotha.maps.acarsFlight.airspaceColors = {'P':{c:'#ee1010',tx:0.4,z:10}, 'R':{c:'#adad10',tx:0.2,z:5}, 'B':{c:'#10e0e0',tx:0.1,z:0}, 'C':{c:'#ffa018', tx:0.125,z:1}, 'D':{c:'#608040', tx:0.175,z:2}};
golgotha.maps.acarsFlight.getACARSData = function(pirepID, doToggle)
{
// Disable checkboxes
var f = document.forms[0];
f.showFDR.disabled = true; f.showRoute.disabled = true; f.showAirspace.disabled = true;

// Build the XML Requester
var xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'acars_pirep.ws?id=' + pirepID, true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var js = JSON.parse(xmlreq.responseText);
	js.positions.forEach(function(p) {
		var mrk;
		golgotha.maps.acarsFlight.routePoints.push(p.ll);
		if (p.icon) {
			mrk = new golgotha.maps.IconMarker({pal:p.pal, icon:p.icon, info:p.info, opacity:0.75}, p.ll);
			golgotha.maps.acarsFlight.routeMarkers.push(mrk);
		} else if (p.color) {
			mrk = new golgotha.maps.Marker({color:p.color, info:p.info, opacity:0.75}, p.ll);
			golgotha.maps.acarsFlight.routeMarkers.push(mrk);
		}	

		// Add ATC data
		if ((p.atc) && (mrk != null)) {
			if ((p.atc.type != 'CTR') && (p.atc.type != 'FSS')) {
				mrk.range = p.atc.range; mrk.atcPosition = p.atc.ll;
				google.maps.event.addListener(mrk, 'click', function() { golgotha.maps.acarsFlight.showAPP(this.atcPosition, this.range); });
			} else {
				mrk.atcID = p.atc.id;
				google.maps.event.addListener(mrk, 'click', function() { golgotha.maps.acarsFlight.showFIR(this.atcID); });
			}
		} else if (mrk != null)
			google.maps.event.addListener(mrk, 'click', golgotha.maps.acarsFlight.hideATC);
	});

	js.airspace.forEach(function(a) {
		if (a.exclude) return false;
		var ac = golgotha.maps.acarsFlight.airspaceColors[a.type];
		var brd = new google.maps.Polygon({paths:[a.border], strokeColor:ac.c, strokeWeight:0.75, strokeOpacity:ac.tx, fillColor:ac.c, fillOpacity:(ac.tx/3), zIndex:golgotha.maps.z.OVERLAY+ac.z});
		brd.info = a.info; brd.ll = a.ll;
		google.maps.event.addListener(brd, 'click', function() { map.infoWindow.setContent(this.info); map.infoWindow.open(map); map.infoWindow.setPosition(this.ll); });
		golgotha.maps.acarsFlight.airspace.push(brd);
	});

	golgotha.maps.acarsFlight.gRoute = new google.maps.Polyline({path:golgotha.maps.acarsFlight.routePoints, strokeColor:'#4080af', strokeWeight:3, strokeOpacity:0.85, geodesic:true, zIndex:golgotha.maps.z.POLYLINE});
	golgotha.event.beacon('ACARS', 'Flight Data');

	// Enable checkboxes
	golgotha.util.disable(f.showFDR, false);
	golgotha.util.disable(f.showRoute, false);
	golgotha.util.disable(f.showAirspace, false);
	if (doToggle) f.showRoute.click();
	return true;
};

xmlreq.send(null);
return true;
};

golgotha.maps.acarsFlight.hideATC = function() {
	while (golgotha.maps.acarsFlight.selectedFIRs.length > 0) {
		var mrk = golgotha.maps.acarsFlight.selectedFIRs.shift();
		mrk.setMap(null);
	}

	return true;
};

golgotha.maps.acarsFlight.showAPP = function(ctr, range) {
	golgotha.maps.acarsFlight.hideATC();
	var c = new google.maps.Circle({map:map, center:ctr, radius:golgotha.maps.miles2Meter(range), strokeColor:'#efefff', strokeWeight:1, strokeOpacity:0.85, fillColor:'#7f7f80', fillOpacity:0.25, zIndex:golgotha.maps.z.POLYGON});
	golgotha.maps.acarsFlight.selectedFIRs.push(c);
	return true;
};

golgotha.maps.acarsFlight.showAirspace = function(p) {
	
	
	
};

golgotha.maps.acarsFlight.showFIR = function(code)
{
var xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'fir.ws?id=' + code, true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var js = JSON.parse(xmlreq.responseText);
	golgotha.maps.acarsFlight.hideATC();
	js.firs.forEach(function(fe) {
		if (fe.border.length == 0) return false;
		fe.border.push(fe.border[0]);
		golgotha.maps.acarsFlight.selectedFIRs.push(new google.maps.Polygon({map:map, paths:[fe.border], strokeColor:'#efefff', strokeWeight:1, stokeOpacity:0.85, fillColor:'#7f7f80', fillOpacity:0.25, zIndex:golgotha.maps.z.POLYGON}));
	});

	golgotha.event.beacon('ACARS', 'Show FIR', code);
	return true;
};

xmlreq.send(null);
return true;
};
