// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.time.Duration;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store average taxi times for an Airport.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class TaxiTime implements Cacheable, Comparable<TaxiTime> {
	
	private final String _icao;
	private final int _year;
	
	private Duration _taxiIn;
	private Duration _taxiOut;

	/**
	 * Creates the bean.
	 * @param icao the Airport's ICAO code
	 * @param year the year or zero for all 
	 */
	public TaxiTime(String icao, int year) {
		_icao = icao;
		_year = year;
	}

	/**
	 * Returns the Airport's ICAO code.
	 * @return the ICAO code
	 */
	public String getICAO() {
		return _icao;
	}
	
	/**
	 * Returns the year.
	 * @return the year, or zero for an aggergated average
	 */
	public int getYear() {
		return _year;
	}
	
	/**
	 * Returns the average inbound tax time.
	 * @return the Duration
	 */
	public Duration getInboundTime() {
		return _taxiIn;
	}

	/**
	 * Returns the average outbound tax time.
	 * @return the Duration
	 */
	public Duration getOutboundTime() {
		return _taxiOut;
	}
	
	/**
	 * Updates the average inbound taxi time.
	 * @param d the Duration
	 * @throws IllegalArgumentException if d is negative
	 */
	public void setInboundTime(Duration d) {
		if (d.isNegative())
			throw new IllegalArgumentException("Taxi time cannot be negative");
		
		_taxiIn = d;
	}
	
	/**
	 * Updates the average outbound taxi time.
	 * @param d the Duration
	 * @throws IllegalArgumentException if d is negative
	 */
	public void setOutboundTime(Duration d) {
		if (d.isNegative())
			throw new IllegalArgumentException("Taxi time cannot be negative");
		
		_taxiOut = d;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_icao);
		buf.append('$').append(_year);
		return buf.toString();
	}

	@Override
	public Object cacheKey() {
		return _icao;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public int compareTo(TaxiTime tt) {
		int tmpResult = _icao.compareTo(tt._icao);
		return (tmpResult == 0) ? Integer.compare(_year, tt._year) : tmpResult;
	}
}