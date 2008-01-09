// Copyright 2005, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.deltava.util.StringUtils;

/**
 * A bean to store scrollable view page parameters.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class ViewContext {

    /**
     * Request attribute to store view data for presentation-layer JSPs.
     */
    public static final String VIEW_CONTEXT = "viewContext";
    
    /**
     * Request parameter to store start position.
     */
    public static final String START = "viewStart";
    
    /**
     * Request parameter to store view window size.
     */
    public static final String COUNT = "viewCount";
    
    /**
     * Request parameter to store sort type.
     */
    public static final String SORTBY = "sortType";
    
    /**
     * Reserved request parameter names. 
     */
    private static final String[] RESERVED_PARAMS = {START, COUNT, SORTBY};

    private Map<String, Object> _params;
    private Collection<? extends Object> _results;
    
    private int _start;
    private int _count;
    private String _sortType;
    
    /**
     * Initializes the view context from the HTTP request.
     * @param req the HTTP request
     */
    @SuppressWarnings("unchecked")
    public ViewContext(HttpServletRequest req, int size) {
        super();
        _start = StringUtils.parse(req.getParameter(START), 0);
        _count = StringUtils.parse(req.getParameter(COUNT), size);
        _sortType = req.getParameter(SORTBY);
        
        // Remove the reserved parameters
        _params = new HashMap<String, Object>(req.getParameterMap());
        for (int x = 0; x < ViewContext.RESERVED_PARAMS.length; x++) {
            String rParam = ViewContext.RESERVED_PARAMS[x];
            if (_params.containsKey(rParam))
                _params.remove(rParam);
        }
    }
    
    /**
     * Returns the start of this slice of the view.
     * @return the start position
     * @see ViewContext#getCount()
     * @see ViewContext#getPreviousStart()
     * 
     */
    public int getStart() {
        return _start;
    }
    
    /**
     * Returns the number of entries in the view to display.
     * @return the number of entries
     * @see ViewContext#getStart()
     */
    public int getCount() {
        return _count;
    }
    
    /**
     * Returns the position of the end of this view slice.
     * @return the end position
     */
    public int getEnd() {
        return isEndOfView() ? _results.size() : (_start + _count);
    }
    
    /**
     * Returns all the request parameters for this view slice, minus any reserved parameters
     * @return the parameters to the request
     */
    public Map<String, Object> getParameters() {
        return _params;
    }
    
    /**
     * Returns the result data to display.
     * @return a Collection of beans
     * @see ViewContext#setResults(Collection)
     */
    public Collection getResults() {
        return _results;
    }
    
    /**
     * Gets the sort parameter for this view slice.
     * @return the sort parameter
     * @see ViewContext#setSortType(String)
     */
    public String getSortType() {
        return _sortType;
    }
    
    /**
     * Gets the start position of the previous slice of the view (for the Page Up tag)
     * @return the start position of the previous slice, or zero if negative
     * @see ViewContext#getStart()
     */
    public int getPreviousStart() {
        int tmpResult = _start - _count;
        return (tmpResult < 0) ? 0 : tmpResult;
    }
    
    /**
     * Returns if we are at the end of the view.
     * @return TRUE if no more data is available, otherwise FALSE
     */
    public boolean isEndOfView() {
        return (_results == null) ? false : (_results.size() < _count);
    }
    
    /**
     * Updates the result data for this view slice.
     * @param results a List of beans
     * @see ViewContext#getResults()
     */
    public void setResults(Collection<? extends Object> results) {
        _results = results;
    }
    
    /**
     * Sets the default sort parameter for this view slice.
     * @param sortType the sort parameter
     * @see ViewContext#getSortType()
     */
    public void setSortType(String sortType) {
       _sortType = sortType;
    }
}