// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.event;

import java.nio.charset.StandardCharsets;

import org.deltava.beans.DatabaseDocumentBean;

/**
 * A bean to store an Online Event briefing.
 * @author Luke
 * @version 10.0
 * @since 9.0
 */

public class Briefing extends DatabaseDocumentBean {
	
	/**
	 * Creates the bean from a binary blob.
	 * @param data the briefing data
	 */
	public Briefing(byte[] data) {
		super();
		load(data);
	}
	
	/**
	 * Creates the bean from a string.
	 * @param data the briefing data
	 */
	public Briefing(String data) {
		this(data.getBytes(StandardCharsets.UTF_8));
	}
}