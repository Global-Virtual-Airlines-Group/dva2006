golgotha.sc = golgotha.sc || {};
golgotha.sc.getPilots = function()
{
const xmlreq = new XMLHttpRequest();	
xmlreq.open('GET', 'sceligible.ws');
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status == 0)) return false;
	if (xmlreq.status != 200) {
		golgotha.util.display('rowError', true);
		golgotha.util.display('rowLoading', false);
		var codeSpan = document.getElementById('errorCode');
		codeSpan.innerHTML = '(' + xmlreq.status + ')';
		return false;
	}

	// Parse the JSON
	const cbo = document.getElementById('selectPilot');
	if (cbo == null) return false;
	cbo.options.length = 1;
	const js = JSON.parse(xmlreq.responseText);
	for (var p = js.pop(); (p != null); p = js.pop()) {
		const o = new Option(p.name + ' (' + p.code + ')', p.id);
		o.pilotID = p.id; o.pilotCode = p.code; o.pilotName = p.name;
		try {
			cbo.add(o, null);
		} catch (err) {
			cbo.add(o); // IE hack
		}
	}

	golgotha.util.display('rowSelectPilot', true);
	golgotha.util.display('rowLoading', false);

	// Initialize onkeyup
	const txt = document.forms[0].pilotSearch;
	if (txt != null) txt.onkeyup = txt.onchange;
	return true;
};
	
golgotha.util.display('rowLoading', true);
golgotha.util.display('rowError', false);
xmlreq.send(null);
return true;
};

golgotha.sc.search = function(searchStr)
{
searchStr = searchStr.toLowerCase();
const combo = document.getElementById('selectPilot');
for (var x = 1; x < combo.options.length; x++) {
	const opt = combo.options[x];
	const txt = opt.text.substring(0, searchStr.length).toLowerCase();
	if (txt == searchStr) {
		combo.selectedIndex = x;
		golgotha.util.display('rowComments', true);
		return true;
	} else if (txt > searchStr) {
		combo.selectedIndex = x;
		golgotha.util.display('rowComments', true);
		return false;
	}
}

return false;
};

golgotha.sc.setPilot = function(combo)
{
const f = document.forms[0];
f.pilotSearch.value = '';
golgotha.util.display('rowComments', (combo.selectedIndex > 0));
return true;
};

golgotha.sc.toggleComments = function(id)
{
const lnk = document.getElementById('tc' + id);
if (lnk == null) return false;
let visible = false;
const rows = golgotha.util.getElementsByClass('nc-' + id);
for (var r = rows.pop(); (r != null); r = rows.pop()) {
	visible = (r.style.display != 'none');
	golgotha.util.display(r, !visible);
}

lnk.innerHTML = visible ? ' + ' : ' - ';
return true;
};

golgotha.sc.toggleAll = function()
{
const lnks = golgotha.util.getElementsByClass('ncToggle');	
for (var l = lnks.pop(); (l != null); l = lnks.pop())
	l.onclick(l.id.substring(2));
	
return true;
};
