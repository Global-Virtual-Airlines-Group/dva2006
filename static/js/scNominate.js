function getPilots()
{
var xmlreq = getXMLHttpRequest();	
xmlreq.open('get', 'sceligible.ws');
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status == 0)) return false;
	if (xmlreq.status != 200) {
		displayObject(getElement('rowError'), true);
		displayObject(getElement('rowLoading'), false);
		var codeSpan = getElement('errorCode');
		codeSpan.innerHTML = '(' + xmlreq.status + ')';
		return false;
	}

	// Parse the XML
	var cbo = getElement('selectPilot');
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

	displayObject(getElement('rowSelectPilot'), true);
	displayObject(getElement('rowLoading'), false);

	// Initialize onkeyup
	var txt = document.forms[0].pilotSearch;
	if (txt != null) txt.onkeyup = txt.onchange;
	return true;
} // function
	
displayObject(getElement('rowLoading'), true);
displayObject(getElement('rowError'), false);
xmlreq.send(null);
return true;
}

function search(searchStr)
{
searchStr = searchStr.toLowerCase();
var combo = getElement('selectPilot');
for (var x = 1; x < combo.options.length; x++) {
	var opt = combo.options[x];
	var txt = opt.text.substring(0, searchStr.length).toLowerCase();
	if (txt == searchStr) {
		combo.selectedIndex = x;
		return true;
	} else if (txt > searchStr) {
		combo.selectedIndex = x;
		return false;
	}
}

return false;
}

function setPilot(combo)
{
var f = document.forms[0];
f.pilotSearch.value = '';
displayObject(getElement('rowComments'), (combo.selectedIndex > 0));
return true;
}
