// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.util.Date;

/**
 * A bean to track ServInfo data locations and their reliability.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class NetworkDataURL implements java.io.Serializable, Comparable {

	private String _url;
	private Date _lastUse;
	private int _totalUses;
	private int _success;
	
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
	 */
	public int getSuccessPercentage() {
		return (_totalUses == 0) ? 100 : (_success * 100) / _totalUses;
	}
	
	/**
	 * Logs usage of this data source. 
	 * @param isSuccess TRUE if the load was successful, otherwise FALSE
	 * @see NetworkDataURL#getSuccess()
	 * @see NetworkDataURL#getFailures()
	 */
	public void logUsage(boolean isSuccess) {
		_totalUses++;
		_lastUse = new Date();
		if (isSuccess)
			_success++;
	}

	/**
	 * Compares two beans by comparing their success percentages. If they are equal, the number of failures
	 * will be compared in <i>inverse</i> order.
	 * @see Comparable#compareTo(Object)
	 * @see NetworkDataURL#getSuccessPercentage()
	 */
	public int compareTo(Object o) {
		NetworkDataURL nd2 = (NetworkDataURL) o;
		int tmpResult = new Integer(getSuccessPercentage()).compareTo(new Integer(nd2.getSuccessPercentage()));
		return (tmpResult == 0) ? new Integer(getFailures()).compareTo(new Integer(nd2.getFailures())) * -1 : tmpResult;
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