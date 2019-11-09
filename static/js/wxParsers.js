// Front options
golgotha.maps.fronts = golgotha.maps.fronts || {};
golgotha.maps.fronts.opts = {
		"WARM":{strokeOpacity:1, strokeColor:"#ff0000", icons:[{icon:{path:"M 0,-2 A 2,2 0 0 1 0,2 z",fillColor:"red",fillOpacity:1,strokeWeight:1,scale:3},offset:"12px",repeat:"24px"}], zIndex:3, geodesic:true},
		"COLD":{strokeOpacity:1, strokeColor:"#0000ff", icons:[{icon:{path:"M 0,-2 0,2 -2,0 z",fillColor:"blue",fillOpacity:1,strokeWeight:1,scale:3},offset:"12px",repeat:"24px"}], zIndex:3, geodesic:true},
		"OCFNT":{strokeOpacity:1, strokeColor:"#912CEE", icons:[{icon:{path:"M 0,-2 0,2 -2,0 z",fillColor:"#912CEE",fillOpacity:1,strokeWeight:1,scale:3},offset:"12px",repeat:"36px"},{icon:{path:"M 0,-2 A 2,2 0 0 1 0,2 z",fillColor:"#912CEE",fillOpacity:1,strokeWeight:1, scale:3},offset:"24px", repeat:"36px"}], zIndex:3, geodesic:true},
		"STNRY":{strokeOpacity:0, icons:[{icon:{path:"M 0,-4 0,4",strokeWeight:3,strokeOpacity:1,strokeColor:"blue"},offset:"0px",repeat:"48px"},{icon:{path:"M 0,-4 0,4",strokeWeight:3,strokeOpacity:1,strokeColor:"red"},offset:"24px",repeat:"48px"},{icon:{path:"M 0,-2 0,2 -2,0 z",fillColor:"blue",fillOpacity:1,scale:3},offset:"0",repeat:"48px"},{icon:{path:"M 0,-2 A 2,2 50 0 1 0,2",fillColor:"red",fillOpacity:1,scale:3},offset:"24px",repeat:"48px"}], zIndex:3, geodesic:true},
		"TROF":{strokeOpacity:0, icons:[{icon:{path:"M 0,-1 0,1",strokeOpacity:1,strokeWeight:2,scale:5,strokeColor:"#999999"},offset:"0",repeat:"20px"}],zIndex:3, geodesic:true}
};

// Front parser 
golgotha.maps.fronts.FrontParser = function(seriesData)
{
const data = seriesData.FRONTS;
const results = new golgotha.maps.LoaderResults();
results.fn.getCopyright = function() { var d = this.get('timestamp'); return (d == null) ? '' : 'Weather Data &copy; ' + d.getFullYear() + ' Weather Underground.'; };
const msz = new google.maps.Size(30, 30); const mpt = new google.maps.Point(15, 15);
	
// Parse fronts
const fd = data.FRONTS;
for (var f = fd.pop(); (f != null); f = fd.pop()) {
	let l = new google.maps.Polyline(golgotha.maps.fronts.opts[f.type]);
	let pts = [];
	f.points.forEach(function(pt) { pts.push({lat:pt.lat, lng:pt.lon});	});
	if (f.type == 'WARM') pts.reverse();
	l.setPath(pts);
	results.mrks.push(l);
}
	
// Parse Highs
const hd = data.HIGHS;
const hm = new google.maps.MarkerImage('/img/wx/H.png', msz, null, mpt);
for (var h = hd.pop(); (h != null); h = hd.pop()) {
	let mrk = new google.maps.Marker({icon:hm, position:{lat:h.lat,lng:h.lon}, title:'High Pressure (' + h.pressuremb + 'mb)'});
	if (h.pressuremb < 1100) results.mrks.push(mrk);
}
	
// Parse Lows
const ld = data.LOWS;
const lm = new google.maps.MarkerImage('/img/wx/L.png', msz, null, mpt);
for (var l = ld.pop(); (l != null); l = ld.pop()) {
	let mrk = new google.maps.Marker({icon:lm, position:{lat:l.lat,lng:l.lon}, title:'Low Pressure (' + l.pressuremb + 'mb)'});
	if (l.pressuremb < 1100) results.mrks.push(mrk);
}
	
results.success = true;
return results;
};

// Lightning parser
golgotha.maps.LightningParser = function(data)
{
const results = new golgotha.maps.LoaderResults();
if (data.status != 200) { console.log(data.body); return results; }
if (!(data.body.LGData instanceof Array)) { results.success = true; return results; }
const tt = new golgotha.maps.util.Timer(true);
const sz = new google.maps.Size(16, 16);
for (var lr = data.body.LGData.pop(); (lr != null); lr = data.body.LGData.pop()) {
	const age = Math.max(0, (results.opts.timestamp.getTime() - Date.parse(lr.LGtm)) / 1000);
	const lbl = lr.LGamp + ' - ' + age + ' seconds';
	const iconURL = 'http://' + self.location.host + '/img/wx/lightning-' + ((lr.LGamp < 0) ? 'minus' : 'plus') + '.png';
	results.mrks.push(new google.maps.Marker({ icon:{size:sz,url:iconURL}, position:{lat:lr.LGlat,lng:lr.LGlong}, opacity:0.85, title:lbl})); 
}

console.log('Loaded ' + results.mrks.length + ' lightning markers in ' + tt.stop() + 'ms');
results.success = true;
return results;
};

// FIR data parser
golgotha.maps.FIRParser = function(data)
{
const results = new golgotha.maps.LoaderResults();
const tt = new golgotha.maps.util.Timer(true);
for (var f = data.pop(); (f != null); f = data.pop()) {
	let z = golgotha.maps.z.POLYGON;
	const sc = (f.oceanic) ? '#60a0e0' : '#a0c060';
	const fc = (f.oceanic) ? '#208090' : '#908020';
	if (f.oceanic || f.aux) z--;
	const opts = {strokeOpacity:0.65, strokeWeight:1.5, strokeColor:sc, fillColor:fc, fillOpacity:0.15, geodesic:false, zIndex:z, paths:f.border};
	const l = new google.maps.Polygon(opts);
	l.name = f.name;
	google.maps.event.addListener(l, 'click', function(e) {
		const dv = document.createElement('div'); dv.setAttribute('id', 'firInfo');
		dv.setAttribute('class', 'mapInfoBox navdata');
		dv.innerText = this.name;
		map.infoWindow.setContent(dv); 
		map.infoWindow.setPosition(e.latLng); 
		map.infoWindow.open(map);
	});
	results.mrks.push(l);
}

console.log('Loaded ' + results.mrks.length + ' FIR polygons in ' + tt.stop() + 'ms');
results.success = true;
return results;
};