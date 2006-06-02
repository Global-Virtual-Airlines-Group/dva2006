// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.http;

import java.io.IOException;

import java.net.*;

import sun.net.www.protocol.http.HttpURLConnection;

/**
 * An HTTP URL connection that implements a connection timeout.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class HttpTimeoutURLConnection extends HttpURLConnection {

	private HttpTimeoutClient _client;
	private HttpTimeoutHandler _hnd;

	/**
	 * @param url
	 * @param hnd
	 * @throws IOException
	 */
	public HttpTimeoutURLConnection(URL url, HttpTimeoutHandler hnd) throws IOException {
		super(url, hnd);
		_hnd = hnd;
		setConnectTimeout(_hnd.getTimeout());
	}

	/**
	 * Returns the underlying TCP/IP socket.
	 * @return the socket
	 */
	public Socket getSocket() {
		return _client.getSocket();
	}

	/**
	 * Closes the connection.
	 * @throws IOException if a network error occurs
	 */
	public void close() throws IOException {
		_client.close();
	}

	/**
	 * Connects to the URL.
	 * @throws IOException if an error occurs, or a timeout
	 */
	public void connect() throws IOException {
		if (connected)
			return;

		if ("http".equals(url.getProtocol())) {
			synchronized (url) {
				http = HttpTimeoutClient.getNew(url);
			}

			_client = (HttpTimeoutClient) http;
			_client.setTimeout(_hnd.getTimeout());
			setReadTimeout(5000);
		} else {
			http = new HttpTimeoutClient(url, _hnd.getProxy(), _hnd.getProxyPort());
		}
		
		connected = true;
	}
}