function getPilots()
{
var xmlreq = getXMLHttpRequest();	
xmlreq.open('get', 'sceligible.ws');
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	
	// Parse the XML
	var cbo = getElement('selectPilot');
	cbo.options.length = 1;
	var xmlDoc = xmlreq.responseXML;
	var pe = xmlDoc.documentElement.getElementsByTagName('pilot');
	for (var x = 0; x < pe.length; x++) {
		var p = pe[x];
		var id = p.getAttribute('id');
		var o = new Option(p.getAttribute('name') + ' (' + p.getAttribute('code') + ')', id);
		o.pilotID = id;
		try {
			cbo.add(o, null);
		} catch (err) {
			cbo.add(o); // IE hack
		}
	}
	
	enableObject(cbo, true);
	return true;
} // function
	
enableElement('selectPilot', false);
xmlreq.send(null);

// Initialize onkeyup
var txt = document.forms[0].pilotSearch;
txt.onkeyup = txt.onchange;
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
return true;
}
