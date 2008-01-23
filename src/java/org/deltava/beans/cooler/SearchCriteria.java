// Copyright 2005, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.cooler;

import java.util.*;

/**
 * A bean to store Water Cooler search criteria.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class SearchCriteria {

	private String _searchStr;
	private String _channel;
	private Date _threadDate;
	
	private boolean _doSubject;
	private boolean _doNameFragment;
	
	private final Collection<Integer> _ids = new LinkedHashSet<Integer>();
	
	/**
	 * Creates a new search criteria bean.
	 * @param searchStr the search string 
	 */
	public SearchCriteria(String searchStr) {
		super();
		_searchStr = searchStr;
	}

	/**
	 * Returns the channel to search in.
	 * @return the channel name
	 */
	public String getChannel() {
		return _channel;
	}
	
	/**
	 * Returns the earliest thread update date to include.
	 * @return the minimum thread last update date/time
	 */
	public Date getMinimumDate() {
		return _threadDate;
	}
	
	/**
	 * Returns the search string.
	 * @return the search string
	 */
	public String getSearchTerm() {
		return _searchStr;
	}
	
	/**
	 * Returns whether thread subjects should also be searched.
	 * @return TRUE if subjects should be searched, otherwise FALSE
	 */
	public boolean getSearchSubject() {
		return _doSubject;
	}
	
	public boolean getSearchNameFragment() {
		return _doNameFragment;
	}
	
	public Collection<Integer> getIDs() {
		return _ids;
	}
	
	public void addID(int id) {
		_ids.add(new Integer(id));
	}
	
	public void addIDs(Collection<Integer> ids) {
		_ids.addAll(ids);
	}
	
	public void setChannel(String c) {
		_channel = c;
	}
	
	public void setMinimumDate(Date dt) {
		_threadDate = dt;
	}
	
	public void setSearchSubject(boolean doSearch) {
		_doSubject = doSearch;
	}
	
	public void setSearchNameFragment(boolean doSearch) {
		_doNameFragment = doSearch;
	}
}