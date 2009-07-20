// Copyright 2005, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servlet;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

/**
 * A scoreboard to track servlet activity.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class ServletScoreboard implements java.io.Serializable {

	private static final Map<Long, ServletScoreboardEntry> _entries = new TreeMap<Long, ServletScoreboardEntry>();
	
	// singleton
	private ServletScoreboard() {
		super();
	}

	/**
	 * Returns the scoreboard entries.
	 * @return a Collection of ServletScoreboardEntry beans
	 */
	public static Collection<ServletScoreboardEntry> getScoreboard() {
		return _entries.values();
	}
	
	/**
	 * Updates the scoreboard upon servlet completion.
	 */
	public static void complete() {
		Long id = new Long(Thread.currentThread().getId());
		ServletScoreboardEntry entry = _entries.get(id);
		if (entry != null)
			entry.complete();
	}

	/**
	 * Updates the scoreboard on servlet invocation.
	 * @param req the servlet request
	 */
	public static void add(HttpServletRequest req) {
		Thread t = Thread.currentThread();
		ServletScoreboardEntry entry = _entries.get(new Long(t.getId()));
		if (entry == null)
			entry = new ServletScoreboardEntry(t.getName());
		else
			entry.start();
		
		entry.setRemoteAddr(req.getRemoteAddr());
		entry.setRemoteHost(req.getRemoteHost());
		entry.setURL(req.getMethod() + " " + req.getRequestURI());
		_entries.put(new Long(t.getId()), entry);
	}
}