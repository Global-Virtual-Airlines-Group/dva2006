// DIV creation function
golgotha.maps.util.buildTile = function(pnt, zoom, doc)
{
const sz = this.tileSize;
const div = doc.createElement('div');
div.style.width = sz.width + 'px';
div.style.height = sz.height + 'px';
div.style.overflow = 'hidden';
div.style.position = 'relative';
const img = doc.createElement('img');
img.onerror = golgotha.maps.util.blankImg;
let c = 'wxTile ' + this.imgClass + ' ' + this.imgClass + '-' + this.date;
let imgURL = '';
const nZ = this.nativeZoom;
if (zoom > nZ) {
	const numTiles = Math.pow(2, zoom - nZ);
	const imgSize = numTiles * sz.width; // assuming square
	const offsetX = pnt.x % numTiles;
	const offsetY = pnt.y % numTiles;
	const addr = new golgotha.maps.TileAddress(pnt.x, pnt.y, zoom);
	imgURL = this.makeURL(addr.convert(nZ), nZ);
	if (imgURL.length == 0) return div;
	img.width = imgSize;
	img.height = imgSize;
	img.style.position = 'relative';
	c += ' subNative';
	if (offsetX > 0)
		img.style.marginLeft = (sz.width * -offsetX) + 'px';
	if (offsetY > 0)
		img.style.marginTop = (sz.height * -offsetY) + 'px';
} else {
	imgURL = this.makeURL(pnt, zoom);
	if (imgURL.length == 0) return div;
	img.width = sz.width;
	img.height = sz.height;
}

img.src = imgURL;
img.setAttribute('class', c);
golgotha.maps.setOpacity(img, this.opacity);
div.appendChild(img);
return div;	
};

golgotha.maps.util.blankImg = function(e) {
	const img = e.target;
	img.onerror = null;
	img.src = golgotha.maps.BLANK_IMG;
	return true;
};

// Tile selection function
golgotha.maps.util.getTileImgs = function(cName, eName, parent)
{
if (parent == null) parent = document;	
const elements = [];
const all = parent.getElementsByTagName((eName == null) ? '*' : eName);
for (var x = 0; x < all.length; x++) {
	const e = all[x];
	const cl = e.className;
	if (cl.split && (cl.split(' ').indexOf(cName) > -1))
		elements.push(e);
}

return elements;
};

// Tile URL generation functions
golgotha.maps.util.RVOverlayLayer = function(name, ts, size) { return 'https://tilecache.rainviewer.com' + name + '/' + size.width; };
golgotha.maps.util.getRVTileUrl = function(pnt, zoom) {
	if (zoom > this.maxZoom) return '';
	const max = Math.pow(2, zoom);
	const x = (pnt.x >= max) ? (pnt.x-max) : ((pnt.x < 0) ? (pnt.x+max) : pnt.x);
	const y = (pnt.y >= max) ? (pnt.y-max) : ((pnt.y < 0) ? (pnt.y+max) : pnt.y);
	return this.baseURL + '/' + zoom + '/' + x + '/' + y + '/' + this.pal + '/1_1.png';
};

// Create a weather overlay type
golgotha.maps.WeatherLayer = function(opts, timestamp) {
	if (opts.range == null) opts.range = [];
	const tileURLFunc = (opts.tileURL) ? opts.tileURL : golgotha.maps.util.RVOverlayLayer;
	this.maxZoom = opts.maxZoom;
	this.baseURL = tileURLFunc(opts.name, timestamp, opts.tileSize);
	this.date = timestamp;
	this.range = opts.range;
	this.timestamp = new Date(timestamp);
	this.tileSize = opts.tileSize;
	this.getTile = golgotha.maps.util.buildTile;
	this.makeURL = (tileURLFunc == golgotha.maps.util.RVOverlayLayer) ? golgotha.maps.util.getRVTileUrl : golgotha.maps.util.getTileUrl;
	this.opacity = opts.opacity;
};
	
golgotha.maps.WeatherLayer.prototype.getMap = function() { return this.map; };
golgotha.maps.WeatherLayer.prototype.setMap = function(m) {
	if ((this.map != null) && (m != null)) {
		if (m == this.map) return false;
		setMap(null);
	}

	if (m != null) {
		golgotha.maps.ovLayers.push(this);
		m.overlayMapTypes.insertAt(0, this);
		this.map = m;
	} else if (this.map != null) {
		m = this.map;
		this.map = null;
		golgotha.maps.ovLayers.remove(this);
		for (var x = 0; x < m.overlayMapTypes.getLength(); x++) {
			const l = m.overlayMapTypes.getAt(x);
			if (l == this) {
				m.overlayMapTypes.removeAt(x);
				return true;
			}
		}
	}

	return true;
};

golgotha.maps.WeatherLayer.prototype.getCopyright = function() { return 'Weather Data &copy; ' + this.timestamp.getFullYear() + ' RainViewer' };
golgotha.maps.WeatherLayer.prototype.getTextDate = function() {
	const d = this.timestamp;
	const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
	return d.getDate() + '-' + months[d.getMonth()] + '-' + d.getFullYear() + '  ' + d.getHours() + ':' + ((d.getMinutes() < 10) ? '0' + d.getMinutes() : d.getMinutes());	
};

golgotha.maps.WeatherLayer.prototype.display = function(isVisible) {
	if (!isVisible && (this.map == null))
		return true;

	const o = isVisible ? this.opacity : 0;
	const imgs = golgotha.maps.util.getTileImgs(this.imgClass + '-' + this.date, 'img');
	for (var x = 0; x < imgs.length; x++)
		golgotha.maps.setOpacity(imgs[x], o);
		
	return true;
};

golgotha.maps.WeatherLayer.prototype.hideAll = function() {
	const imgs = golgotha.maps.util.getTileImgs(this.imgClass);
	imgs.forEach(function(img) { golgotha.maps.setOpacity(img, 0); });
	return true;
};

golgotha.maps.util.getTileUrl = function(pnt, zoom) {
	if (zoom > this.maxZoom) return '';
	const addr = new golgotha.maps.TileAddress(pnt.x, pnt.y, zoom);

	// Check if we are in the bounding box
	const bb = this.range;
	if ((bb != null) && (bb.length > 0) && (zoom > 2)) {
		let notOK = true;
		for (var x = 0; notOK && (x < bb.length); x++) {
			const bt = bb[x];
			const pt = addr.convert(bt.z);
			notOK &= ((bt.x != pt.x) || (bt.y != pt.y));
		}
		
		if (notOK) return '';
	}

	// Get the tile numbers
	let tileID = '';
	for (var x = zoom; x > 0; x--) {
		const digit1 = ((golgotha.maps.masks[x] & pnt.x) == 0) ? 0 : 1;
		const digit2 = ((golgotha.maps.masks[x] & pnt.y) == 0) ? 0 : 2;
		tileID = tileID + (digit1 + digit2);
	}

	// Check for multi-host
	let url = this.baseURL; var pos = url.indexOf('%');
	if (pos > -1) {
		const lastDigit = url.charAt(tileID.length - 1);
		url = url.substr(0, pos) + lastDigit + url.substr(pos + 1);
	}

	return url + tileID + '.png';
};

// Tile Address class
golgotha.maps.TileAddress = function(x, y, z) { this.z = z; this.x = this.normalize(x, z); this.y = this.normalize(y, z); };
golgotha.maps.TileAddress.prototype.normalize = function(c, z) { const MAX = Math.pow(2, z); if (c < 0) return (c + MAX); return (c >= MAX) ? (c - MAX) : c; };
golgotha.maps.TileAddress.prototype.toString = function() { return this.x + '.' + this.y + '.' + this.z; };
golgotha.maps.TileAddress.prototype.convert = function(newZ) {
	const dZ = Math.abs(this.z - newZ);
	return (this.z > newZ) ? new golgotha.maps.TileAddress((this.x >> dZ), (this.y >> dZ), newZ) : new golgotha.maps.TileAddress((this.x << dZ), (this.y << dZ), newZ);
};

golgotha.maps.TileAddress.parse = function(addr)
{
const level = addr.length; let x = 0; let y = 0;
for (var z = 0; z < level; z++) {
	switch (addr.charAt(z)) {
		case '1':
			x += golgotha.maps.masks[level - z];
			break;

		case '2':
			y += golgotha.maps.masks[level - z];
			break;

		case '3':
			x += golgotha.maps.masks[level - z];
			y += golgotha.maps.masks[level - z];
			break;
	}
}

return new golgotha.maps.TileAddress(x, y, level);
};

// Reboot series list parser
golgotha.maps.SeriesLoader = function() { this.imgData = []; this.layers = { names:[], ts:0, data:[] }; this.onLoad = []; };
golgotha.maps.SeriesLoader.prototype.getNames = function() { return this.layers.names; };
golgotha.maps.SeriesLoader.prototype.getLatest = function(name) { return this.getLayers(name, 1)[0]; };
golgotha.maps.SeriesLoader.prototype.getLayers = function(name, max)
{ 
const data = this.layers.data[name];
if (data == null) return null;
const mx = (max == null) ? data.length : max;
const d = data.clone();
if (mx < d.length) d.splice(mx, data.length);
return d;
};

golgotha.maps.SeriesLoader.prototype.setData = function(name, tx, imgClassName, imgSize) {
	const d = {opacity:tx, imgClass:imgClassName};
	d.size = (imgSize) ? imgSize : golgotha.maps.TILE_SIZE.width;
	this.imgData[name] = d;
	return true;
};

golgotha.maps.SeriesLoader.prototype.onload = function(f) { this.onLoad.push(f); return true; };
golgotha.maps.SeriesLoader.prototype.clear = function() { this.layers.names = []; this.layers.data = []; return true; };
golgotha.maps.SeriesLoader.prototype.combine = function(max, ln1, ln2)
{
const l1 = this.getLayers(ln1); const l2 = this.getLayers(ln2);
const results = [];	
for (var x = 0; ((x < l1.length) && (results.length < max)); x++)
	results.push(l1[x]);

max = results.length * 2;
const maxDate = results[results.length-1].timestamp;
for (var x = 0; ((x < l2.length) && (results.length < max)); x++) {
	const ov = l2[x];
	const d = ov.timestamp;
	if (d.getTime() > maxDate)
		results.push(ov);
}

return results;
};

// Returns currently displayed layers created by this loader
golgotha.maps.SeriesLoader.prototype.getDisplayed = function(m)
{
const results = {};
for (var x = 0; x < this.layers.names.length; x++) {
	const ln = this.layers.names[x]; const ldata = this.layers.data[ln]; const mapLayers = [];
	for (var y = 0; y < ldata.length; y++) {
		const ov = ldata[y];
		const mm = ov.getMap();
		if ((mm != null) && ((m == null) || (mm == m)))
			mapLayers.push(ov);
	}

	if (mapLayers.length > 0) results[ln] = {layers:mapLayers};
}

return results;
};

// Loads TWC radar data from web service
golgotha.maps.SeriesLoader.prototype.loadRV = function()
{
const loader = this;
const p = fetch('serieslist.ws?time=' + golgotha.util.getTimestamp(3000));
p.then(function(rsp) {
	if (rsp.status != 200) {
		console.log('Error ' + rsp.status + ' loading tile series data');
		return false;	
	}
	
	rsp.json().then(function(js) { loader.parseRV(js); });
});
};

golgotha.maps.SeriesLoader.prototype.parseRV = function(sd)
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
		const layerOpts = {minZoom:2, name:ts.path, maxZoom:layerData.maxZoom, opacity:myLayerData.opacity, tileSize:golgotha.maps.TILE_SIZE, range:[], zIndex:golgotha.maps.z.OVERLAY};
		layerOpts.tileURL = golgotha.maps.util.RVOverlayLayer;
		let ovLayer = new golgotha.maps.WeatherLayer(layerOpts, ts.unixDate);
		ovLayer.imgClass = myLayerData.imgClass;
		ovLayer.nativeZoom = layerData.nativeZoom;
		ovLayer.tileSize = golgotha.maps.TILE_SIZE;
		ovLayer.latest = (tsX == 0);
		ovLayer.basePath = ts.path;
		ovLayer.pal = layerData.palette;

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
					ovLayer.setMap(dspL.getMap());
					dspL.setMap(null);
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
