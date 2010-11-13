// This stores the raw KML in case the plugin hasn't loaded yet
var acarsDataQueue;

// ATC positions
var selectedFIRs = [];

function getACARSData(pirepID)
{
// Disable checkboxes
var f = document.forms[0];
f.showFDR.disabled = true;
f.showRoute.disabled = true;

// Build the XML Requester
var xmlreq = getXMLHttpRequest();
xmlreq.open('get', 'acars_pirep.ws?id=' + pirepID, true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var xmlDoc = xmlreq.responseXML;
	var ac = xmlDoc.documentElement.getElementsByTagName('pos');
	for (var x = 0; x < ac.length; x++) {
		var a = ac[x]; var mrk;
		var label = a.getCDATA ? a.getCDATA() : getCDATA(a);
		var p = new google.maps.LatLng(parseFloat(a.getAttribute('lat')), parseFloat(a.getAttribute('lng')));
		routePoints.push(p);
		if (a.getAttribute('icon')) {
			mrk = googleIconMarker(a.getAttribute('pal'), a.getAttribute('icon'), p, label.data);
			routeMarkers.push(mrk);
		} else if (a.getAttribute('color')) {
			mrk = googleMarker(a.getAttribute('color'), p, label.data);
			routeMarkers.push(mrk);
		}	

		// Add ATC data
		var ace = a.getChild ? a.getChild('atc') : getChild(a, 'atc');
		if ((ace != null) && (mrk != null)) {
			var type = ace.getAttribute('type');
			if ((type != 'CTR') && (type != 'FSS')) {
				var acp = new google.maps.LatLng(parseFloat(ace.getAttribute('lat')), parseFloat(ace.getAttribute('lng')));
				mrk.range = parseInt(ace.getAttribute('range'));
				google.maps.event.addListener(mrk, 'click', function() { showAPP(this.getPosition(), this.range); });
			} else {
				mrk.atcID = ace.getAttribute('id');
				google.maps.event.addListener(mrk, 'click', function() { showFIR(this.atcID); });
			}
		}
	}

	gRoute = new google.maps.Polyline({path:routePoints, strokeColor:'#4080af', strokeWeight:3, strokeOpacity:0.85, geodesic:true});
	gaEvent('ACARS', 'Flight Data');
	
	// Enable checkboxes
	f.showFDR.disabled = false;
	f.showRoute.disabled = false;
	return true;
} // function

xmlreq.send(null);
return true;
}

function showAPP(ctr, range)
{
selectedFIRs.length = 0;
var c = new google.maps.Circle({center:ctr, range:range, strokeColor:'#efefff', strokeWeight:1, strokeOpacity:0.85, fillColor:'#7f7f80', fillOpacity:0.25});
selectedFIRs.push(c);
c.setMap(map);
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
	
	// Loop through the FIRs
	selectedFIRs.length = 0;
	var fs = re.getElementsByTagName('fir');
	if (fs.length == 0) return true;
	for (var x = 0; x < fs.length; x++) {
		var fe = fs[x];
		var bPts = [];	

		// Display border
		var pts = fe.getElementsByTagName('pt');
		for (var i = 0; i < pts.length; i++) {
			var pt = pts[i];
			bPts.push(new google.maps.LatLng(parseFloat(pt.getAttribute('lat')), parseFloat(pt.getAttribute('lng'))));
		}

		if (bPts.length > 0) {
			bPts.push(bPts[0]);
			var rt = new google.maps.Polygon({path:[bPts], strokeColor:'#efefff', strokeWeight:1, strokeOpacity:0.85, fillColor:'#7f7f80', fillOpacity:0.25});
			selectedFIRs.push(rt);
			rt.setMap(map);
		}
	}

	gaEvent('ACARS', 'Show FIR', code);
	return true;
} // function

xmlreq.send(null);
return true;
}
