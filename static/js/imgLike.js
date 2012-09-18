function getLikes(id)
{
var d = new Date();
var dtime = d.getTime() - (d.getTime() % 3000);
var xmlreq = getXMLHttpRequest();
xmlreq.open('get', 'imglike.ws?id=' + id + '&time=' + dtime, true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var xml = xmlreq.responseXML;
	parseResponse(xml.documentElement);
	return true;
}
	
xmlreq.send(null);
return true;
}

function doLike(id)
{
var xmlreq = getXMLHttpRequest();
xmlreq.open('post', 'imglike.ws?like=true&id=' + id, true);
xmlreq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	var xml = xmlreq.responseXML;
	parseResponse(xml.documentElement);
	return true;
}
	
xmlreq.send(null);
return true;	
}

function parseResponse(xe)
{
var total = parseInt(xe.getAttribute('likes'))
var iLike = (xe.getAttribute('mine') == 'true');
var canLike = (xe.getAttribute('canLike') == 'true');
if (iLike) total--;

// If we liked it, hide the link
showObject(document.getElementById('imgLike'), canLike);
var totalDiv = document.getElementById('imgLikeTotal');
if (!totalDiv) return false;
var msg = ''
if (iLike)
	msg += ((total > 0) ? 'You and ' : 'You '); 
else if (total == 0)
	msg += 'Be the first to ';
if (total > 0) {
	msg += total + ' other member';
	msg += ((total > 1) ? 's ' : ' ');
}

msg += 'like this Image.';
totalDiv.innerHTML = msg;
return true;
}
