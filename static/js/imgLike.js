golgotha.like = golgotha.like || {};

golgotha.like.get = function(id)
{
var xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'imglike.ws?id=' + id + '&time=' + golgotha.util.getTimestamp(3000), true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	golgotha.like.parseResponse(xmlreq.responseXML.documentElement);
	return true;
}
	
xmlreq.send(null);
return true;
}

golgotha.like.exec = function(id)
{
var xmlreq = new XMLHttpRequest();
xmlreq.open('POST', 'imglike.ws?like=true&id=' + id, true);
xmlreq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	golgotha.like.parseResponse(xmlreq.responseXML.documentElement);
	return true;
}
	
xmlreq.send(null);
return true;	
}

golgotha.like.parseResponse =function(xe)
{
var total = parseInt(xe.getAttribute('likes'))
var iLike = (xe.getAttribute('mine') == 'true');
var canLike = (xe.getAttribute('canLike') == 'true');
if (iLike) total--;

// If we liked it, hide the link
golgotha.util.show('imgLike', canLike);
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
