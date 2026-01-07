const golgotha = {event:{},util:{},form:{isSubmitted:false,invalidDomains:[]},local:{},nav:{sideMenu:false},charts:{},sort:{lastSort:{},data:{}}};
golgotha.nav.touch = ("ontouchend" in document);
golgotha.util.getTimestamp = function(ms) { var d = new Date(); return d.getTime() - (d.getTime() % ms); };
golgotha.util.darkMode = false; // (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches);
golgotha.event.beacon = function() { if (gtag) gtag('event', arguments); };
golgotha.event.stop = function(e) { if (e) { e.stopPropagation(); e.preventDefault(); } return false; };
golgotha.event.Error = function(msg, showAlert) { const e = new Error(msg); e.showAlert = showAlert; return e; };
golgotha.event.ValidationError = function(msg, el) { const e = new golgotha.event.Error(msg, true); e.focusElement = el; return e; };

golgotha.charts.bg = golgotha.util.darkMode ? '#000021' : '#efefef';
golgotha.charts.tx =  golgotha.util.darkMode ? '#efefef' : '#00002f';
golgotha.charts.lgStyle = {color:golgotha.charts.tx,fontName:'Verdana',fontSize:9};
golgotha.charts.ttStyle = {color:golgotha.charts.tx,fontName:'Verdana',fontSize:11};
golgotha.charts.dateTX = function(e) { const dt = e[0]; e[0] = new Date(dt.y, dt.m, dt.d, 12, 0, 0); };
golgotha.charts.buildOptions = function(opts) {
	const o = opts || {}; o.legend = o.legend || {};
	const aX = {textStyle:golgotha.charts.lgStyle,titleTextStyle:golgotha.charts.ttStyle};
	o.backgroundColor = golgotha.charts.bg;
	o.vAxis = Object.assign({}, aX);
	o.hAxis = Object.assign({}, aX);
	o.legend = Object.assign({}, o.legend, aX);
	o.fontSize = 10;
	o.fontName = o.fontName || 'Verdana';
	o.textStyle = o.textStyle || Object.assign({}, golgotha.charts.lgStyle);
	o.titleTextStyle = o.titleTextStyle || Object.assign({}, golgotha.charts.ttStyle);
	return o;
};

golgotha.util.loadAsync = function(js) {
	if (!js.endsWith('.js')) js += '.js';
	const p = new Promise(function(rsv, rej) {
		const sc = document.createElement('script');
		sc.setAttribute('async', 'true');
		sc.setAttribute('id', js);
		sc.onload = function() { console.log('Loaded ' + js); rsv(); };
		sc.onerror = rej;
		document.head.appendChild(sc);
		sc.src = golgotha.maps.path + '/' + js;
	});

	return p;
};

golgotha.util.mapAPILoaded = function() {
	console.log('Google Maps API loaded');
	if (golgotha.maps.async) {
		const lp = golgotha.util.loadAsync(golgotha.maps.library);
		lp.then(function() {
			const ps = [];
			golgotha.maps.jsLoad.forEach(function(l) { ps.push(golgotha.util.loadAsync(l)); });
			const p = Promise.all(ps);
			p.then(function() {
				const f = new Function(golgotha.maps.callback + '()');
				f.apply();
			});
		});
	}
	
	return true;
};

golgotha.util.getElementsByClass = function(cName, eName, parent) {
	if (parent == null) parent = document;
	let elements = [];
	const all = parent.getElementsByTagName((eName == null) ? '*' : eName);
	for (var x = 0; x < all.length; x++) {
		const cl = all[x].className;
		if (cl.split && (cl.split(' ').indexOf(cName) > -1))
			elements.push(all[x]);
	}

	return elements;
};

golgotha.util.addClass = function(e, cl) {
	if (!e) return false;
	const c = e.className.split(' ');
	if (c.indexOf(cl) < 0) c.push(cl);
	e.className = (c.length == 0) ? '' : c.join(' ').trim();
	return true;
};

golgotha.util.removeClass = function(e, cl) {
	if (!e) return false;
	const c = e.className.split(' ');
	const hasClass = c.remove(cl);
	e.className = (c.length == 0) ? '' : c.join(' ').trim();
	return hasClass;
};

golgotha.util.hasClass = function(e, cl) {
	if (!e) return false;
	const c = e.className.split(' ');
	return c.remove(cl);
};

golgotha.util.disable = function(e, doDisable) {
	if (!e) return false;
	doDisable = ((doDisable == null) || doDisable);
	if (!e.style) e = document.getElementById(e);
	if (e) e.disabled = doDisable;
	return (e != null);
};

golgotha.util.show = function(e, isVisible) {
	if (!e) return false;
	if (!e.style) e = document.getElementById(e);
	if (e) e.style.visibility = isVisible ? 'visible' : 'hidden';
	return (e != null);
};

golgotha.util.display = function(e, isVisible) {
	if (!e) return false;
	if (!e.style) e = document.getElementById(e);
	if (e) e.style.display = isVisible ? '' : 'none';
	return (e != null);
};

golgotha.util.getStyle = function(sheet, cl, attr) {
	attr = attr || 'color';
	for (var x = 0; x < document.styleSheets.length; x++) {
		const ss = document.styleSheets[x];
		if ((ss.href == null) || (ss.href.indexOf(sheet) == -1)) continue;
		for (var y = ss.cssRules.length; y > 0; y--) {
			const cs = ss.cssRules[y - 1];
			if ((cs.selectorText) && (cs.style) && (cs.selectorText.indexOf(cl) > -1))
				return cs.style[attr];
		}
	}

	return null;
};

golgotha.util.setHTML = function(e, content) {
	if (!e) return false;
	if (!e.style) e = document.getElementById(e);
	if (e) e.innerText = content;
	return true;
};

golgotha.form.resizeAll = function() {
	const boxes = golgotha.util.getElementsByClass('resizable');
	boxes.forEach(function(b) { golgotha.form.resize(b); });
	return true;
};

golgotha.form.resize = function(textbox) {
	if ((!textbox) || (textbox.clientHeight >= textbox.scrollHeight)) return false;
	textbox.style.height = textbox.scrollHeight + 'px';
	if (textbox.clientHeight < textbox.scrollHeight)
		textbox.style.height = (textbox.scrollHeight * 2 - textbox.clientHeight) + 'px';

	return true;
};

golgotha.form.comboSet = function(combo) { return ((combo) && (combo.selectedIndex > 0)); };
golgotha.form.setCombo = function(combo, entryValue)
{
if (!combo) return false;
for (var x = 0; x < combo.options.length; x++) {
	const opt = combo.options[x];
	if ((opt.value == entryValue) || (opt.text == entryValue)) {
		combo.selectedIndex = x;
		return true;
	}
}

combo.selectedIndex = -1;
return false;
};

golgotha.form.getCombo = function(combo) {
	if ((!combo) || (combo.selectedIndex == -1)) return null;
	return combo.options[combo.selectedIndex].value;
};

golgotha.form.getCheck = function(cb) {
	const v = [];
	if ((!cb) || (!cb.length)) return v;
	for (var x = 0; (x < cb.length); x++) {
		if (cb[x].checked) {
			if (cb[x].type == 'radio')
				return cb[x];
			else
				v.push(cb[x].value);
		}
	}

	return v;
};

golgotha.util.isFunction = function(o) { return !!(o && o.constructor && o.call && o.apply); };
golgotha.util.createURLParams = function(o) {
	let params = [];
	for (p in o) {
		let v = o[p];
		if (o.hasOwnProperty(p) && !golgotha.util.isFunction(v) && (v != null))
			params.push(p + '=' + encodeURIComponent(v));
	}

	return params.join('&');
};

golgotha.util.createScript = function(opts)
{
let url = opts.url;
if (url.substring(0, 2) == '//')
	url = self.location.protocol + url;
else if (url.indexOf('http') != 0)
	url = self.location.protocol + "//" + golgotha.maps.wxHost + url;
	
if (url.indexOf(golgotha.maps.wxHost) > -1) {
	const api = 'api=' + golgotha.maps.keys.api;
	url += (url.indexOf('?') > 0) ? '&' : '?';
	url += api;
}
	
const sc = document.createElement('script');
sc.setAttribute('id', opts.id);
sc.src = url;
if (opts.async) sc.setAttribute('async', 'true');
const oldSC = document.getElementById(opts.id);
if (oldSC != null)
	oldSC.parentNode.replaceChild(sc, oldSC);
else
	document.body.appendChild(sc);

return true;
};

golgotha.util.createElement = function(eName, txt, className) {
	const td = document.createElement(eName);
	if (className != null) td.setAttribute('class', className);
	td.appendChild(document.createTextNode(txt));
	return td;
};

golgotha.util.enable = function(n) {
	n = (n instanceof Array) ? n : [n];
	for (var x = 0; x < n.length; x++) {
		const ci = n[x];
		if (ci.style) {
			if (ci.enable) ci.enable();
		} else if (ci.charAt(0) == '#') {
			let ee = golgotha.util.getElementsByClass(ci.substring(1));
			for (var e = ee.pop(); (e != null); e = ee.pop())
				if (e.enable) e.enable();
		} else {
			let e = document.getElementById(ci); 
			if ((e) && (e.enable)) e.enable();
		}
	}

	return true; 
};

golgotha.onDOMReady = function(f) { return document.addEventListener('DOMContentLoaded', f); };
golgotha.getChild = function(e, name) {
	const children = e.getElementsByTagName(name);
	return (children.length == 0) ? null : children[0];
};

golgotha.getCDATA = function(e)
{
let child = e.firstChild;	
while ((child != null) && (child.nodeType != 4))
	child = child.nextSibling;

return child;
};

if (window.Element != undefined) {
	Element.prototype.getChild = function(name) { return golgotha.getChild(this, name); };
	Element.prototype.getCDATA = function() { return golgotha.getCDATA(this); };
}

Array.prototype.contains = function(obj) { return (this.indexOf(obj) != -1); };
Array.prototype.clone = function() { return this.slice(); };
Array.prototype.remove = function(obj) {
for (var x = 0; x < this.length; x++) {
	if (this[x] == obj) {
		this.splice(x, 1);
		return true;
	}
}

return false;
};

golgotha.form.check = function() { return (golgotha.form.isSubmitted != true); };
golgotha.form.submit = function(f) {
	golgotha.form.isSubmitted = true;
	golgotha.form.showSpinner(f);
	return true;
};

golgotha.form.clear = function(f) {
	golgotha.form.isSubmitted = false;
	const dlg = document.getElementById('dlg');
	if ((dlg) && dlg.open) dlg.close();
	const ies = (f) ? golgotha.util.getElementsByClass('button', 'input', f) : [];
	ies.forEach(function(e) { e.disabled = false; });
	return true;
};

golgotha.form.showSpinner = function(f) {
	const ies = (f) ? golgotha.util.getElementsByClass('button', 'input', f) : [];
	const dlg = document.getElementById('dlg');
	const spinImg = document.getElementById('spinImg');
	if ((!dlg) || (!spinImg)) return false;
	dlg.addEventListener('close', function() {
		ies.forEach(function(e) { e.disabled = false; });
		golgotha.util.display(spinImg, false);
	}, {once:true});
	
	ies.forEach(function(e) { e.disabled = true; });
	spinImg.parentNode.removeChild(spinImg);
	dlg.appendChild(spinImg);
	golgotha.util.display('dialogMsg', false);
	golgotha.util.display(spinImg, true);
	dlg.showModal();
	return true;
};

golgotha.form.showDialogMessage = function(msg, opts) {
	const dlg = document.getElementById('dlg');
	const dv = document.getElementById('dialogMsg');
	if ((!dlg) || (!dv)) return false;
	const o = opts || {timeout:0,className:'error bld'};
	dv.className = '';
	golgotha.util.addClass(dv, o.className);
	dv.innerText = msg;
	golgotha.util.display(dv, true);
	dlg.showModal();
	if (o.timeout > 0)
		window.setTimeout(function() { if (dlg.open) dlg.close(); }, o.timeout);

	return true;
};

golgotha.form.get = function(url) { golgotha.form.submit(); self.location = '/' + url; return true; };
golgotha.form.post = function(url) {
	const f = document.forms[0];
	const oldaction = f.action;
	f.action = url;
 
	// Execute the form validation - if any
	if (f.onsubmit) {
		const submitOK = f.onsubmit();
		if (!submitOK) {
			f.action = oldaction;
			return false;
		}
	}

	golgotha.form.submit(f);
	f.submit();
	return true;
};

golgotha.form.resetCombo = function(ev) {
	const e = ev || window.event; const t = e.target;
	t.selectedIndex = 0; t.dispatchEvent(new Event('change'));
	e.stopPropagation();
	return false; 
};

golgotha.form.wrap = function(func, f) {
	try {	
		return func(f);
	} catch (e) {
		const dlg = document.getElementById('dlg');
		if (e.showAlert && dlg)
			golgotha.form.showDialogMessage(e.message);
		else if (e.showAlert) 
			alert(e.message);
		else
			console.log(e);

		if (e.focusElement)
			e.focusElement.focus(); 
	}

	return false;
};

golgotha.form.validate = function(opts)
{
if (!('f' in opts) || !('t' in opts)) throw new golgotha.event.ValidationError('Incomplete Validation Data');
if ('ext' in opts) return golgotha.form.validateFile(opts.f, opts.ext, opts.t, opts.empty, opts.maxSize);
if ('addr' in opts) return golgotha.form.validateEMail(opts.f, opts.t);
if ('l' in opts) return golgotha.form.validateText(opts.f, opts.l, opts.t);
if (!opts.f) return true;
if ('min' in opts) {
	const vf = ((0 in opts.f) || (opts.f.type == 'checkbox')) ? golgotha.form.validateCheckBox : golgotha.form.validateNumber;
	return vf(opts.f, opts.min, opts.t);
}

if (opts.f.options) return golgotha.form.validateCombo(opts.f, opts.t);
throw new golgotha.event.ValidationError('Invalid Validation Data', opts.f);
};

golgotha.form.validateText = function(t, min, title) {
	if ((!t) || (t.disabled)) return true;
	if (t.value.length < min) throw new golgotha.event.ValidationError('Please provide the ' + title + '.', t);
	return true;
};

golgotha.form.validateNumber = function(t, minValue, title) {
	if ((!t) || (t.disabled)) return true;
	const i = parseFloat(t.value);
	if ((t.value.length < 1) || isNaN(i))
		throw new golgotha.event.ValidationError('Please provide a numeric ' + title + '.', t);
	if (i < minValue)
		throw new golgotha.event.ValidationError('The ' + title + ' must be greater than ' + minValue + '.', t);

	return true;
};

golgotha.form.validateEMail = function(t, title)
{
if (!golgotha.form.validateText(t, 5, title)) return false;
const pattern = /^[\w](([_\.\-\+]?[\w]+)*)@([\w]+)(([\.-]?[\w]+)*)\.([A-Za-z]{2,})$/;
if (!pattern.test(t.value))
	throw new golgotha.event.ValidationError('Please provide a valid ' + title + '.', t);

// Validate e-mail domain
const addr = t.value;
const usrDomain = addr.substring(addr.indexOf('@') + 1, addr.length);
for (var x = 0; x < golgotha.form.invalidDomains.length; x++) {
	if (usrDomain == golgotha.form.invalidDomains[x])
		throw new golgotha.event.ValidationError('Your e-mail address (' + addr + ') contains a forbidden domain - ' + golgotha.form.invalidDomains[x], t);
}

return true;
};

golgotha.form.validateCombo = function(c, title) {
	if ((!c) || (c.disabled) || (c.selectedIndex > 0)) return true;
	throw new golgotha.event.ValidationError('Please provide the ' + title + '.', c);
};

golgotha.form.validateFile = function(f, extTypes, title, allowBlank, maxSizeKB) {
	if ((!f) || (f.disabled)) return true;
	if ((maxSizeKB > 0) && (f.files) && (f.files.length > 0)) {
		const size = f.files[0].size / 1024;
		console.log('File size = ' + size + 'K, max = ' + maxSizeKB + 'K');
		if (size > maxSizeKB)
			throw new golgotha.event.ValidationError('The ' + title + ' cannot be larger than ' + maxSizeKB + 'KB.', f);
	}

	if ((allowBlank && (f.value.length == 0)) || (extTypes.length == 0)) return true;
	const ext = f.value.substring(f.value.lastIndexOf('.') + 1).toLowerCase();
	for (var e = extTypes.pop(); (e != null); e = extTypes.pop())
		if (ext == e) return true;

	throw new golgotha.event.ValidationError('The ' + title + ' must be a ' + extTypes + ' file.', f);
};

golgotha.form.validateCheckBox = function(cb, min, title)
{
if ((!cb) || (!cb.length)) return true;
let cnt = 0;
for (var x = 0; ((x < cb.length) && (cnt < min)); x++)
	if (cb[x].checked) cnt++;

if (cnt >= min) return true;
throw new golgotha.event.ValidationError('At least ' + min + ' ' + title + ' must be selected.', cb[0]);
};

golgotha.util.toggleExpand = function(lnk, className) {
	const isDisplayed = (lnk.innerHTML == 'COLLAPSE');
	lnk.innerHTML = isDisplayed ? 'EXPAND' : 'COLLAPSE';
	const rows = golgotha.util.getElementsByClass(className);
	rows.forEach(function(r) { r.style.display = isDisplayed ? 'none' : ''; });
	return true;
};

golgotha.util.isExpanded = function(className) {
	const rows = golgotha.util.getElementsByClass(className);
	if (rows.length == 0) return false;
	return (rows[0].style.display == '');
};

golgotha.nav.toggleMenu = function(e, force) {
	const nv = document.getElementById('nav');
	if (!golgotha.util.hasClass(nv, 'navside')) return false;
	const sm = document.getElementById('navmenu');
	const m = document.getElementById('main');
	const showMenu = (force != null) ? force : !golgotha.util.hasClass(sm, 'show');
	if (showMenu) {
		golgotha.util.addClass(sm, 'show');
		golgotha.util.addClass(m, 'hide');
	} else {
		golgotha.util.removeClass(sm, 'show');
		golgotha.util.removeClass(m, 'hide');
	}

	return showMenu;
};

golgotha.nav.toggleBar = function(e) {
	const nv = document.getElementById('nav');
	if (!golgotha.util.hasClass(nv, 'navbar')) return false;
	const hdrs = golgotha.util.getElementsByClass('submenuTitle', 'li', document.getElementById('navmenu'));
	for (var h = hdrs.pop(); (h != null); h = hdrs.pop())
		golgotha.util.removeClass(h, 'show');

	return golgotha.util.addClass(e.target, 'show');
};

golgotha.nav.initMenu = function() {
	if (!golgotha.nav.sideMenu) return false;
	const hdrs = golgotha.util.getElementsByClass('mm', null, document.getElementById('header'));
	const w = Math.max(document.documentElement.clientWidth, window.innerWidth || 0)
    for (var hdr = hdrs.pop(); (hdr != null); hdr = hdrs.pop()) {
    	if ((w <= 800) && !hdr.hasMenu) {
    		hdr.hasMenu = true;
    		hdr.addEventListener('click', golgotha.nav.toggleMenu);
    	} else if ((w > 800) && hdr.hasMenu) {
    		hdr.removeEventListener('click', golgotha.nav.toggleMenu);
    		delete hdr.hasMenu;
    	}
    }

    return true;
};

golgotha.nav.initBar = function() {
	const nv = document.getElementById('nav');
	if (!golgotha.util.hasClass(nv, 'navbar')) return false;
	const hdrs = golgotha.util.getElementsByClass('submenuTitle', 'li', document.getElementById('navmenu'));
	for (var h = hdrs.pop(); (h != null); h = hdrs.pop())
		h.addEventListener('click', golgotha.nav.toggleBar);

	return true;
};

golgotha.nav.init = function() {
	if (golgotha.nav.sideMenu) return golgotha.nav.initMenu();
	if (golgotha.nav.touch) return golgotha.nav.initBar();
	return false;
};

golgotha.util.validateCAPTCHA = function(token) {
	const p = fetch('recaptcha.ws', {method:'post', body:token, headers:{'Content-Type':'application/json; charset=utf-8'}, signal:AbortSignal.timeout(5000)});
	p.then(function(rsp) {
		const isOK = ((rsp.status == 200) || (rsp.status == 304));
		if (!isOK) 
			console.log('Error ' + rsp.status + ' validating CAPTCHA!');
		else
			rsp.text();
	});

	return true;
};

golgotha.onDOMReady(function() {
	golgotha.nav.init();
	if (golgotha.nav.sideMenu)
		window.addEventListener('resize', golgotha.nav.initMenu);
});

golgotha.sort.mapRows = function(cName) {
	const data = {};
	const rows = golgotha.util.getElementsByClass(cName, 'tr');
	rows.forEach(function(r) { r.parentNode.removeChild(r); data[r.id] = r; });
	return data;
};

golgotha.sort.exec = function(prefix, t) {
	const rowData = golgotha.sort.mapRows(prefix + 'Data');
	const p = golgotha.sort.lastSort[t] || {type:t,isReverse:false};
	const cmp = function(e1, e2) { return p.isReverse ? (e1[t] - e2[t]) : (e2[t] - e1[t]); };
	const data = golgotha.sort.data[prefix].slice();
	data.sort(cmp);

	// Iterate through the table and add rows
	const pr = document.getElementById(prefix + 'Label');
	data.forEach(function(d) { pr.parentNode.insertBefore(rowData[prefix + '-' + d.id], pr.nextSibling); });

	// Save settings	
	p.isReverse = !p.isReverse;
	golgotha.sort.lastSort[t] = p;
	return true;
};
