// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

/**
 * A bean to store Flight Elite score entries.
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

public class EliteScoreEntry implements java.io.Serializable, Comparable<EliteScoreEntry> {
	
	private final int _seq;
	private final int _pts;
	private boolean _isBonus;
	private final String _msg;

	/**
	 * Creates the bean.
	 * @param seq the sequence number
	 * @param score the score points for this entry
	 * @param msg the message
	 */
	EliteScoreEntry(int seq, int score, String msg) {
		super();
		_seq = seq;
		_pts = score;
		_msg = msg;
	}

	/**
	 * Returns the sequence number.
	 * @return the sequence
	 */
	public int getSequence() {
		return _seq;
	}
	
	/**
	 * Returns the number of points in this entry.
	 * @return the number of points
	 */
	public int getPoints() {
		return _pts;
	}
	
	/**
	 * Returns the entry message.
	 * @return the message
	 */
	public String getMessage() {
		return _msg;
	}
	
	/**
	 * Returns whether this is a bonus point entry.
	 * @return TRUE if bonus points, otherwise FALSE
	 */
	public boolean isBonus() {
		return _isBonus;
	}
	
	/**
	 * Updates whether this is a bonus point entry.
	 * @param isBonus TRUE if bonus points, otherwise FALSE
	 */
	public void setBonus(boolean isBonus) {
		_isBonus = isBonus;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_msg);
		if (_isBonus) buf.append(" [BONUS]");
		buf.append(" (");
		buf.append(_pts);
		buf.append(')');
		return buf.toString();
	}
	
	@Override
	public int compareTo(EliteScoreEntry ese2) {
		return Integer.compare(_seq, ese2._seq);
	}
}