self.addEventListener('activate', event => { console.log('activate'); event.waitUntil(clients.claim()); });
self.addEventListener('push', event => event.waitUntil(handlePushEvent(event)));
self.addEventListener('notificationclick', function(event) { event.waitUntil(handleClick(event)); });
self.addEventListener('notificationclose', function() { console.info('notificationclose'); });
self.addEventListener('install', function() { console.log('install'); self.skipWaiting(); });

function handlePushEvent(e) {
	console.info('push');
	if (e.data) {
		const js = e.data.json();
		self.registration.showNotification(js.title, {body:js.body, lang:js.lang, icon:js.icon, requireInteraction:js.requireInteraction, data:js, actions:js.actions});
	} else
		self.registration.showNotification('Delta Virtual', {body:'A new message has arrived', icon:'/img/favicon/favicon-32x32.png'});

	return true;
};

function handleClick(e) {
	console.log('click');
	const url = '/' + e.action + '.do';
	const p = clients.matchAll({ includeUncontrolled: true, type: 'window' }).then(function(r) {
		for (const client of r) {
			if (client.url === url) {
				client.focus();
				e.notification.close();
				return true;
			}
		}
		
	  	clients.openWindow(new URL(url, self.location.origin).href);
		e.notification.close();
	});

	return p;
};