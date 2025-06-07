golgotha.maps.wx.RadarSource = function(name, path, size) {
	this.name = name;
	this._path = path;
	this._size = size;
};

golgotha.maps.wx.RadarSource.prototype.getType = 'Tiles';
golgotha.maps.wx.RadarSource.prototype.getLayer = function() {
	const to = 'https://tilecache.rainviewer.com' + this._path + '/' + this._size + '/{z}/{x}/{y}/' + this.pal + '/1_1.png';
	const so = {type:'raster',tileSize:this._size,tiles:[to]};
	return {id:this.name,type:'raster',paint:{'raster-opacity':this.opacity},source:so};
};

// Reboot series list parser
golgotha.maps.wx.SeriesLoader = function() { this.imgData = []; this.layers = {names:[], ts:0, data:[]}; this.onLoad = []; };
golgotha.maps.wx.SeriesLoader.prototype.getNames = function() { return this.layers.names; };
golgotha.maps.wx.SeriesLoader.prototype.getLatest = function(name) { return this.getLayers(name, 1)[0]; };
golgotha.maps.wx.SeriesLoader.prototype.getLayers = function(name, max) { 
	const data = this.layers.data[name];
	if (data == null) return null;
	const mx = (max == null) ? data.length : max;
	const d = data.clone();
	if (mx < d.length) d.splice(mx, data.length);
	return d;
};

golgotha.maps.wx.SeriesLoader.prototype.setData = function(name, tx, imgClassName, imgSize) {
	const d = {opacity:tx, imgClass:imgClassName};
	d.size = (imgSize) ? imgSize : golgotha.maps.TILE_SIZE.width;
	this.imgData[name] = d;
	return true;
};

golgotha.maps.wx.SeriesLoader.prototype.onload = function(f) { this.onLoad.push(f); return true; };
golgotha.maps.wx.SeriesLoader.prototype.clear = function() { this.layers.names = []; this.layers.data = []; return true; };

// Returns currently displayed layers created by this loader
golgotha.maps.wx.SeriesLoader.prototype.getDisplayed = function(m)
{
const results = {};
for (var x = 0; x < this.layers.names.length; x++) {
	const ln = this.layers.names[x]; const ldata = this.layers.data[ln]; const mapLayers = [];
	for (var y = 0; y < ldata.length; y++) {
		const ov = ldata[y];
		const mm = (map.getLayer(ov.name) != null) ? map : null;
		if ((mm != null) && ((m == null) || (mm == m)))
			mapLayers.push(ov);
	}

	if (mapLayers.length > 0) results[ln] = {layers:mapLayers};
}

return results;
};

// Loads RainViewer radar data from web service
golgotha.maps.wx.SeriesLoader.prototype.loadRV = function()
{
	const loader = this;
	const p = fetch('serieslist.ws?time=' + golgotha.util.getTimestamp(3000), {signal:AbortSignal.timeout(2500)});
	p.then(function(rsp) {
		if (rsp.status != 200) {
			console.log('Error ' + rsp.status + ' loading tile series data');
			return false;	
		}
	
		rsp.json().then(function(js) { loader.parseRV(js); });
	});
};

golgotha.maps.wx.SeriesLoader.prototype.parseRV = function(sd)
{
if ((!sd.seriesNames) || (!sd.seriesInfo)) return false;
const displayedLayers = this.getDisplayed(); this.clear();
for (var layerName = sd.seriesNames.pop(); (layerName != null); layerName = sd.seriesNames.pop()) {
	const layerData = sd.seriesInfo[layerName];
	if ((layerData.nativeZoom == null) || (!(layerData.series instanceof Array))) continue;
	if (layerData.maxZoom == null) layerData.maxZoom = layerData.nativeZoom + 7;
	if (layerData.sizes == null) layerData.sizes = [256];
	const dl = displayedLayers[layerName] || {layers:[], data:[]};

	// Peek inside the series data - if the first entry is FF, iterate through its timestamps
	const timestamps = layerData.series;
	const myLayerData = this.imgData[layerName];
	if (myLayerData == null)
		myLayerData = {opacity:0.5, imgClass:layerName, size:golgotha.maps.TILE_SIZE.width, pal:0};

	const slices = [];
	for (var tsX = 0; tsX < timestamps.length; tsX++) {
		const ts = timestamps[tsX];
		let ovLayer = new golgotha.maps.wx.RadarSource(layerName, ts.path, golgotha.maps.TILE_SIZE.w);
		ovLayer.imgClass = myLayerData.imgClass;
		ovLayer.latest = (tsX == 0);
		ovLayer.basePath = ts.path;
		ovLayer.pal = layerData.palette;
		ovLayer.opacity = myLayerData.opacity;

		// Determine whether we display it - it's either the latest or an arbitrary timeslice
		for (var x = 0; x < dl.layers.length; x++) {
			const dspL = dl.layers[x]; if (dspL == null) continue; 
			const dt = dspL.date;
			if (ovLayer.latest == true) { // If we're processing the latest
				if (dt == ts.unixDate) {
					ovLayer = dspL;
					dl.layers[x] = null;
					console.log('Keeping latest ' + layerName + ' ' + dspL.timestamp);
				} else if (dspL.latest) {
					console.log('Replacing latest ' + layerName + ', replacing ' + dspL.timestamp + ' with ' + ovLayer.timestamp);
					dspL.force = true;
					map.removeLine(dspL);
					map.addLine(ovLayer);
				}
			} else if (dt == ts.unixDate) {
				if ((!dspL.latest) && (!dspL.force)) {
					ovLayer = dspL;
					dl.layers[x] = null;
					console.log('Keeping ' + layerName + ' ' + dspL.timestamp);
				}
			}
		}

		slices.push(ovLayer);
	}

	this.layers.names.push(layerName);
	this.layers.data[layerName] = slices;
	dl.layers.forEach(function(ov) { if (ov != null) ov.setMap(null); });
}

// Remove displayed layers and Fire event handlers
for (var l = this.onLoad.pop(); (l != null); l = this.onLoad.pop())
	l.call(this);
	
return true;
};

golgotha.maps.wx.WXLayerControl = function() { this._layers = []; };
golgotha.maps.wx.WXLayerControl.prototype.addLayer = function(l) { this._layers.push(l); };
golgotha.maps.wx.WXLayerControl.prototype.onAdd = function(map) {
	this._map = map;
	const div = document.createElement('div');
	div.className = 'mapBoxBaseControl mapboxgl-ctrl mapboxgl-ctrl-group';
	for (var x = 0; x < this._layers.length; x++) {
		const l = this._layers[x];	
		const dv = golgotha.maps.CreateButtonDiv(l.name);
		dv.className = 'mapBoxLayerSelect layerSelect wxSelect';
		dv.map = map; dv.layer = l.id || l.name.toLowerCase(); dv.getLayer = l.f;
		dv.id = 'wxselect-' + dv.layer;
		if (l.c != null) golgotha.util.addClass(dv, l.c);
		if (l.disabled == true) { dv.disabled = true; golgotha.util.addClass(dv, 'disabled'); }
		dv.addEventListener('click', function(e) {
			const d = e.currentTarget;
			if (d.disabled) return;
			const dl = d.getLayer();
			if (d.isSelected) {
				d.map.removeLine(dl);
				golgotha.util.removeClass(d, 'displayed');
				delete d.isSelected;
			} else {
				d.map.addLine(dl);
				golgotha.util.addClass(d, 'displayed');
				d.isSelected = true;
			}
		});
		
		dv.enable = function() { golgotha.util.removeClass(this, 'disabled'); delete this.disabled; };
		div.appendChild(dv);
	}
	
	const cdv = golgotha.maps.CreateButtonDiv('None');
	cdv.className = 'mapBoxLayerSelect layerSelect';
	cdv.map = map; cdv.id = 'wxClear';
	cdv.addEventListener('click', function(_e) {
		const ctls = golgotha.util.getElementsByClass('wxSelect', 'div');
		ctls.forEach(function(ctl) {
			if (ctl.isSelected != true) return;
			const cl = ctl.getLayer();
			ctl.map.removeLine(cl);
			golgotha.util.removeClass(ctl, 'displayed');
			delete ctl.isSelected;
		});
	});
	
	div.appendChild(cdv);
	this._container = div;
	return div;	
};

golgotha.maps.wx.WXLayerControl.prototype.onRemove = function() {
	this._container.parentNode.removeChild(this._container);
	delete this._map;
};
