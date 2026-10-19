// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import org.deltava.beans.Auditable;
import org.deltava.util.cache.Cacheable;

/**
 * A bean to store an AI prompt. 
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class Prompt implements Cacheable, Auditable {
	
	private final String _id;
	private String _desc;
	private String _user;
	private String _system;

	/**
	 * Creates the bean.
	 * @param id the Prompt ID
	 */
	public Prompt(String id) {
		super();
		_id = id;
	}

	/**
	 * Returns the prompt ID.
	 * @return the ID
	 */
	public String getID() {
		return _id;
	}
	
	/**
	 * Returns the user prompt.
	 * @return the prompt
	 */
	public String getUser() {
		return _user;
	}
	
	/**
	 * Returns the system prompt.
	 * @return the prompt
	 */
	public String getSystem() {
		return _system;
	}
	
	/**
	 * Returns the prompt description.
	 * @return the description
	 */
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Updates the system prompt.
	 * @param p the prompt text
	 */
	public void setSystem(String p) {
		_system = p;
	}
	
	/**
	 * Updates the user prompt.
	 * @param p the prompt text
	 */
	public void setUser(String p) {
		_user = p;
	}
	
	/**
	 * Updates the prompt description.
	 * @param desc the description
	 */
	public void setDescription(String desc) {
		_desc = desc;
	}
	
	@Override
	public Object cacheKey() {
		return _id;
	}
	
	@Override
	public String toString() {
		return _id;
	}
	
	@Override
	public int hashCode() {
		return _id.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof Prompt p) && _id.equals(p._id);
	}
	
	@Override
	public boolean isCrossApp() {
		return false;
	}

	@Override
	public String getAuditID() {
		return _id;
	}
}