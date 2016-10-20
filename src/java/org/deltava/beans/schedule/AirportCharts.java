// Copyright 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

/**
 * A bean to store a collection of Charts associated with an Airport.
 * @author Luke
 * @version 7.2
 * @since 5.0
 * @param <T> the Chart type
 */

public class AirportCharts<T extends Chart> implements Iterable<T>, java.io.Serializable, Comparable<AirportCharts<?>> {

	private final Airport _a;
	private final Map<String, T> _charts = new TreeMap<String, T>();

	/**
	 * Initializes the collection.
	 * @param a the Airport
	 */
	public AirportCharts(Airport a) {
		super();
		_a = a;
	}
	
	/**
	 * Initializes the collection.
	 * @param a the Airport
	 * @param charts the charts to include
	 */
	public AirportCharts(Airport a, Collection<T> charts) {
		this(a);
		charts.forEach(c -> add(c));
	}

	/**
	 * Returns the Airport.
	 * @return the Airport
	 */
	public Airport getAirport() {
		return _a;
	}
	
	/**
	 * Returns a particular Chart
	 * @param name the chart name
	 * @return the Chart, or null if not found
	 */
	public T get(String name) {
		return _charts.get(name.toUpperCase());
	}

	/**
	 * Returns the Charts.
	 * @return a Collection of Charts
	 */
	public Collection<T> getCharts() {
		return _charts.values();
	}

	/**
	 * Returns the chart names.
	 * @return a Collection of Chart names
	 */
	public Collection<String> getChartNames() {
		return _charts.keySet();
	}

	/**
	 * Adds a chart to the collection.
	 * @param ec the Chart
	 */
	public void add(T ec) {
		_charts.put(ec.getName().replace(",","").toUpperCase(), ec);
	}

	@Override
	public Iterator<T> iterator() {
		return _charts.values().iterator();
	}

	@Override
	public int compareTo(AirportCharts<?> ac2) {
		return _a.compareTo(ac2._a);
	}
}