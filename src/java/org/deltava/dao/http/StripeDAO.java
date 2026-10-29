// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.util.*;

/**
 * A Data Access Object to interact with the Stripe REST API.
 * @author Luke
 * @version 13.1
 * @since 13.1	
 */

abstract class StripeDAO extends DAO {
	
	private static final String API_VERSION = "2026-10-28.endive";
	private String _key;
	
	private final Collection<String> _expands = new LinkedHashSet<String>();

	/**
	 * Sets the Stripe secret API key to use.
	 * @param key the key
	 */
	public void setKey(String key) {
		_key = key;
	}
	
	/**
	 * Sets common request headers.
	 */
	protected void setHeaders() {
		setAuthentication(_key, "");
		setRequestHeader("Stripe-Version", API_VERSION);
	}
	
	@Override
	public void reset() {
		super.reset();
		_expands.clear();
	}
}