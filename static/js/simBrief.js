golgotha.simbrief = golgotha.simbrief || {airframes:{}};
golgotha.simbrief.sbSubmit = function() {
	golgotha.form.submit();
	const f = document.forms[0];
	const sbf = document.getElementById('sbapiform');
	sbf.planformat.value = golgotha.form.getCombo(f.sbFormat).toLowerCase();
	sbf.civalue.value = f.costIndex.value;
	sbf.etopsrule.value = golgotha.form.getCombo(f.etopsOV);
	sbf.units.value = f.sbWeightUnit.value + 'S';
	if (golgotha.form.comboSet(f.tailCode)) 
		sbf.reg.value = golgotha.form.getCombo(f.tailCode).toUpperCase();

	try {
		if (parseInt(sbf.pax.value) < 1)
			return golgotha.simbrief.loadPax(sbf);
	} catch (e) {
		return golgotha.simbrief.loadPax(sbf);
	}

	return simbriefsubmit(self.location.href);
};

golgotha.simbrief.loadPax = function(f) {
	const p = fetch('sbpax.ws?id=' + golgotha.simbrief.id, {signal:AbortSignal.timeout(5000)});
	p.then(function(rsp) {
		if (!rsp.ok) return false;
		rsp.json().then(function(js) {
			if (!js.isCalculated)
				console.log('Using existing passenger load of ' + js.pax);
			else
				f.pax.value = js.pax;	

			return simbriefsubmit(self.location.href);			
		});
	});
};

golgotha.simbrief.loadAirframes = function() {
	const p = fetch('sbairframes.ws?id=' + golgotha.simbrief.id, {signal:AbortSignal.timeout(5000)});
	p.then(function(rsp) {
		if (!rsp.ok) return false;
		const f = document.forms[0];
		const cb = f.tailCode;
		rsp.json().then(function(js) {
			cb.options.length = js.length + 1;	
			for (var x = 0; x < js.length; x++) {
				const ac = js[x];
				cb.options[x + 1] = new Option(ac.tailCode + ' (' + ac.useCount + ' flights)', ac.tailCode);
				golgotha.simbrief.airframes[ac.tailCode] = ac;
			}

			golgotha.util.disable(f.disableCustomAirframe, (js.length == 0));
			golgotha.util.display('sbTailCode', (js.length > 0));
		});
	});
};

golgotha.simbrief.sbAirframeUpdate = function(cb) {
	const sbf = document.getElementById('sbapiform');
	if (cb.selectedIndex < 1) {
		sbf.type.value = golgotha.simbrief.acType;
		golgotha.util.display('sbAirframe', false);
		return true;	
	}

	const noCustom = cb.form.disableCustomAirframe.checked;
	const ac = golgotha.simbrief.airframes[golgotha.form.getCombo(cb)];
	sbf.type.value = (noCustom || !ac.isCustom) ? golgotha.simbrief.acType : ac.id;
	golgotha.simbrief.setSBID(noCustom ? null : ac.id);
	golgotha.util.display('sbAirframe', ac.isCustom);
	return true;
};

golgotha.simbrief.sbCustomToggle = function(cb) {
	const sbf = document.getElementById('sbapiform');
	const ac = golgotha.simbrief.airframes[golgotha.form.getCombo(cb.form.tailCode)];
	if (cb.checked) {
		sbf.type.value = golgotha.simbrief.acType;
		golgotha.simbrief.setSBID();
	} else {
		golgotha.simbrief.setSBID(ac.id);
		sbf.type.value = ac.isCustom ? ac.id: golgotha.simbrief.acType;
	}

	return true;
};

golgotha.simbrief.setSBID = function(id) {
	const sbID = document.getElementById('sbAirframeID');
	sbID.innerText = (id) ? '(SimBrief ID: ' + id +')' : '';
	golgotha.util.display('sbAirframeInfo', (id));
	return true;
};

golgotha.simbrief.showSBMessage = function(msg, cn) {
	const sp = document.getElementById('sbMessage');
	sp.className = cn;
	sp.innerHTML = msg;
	golgotha.util.display('sbMessageBox', true);
	return true;
};

golgotha.simbrief.sbRefresh = function() {
	const f = document.forms[0];
	golgotha.form.submit(f);
	golgotha.util.display('sbMessageBox', false);
	const p = fetch('sbrefresh.ws?id=' + golgotha.simbrief.id, {signal:AbortSignal.timeout(5000)});
	p.then(function(rsp) {
		golgotha.form.clear(f);
		rsp.text();
		if (rsp.status == 200) {
			golgotha.simbrief.showSBMessage('SimBrief package updated', 'ter');
			window.setTimeout(function() { location.reload(); }, 950);
		} else if (rsp.status == 304)
			golgotha.simbrief.showSBMessage('SimBrief package not modified', 'warn');
		else if (rsp.status == 503)
			golgotha.simbrief.showSBMessage(rsp.headers.get('X-SB-Error-Message') + ' (' + rsp.headers.get('X-SB-Error-Code') + ')');
		else if (rsp.status >= 500)
			golgotha.simbrief.showSBMessage('Error ' + rsp.status + ' updating package', 'error');
	});
};

golgotha.simbrief.sbDownloadPlan = function(cb) {
	if (cb.selectedIndex < 1) return false;
	const o = cb.options[cb.selectedIndex];
	self.location = golgotha.simbrief.planURL + o.value;
	return true;
};

golgotha.simbrief.sbBriefingText = function() {
	return window.open('/briefing.do?id=' + golgotha.simbrief.id, 'sbBriefing', 'height=530,width=820,menubar=no,toolbar=no,status=no,scrollbars=yes');
};
