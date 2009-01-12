// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 2.4
 * @since 2.4
 */

public abstract class DAO {
	
	private SSLContext _sslCtxt;
	private String _method = "GET";
	
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
     * Opens a connection to a URL and returns a stream to the data.
     * @param url the URL to connect to
     * @return an InputStream to the data
     * @throws IOException if an error occurs
     */
    protected InputStream getStream(String url) throws IOException {
    	URL uri = new URL(url);
    	HttpURLConnection urlcon = (HttpURLConnection) uri.openConnection();
    	if ("https".equals(uri.getProtocol()) && (_sslCtxt != null)) {
    		HttpsURLConnection sslcon = (HttpsURLConnection) urlcon;
    		sslcon.setSSLSocketFactory(_sslCtxt.getSocketFactory());
    	}
    		
    	// Set timeouts
		urlcon.setConnectTimeout(2500);
		urlcon.setReadTimeout(4500);
		urlcon.setRequestMethod(_method);

		// Open the connection
		return urlcon.getInputStream();
    }
}