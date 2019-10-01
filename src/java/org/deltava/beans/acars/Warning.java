// Copyright 2011, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.time.Instant;

import org.deltava.beans.*;

/**
 * A bean to store ACARS content warnings.
 * @author Luke
 * @version 8.7
 * @since 4.0
 */

public class Warning extends DatabaseBean implements AuthoredBean, CalendarEntry {
	
	private int _authorID;
	private Instant _dt;
	private final int _score;

	/**
	 * Creates the bean.
	 * @param userID the datbase ID of the user being warned
	 * @param authorID the database ID of the user warning
	 * @param score the warnings core
	 */
	public Warning(int userID, int authorID, int score) {
		super();
		setID(userID);
		setAuthorID(authorID);
		_score = Math.max(0, score);
	}
	
	@Override
	public Instant getDate() {
		return _dt;
	}

	@Override
	public int getAuthorID() {
		return _authorID;
	}
	
	/**
	 * Returns the warning score.
	 * @return the score
	 */
	public int getScore() {
		return _score;
	}

	@Override
	public void setAuthorID(int id) {
		validateID(_authorID, id);
		_authorID = id;
	}
	
	/**
	 * Updates the date of the warning.
	 * @param dt the date/time the user was warned
	 */
	public void setDate(Instant dt) {
		_dt = dt;
	}
	
    /**
     * Returns the cache key.
     */
	@Override
    public Object cacheKey() {
    	StringBuilder buf = new StringBuilder(getID());
    	buf.append('-');
    	buf.append(_authorID);
        return buf.toString();
    }
    
    /**
     * Returns the hash code of the database ID/author ID.
     */
	@Override
    public int hashCode() {
    	return cacheKey().hashCode();
    }
}