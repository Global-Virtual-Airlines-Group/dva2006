// Copyright 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.net.*;

import javax.net.ssl.*;

/**
 * An abstract class to supports Data Access Objects that read from an HTTP
 * URL. This differs from a stream-based Data Access Object only that HTTP
 * DAOs create their own stream to a URL. This is used in situations where
 * request-specific data is encoded into the URL. 
 * @author Luke
 * @version 3.4
 * @since 2.4
 */

public abstract class DAO {
	
	private SSLContext _sslCtxt;
	private String _method = "GET";
	
	private HttpURLConnection _urlcon;
	
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
     * Helper method to open the connection.
     * @param url the URI to connect to
     * @throws IOException if an error occurs
     */
    protected void init(String url) throws IOException {
    	if (_urlcon != null)
    		return;
    	
    	URL u = new URL(url);
    	_urlcon = (HttpURLConnection) u.openConnection();
    	if ("https".equals(u.getProtocol()) && (_sslCtxt != null)) {
    		HttpsURLConnection sslcon = (HttpsURLConnection) _urlcon;
    		sslcon.setSSLSocketFactory(_sslCtxt.getSocketFactory());
    	}
    	
    	// Set timeouts and other stuff
		_urlcon.setConnectTimeout(2500);
		_urlcon.setReadTimeout(4500);
		_urlcon.setRequestMethod(_method);
		_urlcon.setDefaultUseCaches(false);
    }
    
    /**
     * Returns the HTTP response code for this request.
     * @return the response code
     * @throws IOException if an error occured
     */
    protected int getResponseCode() throws IOException {
    	if (_urlcon == null)
    		throw new IllegalStateException("Not Initialized");
    	
    	return _urlcon.getResponseCode();
    }

    /**
     * Retrieves an input stream to the URL.
     * @return an InputStream to the data
     * @throws IOException if an error occurs
     */
    protected InputStream getIn() throws IOException {
    	if (_urlcon == null)
    		throw new IllegalStateException("Not Initialized");
    	
		return _urlcon.getInputStream();
    }
    
    /**
     * Retrieves an output stream to the URL.
     * @return an OutputStream to the data
     * @throws IOException if an error occurs
     */
    protected OutputStream getOut() throws IOException {
    	if (_urlcon == null)
    		throw new IllegalStateException("Not Initialized");
    	
    	_urlcon.setDoOutput(true);
    	_urlcon.setRequestProperty ("Content-Type", "application/x-www-form-urlencoded");
		return _urlcon.getOutputStream();
    }
}