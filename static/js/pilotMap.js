var queue = [];

function generateXMLRequest()
{
// Build the XML Requester
var f = document.forms[0];
var xmlreq = getXMLHttpRequest();
xmlreq.open('get', 'pilotmap.ws', true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;

	// Parse the XML
	var xmlDoc = xmlreq.responseXML;
	var ac = xmlDoc.documentElement.getElementsByTagName('pilot');
	for (var x = 0; x < ac.length; x++)
		queue.push(ac[x]);

	removeMarkers('allMarkers');
	var batchSize = Math.round(queue.length / 50);
	pBar.start(100);
	setTimeout('mrkLoad(' + batchSize + ')', 2);
	return true;
} // function

var isLoading = document.getElementById('isLoading');
isLoading.innerHTML = ' - LOADING...';
enableElement(f.noFilter, false);
enableElement(f.eqType, false);
enableElement(f.rank, false);
return xmlreq;
}

function mrkLoad(batchSize)
{
var cnt = 0;
var a = queue.pop();
pBar.updateBar(2);
while ((cnt < batchSize) && (a != null)) {
	var label = a.firstChild;
	var p = new google.maps.LatLng(parseFloat(a.getAttribute('lat')), parseFloat(a.getAttribute('lng')));
	var mrk = googleMarker(a.getAttribute('color'), p, null);
	mrk.infoLabel = label.data;
	mrk.rank = a.getAttribute('rank');
	mrk.eqType = a.getAttribute('eqType');
	mrk.ID = parseInt(a.getAttribute('id'));

	// Set the the click handler and add to the list
	google.maps.event.addListener(mrk, 'click', function() { map.infoWindow.setContent(this.infoLabel); map.infoWindow.open(map, this); } );
	allMarkers.push(mrk);
	mrk.setMap(map);
	cnt++;
	if (cnt < batchSize)
		a = queue.pop();
}

if (a != null)
	setTimeout('mrkLoad(' + batchSize + ')', 2);
else {
	var f = document.forms[0];
	var isLoading = document.getElementById('isLoading');
	isLoading.innerHTML = '';
	enableElement(f.noFilter, true);
	enableElement(f.eqType, true);
	enableElement(f.rank, true);
	pBar.hide();
	gaEvent('Pilot Map', 'Load');
}

return true;
}

function updateMarkers()
{
// Get the rank/program values
var f = document.forms[0];
var checkRank = (f.rank.selectedIndex > 0);
var checkEQ = (f.eqType.selectedIndex > 0);
var rank = checkRank ? f.rank.options[f.rank.selectedIndex].text : null;
var eqType = checkEQ ? f.eqType.options[f.eqType.selectedIndex].text : null;

// Build the queue
var batchSize = Math.round(allMarkers.length / 50);
pBar.start(100);
queue = allMarkers.slice();
setTimeout("mrkUpdate('" + rank + "','" + eqType + "'," + batchSize + ")", 2);
return true;
}

function mrkUpdate(rank, eqType, batchSize)
{
var cnt = 0;
var mrk = queue.pop();
pBar.updateBar(2);
while ((cnt < batchSize) && (mrk != null)) {
	var rankOK = (rank == 'null') || (mrk.rank == rank);
	var eqOK = (eqType == 'null') || (mrk.eqType == eqType);

	mrk.setMap((rankOK && eqOK) ? map : null);
	cnt++;
	if (cnt < batchSize)
		mrk = queue.pop();
}

if (mrk != null)
	setTimeout("mrkUpdate('" + rank + "','" + eqType + "'," + batchSize +")", 2);
else {
	pBar.hide();
	gaEvent('Pilot Map', 'Update');	
}

return true;
}
