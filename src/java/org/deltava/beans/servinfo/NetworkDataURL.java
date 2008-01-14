// Copyright 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.util.*;

/**
 * A bean to track ServInfo data locations and their reliability.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class NetworkDataURL implements Comparable<NetworkDataURL> {
	
	private static final int MAX_LAST_USES = 8;

	private String _url;
	private Date _lastUse;
	private int _totalUses;
	private int _success;
	
	private List<Boolean> _lastUses = new ArrayList<Boolean>();
	
	/**
	 * Creates the URL location bean.
	 * @param url the data source URL 
	 */
	public NetworkDataURL(String url) {
		super();
		_url = url;
	}
	
	/**
	 * Returns the data source URL.
	 * @return the URL
	 */
	public String getURL() {
		return _url;
	}
	
	/**
	 * Returns the last time this data source URL was accessed.
	 * @return the date/time of last access
	 */
	public Date getLastUse() {
		return _lastUse;
	}
	
	/**
	 * Returns the number of times data has been successfully returned from this URL.
	 * @return the number of successes
	 * @see NetworkDataURL#getFailures()
	 */
	public int getSuccess() {
		return _success;
	}
	
	/**
	 * Returns the number of times data has not been returned from this URL.
	 * @return the number of failures
	 * @see NetworkDataURL#getSuccess()
	 */
	public int getFailures() {
		return (_totalUses - _success);
	}
	
	/**
	 * Returns the completion percentage for this data source URL.
	 * @return the completion percentage, from 0-100
	 * @see NetworkDataURL#getSuccess()
	 * @see NetworkDataURL#getFailures()
	 * @see NetworkDataURL#getRecentSuccessPercentage()
	 */
	public synchronized int getSuccessPercentage() {
		return (_totalUses == 0) ? 100 : (_success * 100) / _totalUses;
	}
	
	/**
	 * Returns the completion percentage for the last several attempts to this data source URL.
	 * @return the completion percentage, from 0-100
	 * @see NetworkDataURL#getSuccessPercentage()
	 */
	public synchronized int getRecentSuccessPercentage() {
		if (_lastUses.isEmpty())
			return 100;
		
		// If we haven't made a call lately, reset the percentage
		if ((System.currentTimeMillis() - _lastUse.getTime()) > 3600000) {
			_lastUses.clear();
			return 100;
		}
		
		// Calculate the percentage
		int successCount = 0;
		for (Iterator<Boolean> i = _lastUses.iterator(); i.hasNext(); ) {
			Boolean b = i.next();
			if (b.booleanValue())
				successCount++;
		}
		
		// Return percentage
		return (successCount * 100) / _lastUses.size();
	}
	
	/**
	 * Logs usage of this data source. 
	 * @param isSuccess TRUE if the load was successful, otherwise FALSE
	 * @see NetworkDataURL#getSuccess()
	 * @see NetworkDataURL#getFailures()
	 */
	public synchronized void logUsage(boolean isSuccess) {
		_totalUses++;
		_lastUse = new Date();
		if (isSuccess)
			_success++;
		
		// Log to last uses
		_lastUses.add(Boolean.valueOf(isSuccess));
		if (_lastUses.size() > MAX_LAST_USES)
			_lastUses.remove(0);
	}

	/**
	 * Compares two beans by comparing their recent success percentages. If they are equal, the overall success
	 * count will be compared.
	 * @see Comparable#compareTo(Object)
	 * @see NetworkDataURL#getSuccessPercentage()
	 */
	public int compareTo(NetworkDataURL nd2) {
		int tmpResult = new Integer(getRecentSuccessPercentage()).compareTo(new Integer(nd2.getRecentSuccessPercentage()));
		if (tmpResult == 0)
			tmpResult = new Integer(_success).compareTo(new Integer(nd2._success)) * -1;
		
		return tmpResult;
	}
	
	/**
	 * Returns a String representation of the bean for debugging.
	 */
	public String toString() {
		StringBuilder buf = new StringBuilder(_url);
		buf.append(", uses=");
		buf.append(_totalUses);
		buf.append(", failures=");
		buf.append(getFailures());
		return buf.toString();
	}
}