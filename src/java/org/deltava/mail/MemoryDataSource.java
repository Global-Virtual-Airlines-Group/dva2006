// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.mail;

import java.io.*;
import javax.activation.*;

/**
 * A Java Activation Data Source to allow JavaMail Messages to send attachments that have not been persisted
 * to the local filesystem.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MemoryDataSource implements DataSource {

	private String _name;
	private byte[] _buffer;
	
	/**
	 * Creates a new Memory Data Source.
	 * @param name the name of the file
	 * @param buf a byte array containing the file data
	 */
	public MemoryDataSource(String name, byte[] buf) {
		super();
		_name = name.trim();
		_buffer = buf;
	}

	/**
	 * Returns the content type of the file.
	 * @return the file's content-type
	 */
	public String getContentType() {
		FileTypeMap typeMap = FileTypeMap.getDefaultFileTypeMap();
		return typeMap.getContentType(_name);
	}

	/**
	 * Returns an input stream to the file data.
	 * @return an InputStream to the buffer
	 */
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(_buffer);
	}

	/**
	 * Returns the name of the file.
	 * @return the file name
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Returns an output stream to the file. <i>This does not overwrite the file data.</i>
	 * @return an OutputStream
	 */
	public OutputStream getOutputStream() throws IOException {
		return new ByteArrayOutputStream();
	}
}