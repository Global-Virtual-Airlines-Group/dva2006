golgotha.maps.oceanic = golgotha.maps.oceanic || {};

golgotha.maps.oceanic.copyRoute = function() {
	const d = document.getElementById('trackData');
	const t = document.createElement('textarea');
	t.value = d.innerText;
	document.body.appendChild(t);
	t.select();
	navigator.clipboard.writeText(t.value);
	t.remove();
	return true;
};

golgotha.maps.oceanic.showTrackInfo = function(marker) {
	golgotha.util.setHTML('trackLabel', 'Track ' + marker.title);
	golgotha.util.setHTML('trackData', marker.trackPoints);
	golgotha.util.display('trackCopy', true);
	return true;
};

golgotha.maps.oceanic.resetTracks = function() {
	golgotha.maps.oceanic.tracks = {W:[], E:[], C:[]}; golgotha.maps.oceanic.points = {W:[], E:[], C:[]};
	const f = document.forms[0];
	f.showTracks.forEach(function(cb) { cb.checked = true; } );

	// Reset track data label
	golgotha.util.setHTML('trackLabel', 'Track Data');
	golgotha.util.setHTML('trackData', 'N/A');
	golgotha.util.display('trackCopy', false);
	return true;
};

// Toggle the points and tracks
golgotha.maps.oceanic.updateTracks = function(checkbox) {
	const xtracks = golgotha.maps.oceanic.tracks[checkbox.value];
	const trackPoints = golgotha.maps.oceanic.points[checkbox.value];
	const m = checkbox.checked ? map : null;
	trackPoints.forEach(function(pt) { pt.setMap(m); });
	xtracks.forEach(function(t) { (m) ? m.addLine(t) : map.removeLine(t); });
	return true;
};

golgotha.maps.oceanic.loadTracks = function(type)
{
// Check the combobox
const f = document.forms[0];
const dt = f.date.options[f.date.selectedIndex];
if (f.date.selectedIndex < 1) {
	golgotha.util.display('fetchData', false);
	return;
}

// Fetch the tracks
golgotha.util.setHTML('isLoading', ' - LOADING...');
const p = fetch('otrackinfo.ws?type=' + type + '&date=' + dt.text, {signal:AbortSignal.timeout(5000)});
p.then(function(rsp) {
	if (rsp.status != 200) {
		golgotha.util.setHTML('isLoading', ' - ERROR ' + rsp.status);
		golgotha.util.display('fetchData', false);		
		return false;
	}

	map.removeMarkers(golgotha.maps.displayedMarkers); 
	const layers = golgotha.maps.displayedLayers.slice();
	layers.forEach(function(l) { if (!golgotha.maps.util.isTiles(l)) map.removeLine(l); });
	golgotha.maps.oceanic.resetTracks();
	rsp.json().then(function(jsData) {
		jsData.tracks.forEach(function(t) {
			t.waypoints.forEach(function(wp) {
				const mrk = new golgotha.maps.Marker({map:map, color:wp.color, info:t.info, label:wp.code, opacity:0.75, pt:wp.ll});
				mrk.title = t.code; mrk.trackPoints = t.track;
				mrk.getElement().addEventListener('click', function(e) {
					const m = e.currentTarget.marker; 
					golgotha.maps.oceanic.showTrackInfo(m); 
				});
 
				golgotha.maps.oceanic.points[t.type].push(mrk);
			});

			// Draw the route
			const trackLine = new golgotha.maps.Line('NAT-' + t.code, {color:t.color, width:2, opacity:0.7}, t.pts);
			map.addLine(trackLine);
			golgotha.maps.oceanic.tracks[t.type].push(trackLine);
		});

		if (jsData.src) {
			document.getElementById('fetchDate').innerText = jsData.fetchDate;
			document.getElementById('fetchSrc').innerText = jsData.src;
		}

		golgotha.util.display('fetchData', (jsData.src));
		golgotha.util.setHTML('isLoading', '');
	});
});

return true;
};
