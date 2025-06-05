// FIR loader
golgotha.maps.FIRLoader = function() { this._onLoad = []; };
golgotha.maps.FIRLoader.prototype.getFIRs = function() { return this._firs; };
golgotha.maps.FIRLoader.prototype.getOceanic = function() { return this._oFirs; };
golgotha.maps.FIRLoader.prototype.onload = function(e) { this._onLoad.push(e); };
golgotha.maps.FIRLoader.prototype.load = function() {
	const p = fetch('firs.ws', {signal:AbortSignal.timeout(3500)}); const ldr = this;
	p.then(function(rsp) {
		if (!rsp.ok) return false;
		rsp.json().then(function(js) {
			ldr._firs = new golgotha.maps.PolygonLayer('FIRs', {width:2, opacity:0.725, color:'#80a040', fillColor:'#908020', fillOpacity:0.15});
			ldr._oFirs = new golgotha.maps.PolygonLayer('Oceanic FIRs', {width:2, opacity:0.625, color:'#60a0e0', fillColor:'#208090', fillOpacity:0.125});
			for (var x = 0; x < js.length; x++) {
				const fir = js[x];
				const fc = fir.oceanic ? ldr._oFirs : ldr._firs;
				fc.add(fir.name, fir.border);
			}

			// Remove displayed layers and Fire event handlers
			for (var l = ldr._onLoad.pop(); (l != null); l = ldr._onLoad.pop())
				l.call(this);
		});
	});
};
