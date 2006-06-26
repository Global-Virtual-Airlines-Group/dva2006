// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.http;

import java.net.*;
import java.io.IOException;

import sun.net.www.protocol.http.Handler;

/**
 * A handler for HTTP connections with socket timeouts.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class HttpTimeoutHandler extends Handler {
	
	private int _timeOut;
	private HttpTimeoutURLConnection _con;

	/**
	 * Creates a new timeout handler.
	 * @param timeout the socket timeout in milliseconds
	 */
	public HttpTimeoutHandler(int timeout) {
		super();
		_timeOut = timeout;
	}
	
	/**
	 * Creates a new HTTP URL connection with a timeout.
	 * @param u the URL to connect to
	 * @throws IOException if a network error occurs
	 */
	protected URLConnection openConnection(URL u) throws IOException {
		_con = new HttpTimeoutURLConnection(u, this);
		_con.setReadTimeout(_timeOut);
		return _con;
	}
	
	/**
	 * Returns the underlying TCP/IP socket.
	 * @return the socket
	 */
	public Socket getSocket() {
		return _con.getSocket();
	}
	
	int getTimeout() {
		return _timeOut;
	}
	
	/**
	 * Closes the connection.
	 * @throws IOException if a network error occurs
	 */
	public void close() throws IOException {
		_con.close();
	}

	/**
	 * Returns the HTTP proxy host name.
	 * @return the proxy host
	 */
    public String getProxy() {
    	return proxy;
    }
    
    /**
     * Return the HTTP proxy port.
     * @return the port
     */
    public int getProxyPort() {
    	return proxyPort;
    }
}