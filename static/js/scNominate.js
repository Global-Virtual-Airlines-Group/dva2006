function getPilots()
{
var xmlreq = getXMLHttpRequest();	
xmlreq.open('get', 'sceligible.ws');
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status == 0)) return false;
	if (xmlreq.status != 200) {
		displayObject(document.getElementById('rowError'), true);
		displayObject(document.getElementById('rowLoading'), false);
		var codeSpan = document.getElementById('errorCode');
		codeSpan.innerHTML = '(' + xmlreq.status + ')';
		return false;
	}

	// Parse the XML
	var cbo = document.getElementById('selectPilot');
	if (cbo == null) return false;
	cbo.options.length = 1;
	var xmlDoc = xmlreq.responseXML;
	var pe = xmlDoc.documentElement.getElementsByTagName('pilot');
	for (var x = 0; x < pe.length; x++) {
		var p = pe[x];
		var id = p.getAttribute('id');
		var code = p.getAttribute('code');
		var o = new Option(p.getAttribute('name') + ' (' + code + ')', id);
		o.pilotID = id;
		o.pilotCode = code;
		try {
			cbo.add(o, null);
		} catch (err) {
			cbo.add(o); // IE hack
		}
	}

	displayObject(document.getElementById('rowSelectPilot'), true);
	displayObject(document.getElementById('rowLoading'), false);

	// Initialize onkeyup
	var txt = document.forms[0].pilotSearch;
	if (txt != null) txt.onkeyup = txt.onchange;
	return true;
} // function
	
displayObject(document.getElementById('rowLoading'), true);
displayObject(document.getElementById('rowError'), false);
xmlreq.send(null);
return true;
}

function search(searchStr)
{
searchStr = searchStr.toLowerCase();
var combo = document.getElementById('selectPilot');
for (var x = 1; x < combo.options.length; x++) {
	var opt = combo.options[x];
	var txt = opt.text.substring(0, searchStr.length).toLowerCase();
	if (txt == searchStr) {
		combo.selectedIndex = x;
		displayObject(document.getElementById('rowComments'), true);
		return true;
	} else if (txt > searchStr) {
		combo.selectedIndex = x;
		displayObject(document.getElementById('rowComments'), true);
		return false;
	}
}

return false;
}

function setPilot(combo)
{
var f = document.forms[0];
f.pilotSearch.value = '';
displayObject(document.getElementById('rowComments'), (combo.selectedIndex > 0));
return true;
}

function toggleComments(id)
{
var lnk = document.getElementById('tc' + id);
if (lnk == null) return false;

var visible = false;
var rows = getElementsByClass('nc-' + id);
for (var x = 0; x < rows.length; x++) {
	var row = rows[x];
	visible = (row.style.display != 'none');
	displayObject(row, !visible);
}

lnk.innerHTML = visible ? ' + ' : ' - ';
return true;
}

function toggleAll()
{
var lnks = getElementsByClass('ncToggle');	
for (var x = 0; x < lnks.length; x++) {
	var lnk = lnks[x];
	var linkID = lnk.id.substring(2);
	lnk.onclick(linkID);
}
	
return true;
}
