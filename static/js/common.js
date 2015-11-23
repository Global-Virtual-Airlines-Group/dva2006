var golgotha = {event:{}, util:{}, form:{isSubmitted:false, invalidDomains:[]}, local:{}, nav:{sideMenu:false}};
golgotha.util.isIE = (navigator.appName == 'Microsoft Internet Explorer');
golgotha.util.oldIE = (golgotha.util.isIE && ((navigator.appVersion.indexOf('IE 7.0') > 0) || (navigator.appVersion.indexOf('IE 8.0') > 0)));
golgotha.util.isIOS = (!golgotha.util.isIE && ((navigator.platform == 'iPad') || (navigator.platform == 'iPhone')));
golgotha.util.isAndroid = (!golgotha.util.isIE && (navigator.platform.indexOf('Android') > -1));
golgotha.nav.touch = (golgotha.util.isIOS || golgotha.util.isAndroid); 
golgotha.util.getTimestamp = function(ms) { var d = new Date(); return d.getTime() - (d.getTime() % ms); };
golgotha.event.beacon = function() { return false; };
golgotha.event.stop = function(e) { if (e) { e.stopPropagation(); e.preventDefault(); } return false; };
golgotha.event.Error = function(msg, showAlert) { var e = new Error(msg); e.showAlert = showAlert; return e; };
golgotha.event.ValidationError = function(msg, el) { var e = new golgotha.event.Error(msg, true); e.focusElement = el; return e; };

golgotha.util.getElementsByClass = function(cName, eName, parent)
{
if (parent == null) parent = document;
var elements = [];
var all = parent.getElementsByTagName((eName == null) ? '*' : eName);
for (var x = 0; x < all.length; x++) {
	var cl = all[x].className;
	if (cl.split && (cl.split(' ').indexOf(cName) > -1))
		elements.push(all[x]);
}

return elements;
};

golgotha.util.addClass = function(e, cl)
{
	if (!e) return false;
	var c = e.className.split(' ');
	if (c.indexOf(cl) < 0) c.push(cl);
	e.className = (c.length == 0) ? '' : c.join(' ');
	return true;
};

golgotha.util.removeClass = function(e, cl) {
	if (!e) return false;
	var c = e.className.split(' ');
	var hasClass = c.remove(cl);
	e.className = (c.length == 0) ? '' : c.join(' ');
	return hasClass;
};

golgotha.util.hasClass = function(e, cl) {
	if (!e) return false;
	var c = e.className.split(' ');
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

golgotha.util.getStyle = function(sheet, cl) {
	if (golgotha.util.oldIE) return null;
	for (var x = 0; x < document.styleSheets.length; x++) {
		var ss = document.styleSheets[x];
		if ((ss.href == null) || (ss.href.indexOf(sheet) == -1)) continue;
		for (var y = 0; y < ss.cssRules.length; y++) {
			var cs = ss.cssRules[y];
			if ((cs.selectorText) && (cs.style) && (cs.selectorText.indexOf(cl) > -1))
				return cs.style.color;
		}
	}
	
	return null;
};

golgotha.form.resizeAll = function() {
	var boxes = golgotha.util.getElementsByClass('resizable');
	for (var x = 0; x < boxes.length; x++) golgotha.form.resize(boxes[x]);
	return true;
};

golgotha.form.resize = function(textbox)
{
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
	var opt = combo.options[x];
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

golgotha.util.isFunction = function(o) { return !!(o && o.constructor && o.call && o.apply); };
golgotha.util.createScript = function(opts)
{
var url = opts.url;
if (url.substring(0, 2) == '//')
	url = self.location.protocol + url;
else if (url.indexOf('http') != 0)
	url = self.location.protocol + "//" + golgotha.maps.wxHost + url;
	
if (url.indexOf(golgotha.maps.wxHost) > -1) {
	var api = 'api=' + golgotha.maps.keys.api;
	url += (url.indexOf('?') > 0) ? '&' : '?';
	url += api;
}
	
var sc = document.createElement('script');
sc.setAttribute('id', opts.id);
sc.src = url;
if (opts.async) sc.setAttribute('async', 'true');
else if (opts.defer) sc.setAttribute('defer', 'true');

var oldSC = document.getElementById(opts.id);
if (oldSC != null)
	oldSC.parentNode.replaceChild(sc, oldSC);
else
	document.body.appendChild(sc);

return true;
};

golgotha.util.enable = function(n) {
	n = (n instanceof Array) ? n : [n];
	for (var x = 0; x < n.length; x++) {
		var ci = n[x];
		if (ci.style) {
			if (ci.enable) ci.enable();
		} else if (ci.charAt(0) == '#') {
			var ee = golgotha.util.getElementsByClass(ci.substring(1));
			for (var e = ee.pop(); (e != null); e = ee.pop())
				if (e.enable) e.enable();
		} else {
			var e = document.getElementById(ci); 
			if ((e) && (e.enable)) e.enable();
		}
	}
	
	return true; 
};

golgotha.onDOMReady = function(f) {
	if (golgotha.util.oldIE)
		return document.attachEvent('onreadystatechange', f); 
	else
		return document.addEventListener('DOMContentLoaded', f);
};

golgotha.attach = function(f, name) {
};

golgotha.getChild = function(e, name) {
	var children = e.getElementsByTagName(name);
	return (children.length == 0) ? null : children[0];
};

if (window.Element != undefined)
	Element.prototype.getChild = function(name) { return golgotha.getChild(this, name); };

golgotha.getCDATA = function(e)
{
var child = e.firstChild;	
while ((child != null) && (child.nodeType != 4))
	child = child.nextSibling;

return child;
};

if (window.Element != undefined)
	Element.prototype.getCDATA = function() { return golgotha.getCDATA(this); };

// IE9 hack for setTimeout
if (document.all && !window.setTimeout.isPolyfill)
{
	var __nativeST__ = window.setTimeout;
	window.setTimeout = function (vCallback, nDelay /*, argumentToPass1, argumentToPass2, etc. */) {
		var aArgs = Array.prototype.slice.call(arguments, 2);
	    return __nativeST__(vCallback instanceof Function ? function () { vCallback.apply(null, aArgs); } : vCallback, nDelay);
	};

	window.setTimeout.isPolyfill = true;
}

Array.prototype.remove = function(obj) {
for (var x = 0; x < this.length; x++) {
	if (this[x] == obj) {
		this.splice(x, 1);
		return true;
	}
}

return false;
};

if (!Array.prototype.indexOf)
{
	Array.prototype.indexOf = function(obj) {
		for (var x = 0; x < this.length; x++) {	
			if (this[x] == obj)
				return x;
		}

		return -1;
	}
}

Array.prototype.contains = function(obj) { return (this.indexOf(obj) != -1); };
Array.prototype.clone = function() {
var result = [];	
for (var x = 0; x < this.length; x++)
	result.push(this[x]);

return result;
};

golgotha.form.check = function() { return (golgotha.form.isSubmitted != true); };
golgotha.form.submit = function(f) {
	golgotha.form.isSubmitted = true;
	if (f != null) {
		var ies = golgotha.util.getElementsByClass('button', 'input', f);
		for (var e = ies.pop(); (e != null); e = ies.pop())
			e.disabled = true;
	}
	
	var dv = document.getElementById('spinner');
	if (!dv) return true;
	
	// Add background
	var sb = document.createElement('div');
	sb.setAttribute('id', 'spinnerBack');
	document.body.appendChild(sb);
	
	// Add spinner message
	var w = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
	var h = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
	dv.style.top = ((h - 160) / 2) + 'px';
	dv.style.left = ((w - 185) / 2) + 'px';
	dv.style.display = '';
	return true;
};

golgotha.form.clear = function(f) {
	golgotha.form.isSubmitted = false;
	var sb = document.getElementById('spinnerBack');
	if (sb) document.body.removeChild(sb);
	var dv = document.getElementById('spinner');
	if (dv) dv.style.display = 'none';
	if (f != null) {
		var ies = golgotha.util.getElementsByClass('button', 'input', f);
		for (var e = ies.pop(); (e != null); e = ies.pop())
			e.disabled = false;
	}

	return true;
};

golgotha.form.get = function(url) { golgotha.form.submit(); self.location = '/' + url; return true; };
golgotha.form.post = function(url)
{
var f = document.forms[0];
var oldaction = f.action;
f.action = url;
 
// Execute the form validation - if any
if (f.onsubmit) {
	var submitOK = f.onsubmit();
	if (!submitOK) {
		f.action = oldaction;
		return false;
	}
}
  
golgotha.form.submit(f);
f.submit();
return true;
};

golgotha.form.wrap = function(func, f) {
	try {	
		return func(f); 
	} catch (e) {
		if (e.showAlert) alert(e.message);
		else console.log(e);
		if (e.focusElement) e.focusElement.focus();
	}

	return false;
};

golgotha.form.validate = function(opts)
{
if (!('f' in opts) || !('t' in opts)) throw new golgotha.event.ValidationError('Incomplete Validation Data');
if ('ext' in opts) return golgotha.form.validateFile(opts.f, opts.ext, opts.t, opts.empty);
if ('addr' in opts) return golgotha.form.validateEMail(opts.f, opts.t);
if ('l' in opts) return golgotha.form.validateText(opts.f, opts.l, opts.t);
if (!opts.f) return true;
if ('min' in opts) {
	var vf = (0 in opts.f) ? golgotha.form.validateCheckBox : golgotha.form.validateNumber;
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

golgotha.form.validateNumber = function(t, minValue, title)
{
if ((!t) || (t.disabled)) return true;
var i = parseFloat(t.value);
if ((t.value.length < 1) || (i == Number.NaN))
	throw new golgotha.event.ValidationError('Please provide a numeric ' + title + '.', t);
if (i < minValue)
	throw new golgotha.event.ValidationError('The ' + title + ' must be greater than ' + minValue + '.', t);

return true;
};

golgotha.form.validateEMail = function(t, title)
{
if (!golgotha.form.validateText(t, 5, title)) return false;
var pattern = /^[\w](([_\.\-\+]?[\w]+)*)@([\w]+)(([\.-]?[\w]+)*)\.([A-Za-z]{2,})$/;
if (!pattern.test(t.value))
	throw new golgotha.event.ValidationError('Please provide a valid ' + title + '.', t);

// Validate e-mail domain
var addr = t.value;
var usrDomain = addr.substring(addr.indexOf('@') + 1, addr.length);
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

golgotha.form.validateFile = function(f, extTypes, title, allowBlank)
{
if ((!f) || (f.disabled)) return true;
if (allowBlank && (f.value.length == 0)) return true;
var ext = f.value.substring(f.value.lastIndexOf('.') + 1).toLowerCase();
for (var e = extTypes.pop(); (e != null); e = extTypes.pop())
	if (ext == e) return true;

throw new golgotha.event.ValidationError('The ' + title + ' must be a ' + ext.toUpperCase() + ' file.', f);
};

golgotha.form.validateCheckBox = function(cb, min, title)
{
if ((!cb) || (!cb.length)) return true;
var cnt = 0;
for (var x = 0; ((x < cb.length) && (cnt < min)); x++)
	if (cb[x].checked) cnt++;

if (cnt >= min) return true;
throw new golgotha.event.ValidationError('At least ' + min + ' ' + title + ' must be selected.', cb[0]);
};

golgotha.util.toggleExpand = function(lnk, className)
{
var isDisplayed = (lnk.innerHTML == 'COLLAPSE');
lnk.innerHTML = isDisplayed ? 'EXPAND' : 'COLLAPSE';
var rows = golgotha.util.getElementsByClass(className);
for (var r = rows.pop(); (r != null); r = rows.pop())
	r.style.display = isDisplayed ? 'none' : '';

return true;
};

golgotha.nav.toggleMenu = function(e, force) {
	var nv = document.getElementById('nav');
	if (!golgotha.util.hasClass(nv, 'navside')) return false;
	var sm = document.getElementById('navmenu');
	var m = document.getElementById('main');
	var showMenu = (force != null) ? force : !golgotha.util.hasClass(sm, 'show');
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
	var nv = document.getElementById('nav');
	if (!golgotha.util.hasClass(nv, 'navbar')) return false;
	var hdrs = golgotha.util.getElementsByClass('submenuTitle', 'li', document.getElementById('navmenu'));
	for (var h = hdrs.pop(); (h != null); h = hdrs.pop())
		golgotha.util.removeClass(h, 'show');

	return golgotha.util.addClass(e.target, 'show');
};

golgotha.nav.initMenu = function() {
	if (!golgotha.nav.sideMenu) return false;
	var hdr = document.getElementById('hdrLink');
    var w = Math.max(document.documentElement.clientWidth, window.innerWidth || 0)
    if ((w <= 800) && !hdr.hasMenu) {
    	hdr.hasMenu = true;
    	hdr.addEventListener('click', golgotha.nav.toggleMenu);
    } else if ((w > 800) && hdr.hasMenu) {
    	hdr.removeEventListener('click', golgotha.nav.toggleMenu);
    	delete hdr.hasMenu;
    }
    
    return true;
};

golgotha.nav.initBar = function() {
	var nv = document.getElementById('nav');
	if (!golgotha.util.hasClass(nv, 'navbar')) return false;
	var hdrs = golgotha.util.getElementsByClass('submenuTitle', 'li', document.getElementById('navmenu'));
	for (var h = hdrs.pop(); (h != null); h = hdrs.pop())
		h.addEventListener('click', golgotha.nav.toggleBar);

	return true;
};

golgotha.nav.init = function() {
	if (golgotha.nav.sideMenu) return golgotha.nav.initMenu();
	if (golgotha.nav.touch) return golgotha.nav.initBar();
	return false;
};

golgotha.onDOMReady(function() {
	golgotha.nav.init();
	if (golgotha.nav.sideMenu) {
		if (golgotha.util.oldIE)
			document.attachEvent('onresize', golgotha.nav.initMenu);
		else
			window.addEventListener('resize', golgotha.nav.initMenu);
	}
});
