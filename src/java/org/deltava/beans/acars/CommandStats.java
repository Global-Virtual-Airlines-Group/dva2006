// Copyright 2005, 2006 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.*;

import org.deltava.util.IPCUtils;

import org.gvagroup.ipc.IPCInfo;

/**
 * A bean to track ACARS server command statistics. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CommandStats implements IPCInfo<CommandStats.Entry>{

	private static final Map<String, Entry> _stats = new TreeMap<String, Entry>();
	private static final CommandStats _instance = new CommandStats();

	public static class Entry implements java.io.Serializable, Comparable<CommandStats.Entry> {
		
		private String _cmd;
		private long _count;
		private long _totalTime;
		private long _maxTime = Long.MIN_VALUE;
		private long _minTime = Long.MAX_VALUE;
		
		Entry(Class cmd) {
			super();
			_cmd = cmd.getSimpleName();
		}
		
		public String getName() {
			return _cmd;
		}
		
		public long getCount() {
			return _count;
		}
		
		public long getTotalTime() {
			return _totalTime;
		}
		
		public long getMaxTime() {
			return _maxTime;
		}
		
		public long getMinTime() {
			return _minTime;
		}
		
		public void log(long execTime) {
			if (execTime == 0)
				execTime++;
			
			_count++;
			_totalTime += execTime;
			if (execTime > _maxTime)
				_maxTime = execTime;
			
			if (execTime < _minTime)
				_minTime = execTime;
		}
		
		public int compareTo(Entry e2) {
			return _cmd.compareTo(e2._cmd);
		}
	}

	// singleton constructor
	private CommandStats() {
		super();
	}
	
	public static CommandStats getInstance() {
		return _instance;
	}
	
	/**
	 * Logs a command invocation.
	 * @param cmd the Command class
	 * @param execTime the execution time in millseconds
	 */
	public static synchronized void log(Class cmd, long execTime) {
		Entry e = _stats.get(cmd.getSimpleName());
		if (e == null) {
			e = new Entry(cmd);
			_stats.put(e.getName(), e);
		}
		
		e.log(execTime);
	}
	
	/**
	 * Returns the command statistics.
	 * @return a Collection of CommandStats.Entry beans
	 */
	public synchronized Collection<byte[]> getSerializedInfo() {
		return IPCUtils.serialize(_stats.values());
	}
}