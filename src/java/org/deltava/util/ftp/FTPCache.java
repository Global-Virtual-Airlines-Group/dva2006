// Copyright 2006, 2007, 2008, 2012, 2013, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.ftp;

import java.io.*;
import java.time.Instant;
import java.util.zip.*;

import org.apache.log4j.Logger;

import org.deltava.util.*;

/**
 * A utility class to provide cached access to a remote FTP server.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class FTPCache {

	private static final Logger log = Logger.getLogger(FTPCache.class);

	private final String _cachePath;
	private String _host;
	private String _user;
	private String _pwd;

	private FTPDownloadData _fileInfo;

	/**
	 * Initializes the FTP cache bean.
	 * @param path the local cache directory
	 */
	public FTPCache(String path) {
		super();
		_cachePath = path;
	}

	/**
	 * Sets the host name of the remote FTP server.
	 * @param host the FTP server host name
	 */
	public void setHost(String host) {
		_host = host;
	}

	/**
	 * Sets the credentials used to connect to the remote FTP server.
	 * @param user the user ID
	 * @param pwd the password
	 */
	public void setCredentials(String user, String pwd) {
		_user = user;
		_pwd = pwd;
	}

	/**
	 * Returns data about the last file downloaded.
	 * @return an FTPDownloadData bean, or null if no file downloaded
	 */
	public FTPDownloadData getDownloadInfo() {
		return _fileInfo;
	}

	/**
	 * Returns the newest file on the remote server.
	 * @param dirName the directory on the server
	 * @param filter a FilenameFilter or null if none
	 * @return the file name, or null if no files found
	 * @see FTPConnection#getNewest(String, FilenameFilter)
	 */
	public String getNewest(String dirName, FilenameFilter filter) {

		// Init the FTPConnection object
		try (FTPConnection con = new FTPConnection(_host)) {
			con.connect(_user, _pwd);
			log.info("Connected to " + _host);
			String fileName = con.getNewest(dirName, filter); 
			if (fileName != null)
				return fileName;
		} catch (FTPClientException ce) {
			log.error(ce.getMessage() + " to " + _host);
		}

		// Return the newest from the cache if this has an exception
		File f = FileUtils.findNewest(_cachePath, filter);
		if (f == null)
			return null;

		log.info("Newest cache file is " + f.getName());
		return f.getName();
	}

	/**
	 * Checks the cache for a file, and downloads a new copy if not found or the remote copy is newer.
	 * @param fileName the file name
	 * @return an InputStream to the file data
	 * @throws FTPClientException if an error occurs
	 */
	public InputStream getFile(String fileName) throws FTPClientException {

		// Get the local file information
		File cf = new File(_cachePath, fileName);
		Instant ldt = cf.isFile() ? Instant.ofEpochMilli(cf.lastModified()) : null;

		// Init the FTPConnection object
		InputStream is = null;
		try (FTPConnection con = new FTPConnection(_host)) {
			con.connect(_user, _pwd);
			log.info("Connected to " + _host);

			// Check the remote file date
			Instant rdt = con.getTimestamp("", fileName);
			if (rdt == null) {
				if (!cf.exists()) {
					throw new FTPClientException("Cannot find " + fileName + " on local or " + _host);
				}
			
				rdt = Instant.EPOCH;
			}
			
			if ((ldt == null) || (ldt.isBefore(rdt))) {
				TaskTimer tt = new TaskTimer();
				log.info("Downloading " + cf.getName() + ", local=" + ldt + ", remote=" + rdt);
				is = con.get(fileName, cf);
				cf.setLastModified(rdt.toEpochMilli());
				long time = tt.stop();
				log.info("Download Complete. " + is.available() + " bytes, " + time + " ms");
				_fileInfo = new FTPDownloadData(fileName, is.available(), time);
			} else {
				log.info("Using local copy " + cf.getAbsolutePath());
				is = new FileInputStream(cf);
				_fileInfo = new FTPDownloadData(cf);
			}
		} catch (IOException ie) {
			log.warn(ie.getMessage());
		}

		// If we have no input stream, abort
		if (is == null)
			throw new FTPClientException("Cannot download " + fileName);

		// If the file name ends with .zip, then wrap in a ZIP stream
		String fn = fileName.toLowerCase();
		if (fn.endsWith(".zip")) {
			ZipInputStream zis = new ZipInputStream(is);
			try {
				ZipEntry entry = zis.getNextEntry();
				log.info("Detected ZIP File - Returning " + entry.getName());
				return zis;
			} catch (IOException ie) {
				throw new FTPClientException("Error opening ZIP file - " + ie.getMessage());
			}
		} else if (fn.endsWith(".gz")) {
			try {
				log.info("Detected ZIP File - Returning " + fn.substring(0, fn.length() - 3));
				return new GZIPInputStream(is);
			} catch (IOException ie) {
				throw new FTPClientException("Error opening GZIP file - " + ie.getMessage());
			}
		}

		return is;
	}
}