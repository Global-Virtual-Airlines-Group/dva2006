// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.ftp;

import java.io.File;

/**
 * A bean to store FTP download data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FTPDownloadData {
	
	private String _fileName;
	private long _size;
	private long _time;
	private boolean _cached;

	/**
	 * Creates a new FTP download entry for a downloaded file.
	 * @param fileName the file name
	 * @param size the file size
	 * @param time the download time in milliseconds
	 */
	FTPDownloadData(String fileName, long size, long time) {
		super();
		_fileName = fileName;
		_size = size;
		_time = (time < 1) ? 1 : time;
	}
	
	/**
	 * Creates a new FTP download entry from a cached local file.
	 * @param localFile the local file
	 */
	FTPDownloadData(File localFile) {
		super();
		_fileName = localFile.getName();
		_size = localFile.length();
		_cached = true;
		_time = 1;
	}

	/**
	 * Returns the file size.
	 * @return the size in bytes
	 */
	public long getSize() {
		return _size;
	}
	
	/**
	 * Returns the total download time. 
	 * @return the time in milliseconds
	 */
	public long getDownloadTime() {
		return _time;
	}
	
	/**
	 * Returns the download speed.
	 * @return the speed in bytes per second
	 */
	public int getSpeed() {
		return (int) (_size / _time * 1000);
	}
	
	/**
	 * Returns the name of the file.
	 * @return the file name
	 */
	public String getFileName() {
		return _fileName;
	}
	
	/**
	 * Returns wether the file was downloaded, or is a cached local copy.
	 * @return TRUE if the file was cached on the local filesystem, otherwise FALSE
	 */
	public boolean isCached() {
		return _cached;
	}
}