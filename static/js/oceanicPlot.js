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
	const markerMap = checkbox.checked ? map : null;
	trackPoints.forEach(function(pt) { pt.setMap(markerMap); });
	xtracks.forEach(function(pt) { pt.setMap(markerMap); });
	return true;
};

golgotha.maps.oceanic.loadTracks = function(type)
{
// Check the combobox
const f = document.forms[0];
const dt = f.date.options[f.date.selectedIndex];
if (f.date.selectedIndex < 1) return;

// Fetch the tracks
golgotha.util.setHTML('isLoading', ' - LOADING...');
const p = fetch('otrackinfo.ws?type=' + type + '&date=' + dt.text, {signal:AbortSignal.timeout(5000)});
p.then(function(rsp) {
	if (rsp.status != 200) {
		golgotha.util.setHTML('isLoading', ' - ERROR ' + rsp.status);		
		return false;
	}
	
	map.clearOverlays();
	golgotha.maps.oceanic.resetTracks();
	rsp.json().then(function(jsData) {
		jsData.tracks.forEach(function(t) {
			const trackPos = [];
			t.waypoints.forEach(function(wp) {
				trackPos.push(wp.ll);

				// Create the map marker
				const mrk = new golgotha.maps.Marker({map:map, color:wp.color, info:t.info, label:wp.code, opacity:0.75}, wp.ll);
				mrk.title = t.code; mrk.trackPoints = t.track; 
				google.maps.event.addListener(mrk, 'click', function() { golgotha.maps.oceanic.showTrackInfo(this); });
				golgotha.maps.oceanic.points[t.type].push(mrk);
			});

			// Draw the route
			const trackLine = new google.maps.Polyline({map:map, path:trackPos, strokeColor:t.color, strokeWeight:2, strokeOpacity:0.7, geodesic:true, zIndex:golgotha.maps.z.POLYLINE});
			golgotha.maps.oceanic.tracks[t.type].push(trackLine);
		});

		golgotha.util.setHTML('isLoading', '');
	});
});

return true;
};
