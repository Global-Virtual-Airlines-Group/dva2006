// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.beans.cooler;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to store Water Cooler poll votes.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PollVote extends DatabaseBean {

	private int _pilotID;
	private int _optID;
	
	/**
	 * Creates a new Water Cooler poll vote.
	 * @param threadID the Message Thread database ID
	 * @param pilotID the Pilot database ID
	 * @throws IllegalArgumentException if threadID or pilotID are zero or negative
	 */
	public PollVote(int threadID, int pilotID) {
		super();
		setID(threadID);
		setPilotID(pilotID);
	}

	/**
	 * Returns the Pilot's database ID.
	 * @return the database ID
	 */
	public int getPilotID() {
		return _pilotID;
	}

	/**
	 * Returns the Option's database ID.
	 * @return the database ID
	 */
	public int getOptionID() {
		return _optID;
	}
	
	/**
	 * Updates the Pilot's database ID.
	 * @param id the database ID
	 * @throws IllegalArgumentException if id is zero or negative
	 */
	public void setPilotID(int id) {
		validateID(_pilotID, id);
		_pilotID = id;
	}

	/**
	 * Updates the Option's database ID.
	 * @param id the database ID
	 * @throws IllegalArgumentException if id is zero or negative
	 */
	public void setOptionID(int id) {
		validateID(_optID, id);
		_optID = id;
	}
}
