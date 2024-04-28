// Copyright 2009, 2010, 2011, 2012, 2016, 2020, 2021, 2022, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;

import org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStream;
import org.deltava.beans.stats.HTTPCompressionInfo;
import org.deltava.beans.system.VersionInfo;

import org.deltava.util.StringUtils;

/**
 * An abstract class to supports Data Access Objects that read from an HTTP URL. This differs from a stream-based Data Access Object only
 * that HTTP DAOs create their own stream to a URL. This is used in situations where request-specific data is encoded into the URL.
 * @author Luke
 * @version 11.2
 * @since 2.4
 */

abstract class DAO {
	
	private String _method = "GET";
	private final Collection<Compression> _compression = new HashSet<Compression>();
	private Compression _rspCompression = Compression.NONE;
	
	private final HTTPCompressionInfo _stats = HTTPCompressionInfo.get(getClass().getSimpleName()); 
	
	private int _readTimeout = 4500;
	private int _connectTimeout = 2500;

	private URLConnection _urlcon;
	private CountingInputStream _rawStream;
	private boolean _getErrorStream;

	/*
	 * Helper to check connection state.
	 */
	private void checkConnected() {
		if (_urlcon == null)
			throw new IllegalStateException("Not Initialized");
	}
	
	/**
	 * Returns the response compression type.
	 * @return a Compression enumeration value
	 */
	public Compression getCompression() {
		return _rspCompression;
	}
	
	/**
	 * Sets the HTTP method to use if not GET.
	 * @param method the method name
	 */
	public void setMethod(String method) {
		if (method != null)
			_method = method.toUpperCase();
	}

	/**
	 * Sets the HTTP connect timeout.
	 * @param timeout the timeout in milliseconds
	 */
	public void setConnectTimeout(int timeout) {
		_connectTimeout = Math.max(0, timeout);
	}

	/**
	 * Sets the HTTP connect timeout.
	 * @param timeout the timeout in milliseconds
	 */
	public void setReadTimeout(int timeout) {
		_readTimeout = Math.max(0, timeout);
	}

	/**
	 * Sets whether the error stream should be returned when connecting and an error occurs.
	 * @param returnErrStream TRUE if the error stream should be returned, otherwise FALSE
	 * @see DAO#getIn()
	 */
	public void setReturnErrorStream(boolean returnErrStream) {
		_getErrorStream = returnErrStream;
	}
	
	/**
	 * Updates the allowed compression for this request.
	 * @param cmps the Compression values
	 */
	public void setCompression(Compression... cmps) {
		_compression.clear();
		_compression.addAll(Arrays.asList(cmps));
	}

	/**
	 * Helper method to open the connection.
	 * @param url the URI to connect to
	 * @throws IOException if an error occurs
	 */
	protected void init(String url) throws IOException {
		URL u;
		try {
			u = new URI(url).toURL();
			if (_urlcon != null) {
				if (!_urlcon.getURL().equals(u))
					throw new InterruptedIOException(String.format("Already connected to %s", _urlcon.getURL().toExternalForm()));
			}
		} catch (URISyntaxException se) {
			throw new IOException(String.format("Invalid URL - %s", url));
		}
		
		// Set timeouts and other stuff
		_rspCompression = Compression.NONE;
		_urlcon = u.openConnection();
		_urlcon.setConnectTimeout(_connectTimeout);
		_urlcon.setReadTimeout(_readTimeout);
		_urlcon.setDefaultUseCaches(false);
		if (_urlcon instanceof HttpURLConnection urlcon) {
			urlcon.setRequestMethod(_method);
			urlcon.setInstanceFollowRedirects(true);
		}

		setRequestHeader("User-Agent", VersionInfo.getUserAgent());
		setRequestHeader("Accept-Encoding", StringUtils.listConcat(_compression.stream().map(Compression::getEncoding).collect(Collectors.toSet()), ", "));
	}

	/**
	 * Sets a request header.
	 * @param name the header name
	 * @param value the header value
	 */
	protected void setRequestHeader(String name, String value) {
		checkConnected();
		_urlcon.setRequestProperty(name, value);
	}

	/**
	 * Sets an authentication request header.
	 * @param userID the user ID
	 * @param pwd the password
	 */
	protected void setAuthentication(String userID, String pwd) {
		StringBuilder authBuf = new StringBuilder(userID);
		authBuf.append(':').append(pwd);
		Base64.Encoder enc = Base64.getEncoder();
		StringBuilder buf = new StringBuilder("BASIC ");
		buf.append(enc.encodeToString(authBuf.toString().getBytes(StandardCharsets.ISO_8859_1)));
		setRequestHeader("Authorization", buf.toString());
	}

	/**
	 * Returns the HTTP response code for this request.
	 * @return the response code
	 * @throws IOException if an error occured
	 * @throws HTTPDAOException if the read times out, with stack dump disabled
	 */
	protected int getResponseCode() throws IOException, HTTPDAOException {
		checkConnected();
		try {
			return (_urlcon instanceof HttpURLConnection urlcon) ? urlcon.getResponseCode() : 0;
		} catch (SocketTimeoutException se) {
			throw new HTTPDAOException(String.format("Socket timeout - %s (%dms)", _urlcon.getURL().toExternalForm(), Integer.valueOf(_urlcon.getReadTimeout())), 0) {{ setLogStackDump(false); }};
		}
	}

	/**
	 * Returns an HTTP response header.
	 * @param name the header name
	 * @return the header value
	 */
	protected String getHeaderField(String name) {
		checkConnected();
		return _urlcon.getHeaderField(name);
	}

	/**
	 * Retrieves an input stream to the URL.
	 * @return an InputStream to the data
	 * @throws IOException if an error occurs
	 */
	protected InputStream getIn() throws IOException {
		checkConnected();

		try {
			String hdr = _urlcon.getHeaderField("Content-Encoding");
			if (!StringUtils.isEmpty(hdr)) {
				List<String> enc = StringUtils.split(hdr, ", ");
				for (String e : enc)
					_rspCompression = Compression.fromHeader(e);
			}
			
			// Get the InputStream, and wrap it to count bytes
			_rawStream = new CountingInputStream(_urlcon.getInputStream(), _stats::updateRaw);
			InputStream is = switch (_rspCompression) {
			case GZIP -> new GZIPInputStream(_rawStream);
			case DEFLATE -> new InflaterInputStream(_rawStream, new Inflater(true));
			case BROTLI -> new BrotliCompressorInputStream(_rawStream);
			default -> _rawStream;
			};
			
			return new CountingInputStream(is, _stats::updateTotal);
		} catch (IOException ie) {
			if (_getErrorStream)
				return (_urlcon instanceof HttpURLConnection urlcon) ? urlcon.getErrorStream() : null;

			throw ie;
		}
	}

	/**
	 * Retrieves an output stream to the URL.
	 * @return an OutputStream to the data
	 * @throws IOException if an error occurs
	 */
	protected OutputStream getOut() throws IOException {
		checkConnected();
		_urlcon.setDoOutput(true);
		if (_urlcon.getRequestProperty("Content-Type") == null)
			setRequestHeader("Content-Type", "application/x-www-form-urlencoded");

		return _urlcon.getOutputStream();
	}

	/**
	 * Resets the connection for subsequent reuse.
	 */
	public void reset() {
		if (_urlcon == null) return;
		if (_urlcon instanceof HttpURLConnection urlcon) urlcon.disconnect();
		_compression.clear();
		_urlcon = null;
		_rawStream = null;
	}
}