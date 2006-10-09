// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.net.*;

/**
 * A URLConnection to allow HTTP-based Data Access Objects to access data sources on the local filesystem.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FileURLConnection extends URLConnection {

	private String _fileName;
	private InputStream _stream;

	/**
	 * Initializes the URL connection.
	 * @param fileName the file to access
	 * @throws MalformedURLException if the filename is invalid
	 */
	public FileURLConnection(String fileName) throws MalformedURLException {
		super(new URL("http://localhost/"));
		_fileName = fileName;
	}

	/**
	 * Creates a connection to the file.
	 * @throws IOException if the file does not exist, or an I/O error occurs
	 */
	public void connect() throws IOException {
		if (!connected) {
			_stream = getClass().getResourceAsStream(_fileName);
			if (_stream == null)
				_stream = new FileInputStream((_fileName.charAt(0) == '/') ? _fileName.substring(1) : _fileName);
		}

		connected = true;
	}

	/**
	 * Retrieves an input stream to the file resource. If we are not connected yet, then the connect() method will be
	 * called.
	 * @return an input stream
	 * @throws IOException if the file does not exist, or an I/O error occurs
	 * @see FileURLConnection#connect()
	 */
	public final InputStream getInputStream() throws IOException {
		if (!connected) connect();

		return _stream;
	}

	/**
	 * Retrieves an output stream to the file resource. <i>NOT SUPPORTED</i>
	 * @throws UnknownServiceException always
	 */
	public final OutputStream getOutputStream() throws IOException {
		throw new UnknownServiceException();
	}
}