// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.time.Instant;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store Airport ATIS information. 
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class ATIS implements Cacheable {
	
	private final AirportLocation _a;
	private final ATISType _type;
	private String _data;
	private char _code;
	private Instant _dt;

	/**
	 * Creates the bean.
	 * @param al an AirportLocation
	 * @param t  the ATISType
	 */
	public ATIS(AirportLocation al, ATISType t) {
		super();
		_a = al;
		_type = t;
	}
	
	/**
	 * Returns the Airport for this ATI.
	 * @return an AirportLocation
	 */
	public AirportLocation getAirport() {
		return _a;
	}
	
	/**
	 * Returns the ATIS type.
	 * @return an ATISType
	 */
	public ATISType getType() {
		return _type;
	}
	
	/**
	 * Returns the ATIS effective date.
	 * @return the effective date/time
	 */
	public Instant getEffectiveDate() {
		return _dt;
	}
	
	/**
	 * Returns the ATIS code.
	 * @return the code
	 */
	public char getCode() {
		return _code;
	}
	
	/**
	 * Returns the ATIS data.
	 * @return the ATIS message
	 */
	public String getData() {
		return _data;
	}

	/**
	 * Updates the effective date.
	 * @param dt the effective date/time
	 */
	public void setEffectiveDate(Instant dt) {
		_dt = dt;
	}
	
	/**
	 * Updates the ATIS code.
	 * @param c the code
	 */
	public void setCode(char c) {
		_code = Character.toUpperCase(c);
	}
	
	/**
	 * Updates the ATIS data.
	 * @param msg the ATIS message
	 */
	public void setData(String msg) {
		_data = msg; 
	}

	@Override
	public Object cacheKey() {
		return (_type == ATISType.COMBINED) ? _a.getCode() : String.format("%s/%s", _a.getCode(), _type.name());
	}
}