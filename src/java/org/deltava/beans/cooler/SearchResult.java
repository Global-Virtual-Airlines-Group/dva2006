// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.cooler;

/**
 * A bean to store Lucene search results and scores. 
 * @author Luke
 * @version 2.5
 * @since 2.5
 */

@Deprecated
public class SearchResult implements Comparable<SearchResult> {
	
	private int _id;
	private int _hits = 1;
	private double _score;
	
	private MessageThread _thread;

	/**
	 * Initializes the bean.
	 * @param threadID the message thread database ID
	 * @param score the result score 
	 */
	public SearchResult(int threadID, double score) {
		super();
		_id = Math.max(0, threadID);
		_score = score;
	}

	/**
	 * Adds another hit to this thread. The aggregate score for the thread will be
	 * increased by the score of this hit, divided by the total number of hits for the thread.
	 * @param score the score of the hit
	 */
	public void addHit(double score) {
		_hits++;
		_score += (score / _hits);
	}
	
	/**
	 * Returns the number of search hits for this thread. 
	 * @return the number of hits
	 */
	public int getHits() {
		return _hits;
	}
	
	/**
	 * Returns the aggregate score for this thread.
	 * @return the score
	 */
	public double getScore() {
		return _score;
	}
	
	/**
	 * Returns the message thread.
	 * @return the MessageThread bean
	 */
	public MessageThread getThread() {
		return _thread;
	}
	
	/**
	 * Returns the thread ID.
	 */
	public final int getID() {
		return (_thread == null) ? _id : _thread.getID();
	}
	
	/**
	 * Sets the Message Thread bean
	 * @param mt the MessageThread
	 */
	public void setThread(MessageThread mt) {
		_thread = mt;
	}
	
	/**
	 * Compares two results by comparing their scores.
	 */
	public int compareTo(SearchResult sr2) {
		int tmpResult = new Double(_score).compareTo(new Double(sr2._score));
		if (tmpResult == 0)
			tmpResult = Integer.valueOf(_hits).compareTo(Integer.valueOf(sr2._hits));
		
		return (tmpResult == 0) ? new Integer(getID()).compareTo(new Integer(sr2.getID())) : tmpResult;
	}
	
	public boolean equals(Object o2) {
		return (o2 instanceof SearchResult) ? (getID() == ((SearchResult) o2).getID()) : false; 
	}
	
	public int hashCode() {
		return getID();
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder("SearchResult-");
		buf.append(getID());
		buf.append('-');
		buf.append(_hits);
		buf.append('-');
		buf.append(_score);
		return buf.toString();
	}
}