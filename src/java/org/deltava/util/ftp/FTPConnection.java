// Copyright 2006, 2007, 2009, 2012, 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.ftp;

import java.io.*;
import java.time.Instant;

import com.enterprisedt.net.ftp.*;

/**
 * A utility class to encapsulate FTP operations.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class FTPConnection implements Closeable {

	private final FTPClient _client = new FTPClient();

	class TempInputStream extends FileInputStream {

		private final File _f;

		TempInputStream(File f) throws FileNotFoundException {
			super(f);
			_f = f;
		}

		/**
		 * Deletes the temporary file on close.
		 */
		@Override
		public void close() throws IOException {
			super.close();
			_f.delete();
		}
	}

	/**
	 * Creates a new FTP connection object.
	 * @param host the remote host name
	 */
	public FTPConnection(String host) {
		super();
		try {
			_client.setTimeout(5000);
			_client.setRemoteHost(host);
		} catch (Exception e) { // empty
		}
	}

	/**
	 * Returns the underlying FTP client object.
	 * @return the client object
	 */
	public FTPClient getClient() {
		return _client;
	}

	/**
	 * Returns if connected to the remote server.
	 * @return TRUE if connected to the server, otherwise FALSE
	 */
	public boolean isConnected() {
		return _client.connected();
	}

	/**
	 * Connects and logs into the remote server.
	 * @param user the user ID
	 * @param pwd the password
	 * @throws FTPClientException if a connection error occurs
	 */
	public void connect(String user, String pwd) throws FTPClientException {
		try {
			_client.connect();
			_client.login(user, pwd);
			_client.setType(FTPTransferType.BINARY);
		} catch (Exception e) {
			throw new FTPClientException(e);
		}
	}

	/**
	 * Closes the connection. This swallows any exceptions.
	 */
	@Override
	public void close() {
		try {
			if (_client.connected())
				_client.quit();
		} catch (Exception e) { // empty
		}
	}

	/**
	 * Downloads a file from the remote server.
	 * @param fName the remote file name
	 * @param saveToDisk TRUE if the file should be saved to a temporary file, otherwise FALSE
	 * @return an InputStream pointing to the file data
	 * @throws FTPClientException if an error occurs
	 */
	public InputStream get(String fName, boolean saveToDisk) throws FTPClientException {
		try {
			if (!saveToDisk)
				return new ByteArrayInputStream(_client.get(fName));

			// Create a temp file
			File tmp = File.createTempFile(fName, "ftp");
			_client.get(new FileOutputStream(tmp), fName);
			return new TempInputStream(tmp);
		} catch (Exception e) {
			throw new FTPClientException(e);
		}
	}

	/**
	 * Downloads a file from the remote server into a specified location.
	 * @param fName the remote file name
	 * @param destFile the destination location
	 * @return an InputStream pointing to the file data
	 * @throws FTPClientException if an error occurs
	 */
	public InputStream get(String fName, File destFile) throws FTPClientException {
		try {
			_client.get(new FileOutputStream(destFile), fName);
			return new FileInputStream(destFile);
		} catch (Exception e) {
			throw new FTPClientException(e);
		}
	}

	/**
	 * Uploads a file to the remote server.
	 * @param f the local File
	 * @throws FTPClientException if an error occurs
	 */
	public void put(File f) throws FTPClientException {
		try {
			_client.put(new FileInputStream(f), f.getName());
		} catch (Exception e) {
			throw new FTPClientException(e);
		}
	}

	/**
	 * Returns whether a particular file exists on the remote server.
	 * @param dirName the remote directory name
	 * @param fName the remote file name
	 * @return TRUE if the file exists, otherwise FALSE
	 * @throws FTPClientException if an error occurs
	 */
	public boolean hasFile(String dirName, String fName) throws FTPClientException {
		try {
			String curPath = _client.pwd();
			_client.chdir(dirName);
			boolean hasFile = _client.existsFile(fName);
			_client.chdir(curPath);
			return hasFile;
		} catch (Exception e) {
			throw new FTPClientException(e);
		}
	}

	/**
	 * Returns the last modified date for a particular file on the remote server.
	 * @param dirName the remote directory name
	 * @param fName the remote file name
	 * @return the last modified date/time, or null if the file does not exist on the remote server
	 * @throws FTPClientException if an error occurs
	 */
	public java.time.Instant getTimestamp(String dirName, String fName) throws FTPClientException {
		try {
			FTPFile[] files = _client.dirDetails(dirName);
			for (int x = 0; x < files.length; x++) {
				FTPFile f = files[x];
				if (f.getName().equals(fName) && !f.isDir())
					return Instant.ofEpochMilli(f.lastModified().getTime());
			}
		} catch (Exception e) {
			throw new FTPClientException(e);
		}

		return null;
	}
	
	/**
	 * Returns the name of the newest directory on the FTP server.
	 * @param dirName the directory name
	 * @param filter a FilenameFilter, or null if none
	 * @return the file name, or null if not found
	 * @throws FTPClientException if an error occurs
	 */
	public String getNewestDirectory(String dirName, FilenameFilter filter) throws FTPClientException {
		try {
			FTPFile[] files = _client.dirDetails(dirName);
			if (files == null)
				return null;
			
			// Iterate through the directories
			FTPFile latest = null;
			for (int x = 0; x < files.length; x++) {
				FTPFile f = files[x];
				if (f.isDir() && !f.isLink()) {
					boolean isOK = (filter == null) || (filter.accept(null, f.getName()));
					if (isOK) {
						if ((latest == null) || (f.lastModified().after(latest.lastModified())))
							latest = f;
					}
				}
			}

			return (latest == null) ? null : latest.getName();
		} catch (Exception e) {
			throw new FTPClientException(e);
		}
	}
	
	/**
	 * Returns the name of the newest file on the FTP server.
	 * @param dirName the directory name
	 * @param filter a FilenameFilter, or null if none
	 * @return the file name, or null if not found
	 * @throws FTPClientException if an error occurs
	 */
	public String getNewest(String dirName, FilenameFilter filter) throws FTPClientException {
		try {
			FTPFile[] files = _client.dirDetails(dirName);
			if (files == null)
				return null;

			// Iterate through the files
			FTPFile latest = null;
			for (int x = 0; x < files.length; x++) {
				FTPFile f = files[x];
				if (!f.isDir() && !f.isLink()) {
					boolean isOK = (filter == null) || (filter.accept(null, f.getName()));
					if (isOK) {
						if ((latest == null) || (f.lastModified().after(latest.lastModified())))
							latest = f;
					}
				}
			}

			return (latest == null) ? null : latest.getName();
		} catch (Exception e) {
			throw new FTPClientException(e);
		}
	}
}