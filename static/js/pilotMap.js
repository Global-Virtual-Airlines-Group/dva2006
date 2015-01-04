golgotha.pilotMap = golgotha.pilotMap || {queue:[], mrks:[], heatMapData:[]};

golgotha.pilotMap.generateXMLRequest = function()
{
var xmlreq = getXMLHttpRequest();
xmlreq.open('get', 'pilotmap.ws', true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var xmlDoc = xmlreq.responseXML;
	var ac = xmlDoc.documentElement.getElementsByTagName('pilot');
	for (var x = 0; x < ac.length; x++) {
		var a = ac[x];
		a.ll = new google.maps.LatLng(parseFloat(a.getAttribute('lat')), parseFloat(a.getAttribute('lng')));
		golgotha.pilotMap.queue.push(a);
		golgotha.pilotMap.heatMapData.push(a.ll);
	}

	removeMarkers('golgotha.pilotMap.mrks');
	golgotha.pilotMap.hmap.setData(golgotha.pilotMap.heatMapData);
	var batchSize = Math.round(golgotha.pilotMap.queue.length / 50);
	golgotha.pilotMap.pBar.start(100);
	setTimeout('golgotha.pilotMap.load(' + batchSize + ')', 2);
	return true;
};

var f = document.forms[0];
var isLoading = document.getElementById('isLoading');
isLoading.innerHTML = ' - LOADING...';
enableElement(f.noFilter, false);
enableElement(f.eqType, false);
enableElement(f.rank, false);
return xmlreq;
};

golgotha.pilotMap.load = function(batchSize)
{
var cnt = 0;
var a = golgotha.pilotMap.queue.pop();
golgotha.pilotMap.pBar.updateBar(2);
while ((cnt < batchSize) && (a != null)) {
	var label = a.firstChild;
	var mrk = new golgotha.maps.Marker({map:map, color:a.getAttribute('color')}, a.ll);
	mrk.infoLabel = label.data;
	mrk.rank = a.getAttribute('rank');
	mrk.eqType = a.getAttribute('eqType');
	mrk.ID = parseInt(a.getAttribute('id'));

	// Set the the click handler and add to the list
	google.maps.event.addListener(mrk, 'click', function() { map.infoWindow.setContent(this.infoLabel); map.infoWindow.open(map, this); } );
	golgotha.pilotMap.mrks.push(mrk);
	cnt++;
	if (cnt < batchSize)
		a = golgotha.pilotMap.queue.pop();
}

if (a != null)
	setTimeout('golgotha.pilotMap.load(' + batchSize + ')', 2);
else {
	var f = document.forms[0];
	var isLoading = document.getElementById('isLoading');
	isLoading.innerHTML = '';
	enableElement(f.noFilter, true);
	enableElement(f.eqType, true);
	enableElement(f.rank, true);
	golgotha.pilotMap.pBar.hide();
	golgotha.event.beacon('Pilot Map', 'Load');
}

return true;
};

golgotha.pilotMap.updateMarkers = function()
{
// Get the rank/program values
var f = document.forms[0];
var checkRank = (f.rank.selectedIndex > 0);
var checkEQ = (f.eqType.selectedIndex > 0);
var rank = checkRank ? f.rank.options[f.rank.selectedIndex].text : null;
var eqType = checkEQ ? f.eqType.options[f.eqType.selectedIndex].text : null;

// Build the queue
var batchSize = Math.round(golgotha.pilotMap.mrks.length / 50);
golgotha.pilotMap.pBar.start(100);
golgotha.pilotMap.queue = golgotha.pilotMap.mrks.slice();
setTimeout("golgotha.pilotMap.mrkUpdate('" + rank + "','" + eqType + "'," + batchSize + ")", 2);
return true;
};

golgotha.pilotMap.mrkUpdate = function(rank, eqType, batchSize)
{
var cnt = 0;
var mrk = golgotha.pilotMap.queue.pop();
golgotha.pilotMap.pBar.updateBar(2);
while ((cnt < batchSize) && (mrk != null)) {
	var rankOK = (rank == 'null') || (mrk.rank == rank);
	var eqOK = (eqType == 'null') || (mrk.eqType == eqType);

	mrk.setMap((rankOK && eqOK) ? map : null);
	cnt++;
	if (cnt < batchSize)
		mrk = golgotha.pilotMap.queue.pop();
}

if (mrk != null)
	setTimeout("golgotha.pilotMap.mrkUpdate('" + rank + "','" + eqType + "'," + batchSize +")", 2);
else {
	pBar.hide();
	golgotha.event.beacon('Pilot Map', 'Update');	
}

return true;
};

golgotha.pilotMap.updateMapOptions = function(opt)
{
golgotha.event.beacon('Pilot Map', 'Switch Type');
var toggleOpts = {checked:(opt.value != 'MAP')};
toggleMarkers(map, 'golgotha.pilotMap.mrks', toggleOpts);
golgotha.pilotMap.hmap.setMap(toggleOpts.checked ? null : map);
hq.setMap(toggleOpts.checked ? map : null);

// Toggle filter rows
var rows = getElementsByClass('locFilter');
for (var x = 0; x < rows.length; x++)
	displayObject(rows[x], toggleOpts.checked);

return true;
};
