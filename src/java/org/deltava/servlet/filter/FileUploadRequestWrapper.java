// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.filter;

import java.util.*;

import javax.servlet.http.*;

import org.apache.log4j.Logger;

/**
 * A serlvet request wrapper for File Upload POST requests. This wrapper is created by parsing a multi-part form
 * request; file parts are stored within the request, and this wrapper is used to access the parameter parts by the
 * standard method calls contained within the Java Servlet API.
 * @author Luke
 * @version 2.0
 * @since 1.0
 */

public class FileUploadRequestWrapper extends HttpServletRequestWrapper {

	private static final Logger log = Logger.getLogger(FileUploadRequestWrapper.class);

	private final Map<String, String[]> _params = new HashMap<String, String[]>();

	/**
	 * Create a new request wrapper from an existing servlet request.
	 * @param req the servlet request
	 */
	public FileUploadRequestWrapper(HttpServletRequest req) {
		super(req);
	}

	/**
	 * Adds a parameter to the request from a multi-part form. If a parameter has multiple values, this method can be
	 * called multiple times.
	 * @param pName the parameter name
	 * @param pValues the parameter values
	 */
	void addParameter(String pName, String... pValues) {
		
		// Add the parameter
		if (_params.containsKey(pName)) {
			if (log.isDebugEnabled())
				log.debug("Adding to parameter " + pName);
			
			List<String> values = new ArrayList<String>(Arrays.asList(getParameterValues(pName)));
			values.addAll(Arrays.asList(pValues));
			_params.put(pName, values.toArray(new String[0]));
		} else {
			if (log.isDebugEnabled())
				log.debug("Creating parameter " + pName);
			
			_params.put(pName, pValues);
		}
	}

	public final String getParameter(String pName) {
		String[] values = getParameterValues(pName);
		return (values == null) ? null : values[0];
	}

	/**
	 * Returns the value(s) of a request parameter.
	 * @return the parameter value(s)
	 */
	public final String[] getParameterValues(String pName) {
		return _params.get(pName);
	}

	/**
	 * Returns the parameters as a map of String arrays.
	 * @return a Map of String[] arrays
	 */
	public final Map getParameterMap() {
		return _params;
	}

	/**
	 * Returns all parameter names.
	 * @return an Enumeration of parameter names
	 */
	public final Enumeration getParameterNames() {
		return Collections.enumeration(_params.keySet());
	}
}