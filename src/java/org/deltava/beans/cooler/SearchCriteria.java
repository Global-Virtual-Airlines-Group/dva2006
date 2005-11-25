// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.beans.cooler;

import java.util.*;

/**
 * A bean to store Water Cooler search criteria.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SearchCriteria {

	private String _searchStr;
	private String _channel;
	
	private boolean _doSubject;
	private boolean _doNameFragment;
	
	private Collection<Integer> _ids;
	
	/**
	 * Creates a new search criteria bean.
	 * @param searchStr the search string 
	 */
	public SearchCriteria(String searchStr) {
		super();
		_searchStr = searchStr;
		_ids = new HashSet<Integer>();
	}

	public String getChannel() {
		return _channel;
	}
	
	public String getSearchTerm() {
		return _searchStr;
	}
	
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
	
	public void setSearchSubject(boolean doSearch) {
		_doSubject = doSearch;
	}
	
	public void setSearchNameFragment(boolean doSearch) {
		_doNameFragment = doSearch;
	}
}