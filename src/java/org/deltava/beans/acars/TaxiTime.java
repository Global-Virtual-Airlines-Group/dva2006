// Copyright 2021, 2022, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.time.Duration;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store average taxi times for an Airport.
 * @author Luke
 * @version 11.2
 * @since 10.0
 */

public class TaxiTime implements Cacheable, Comparable<TaxiTime> {
	
	private final String _icao;
	private final int _year;
	
	private int _inCount;
	private Duration _taxiIn;
	private Duration _stdDevIn;
	private int _outCount;
	private Duration _taxiOut;
	private Duration _stdDevOut;

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
	 * Returns the number of inbound flights used to calculate the data.
	 * @return the number of flights
	 */
	public int getInboundCount() {
		return _inCount;
	}
	
	/**
	 * Returns the average inbound taxi time.
	 * @return the Duration
	 */
	public Duration getInboundTime() {
		return _taxiIn;
	}
	
	/**
	 * Returns the standard deviation of inbound taxi times.
	 * @return the deviation as a Duration
	 */
	public Duration getInboundStdDev() {
		return _stdDevIn;
	}
	
	/**
	 * Returns the number of outbound flights used to calculate the data.
	 * @return the number of flights
	 */
	public int getOutboundCount() {
		return _outCount;
	}

	/**
	 * Returns the average outbound tax time.
	 * @return the Duration
	 */
	public Duration getOutboundTime() {
		return _taxiOut;
	}
	
	/**
	 * Returns the standard deviation of outbound taxi times.
	 * @return the deviation as a Duration
	 */
	public Duration getOutboundStdDev() {
		return _stdDevOut;
	}
	
	/**
	 * Returns if the inbound and outbound taxi times are both zero.
	 * @return TRUE if both are zero, otherwise FALSE
	 */
	public boolean isEmpty() {
		return _taxiOut.equals(Duration.ZERO) && _taxiIn.equals(Duration.ZERO); 
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
	 * Updates the inbound taxi time standard deviation.
	 * @param d the deviation as a Duration
	 */
	public void setInboundStdDev(Duration d) {
		_stdDevIn = d.isNegative() ? d.negated() : d;
	}
	
	/**
	 * Updates the number of inbound fligts used to calculate the data.
	 * @param cnt the number of flights
	 */
	public void setInboundCount(int cnt) {
		_inCount = Math.max(0, cnt);
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
	
	/**
	 * Updates the outbound taxi time standard deviation.
	 * @param d the deviation as a Duration
	 */
	public void setOutboundStdDev(Duration d) {
		_stdDevOut = d.isNegative() ? d.negated() : d;
	}
	
	/**
	 * Updates the number of outbound fligts used to calculate the data.
	 * @param cnt the number of flights
	 */
	public void setOutboundCount(int cnt) {
		_outCount = Math.max(0, cnt);
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_icao);
		buf.append('$').append(_year);
		return buf.toString();
	}

	@Override
	public Object cacheKey() {
		return toString();
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