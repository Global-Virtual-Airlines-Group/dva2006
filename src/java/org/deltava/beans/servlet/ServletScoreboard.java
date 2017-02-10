// Copyright 2005, 2007, 2009, 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servlet;

import java.util.*;
import java.util.concurrent.locks.*;

import javax.servlet.http.HttpServletRequest;

import org.deltava.util.CollectionUtils;

/**
 * A scoreboard to track servlet activity.
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

public class ServletScoreboard implements java.io.Serializable {

	private static final Map<Long, ServletScoreboardEntry> _entries = new TreeMap<Long, ServletScoreboardEntry>();
	
	private static final ReentrantReadWriteLock _l = new ReentrantReadWriteLock(true);
	private static final Lock _r = _l.readLock();
	private static final Lock _w = _l.writeLock();
	
	// singleton
	private ServletScoreboard() {
		super();
	}

	/**
	 * Returns the scoreboard entries.
	 * @return a Collection of ServletScoreboardEntry beans
	 */
	public static Collection<ServletScoreboardEntry> getScoreboard() {
		return new ArrayList<ServletScoreboardEntry>(_entries.values());
	}
	
	/**
	 * Updates the scoreboard upon servlet completion.
	 */
	public static void complete() {
		Long id = Long.valueOf(Thread.currentThread().getId());
		try {
			_r.lock();
			ServletScoreboardEntry entry = _entries.get(id);
			if (entry != null)
				entry.complete();	
		} finally {
			_r.unlock();
		}
	}

	/**
	 * Updates the scoreboard on servlet invocation.
	 * @param req the servlet request
	 */
	public static void add(HttpServletRequest req) {
		Thread t = Thread.currentThread(); Long k = Long.valueOf(t.getId());
		ServletScoreboardEntry entry = null;
		try {
			_r.lock();
			entry = _entries.get(k);
		} finally {
			_r.unlock();
		}
			
		if (entry == null) {
			entry = new ServletScoreboardEntry(t.getName());
			try {
				_w.lock();
				_entries.put(k, entry);
			} finally {
				_w.unlock();
			}
		} else
			entry.start();
		
		entry.setRemoteAddr(req.getRemoteAddr());
		entry.setRemoteHost(req.getRemoteHost());
		entry.setURL(req.getMethod() + " " + req.getRequestURI());
	}
	
	/**
	 * Updates threads that are still alive.
	 */
	public static void updateActiveThreads() {
		ThreadGroup tg = Thread.currentThread().getThreadGroup();
		try {
			_w.lock();
			Thread[] threads = new Thread[tg.activeCount()];
			tg.enumerate(threads);
			Map<String, Thread> threadMap = CollectionUtils.createMap(Arrays.asList(threads), Thread::getName);
			for (ServletScoreboardEntry entry : _entries.values()) {
				Thread t = threadMap.get(entry.getName());
				entry.setAlive((t != null) && t.isAlive());
			}
		} finally {
			_w.unlock();
		}
	}
}