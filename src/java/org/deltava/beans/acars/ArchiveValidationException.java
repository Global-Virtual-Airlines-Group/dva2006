// Copyright 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

/**
 * An exception thrown when validating an ACARS position archive. This is an unchecked exception to allow selective handling.
 * @author Luke
 * @version 12.4
 * @since 11.2
 */

public class ArchiveValidationException extends RuntimeException {
	
	private final boolean _fileExists;

	/**
	 * Creates the exception.
	 * @param msg the exception message
	 * @param fileExists TRUE if the archive file exists, otherwise FALSE
	 */
	public ArchiveValidationException(String msg, boolean fileExists) {
		super(msg);
		_fileExists = fileExists;
	}

	/**
	 * Creates the exception from a parent Exception.
	 * @param e the Exception
	 */
	public ArchiveValidationException(Exception e) {
		super(e);
		_fileExists = true;
	}
	
	/**
	 * Returns whether the position archive file exists on disk.
	 * @return TRUE if the file exists, otherwise FALSE
	 */
	public boolean getFileExists() {
		return _fileExists;
	}
}