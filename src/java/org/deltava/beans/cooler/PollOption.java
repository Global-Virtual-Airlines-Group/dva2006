// Copyright 2005, 2007, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.cooler;

import org.deltava.beans.*;

import org.deltava.util.StringUtils;

/**
 * A bean to store Water Cooler poll options.
 * @author Luke
 * @version 8.0
 * @since 1.0
 */

public class PollOption extends DatabaseBean implements ComboAlias {
	
	private final String _name;
	private int _optID;
	private int _votes;

	/**
	 * Creates a new Water Cooler poll option.
	 * @param threadID the Message Thread database ID
	 * @param name the option name
	 * @throws IllegalArgumentException if thread ID is zero or negative
	 * @throws NullPointerException if name is null
	 */
	public PollOption(int threadID, String name) {
		super();
		setID(threadID);
		_name = name.trim();
	}

	public String getName() {
		return _name;
	}
	
	public int getOptionID() {
		return _optID;
	}
	
	public int getVotes() {
		return _votes;
	}
	
	@Override
	public String getComboName() {
		return _name;
	}
	
	@Override
	public String getComboAlias() {
		return StringUtils.formatHex(_optID);
	}
	
	/**
	 * Sets the option ID.
	 * @param id the new option ID
	 * @throws IllegalArgumentException if id is zero or negative
	 * @see PollOption#getOptionID()
	 */
	public void setOptionID(int id) {
		validateID(_optID, id);
		_optID = id;
	}

	/**
	 * Updates the number of votes.
	 * @param voteCount the number of votes
	 * @see PollOption#getVotes()
	 */
	public void setVotes(int voteCount) {
		_votes = Math.max(0, voteCount);
	}
	
	@Override
	public int compareTo(Object o2) {
		PollOption po2 = (PollOption) o2;
		int tmpResult = Integer.compare(getID(), po2.getID());
		return (tmpResult == 0) ? Integer.compare(_optID, po2._optID) : tmpResult;
	}
	
	@Override
	public int hashCode() {
		return _name.hashCode();
	}
	
	@Override
	public String toString() {
		return _name;
	}
}