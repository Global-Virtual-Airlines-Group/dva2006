// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store aircraft tail codes.
 * @author Luke
 * @version 8.0
 * @since 8.0
 */

public class TailCode implements Cacheable {
	
	private final String _tailCode;
	private final String _icao;

	/**
	 * Creates the bean.
	 * @param tailCode the tail code
	 * @param icao the ICAO equipment code
	 */
	public TailCode(String tailCode, String icao) {
		super();
		_tailCode = tailCode.trim().toUpperCase();
		_icao = icao.trim().toUpperCase();
	}
	
	/**
	 * Returns the aircraft's ICAO equipment code.
	 * @return the ICAO equipment code
	 */
	public String getICAO() {
		return _icao;
	}
	
	/**
	 * Returns the aircraft's registration code.
	 * @return the tail code
	 */
	public String getTailCode() {
		return _tailCode;
	}

	@Override
	public Object cacheKey() {
		return _tailCode;
	}

	@Override
	public int hashCode() {
		return _tailCode.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return _tailCode.equals(o) || toString().equals(o);
	}
	
	@Override
	public String toString() {
		return _tailCode + "=" + _icao;
	}
}