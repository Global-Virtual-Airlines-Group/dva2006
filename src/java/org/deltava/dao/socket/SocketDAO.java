// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.socket;

import java.io.*;
import java.net.Socket;

/**
 * An abstract class for Data Access Objects that communciate over a TCP/IP socket. 
 * @author Luke
 * @version 6.1
 * @since 6.1
 */

abstract class SocketDAO implements Closeable {
	
	private Socket _s;
	
	/**
	 * The socket input stream.
	 */
	protected InputStream _inStream;
	
	/**
	 * The socket output stream.
	 */
	protected OutputStream _outStream;
	
	/**
	 * Connects to the destination socket. If already connected, the existing connection is closed.
	 * @param host the host name or IP address
	 * @param port the port number
	 * @throws IOException if an I/O error occurs
	 */
	protected void connect(String host, int port) throws IOException {
		close();
		_s = new Socket(host, port);
		_inStream = _s.getInputStream();
		_outStream = _s.getOutputStream();
	}
	
	/**
	 * Closes the socket connection.
	 * @throws IOException if an error occurs
	 */
	@Override
	public void close() throws IOException {
		if (_s == null) return;
		_s.close();
	}
}