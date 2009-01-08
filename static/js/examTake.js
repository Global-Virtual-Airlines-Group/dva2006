function validate(form)
{
if (!checkSubmit()) return false;

// Check if all questions were answered
var isOK = true;
var qNum = 1;
var a = getElementsById('A' + qNum);
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
	a = getElementsById('A' + qNum);
}

if ((!isOK) && (!document.isExpired)) {
	if (!confirm("You have not answered all Questions. Hit OK to submit.")) return false;
}

setSubmit();
disableButton('SubmitButton');
return true;
}

function getAnswer(txtbox)
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
}

function showRemaining(interval)
{
var now = new Date();
var tr = getElement('timeRemaining');
var secondsLeft = (expiry - now.getTime() + timeOffset) / 1000;

// Update the text color
if (secondsLeft < 300)
	tr.className = 'error bld';
else if (secondsLeft < 600)
	tr.className = 'warn bld';

// Display the text and decrement the counter
tr.innerHTML = Math.floor(secondsLeft / 60) + ' minutes ' + Math.round(secondsLeft % 60) + ' seconds';

// If we're out of time, set a flag and submit
if (secondsLeft <= interval) {
	document.isExpired = true;
	document.forms[0].submit();
	return true;
}

// Fire this off again
window.setTimeout('void showRemaining(' + interval + ')', interval * 1000);
return true;
}

function saveAnswer(qNum, id)
{
var txtbox = getElementsById('A' + qNum);
if (!txtbox) return false;
if (txtbox.length == 1) {
	txtbox[0].oldBorder = txtbox[0].style.border;
	txtbox[0].style.border = '1px dashed #787980';
}

// Create the AJAX request
var xmlreq = getXMLHttpRequest();
xmlreq.open('post', 'answer.ws?id=' + id + '&q=' + qNum);
xmlreq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	var time = parseInt(xmlreq.responseText);
	if (!isNaN(time)) secondsLeft = time;
	if (txtbox.length == 1) {
		txtbox[0].style.border = txtbox[0].oldBorder;
		delete txtbox[0].oldBorder;
	}

	return true;
}

// Save the answer
var answer = getAnswer(txtbox);
if (answer != null) {
	xmlreq.send('answer=' + escape(answer));
	gaEvent('Examination', 'Submit Answer');
}

return true;
}

function viewImage(id, x, y)
{
var flags = 'height=' + y + ',width=' + x + ',menubar=no,toolbar=no,status=yes,scrollbars=yes';
var w = window.open('/exam_rsrc/' + id, 'questionImage', flags);
return true;
}

function updateMap(rpq)
{
// Generate an XMLHTTP request
var d = new Date();
var xmlreq = GXmlHttp.create();
xmlreq.open("POST", "examplot.ws?id=" + rpq.examID + "&q=" + rpq.idx + "&date=" + d.getTime(), true);
xmlreq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
var txtbox = getElementsById('A' + rpq.idx);
if (!txtbox) return false;
if (txtbox.length == 1) {
	txtbox[0].oldBorder = txtbox[0].style.border;
	txtbox[0].style.border = '1px dashed #787980';
}
	
//Build the update handler	
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	rpq.map.clearOverlays();

	// Draw the markers and load the codes
	var positions = new Array();
	var codes = new Array();
	var xdoc = xmlreq.responseXML.documentElement;
	var waypoints = xdoc.getElementsByTagName("pos");
	for (var i = 0; i < waypoints.length; i++) {
		var wp = waypoints[i];
		var label = wp.firstChild;
		var p = new GLatLng(parseFloat(wp.getAttribute("lat")), parseFloat(wp.getAttribute("lng")));
		positions.push(p);
		codes.push(wp.getAttribute("code"));
		if (wp.getAttribute("pal"))
			rpq.map.addOverlay(googleIconMarker(wp.getAttribute("pal"), wp.getAttribute("icon"), p, label.data));
		else
			rpq.map.addOverlay(googleMarker(document.imgPath, wp.getAttribute("color"), p, label.data));
	} // for

	// Draw the route
	rpq.map.addOverlay(new GPolyline(positions, '#4080AF', 1.65, 0.8));

	// Save the codes
	if (txtbox.length == 1) {
		txtbox[0].value = codes.join(' ');
		txtbox[0].style.border = txtbox[0].oldBorder;
		delete txtbox[0].oldBorder;
	}

	return true;
}

// Build parameters
xmlreq.send("route=" + escape(getAnswer(txtbox)));
gaEvent('Examination', 'Route Plot');
return true;
}
