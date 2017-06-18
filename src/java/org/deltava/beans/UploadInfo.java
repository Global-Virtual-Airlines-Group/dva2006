// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.io.File;
import java.util.*;

import org.deltava.util.cache.Cacheable;

import java.time.Instant;

/**
 * A bean to store resumable upload data.
 * @author Luke
 * @version 7.5
 * @since 7.5
 */

public class UploadInfo implements Cacheable {
	
	private final Collection<Integer> _completedChunks = new TreeSet<Integer>();
	
	private final int _chunkSize;
	private final long _totalSize;
	
	private String _id;
	private String _fileName;
	
	private File _tempPath;
	
	private final Instant _startedOn = Instant.now();

	/**
	 * Creates the bean.
	 * @param chunkSize the chunk size in bytes 
	 * @param totalSize the total upload size in bytes
	 */
	public UploadInfo(int chunkSize, long totalSize) {
		super();
		_chunkSize = chunkSize;
		_totalSize = totalSize;
	}

	/**
	 * Returns the chunk size.
	 * @return the chunk size in bytes
	 */
	public int getChunkSize() {
		return _chunkSize;
	}
	
	/**
	 * Returns the total upload length.
	 * @return the upload length in bytes
	 */
	public long length() {
		return _totalSize;
	}
	
	/**
	 * Returns the upload ID.
	 * @return the ID
	 */
	public String getID() {
		return _id;
	}
	
	/**
	 * Returns the upload file name.
	 * @return the file name
	 */
	public String getFileName() {
		return _fileName;
	}
	
	/**
	 * Returns the temporary storage location.
	 * @return the temp File
	 */
	public File getTempFile() {
		return _tempPath;
	}
	
	/**
	 * Returns the start time of the file upload. 
	 * @return the start date/time
	 */
	public Instant getCreatedOn() {
		return _startedOn;
	}
	
	/**
	 * Returns whether a particular chunk has been uploaded.
	 * @param chunk the chunk number
	 * @return TRUE if uploaded, otherwise FALSE
	 */
	public boolean isComplete(int chunk) {
		return _completedChunks.contains(Integer.valueOf(chunk));
	}
	
	/**
	 * Returns whether all chunks have been uploaded.
	 * @return TRUE if all uploaded, otherwise FALSE
	 */
	public boolean isComplete() {
		int chunks = (int) _totalSize / _chunkSize;
		for (int c = 1; c <= chunks; c++) {
			if (!isComplete(c))
				return false;
		}
		
		return true;
	}
	
	/**
	 * Marks a chunk as complete.
	 * @param chunk the chunk number
	 */
	public void complete(int chunk) {
		_completedChunks.add(Integer.valueOf(chunk));
	}

	/**
	 * Sets the upload ID.
	 * @param id the ID
	 */
	public void setID(String id) {
		_id = id;
	}

	/**
	 * Sets the upload filename.
	 * @param fName the file name
	 */
	public void setFileName(String fName) {
		_fileName = fName;
	}

	/**
	 * Updates the temporary storage location.
	 * @param f the temp File where chunks are written to
	 */
	public void setTempFile(File f) {
		_tempPath = f;
	}

	@Override
	public Object cacheKey() {
		return _id;
	}
}