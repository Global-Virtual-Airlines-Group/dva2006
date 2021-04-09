golgotha.push = golgotha.push || {};
golgotha.push.hasPush = function() { return ("serviceWorker" in navigator); };
golgotha.push.init = async function() {
	if (!golgotha.push.hasPush()) {
		console.log('Push notification support not detected');
		return false;
	}

	const reg = await navigator.serviceWorker.register('/js/pushWorker.js', { scope: '/' });
	reg.onupdatefound = function() { console.log('Service Worker update found'); };
	await navigator.serviceWorker.ready;
 	if (reg.update)
 		await reg.update();
 
 	await navigator.serviceWorker.ready;
	golgotha.push.isSubscribed = await golgotha.push.check();
	return true;
};

golgotha.push.check = async function() {
	const reg = await navigator.serviceWorker.ready;
  	const sub = await reg.pushManager.getSubscription();
  	if (!sub) return false;

	const po = {endpoint:sub.endpoint};
	const rsp = await fetch('/pushstatus.ws', {method:'post', body:JSON.stringify(po), headers:{"content-type":"application/json"}});
	const js = await rsp.json();
	return ((js) && js.subscribed);
};

golgotha.push.sub = async function() {
	const reg = await navigator.serviceWorker.ready;
	const sub = await reg.pushManager.subscribe({userVisibleOnly: true, applicationServerKey: golgotha.push.pubKey});

	const rsp = await fetch('/pushsub.ws', {method:'post', body:JSON.stringify(sub), haleaders:{"content-type":"application/json"}});
	const js = await rsp.json();
	golgotha.push.isSubscribed = js.isSubscribed;
	golgotha.util.display('pushunsub', js.isSubscribed);
	golgotha.util.display('pushsub', !js.isSubscribed);
	golgotha.util.display('pushclear', true);
};

golgotha.push.unsub = async function() {
	const reg = await navigator.serviceWorker.ready;
	const sub = await reg.pushManager.getSubscription();
	if (!sub) return true;

	await sub.unsubscribe();
	const rsp = await fetch('/pushunsub.ws', {method:'post', body:JSON.stringify(sub), headers:{"content-type":"application/json"}});
	const js = await rsp.json();
	golgotha.push.isSubscribed = !js.isUnsubscribed;
	golgotha.util.display('pushunsub', js.isUnsubscribed);
	golgotha.util.display('pushsub', !js.isUnsubscribed);
	golgotha.util.display('pushclear', (js.cpunt > 0));
};

golgotha.push.clear = function() {
	const xmlreq = new XMLHttpRequest();
	xmlreq.open('post', 'pushclear.ws', true);
	xmlreq.onreadystatechange = function() {
		if (xmlreq.readyState != 4) return false;
		if (xmlreq.status != 200) {
			console.log('Error ' + xmlreq.status + ' clearing push subscriptions');
			return false;
		}

		const js = JSON.parse(xmlreq.responseText);
		golgotha.push.isSubscribed = !js.isUnsubscribed;
		golgotha.util.display('pushunsub', !js.isUnsubscribed);
		golgotha.util.display('pushsub', js.isUnsubscribed);
		golgotha.util.display('pushclear', !js.isUnsubscribed);
		return true;
	};

	xmlreq.send(null);
	return true;
};

golgotha.push.test = async function(doCurrent) {
	const reg = await navigator.serviceWorker.ready;
	const sub = await reg.pushManager.getSubscription();
	if (!sub) return false;

	sub.doCurrent = doCurrent;
	const rsp = await fetch('/pushtest.ws', {method:'post', body:JSON.stringify(sub), headers:{"content-type":"application/json"}});
	const js = await rsp.json();
	console.log('Sent ' + js.sent + '/' + js.size + ' notifications');
	return true;
};