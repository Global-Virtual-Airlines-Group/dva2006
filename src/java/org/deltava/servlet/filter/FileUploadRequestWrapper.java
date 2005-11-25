// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.servlet.filter;

import java.util.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

/**
 * A serlvet request wrapper for File Upload POST requests. This wrapper is created by parsing a multi-part form request; file
 * parts are stored within the request, and this wrapper is used to access the parameter parts by the standard method calls
 * contained within the Java Servlet API. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FileUploadRequestWrapper extends HttpServletRequestWrapper {
   
   private static final Logger log = Logger.getLogger(FileUploadRequestWrapper.class);

    private Map<String, Object> _params;
    
    /**
     * Create a new request wrapper from an existing servlet request.
     * @param req the servlet request
     */
    public FileUploadRequestWrapper(HttpServletRequest req) {
        super(req);
        _params = new HashMap<String, Object>();
    }

    /**
     * Adds a parameter to the request from a multi-part form. If a parameter has multiple values, this method can be called multiple times.
     * @param pName the parameter name
     * @param pValue the parameter value
     */
    void addParameter(String pName, String pValue) {
        if (_params.containsKey(pName)) {
           log.debug("Adding to parameter " + pName);
            List<String> values = new ArrayList<String>(Arrays.asList(getParameterValues(pName)));
            values.add(pValue);
            _params.put(pName, values.toArray(new String[0]));
        } else {
           log.debug("Creating parameter " + pName);
            _params.put(pName, new String[] { pValue } );
        }
    }
    
    void addParameter(String pName, String[] pValues) {
    	if (_params.containsKey(pName)) {
            log.debug("Adding to parameter " + pName);
             List<String> values = new ArrayList<String>(Arrays.asList(getParameterValues(pName)));
             values.addAll(Arrays.asList(pValues));
             _params.put(pName, values.toArray(new String[0]));
    	} else {
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
        return (String[]) _params.get(pName);
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