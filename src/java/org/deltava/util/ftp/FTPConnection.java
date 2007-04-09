// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.ftp;

import java.io.*;

import com.enterprisedt.net.ftp.*;

/**
 * A utility class to encapsulate FTP operations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FTPConnection {

	private FTPClient _client;

	class TempInputStream extends FileInputStream {

		private File _f;

		TempInputStream(File f) throws FileNotFoundException {
			super(f);
			_f = f;
		}

		/**
		 * Deletes the temporary file on close.
		 */
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
		_client = new FTPClient();
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
			if (saveToDisk)
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
	 * Returns wether a particular file exists on the remote server.
	 * @param dirName the remote directory name
	 * @param fName the remote file name
	 * @return TRUE if the file exists, otherwise FALSE
	 * @throws FTPClientException if an error occurs
	 */
	public boolean hasFile(String dirName, String fName) throws FTPClientException {
		try {
			FTPFile[] files = _client.dirDetails(dirName);
			for (int x = 0; x < files.length; x++) {
				FTPFile f = files[x];
				if (f.getName().equals(fName) && !f.isDir())
					return true;
			}
		} catch (Exception e) {
			throw new FTPClientException(e);
		}

		return false;
	}

	/**
	 * Returns the last modified date for a particular file on the remote server.
	 * @param dirName the remote directory name
	 * @param fName the remote file name
	 * @return the last modified date/time, or null if the file does not exist on the remote server
	 * @throws FTPClientException if an error occurs
	 */
	public java.util.Date getTimestamp(String dirName, String fName) throws FTPClientException {
		try {
			FTPFile[] files = _client.dirDetails(dirName);
			for (int x = 0; x < files.length; x++) {
				FTPFile f = files[x];
				if (f.getName().equals(fName) && !f.isDir())
					return f.lastModified();
			}
		} catch (Exception e) {
			throw new FTPClientException(e);
		}

		return null;
	}

	/**
	 * Returns the name of the newest file on the FTP server.
	 * @return the file name, or null if not found
	 * @throws FTPClientException if an error occurs
	 */
	public String getNewest(String dirName) throws FTPClientException {
		try {
			FTPFile[] files = _client.dirDetails(dirName);
			if (files == null)
				return null;

			// Iterate through the files
			FTPFile latest = null;
			for (int x = 0; x < files.length; x++) {
				FTPFile f = files[x];
				if (!f.isDir() && !f.isLink()) {
					if ((latest == null) || (f.lastModified().after(latest.lastModified())))
						latest = f;
				}
			}

			return latest.getName();
		} catch (Exception e) {
			throw new FTPClientException(e);
		}
	}
}