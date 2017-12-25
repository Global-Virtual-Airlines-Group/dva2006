// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;

import org.deltava.util.Tuple;

/**
 * A bean to store Equipment Stage statistics entries. 
 * @author Luke
 * @version 8.1
 * @since 8.1
 */

public class StageStatsEntry implements java.io.Serializable {

	private static final Tuple<Integer, Double> ZERO = Tuple.create(Integer.valueOf(0), Double.valueOf(0));
	
	private final String _label;
	private final SortedMap<Integer, Tuple<Integer, Double>> _legs = new TreeMap<Integer, Tuple<Integer, Double>>();

	/**
	 * Creates the bean.
	 * @param l the label
	 */
	public StageStatsEntry(String l) {
		super();
		_label = l;
	}
	
	/**
	 * Returns the label.
	 * @return the label
	 */
	public String getLabel() {
		return _label;
	}
	
	/**
	 * Returns the number of flight legs for a particular stage.
	 * @param stage the stage number
	 * @return the number of legs
	 */
	public int getLegs(int stage) {
		return _legs.getOrDefault(Integer.valueOf(stage), ZERO).getLeft().intValue();
	}
	
	/**
	 * Returns the number of flight hours for a particular stage.
	 * @param stage the stage number
	 * @return the number of hours
	 */
	public double getHours(int stage) {
		return _legs.getOrDefault(Integer.valueOf(stage), ZERO).getRight().doubleValue();
	}
	
	/**
	 * Returns the maximum stage number in this bean.
	 * @return the highest stage
	 */
	public int getMaxStage() {
		return _legs.lastKey().intValue();
	}
	
	/**
	 * Sets stage statistics.
	 * @param stage the stage number
	 * @param legs the number of legs
	 * @param hours the number of hours
	 */
	public void setStage(int stage, int legs, double hours) {
		_legs.put(Integer.valueOf(stage), Tuple.create(Integer.valueOf(legs), Double.valueOf(hours)));
	}
	
	@Override
	public int hashCode() {
		return _label.hashCode();
	}
}