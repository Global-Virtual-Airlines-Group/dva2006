// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.util.*;
import java.time.Instant;

/**
 * A bean to store Google reCAPTCHA validation responses. 
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class CAPTCHAResult implements java.io.Serializable {
	
	private final boolean _isSuccess;
	private String _hostName;
	private Instant _challengeTime;
	
	private final Collection<String> _msgs = new ArrayList<String>();

	/**
	 * Creates the bean.
	 * @param isSuccess TRUE if successful, otherwise FALSE
	 */
	public CAPTCHAResult(boolean isSuccess) {
		super();
		_isSuccess = isSuccess;
	}

	/**
	 * Returns if the challenge was successful.
	 * @return TRUE if successful, otherwise FALSE
	 */
	public boolean getIsSuccess() {
		return _isSuccess;
	}
	
	/**
	 * Returns the site host name.
	 * @return the host name
	 */
	public String getHostName() {
		return _hostName;
	}
	
	/**
	 * Returns the CAPTCHA challenge time.
	 * @return the date/time
	 */
	public Instant getChallengeTime() {
		return _challengeTime;
	}
	
	/**
	 * Returns any error/status messages.
	 * @return a Collection of messages
	 */
	public Collection<String> getMessages() {
		return _msgs;
	}
	
	/**
	 * Updates the site host name.
	 * @param host the site host name
	 */
	public void setHostName(String host) {
		_hostName = host;
	}
	
	/**
	 * Updates the date/time of the challenge.
	 * @param dt the date/time
	 */
	public void setChallengeTime(Instant dt) {
		_challengeTime = dt;
	}

	/**
	 * Adds an error message.
	 * @param msg a message
	 */
	public void addMessage(String msg) {
		_msgs.add(msg);
	}
}