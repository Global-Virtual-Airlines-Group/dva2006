golgotha.maps.acarsFlight = golgotha.maps.acarsFlight || {selectedFIRs:[], routePoints:[], routeMarkers:[]}; 

golgotha.maps.acarsFlight.getACARSData = function(pirepID, doToggle)
{
// Disable checkboxes
var f = document.forms[0];
f.showFDR.disabled = true;
f.showRoute.disabled = true;

// Build the XML Requester
var xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'acars_pirep.ws?id=' + pirepID, true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var js = JSON.parse(xmlreq.responseText);
	for (var p = js.positions.pop(); (p != null); p = js.positions.pop()) {
		var mrk;
		golgotha.maps.acarsFlight.routePoints.push(p.ll);
		if (p.icon) {
			mrk = new golgotha.maps.IconMarker({pal:p.pal, icon:p.icon, info:p.info}, p.ll);
			golgotha.maps.acarsFlight.routeMarkers.push(mrk);
		} else if (p.color) {
			mrk = new golgotha.maps.Marker({color:p.color, info:p.info}, p.ll);
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
	}

	golgotha.maps.acarsFlight.gRoute = new google.maps.Polyline({path:golgotha.maps.acarsFlight.routePoints, strokeColor:'#4080af', strokeWeight:3, strokeOpacity:0.85, geodesic:true, zIndex:golgotha.maps.z.POLYLINE});
	golgotha.event.beacon('ACARS', 'Flight Data');

	// Enable checkboxes
	golgotha.util.disable(f.showFDR, false);
	golgotha.util.disable(f.showRoute, false);
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
