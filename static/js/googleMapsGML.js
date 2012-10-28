golgotha.maps.gml = {};
golgotha.maps.gml.colors = ['blue','green','white','yellow','grey','purple','orange'];

// Wrapper to treat the Point layers like overlay layers
golgotha.maps.GMLPointLayer = function(name, timestamp)
{
	this.options = {};
	this.elements = [];
	this.name = name;
	if (!timestamp) timestamp = (new Date()).getTime();
	this.set('date', timestamp);
	this.set('timestamp', new Date(timestamp));
}

golgotha.maps.GMLPointLayer.prototype.getName = function() { return this.name; }
golgotha.maps.GMLPointLayer.prototype.get = function(k) { return this.options[k]; }
golgotha.maps.GMLPointLayer.prototype.set = function(k, v) { this.options[k] = v; }
golgotha.maps.GMLPointLayer.prototype.addEntry = function(e) { this.elements.push(e); return true; }
golgotha.maps.GMLPointLayer.prototype.getMap = function() { return this.map; }
golgotha.maps.GMLPointLayer.prototype.setMap = function(m) {
	if ((this.map != null) && (m != null)) {
		if (m == this.map) return false;
		setMap(null);
	}
	
	if (m != null) {
		golgotha.maps.ovLayers.push(this);
		this.map = m;
		for (var x = 0; x < this.elements.length; x++)
			this.elements[x].setMap(m);
	} else if (this.map != null) {
		m = this.map;
		this.map = null;
		golgotha.maps.ovLayers.remove(this);
		for (var x = 0; x < this.elements.length; x++)
			this.elements[x].setMap(null);
	}
	
	return true;
}

golgotha.maps.GMLPointLayer.prototype.getCopyright = function() {
	var d = this.get('timestamp');
	return 'Weather Data &copy; ' + d.getFullYear() + ' weather.com';
}

golgotha.maps.GMLPointLayer.prototype.getTextDate = function() {
	var d = this.get('timestamp');
	var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
	return d.getDate() + '-' + months[d.getMonth()] + '-' + d.getFullYear() + '  ' 
		+ d.getHours() + ':' + ((d.getMinutes() < 10) ? '0' + d.getMinutes() : d.getMinutes());	
}

golgotha.maps.GMLPointLayer.prototype.display = function(isVisible) {
	for (var x = 0; x < this.elements.length; x++)
		this.elements[x].setVisible(isVisible);

	return true;
}

golgotha.maps.GMLPointLayer.prototype.hideAll = function() {
	for (var x = 0; x < this.elements.length; x++)
		this.elements[x].setVisible(false);

	return true;
}

// AJAX GML loader
golgotha.maps.GMLLoader = function() {
	this.layers = [];
}

golgotha.maps.GMLLoader.prototype.clear = function() { this.layers = {}; }
golgotha.maps.GMLLoader.prototype.getLayer = function(name) { return this.layers[name]; }
golgotha.maps.GMLLoader.prototype.load = function(url, cb)
{
var xmlreq = new XMLHttpRequest();
xmlreq.parent = this;
xmlreq.open('get', url, false);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	var l = cb(xmlreq.responseXML.documentElement);
	for (var x = 0; x < l.length; x++) {
		var ll = l[x];
		this.parent.layers[ll.getName()] = ll;
	}

	return true;
}
	
xmlreq.send(null);
return true;
}

golgotha.maps.GMLLoader.prototype.getLayerNames = function() {
	var results = [];	
	for (var lID in this.layers) {
		if (this.layers.hasOwnProperty(lID)) results.push(lID);
	}

	return results;
}

golgotha.maps.gml.TropicalParser = function(e)
{
var posList = e.getElementsByTagName('STORM_POSITION');
var storms = []; var cnt = 0;
var ct = function(n, name) { var ch = n.getElementsByTagName(name); return (ch.length == 0) ? '' : ch[0].firstChild.nodeValue; }
var mColors = {"Tropical Storm":'orange','Tropical Depression':'green',"Hurricane":'red',"Typhoon":'red'};

for (var i = 0; i < posList.length; i++) {
	var pos = posList[i];
	var stormID = ct(pos, 'STORM_ID');
	var s = storms[stormID];
	if (!s) {
		s = {id:stormID, pts:[], mrks:[]}
		storms[stormID] = s;
		s.color = golgotha.maps.gml.colors[cnt];
		cnt++;
	}
	
	// Get the date
	var rawDate = ct(pos,'VALID_TIME');
	var dt = Date.parse(rawDate.substring(0, rawDate.lastIndexOf(' ')));
	
	// Get the point
	var loc = pos.getElementsByTagNameNS('http://www.opengis.net/gml','coord')[0];
	var x = loc.childNodes[0];
	var y = loc.childNodes[1];
	var pt = new google.maps.LatLng(parseFloat(y.firstChild.nodeValue), parseFloat(x.firstChild.nodeValue));
	pt.dt = dt;
	s.pts.push(pt);
	
	// Create the marker text
	var sType = ct(pos,'STORM_TYPE');
	var txt = '<div class="small"><span class="bld">';
	txt += (sType + ' ' + ct(pos,'STORM_NAME'));
	txt += '</span><br>';
	txt += rawDate;
	txt += '<br><br>Winds: ';
	txt += ct(pos,'MAX_WINDS');
	txt += 'mph gusting to ';
	txt += ct(pos,'GUSTS');
	txt += 'mph<br>Direction: ';
	txt += ct(pos,'DIRECTION');
	txt += ' at ';
	txt += ct(pos,'SPEED');
	txt += 'mph</div>';
	
	// Add the marker
	if (!mColors[sType]) console.log('Unknown storm type ' + sType);
	var mrk = googleMarker(mColors[sType], pt, txt);
	mrk.dt = dt;
	s.mrks.push(mrk);
}

// Convert the storms
var layers = [];
var all = new golgotha.maps.GMLPointLayer('Tropical');
for (var sID in storms) {
	if (!storms.hasOwnProperty(sID)) continue;
	var s = storms[sID];
	var gml = new golgotha.maps.GMLPointLayer('Tropical-' + sID);

	// Sort the markers and the points
	var dtSort = function(a,b) { return a.dt-b.dt; }
	s.pts.sort(dtSort);
	s.mrks.sort(dtSort);

	var opts = {strokeColor:s.color,strokeWeight:2.5,strokeOpacity:0.75,geodesic:true,path:s.pts};
	var l = new google.maps.Polyline(opts);
	gml.addEntry(l);
	all.addEntry(l);
	for (var x = 0; x < s.mrks.length; x++) {
		gml.addEntry(s.mrks[x]);
		all.addEntry(s.mrks[x]);
	}

	layers.push(gml);
}
	
layers.push(all);
return layers;	
}

golgotha.maps.gml.TropicalModelParser = function(e)
{
var storms = []; var names = [];
var posList = e.getElementsByTagName('WxFeature');
var ct = function(n, name) { var ch = n.getElementsByTagName(name); return (ch.length == 0) ? '' : ch[0].firstChild.nodeValue; }

for (var x = 0; x < posList.length; x++) {
	var pos = posList[x]; 	
	var stormID = ct(pos, 'storm_numb'); var stormName = ct(pos,'storm_name');
	var tracks = storms[stormID];
	if (!tracks) {
		tracks = [];
		storms[stormID] = tracks;
		names[stormID] = stormName;
	}

	// Get the points
	var pts = [];
	var loc = pos.getElementsByTagNameNS('http://www.opengis.net/gml','coordinates')[0];
	var lls = loc.childNodes[0].nodeValue.split(' ');
	for (var ci = 0; ci < lls.length; ci++) {
		var ll = lls[ci].split(',');
		var pt = new google.maps.LatLng(parseFloat(ll[1]), parseFloat(ll[0]));
		pts.push(pt);
	}

	var lColor = golgotha.maps.gml.colors[tracks.length];
	var opts = {strokeColor:lColor,strokeWeight:1.55,strokeOpacity:0.65,geodesic:true,path:pts};
	var gl = new google.maps.Polyline(opts);
	gl.set('storm_name', stormName);
	gl.set('model', ct(pos, 'report_typ'));
	tracks.push(gl);
}

var layers = [];
var all = new golgotha.maps.GMLPointLayer('TropicalModels'); 
for (var sID in storms) {
	if (!storms.hasOwnProperty(sID)) continue;
	var name = names[sID]; var mdls = storms[sID];
	var gml = new golgotha.maps.GMLPointLayer('TropicalModel-' + name);
	for (var x = 0; x < mdls.length; x++) {
		all.addEntry(mdls[x]);
		gml.addEntry(mdls[x]);
	}
		
	layers.push(gml);
}

layers.push(all);
return layers;
}
