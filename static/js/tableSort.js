golgotha.sort = golgotha.sort || {lastSort:{},data:{}};

golgotha.sort.mapRows = function(cName) {
	const data = {};
	const rows = golgotha.util.getElementsByClass(cName, 'tr');
	rows.forEach(function(r) { r.parentNode.removeChild(r); data[r.id] = r; });
	return data;
};

golgotha.sort.exec = function(prefix, t) {
	const rowData = golgotha.sort.mapRows(prefix + 'Data');
	const p = golgotha.sort.lastSort[prefix] || {type:t,isReverse:false};
	const cmp = function(e1, e2) { return p.isReverse ? (e1[t] - e2[t]) : (e2[t] - e1[t]); };
	const data = golgotha.sort.data[prefix].slice();
	data.sort(cmp);

	// Iterate through the table and add rows
	const pr = document.getElementById(prefix + 'Label');
	data.forEach(function(d) { pr.parentNode.insertBefore(rowData[prefix + '-' + d.id], pr.parentNode.lastChild); });

	// Save settings	
	p.isReverse = !p.isReverse;
	golgotha.sort.lastSort[prefix] = p;
	return true;
};
