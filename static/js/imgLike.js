golgotha.like = golgotha.like || {};

golgotha.like.get = function(id)
{
var xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'imglike.ws?id=' + id + '&time=' + golgotha.util.getTimestamp(3000), true);
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	golgotha.like.parseResponse(JSON.parse(xmlreq.responseText));
	return true;
}
	
xmlreq.send(null);
return true;
}

golgotha.like.exec = function(id)
{
var xmlreq = new XMLHttpRequest();
xmlreq.open('POST', 'imglike.ws', true);
xmlreq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
xmlreq.onreadystatechange = function() {
	if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
	golgotha.like.parseResponse(JSON.parse(xmlreq.responseText));
	return true;
}
	
xmlreq.send('like=true&id=' + id);
return true;	
}

golgotha.like.parseResponse = function(js)
{
if (js.mine) js.likes--;

// If we liked it, hide the link
golgotha.util.show('imgLike', js.canLike);
var totalDiv = document.getElementById('imgLikeTotal');
if (!totalDiv) return false;
var msg = ''
if (js.mine)
	msg += ((js.likes > 0) ? 'You and ' : 'You '); 
else if (js.likes <= 0)
	msg += 'Be the first to ';
if (js.likes > 0) {
	msg += js.likes + ' other member';
	msg += ((js.likes > 1) ? 's ' : ' ');
}

msg += 'like this Image.';
totalDiv.innerHTML = msg;
return true;
}
