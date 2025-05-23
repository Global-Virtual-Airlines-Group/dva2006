golgotha.pilotMap = golgotha.pilotMap || {mrks:[]};

golgotha.pilotMap.load = function() {
	const f = document.forms[0];
	const p = fetch('pilotmap.ws', {signal:AbortSignal.timeout(7500)});
	p.then(function(rsp) {
		if (rsp.status != 200) return false;
		rsp.json().then(function(js) {
			map.removeMarkers(golgotha.pilotMap.mrks);		
			js.forEach(function(a) {
				const mrk = new golgotha.maps.Marker({map:map, color:a.color, pt:a.ll, info:a.info});			
				mrk.ID = a.id; mrk.rank = a.rank; mrk.eqType = a.eqType;			
				golgotha.pilotMap.mrks.push(mrk);			
			});

			golgotha.util.setHTML('isLoading', '');
			golgotha.util.disable(f.noFilter, false);
			golgotha.util.disable(f.eqType, false);
			golgotha.util.disable(f.rank, false);
			golgotha.event.beacon('Pilot Map', 'Load');
		});
	});

	golgotha.util.setHTML('isLoading', ' - LOADING...');
	golgotha.util.disable(f.noFilter);
	golgotha.util.disable(f.eqType);
	golgotha.util.disable(f.rank);
	return true;
};

golgotha.pilotMap.updateMarkers = function() {
	const f = document.forms[0];
	const rank = golgotha.form.comboSet(f.rank) ? f.rank.options[f.rank.selectedIndex].text : null;
	const eqType = golgotha.form.comboSet(f.eqType) ? f.eqType.options[f.eqType.selectedIndex].text : null;
	golgotha.pilotMap.mrkUpdate(rank, eqType);
	return true;
};

golgotha.pilotMap.mrkUpdate = function(rank, eqType) {
	golgotha.pilotMap.mrks.forEach(function(mrk) {
		const rankOK = (rank == null) || (mrk.rank == rank);
		const eqOK = (eqType == null) || (mrk.eqType == eqType);
		mrk.setMap((rankOK && eqOK) ? map : null);
	});

	golgotha.event.beacon('Pilot Map', 'Update');	
	return true;
};
