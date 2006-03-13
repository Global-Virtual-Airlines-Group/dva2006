// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.http;

import java.net.*;
import java.io.IOException;

import sun.net.www.http.HttpClient;

/**
 * An HTTP client class supporting timed out socket connections.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class HttpTimeoutClient extends HttpClient {

	/**
	 * Creates a new HTTP client.
	 * @param url the URL to connect to
	 * @throws IOException if a network error occurs
	 */
	public HttpTimeoutClient(URL url) throws IOException {
		this(url, null, -1);
	}

	/**
	 * Creates a new HTTP client.
	 * @param url the URL to connect to
	 * @param proxyHost the proxy server host
	 * @param proxyPort the proxy server port
	 * @throws IOException if a network error occurs
	 */
	public HttpTimeoutClient(URL url, String proxyHost, int proxyPort) throws IOException {
		super(url, proxyHost, proxyPort);
	}
	
	/**
	 * Returns a new URL connection client. If there is currently an active client for a particular URL, it
	 * will be returned.
	 * @param url the URL to connect to
	 * @return the Http Client
	 * @throws IOException if a network error occurs
	 */
	public static HttpTimeoutClient getNew(URL url) throws IOException {
		
		// See if there's an existing connection
		HttpTimeoutClient c = (HttpTimeoutClient) kac.get(url);
		if (c == null) {
		    c = new HttpTimeoutClient(url);
		} else {
		    c.url = url;
		}
		
		return c;
	}
	
	/**
	 * Sets the socket timeout.
	 * @param timeout the timeout in milliseconds
	 * @throws SocketException if a network error occurs
	 */
	public int setTimeout(int timeout) throws SocketException { 
    	serverSocket.setSoTimeout(timeout);
    	return 0;
	}
	
	/**
	 * Returns the underlying TCP/IP socket.
	 * @return the socket
	 */
	public Socket getSocket() {
		return serverSocket;
	}
	
	/**
	 * Closes the connection.
	 * @throws IOException if an error occurs
	 */
	public void close() throws IOException {
		serverSocket.close();
	}
}