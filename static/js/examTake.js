golgotha.exam = golgotha.exam || {isExpired:false, rpInfo:[]};

golgotha.local.validate = function(f)
{
if (!checkSubmit()) return false;

// Check if all questions were answered
var isOK = true;
var qNum = 1;
var a = golgotha.util.getElementsById('A' + qNum);
while (isOK && (a.length > 0)) {
	if (a.length == 1)
		isOK = (isOK && (a[0].value.length > 1));
	else {
		var checkCount = 0;
		for (var x = 0; x < a.length; x++) {
			if (a[x].checked)
				checkCount++;
		}

		isOK = (isOK && (checkCount > 0));
	}

	qNum++;
	a = golgotha.util.getElementsById('A' + qNum);
}

if ((!isOK) && (!document.isExpired)) {
	if (!confirm('You have not answered all Questions. Hit OK to submit.')) return false;
}

golgotha.form.submit();
disableButton('SubmitButton');
return true;
};

golgotha.exam.getAnswer = function(txtbox)
{
if ((txtbox.length == 1) && (txtbox[0].value.length > 1))
	return txtbox[0].value;
else if (txtbox.length == 1)
	return null;

for (var x = 0; x < txtbox.length; x++) {
	if (txtbox[x].checked)
		return txtbox[x].value;
}
	
return null;
};

golgotha.exam.showRemaining = function(interval)
{
var now = new Date();
var tr = document.getElementById('timeRemaining');
var secondsLeft = (golgotha.exam.expiry - now.getTime() + golgotha.exam.timeOffset) / 1000;
if (!tr) return false;

// Update the text color
if (secondsLeft < 300)
	tr.className = 'error bld';
else if (secondsLeft < 600)
	tr.className = 'warn bld';

// Display the text and decrement the counter
tr.innerHTML = Math.floor(secondsLeft / 60) + ' minutes ' + Math.round(secondsLeft % 60) + ' seconds';

// If we're out of time, set a flag and submit
if (secondsLeft <= interval) {
	golgotha.exam.isExpired = true;
	document.forms[0].submit();
	return true;
}

return window.setTimeout('void golgotha.exam.showRemaining(' + interval + ')', interval * 1000);
};

golgotha.exam.saveAnswer = function(qNum, id)
{
var txtbox = golgotha.util.getElementsById('A' + qNum);
if (!txtbox) return false;
if (txtbox.length == 1) {
	txtbox[0].oldBorder = txtbox[0].style.border;
	txtbox[0].style.border = '1px dashed #787980';
}

// Create the AJAX request
var xmlreq = new XMLHttpRequest();
xmlreq.open('POST', 'answer.ws?id=' + id + '&q=' + qNum + '&date=' + golgotha.util.getTimestamp(100));
xmlreq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var time = parseInt(xmlreq.responseText);
	if (!isNaN(time)) secondsLeft = time;
	if (txtbox.length == 1) {
		txtbox[0].style.border = txtbox[0].oldBorder;
		delete txtbox[0].oldBorder;
	}

	return true;
};

// Save the answer
var answer = golgotha.exam.getAnswer(txtbox);
if (answer != null) {
	xmlreq.send('answer=' + escape(answer));
	golgotha.event.beacon('Examination', 'Submit Answer');
}

return true;
};

golgotha.exam.viewImage = function(id, x, y)
{
var flags = 'height=' + (y+45) + ',width=' + (x+45) + ',menubar=no,toolbar=no,status=yes,scrollbars=yes';
return window.open('/exam_rsrc/' + id, 'questionImage', flags);
};

golgotha.exam.updateMap = function(rpq)
{
var xmlreq = new XMLHttpRequest();
xmlreq.open('POST', 'examplot.ws?id=' + rpq.examID + '&q=' + rpq.idx + '&date=' + golgotha.util.getTimestamp(100), true);
xmlreq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
var txtbox = golgotha.util.getElementsById('A' + rpq.idx);
if (!txtbox) return false;
if (txtbox.length == 1) {
	txtbox[0].oldBorder = txtbox[0].style.border;
	txtbox[0].style.border = '1px dashed #787980';
}
	
// Build the update handler	
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	rpq.map.clearOverlays();

	// Draw the markers and load the codes
	var positions = [];
	var codes = [];
	var xdoc = xmlreq.responseXML.documentElement;
	var waypoints = xdoc.getElementsByTagName('pos');
	for (var i = 0; i < waypoints.length; i++) {
		var wp = waypoints[i];
		var label = wp.firstChild;
		var p = {lat:parseFloat(wp.getAttribute('lat')), lng:parseFloat(wp.getAttribute('lng'))};
		positions.push(p);
		codes.push(wp.getAttribute('code'));
		var mrk = null;
		if (wp.getAttribute('pal'))
			mrk = new golgotha.maps.IconMarker({pal:wp.getAttribute('pal'), icon:wp.getAttribute('icon'), info:label.data}, p);
		else
			mrk = new golgotha.maps.Marker({color:wp.getAttribute('color'), info:label.data}, p);

		mrk.setMap(rpq.map);
	}

	// Draw the route
	var rt = new google.maps.Polyline({path:positions, strokeColor:'#4080af', strokeWeight:1.65, strokeOpacity:0.8, geodesic:true, zIndex:golgotha.maps.z.POLYLINE});
	rt.setMap(rpq.map);

	// Save the codes
	if (txtbox.length == 1) {
		txtbox[0].value = codes.join(' ');
		txtbox[0].style.border = txtbox[0].oldBorder;
		delete txtbox[0].oldBorder;
	}

	return true;
};

xmlreq.send('route=' + escape(getAnswer(txtbox)));
golgotha.event.beacon('Examination', 'Route Plot');
return true;
};
