// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.http;

import java.net.*;

/**
 * A factory for creating HTTP connections with socket timeouts.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class HttpTimeoutFactory implements URLStreamHandlerFactory {
	
	private int _timeOut;

	/**
	 * Creates a new HTTP URL stream handler.
	 * @param timeout the timeout in milliseconds
	 */
	public HttpTimeoutFactory(int timeout) {
		super();
		_timeOut = timeout; 
	}

	/**
	 * Creates a new timed HTTP stream handler.
	 * @see URLStreamHandlerFactory#createURLStreamHandler(String)
	 */
	public URLStreamHandler createURLStreamHandler(String protocol) {
		return new HttpTimeoutHandler(_timeOut);
	}
}