// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.beans.servlet;

import java.util.*;

import java.io.Serializable;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;

/**
 * A scoreboard to track servlet activity.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ServletScoreboard implements Serializable {

	private static Map<Long, ServletScoreboardEntry> _entries = new TreeMap<Long, ServletScoreboardEntry>();
	
	// singleton
	private ServletScoreboard() {
	}

	public static Collection<ServletScoreboardEntry> getScoreboard() {
		return _entries.values();
	}
	
	public static void complete() {
		Long id = new Long(Thread.currentThread().getId());
		ServletScoreboardEntry entry = _entries.get(id);
		if (entry != null)
			entry.complete();
	}
	
	public static void add(HttpServletRequest req, Servlet srv) {
		Thread t = Thread.currentThread();
		ServletScoreboardEntry entry = new ServletScoreboardEntry(t.getName());
		entry.setServletClass(srv.getClass());
		entry.setRemoteAddr(req.getRemoteAddr());
		entry.setRemoteHost(req.getRemoteHost());
		entry.setURL(req.getMethod() + " " + req.getRequestURI());
		_entries.put(new Long(t.getId()), entry);
	}
}