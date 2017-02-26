// Copyright 2009, 2010, 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.net.*;
import java.util.Base64;

import javax.net.ssl.*;

import org.deltava.beans.system.VersionInfo;

/**
 * An abstract class to supports Data Access Objects that read from an HTTP URL. This differs from a stream-based Data Access Object only 
 * that HTTP DAOs create their own stream to a URL. This is used in situations where request-specific data is encoded into the URL. 
 * @author Luke
 * @version 7.2
 * @since 2.4
 */

public abstract class DAO {
	
	private SSLContext _sslCtxt;
	private String _method = "GET";
	
	private int _readTimeout = 4500;
	private int _connectTimeout = 2500;
	
	private URLConnection _urlcon;
	private boolean _getErrorStream;
	
	/*
	 * Helper to check connection state.
	 */
	private void checkConnected() {
		if (_urlcon == null)
    		throw new IllegalStateException("Not Initialized");
	}
	
    /**
     * Overrides the context used to generate SSL context.
     * @param ctxt the SSLContext
     */
    public void setSSLContext(SSLContext ctxt) {
    	_sslCtxt = ctxt;
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
     * Helper method to open the connection.
     * @param url the URI to connect to
     * @throws IOException if an error occurs
     */
    protected void init(String url) throws IOException {
    	URL u = new URL(url);
    	if (_urlcon != null) {
    		if (!_urlcon.getURL().equals(u))
    			throw new InterruptedIOException("Already connected to " + _urlcon.getURL().toExternalForm());
    	}
    	
    	_urlcon = u.openConnection();
    	if ("https".equals(u.getProtocol()) && (_sslCtxt != null)) {
    		HttpsURLConnection sslcon = (HttpsURLConnection) _urlcon;
    		sslcon.setSSLSocketFactory(_sslCtxt.getSocketFactory());
    	}
    	
    	// Set timeouts and other stuff
		_urlcon.setConnectTimeout(_connectTimeout);
		_urlcon.setReadTimeout(_readTimeout);
		_urlcon.setDefaultUseCaches(false);
		if (_urlcon instanceof HttpURLConnection) {
			HttpURLConnection urlcon = (HttpURLConnection) _urlcon;
			urlcon.setRequestMethod(_method);
			urlcon.setInstanceFollowRedirects(true);
		}
		
		setRequestHeader("User-Agent", VersionInfo.USERAGENT);
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
    	String up = userID + ":" + pwd;
    	Base64.Encoder enc = Base64.getEncoder();
    	StringBuilder buf = new StringBuilder("BASIC ");
    	buf.append(enc.encodeToString(up.getBytes()));
    	setRequestHeader("Authorization", buf.toString());
    }
    
    /**
     * Returns the HTTP response code for this request.
     * @return the response code
     * @throws IOException if an error occured
     */
    protected int getResponseCode() throws IOException {
    	checkConnected();
    	return (_urlcon instanceof HttpURLConnection) ? ((HttpURLConnection)_urlcon).getResponseCode() : 0;
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
    		return _urlcon.getInputStream();
    	} catch (IOException ie) {
    		if (_getErrorStream)
    			return (_urlcon instanceof HttpURLConnection) ? ((HttpURLConnection)_urlcon).getErrorStream() : null;
    			
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
    	if (_urlcon == null)
    		return;
    	
    	if (_urlcon instanceof HttpURLConnection)
    		((HttpURLConnection)_urlcon).disconnect();
    	
   		_urlcon = null;
    }
}