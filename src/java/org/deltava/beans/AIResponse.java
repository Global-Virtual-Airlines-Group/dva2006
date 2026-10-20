// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.time.Instant;

/**
 * A container bean for AI response data.
 * @author Luke
 * @version 13.0
 * @since 12.5
 */

public abstract class AIResponse extends DatabaseBean {
	
	private Instant _createdOn = Instant.now();

	private int _inTokens;
	private int _outTokens;
	private long _genTime;
	
	/**
	 * Creates the Response.
	 * @param id the database ID
	 */
	public AIResponse(int id) {
		super();
		setID(id);
	}
	
	/**
	 * Returns the creation date of this response.
	 * @return the creation date/time
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}

	/**
	 * Returns the generation time for this respnse.
	 * @return the generation time in milliseconds
	 */
	public long getGenerationTime() {
		return _genTime;
	}
	
	/**
	 * Returns the number of input tokens used to generate this response.
	 * @return the number of tokens
	 */
	public int getInputTokens() {
		return _inTokens;
	}

	/**
	 * Returns the number of output tokens used to generate this response.
	 * @return the number of tokens
	 */
	public int getOutputTokens() {
		return _outTokens;
	}
	
	/**
	 * Updates the generation time for this response.
	 * @param ms the generation time in milliseconds
	 */
	public void setGenerationTime(long ms) {
		_genTime = ms;
	}
	
	/**
	 * Updates the creation date of this response.
	 * @param dt the creation date/time
	 */
	public void setCreatedOn(Instant dt) {
		_createdOn = dt;
	}
	
	/**
	 * Updates the number of tokens used to generate this response.
	 * @param inTokens the number of input tokens
	 * @param outTokens the number of output tokens
	 */
	public void setTokens(int inTokens, int outTokens) {
		_inTokens = inTokens;
		_outTokens = outTokens;
	}
}