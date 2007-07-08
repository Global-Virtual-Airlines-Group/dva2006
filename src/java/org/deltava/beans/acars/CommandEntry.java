// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

/**
 * An ACARS server command log entry. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CommandEntry implements Comparable<CommandEntry> {
	
	private String _name;
	private long _count;
	private long _totalTime;
	private int _maxTime;
	private int _minTime;
	
	/**
	 * Creates the entry.
	 * @param name the command name
	 */
	public CommandEntry(String name) {
		super();
		_name = name;
	}
	
	public String getName() {
		return _name;
	}
	
	public long getCount() {
		return _count;
	}
	
	public long getTotalTime() {
		return _totalTime;
	}
	
	public int getMaxTime() {
		return _maxTime;
	}
	
	public int getMinTime() {
		return _minTime;
	}
	
	public void setCount(long count) {
		_count = count;
	}
	
	public void setTotalTime(long time) {
		_totalTime = time;
	}
	
	public void setMaxTime(int time) {
		_maxTime = Math.max(0, time);
	}
	
	public void setMinTime(int time) {
		_minTime = Math.max(0, time);
	}
	
	public int compareTo(CommandEntry e2) {
		return _name.compareTo(e2._name);
	}
	
	public int hashCode() {
		return _name.hashCode();
	}
	
	public String toString() {
		return _name;
	}
}