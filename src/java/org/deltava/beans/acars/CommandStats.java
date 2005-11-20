// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.*;

/**
 * A bean to track ACARS server command statistics. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CommandStats implements java.io.Serializable {

	private static Map<String, Entry> _stats = new TreeMap<String, Entry>();

	public static class Entry implements Comparable {
		
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
			_count++;
			_totalTime += execTime;
			if (execTime > _maxTime)
				_maxTime = execTime;
			
			if ((execTime < _minTime) && (execTime > 0))
				_minTime = execTime;
		}
		
		public int compareTo(Object o2) {
			Entry e2 = (Entry) o2;
			return _cmd.compareTo(e2._cmd);
		}
	}
	
	public static synchronized void log(Class cmd, long execTime) {
		Entry e = _stats.get(cmd.getSimpleName());
		if (e == null) {
			e = new Entry(cmd);
			_stats.put(e.getName(), e);
		}
		
		e.log(execTime);
	}
	
	public static Collection<Entry> getInfo() {
		return _stats.values();
	}
}