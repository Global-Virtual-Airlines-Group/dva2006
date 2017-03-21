golgotha.pilotMap = golgotha.pilotMap || {queue:[], mrks:[], heatMapData:[]};

golgotha.pilotMap.generateXMLRequest = function()
{
var xmlreq = new XMLHttpRequest();
xmlreq.open('get', 'pilotmap.ws', true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var js = JSON.parse(xmlreq.responseText);
	js.forEach(function(p) { 
		golgotha.pilotMap.queue.push(p);
		golgotha.pilotMap.heatMapData.push(new google.maps.LatLng(p.ll.lat, p.ll.lng));
	});

	map.removeMarkers(golgotha.pilotMap.mrks);
	golgotha.pilotMap.hmap.setData(golgotha.pilotMap.heatMapData);
	var batchSize = Math.round(golgotha.pilotMap.queue.length / 50);
	golgotha.pilotMap.pBar.start(100);
	window.setTimeout(golgotha.pilotMap.load, 2, batchSize);
	return true;
};

var f = document.forms[0];
var isLoading = document.getElementById('isLoading');
isLoading.innerHTML = ' - LOADING...';
golgotha.util.disable(f.noFilter);
golgotha.util.disable(f.eqType);
golgotha.util.disable(f.rank);
return xmlreq;
};

golgotha.pilotMap.load = function(batchSize)
{
var cnt = 0;
var a = golgotha.pilotMap.queue.pop();
golgotha.pilotMap.pBar.updateBar(2);
while ((cnt < batchSize) && (a != null)) {
	var mrk = new golgotha.maps.Marker({map:map, color:a.color}, a.ll);
	mrk.infoLabel = a.info; mrk.ID = a.id; mrk.rank = a.rank; mrk.eqType = a.eqType;

	// Set the the click handler and add to the list
	google.maps.event.addListener(mrk, 'click', function() { map.infoWindow.setContent(this.infoLabel); map.infoWindow.open(map, this); } );
	golgotha.pilotMap.mrks.push(mrk);
	cnt++;
	if (cnt < batchSize)
		a = golgotha.pilotMap.queue.pop();
}

if (a != null)
	window.setTimeout(golgotha.pilotMap.load, 2, batchSize);
else {
	var f = document.forms[0];
	var isLoading = document.getElementById('isLoading');
	isLoading.innerHTML = '';
	golgotha.util.disable(f.noFilter, false);
	golgotha.util.disable(f.eqType, false);
	golgotha.util.disable(f.rank, false);
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
window.setTimeout(golgotha.pilotMap.mrkUpdate, 2, rank, eqType, batchSize);
return true;
};

golgotha.pilotMap.mrkUpdate = function(rank, eqType, batchSize)
{
var cnt = 0;
var mrk = golgotha.pilotMap.queue.pop();
golgotha.pilotMap.pBar.updateBar(2);
while ((cnt < batchSize) && (mrk != null)) {
	var rankOK = (rank == null) || (mrk.rank == rank);
	var eqOK = (eqType == null) || (mrk.eqType == eqType);

	mrk.setMap((rankOK && eqOK) ? map : null);
	cnt++;
	if (cnt < batchSize)
		mrk = golgotha.pilotMap.queue.pop();
}

if (mrk != null)
	window.setTimeout(golgotha.pilotMap.mrkUpdate, 2, rank, eqType, batchSize);
else {
	golgotha.pilotMap.pBar.hide();
	golgotha.event.beacon('Pilot Map', 'Update');	
}

return true;
};

golgotha.pilotMap.updateMapOptions = function(opt)
{
golgotha.event.beacon('Pilot Map', 'Switch Type');
var isHeatMap = (opt.value != 'MAP');
map.toggle(golgotha.pilotMap.mrks, isHeatMap);
golgotha.pilotMap.hmap.setMap(isHeatMap ? null : map);
hq.setMap(isHeatMap ? map : null);

// Toggle filter rows
var rows = golgotha.util.getElementsByClass('locFilter');
rows.forEach(function(r) { golgotha.util.display(r, isHeatMap); }); 
return true;
};
