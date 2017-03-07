// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.time.Instant;

import org.deltava.beans.OnlineNetwork;

/**
 * A bean to store VATSIM Pilot Rating certificates.
 * @author Luke
 * @version 7.2
 * @since 7.2
 */

public class PilotRating extends NetworkUser {
	
	private String _atoName;
	private String _atoURL;
	
	private int _instructor;
	private final String _rating;
	private Instant _date;

	/**
	 * Creates the bean.
	 * @param id the certificate ID
	 * @param rating the rating code
	 */
	public PilotRating(int id, String rating) {
		super(id, OnlineNetwork.VATSIM);
		_rating = rating;
	}
	
	/**
	 * Returns the Authorized Training Organization name.
	 * @return the name
	 */
	public String getATOName() {
		return _atoName;
	}
	
	/**
	 * Returns the Authorized Training Organization URL.
	 * @return the URL
	 */
	public String getATO() {
		return _atoURL;
	}
	
	/**
	 * Returns the Instructor's certificate ID.
	 * @return the cert ID
	 */
	public int getInstructor() {
		return _instructor;
	}
	
	/**
	 * Returns the rating code.
	 * @return the code
	 */
	public String getRatingCode() {
		return _rating;
	}
	
	/**
	 * Returns the certification's issuance date.
	 * @return the issue date/time
	 */
	public Instant getIssueDate() {
		return _date;
	}
	
	/**
	 * Sets the Authorized Training Organizataion.
	 * @param name the ATO name
	 * @param url the ATO URL
	 */
	public void setATO(String name, String url) {
		_atoName = name;
		_atoURL = url;
	}
	
	/**
	 * Sets the rating's date of issue
	 * @param dt the date/time
	 */
	public void setIssueDate(Instant dt) {
		_date = dt;
	}
	
	/**
	 * Sets the Instructor Certificate ID.
	 * @param cid the Instructor's cert ID
	 */
	public void setInstructorID(int cid) {
		_instructor = Math.max(0, cid);
	}

	@Override
	public Type getType() {
		return Type.RATING;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(getID());
		buf.append(' ');
		buf.append(_rating);
		return buf.toString();
	}
}