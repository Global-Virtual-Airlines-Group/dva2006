// Copyright 2005, 2008, 2009, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.cooler;

import java.time.Instant;

/**
 * A bean to store Water Cooler search criteria.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class SearchCriteria {

	private String _searchStr;
	private String _channel;
	private String _author;
	private Instant _threadDate;
	
	private boolean _doSubject;
	private boolean _doNameFragment;
	
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
	 * Returns the author name to search for.
	 * @return the author name
	 */
	public String getAuthorName() {
		return _author;
	}
	
	/**
	 * Returns the earliest thread update date to include.
	 * @return the minimum thread last update date/time
	 */
	public Instant getMinimumDate() {
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
	
	public void setChannel(String c) {
		_channel = c;
	}
	
	public void setAuthorName(String aName) {
		_author = aName;
	}
	
	public void setMinimumDate(Instant dt) {
		_threadDate = dt;
	}
	
	public void setSearchSubject(boolean doSearch) {
		_doSubject = doSearch;
	}
	
	public void setSearchNameFragment(boolean doSearch) {
		_doNameFragment = doSearch;
	}
}