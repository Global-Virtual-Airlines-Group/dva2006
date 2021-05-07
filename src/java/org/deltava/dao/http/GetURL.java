// Copyright 2012, 2017, 2018, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.util.Date;

import static javax.servlet.http.HttpServletResponse.*;

import org.apache.commons.httpclient.util.DateUtil;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to download a file via HTTP. 
 * @author Luke
 * @version 9.0
 * @since 5.0
 */

public class GetURL extends DAO {

	private final String _url;
	private final String _outFile;
	
	private boolean _forceDL;
	private long _lastModified = -1;
	
	/**
	 * Initializes the Data Access Object
	 * @param url the URL to download from
	 * @param fileName the file to save the contents to
	 */
	public GetURL(String url, String fileName) {
		super();
		_url = url;
		_outFile = fileName;
	}
	
	/**
	 * Forces file download even if the remote resource has not changed.
	 * @param doForce TRUE if download is forced, otherwise FALSE
	 */
	public void setForce(boolean doForce) {
		_forceDL = doForce;
	}
	
	/**
	 * Updates the timestamp used to send an If-Modified-Since header.
	 * @param dt the epoch timestamp in milliseconds
	 */
	public void setIfModifiedSince(long dt) {
		_lastModified = dt;
	}
	
	/**
	 * Downloads the file.
	 * @return TRUE if a new copy was downloaded, otherwise FALSE
	 * @throws DAOException if an error occurs
	 */
	public File download() throws DAOException {
		File tmp = null;
		try {
			File outF = new File(_outFile);
			if (_lastModified == -1)
				_lastModified = outF.exists() ? outF.lastModified() : -1;
			init(_url);
			
			// If we're not forcing the download and it exists, do a head request to check
			if (!_forceDL && (_lastModified > -1))
				setRequestHeader("If-Modified-Since", DateUtil.formatDate(new Date(_lastModified)));
				
			// Check the status code, if not modified exit out
			int statusCode = getResponseCode();
			if (!_forceDL && (statusCode == SC_NOT_MODIFIED))
				return outF;
			
			// If we're an error, throw a status code exception
			if (statusCode >= SC_BAD_REQUEST)
				throw new HTTPDAOException(_url, statusCode);
			
			// Download the file
			tmp = File.createTempFile("dl-url", "$$$", outF.getParentFile());
			try (InputStream in = getIn(); OutputStream out = new FileOutputStream(tmp)) {
				byte[] buffer = new byte[16384];
				int bytesRead = in.read(buffer);
				while (bytesRead > 0) {
					out.write(buffer, 0, bytesRead);
					bytesRead = in.read(buffer);
				}
			}
			
			if (outF.exists()) outF.delete();
			tmp.renameTo(outF);
			return outF;
		} catch (IOException ie) {
			throw new DAOException(ie);
		} finally {
			reset();
			if (tmp != null) tmp.delete();
		}
	}
	
	/**
	 * Loads a URL into a buffer.
	 * @return the buffer
	 * @throws DAOException if an error occurs
	 */
	public byte[] load() throws DAOException {
		try {
			init(_url);
			
			// If we're not forcing the download and it exists, do a head request to check
			if (!_forceDL && (_lastModified > -1))
				setRequestHeader("If-Modified-Since", DateUtil.formatDate(new Date(_lastModified)));
			
			// Check the status code, if not modified exit out
			int statusCode = getResponseCode();
			if (!_forceDL && (statusCode == SC_NOT_MODIFIED))
				return null;
			
			// If we're an error, throw a status code exception
			if (statusCode >= SC_BAD_REQUEST)
				throw new HTTPDAOException(_url, statusCode);
			
			// Download the file
			try (InputStream in = getIn(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				byte[] buffer = new byte[16384];
				int bytesRead = in.read(buffer);
				while (bytesRead > 0) {
					out.write(buffer, 0, bytesRead);
					bytesRead = in.read(buffer);
				}
				
				return out.toByteArray();
			}
		} catch (IOException ie) {
			throw new DAOException(ie);
		} finally {
			reset();
		}
	}
	
	/**
	 * Checks whether the URL is available for download.
	 * @return TRUE if content is available, otherwise FALSE
	 * @throws DAOException if an unexpected error occurs
	 */
	public boolean isAvailable() throws DAOException {
		try {
			setMethod("GET");
			init(_url);
			return (getResponseCode() == 200); 
		} catch (FileNotFoundException fne) {
			return false;
		} catch (IOException ie) {
			throw new DAOException(ie);
		} finally {
			reset();
		}
	}
}