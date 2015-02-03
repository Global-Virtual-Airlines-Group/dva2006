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
	var xmlDoc = xmlreq.responseXML;
	var ac = xmlDoc.documentElement.getElementsByTagName('pos');
	for (var x = 0; x < ac.length; x++) {
		var a = ac[x]; var mrk;
		var label = golgotha.getCDATA(a);
		var p = {lat:parseFloat(a.getAttribute('lat')), lng:parseFloat(a.getAttribute('lng'))};
		golgotha.maps.acarsFlight.routePoints.push(p);
		if (a.getAttribute('icon')) {
			mrk = new golgotha.maps.IconMarker({pal:a.getAttribute('pal'), icon:a.getAttribute('icon'), info:label.data}, p);
			golgotha.maps.acarsFlight.routeMarkers.push(mrk);
		} else if (a.getAttribute('color')) {
			mrk = new golgotha.maps.Marker({color:a.getAttribute('color'), info:label.data}, p);
			golgotha.maps.acarsFlight.routeMarkers.push(mrk);
		}	

		// Add ATC data
		var ace = golgotha.getChild(a, 'atc');
		if ((ace != null) && (mrk != null)) {
			var type = ace.getAttribute('type');
			if ((type != 'CTR') && (type != 'FSS')) {
				var acp = {lat:parseFloat(ace.getAttribute('lat')), lng:parseFloat(ace.getAttribute('lng'))};
				mrk.range = parseInt(ace.getAttribute('range'));
				google.maps.event.addListener(mrk, 'click', function() { golgotha.maps.acarsFlight.showAPP(this.getPosition(), this.range); });
			} else {
				mrk.atcID = ace.getAttribute('id');
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
	var re = xmlreq.responseXML.documentElement;

	// Loop through the FIRs
	golgotha.maps.acarsFlight.hideATC();
	var fs = re.getElementsByTagName('fir');
	if (fs.length == 0) return true;
	for (var x = 0; x < fs.length; x++) {
		var fe = fs[x]; var bPts = [];	

		// Display border
		var pts = fe.getElementsByTagName('pt');
		for (var i = 0; i < pts.length; i++) {
			var pt = pts[i];
			bPts.push({lat:parseFloat(pt.getAttribute('lat')), lng:parseFloat(pt.getAttribute('lng'))});
		}

		if (bPts.length > 0) {
			bPts.push(bPts[0]);
			var rt = new google.maps.Polygon({path:[bPts], strokeColor:'#efefff', strokeWeight:1, strokeOpacity:0.85, fillColor:'#7f7f80', fillOpacity:0.25, zIndex:golgotha.maps.z.POLYGON});
			golgotha.maps.acarsFlight.selectedFIRs.push(rt);
			rt.setMap(map);
		}
	}

	golgotha.event.beacon('ACARS', 'Show FIR', code);
	return true;
};

xmlreq.send(null);
return true;
};
