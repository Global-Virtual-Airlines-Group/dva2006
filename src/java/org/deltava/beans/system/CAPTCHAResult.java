// Copyright 2020, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.util.*;
import java.time.Instant;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store Google reCAPTCHA validation responses. 
 * @author Luke
 * @version 11.6
 * @since 9.0
 */

public class CAPTCHAResult implements Cacheable {
	
	private final boolean _isSuccess;
	private final String _hostName;
	private Instant _challengeTime;
	
	private final Collection<String> _msgs = new ArrayList<String>();

	/**
	 * Creates the bean.
	 * @param isSuccess TRUE if successful, otherwise FALSE
	 * @param hostName the remote host name
	 */
	public CAPTCHAResult(boolean isSuccess, String hostName) {
		super();
		_isSuccess = isSuccess;
		_hostName = hostName;
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
	
	@Override
	public Object cacheKey() {
		return _hostName;
	}
}