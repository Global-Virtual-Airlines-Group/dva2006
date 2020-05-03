// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.util.*;
import java.util.concurrent.*;

import org.deltava.beans.Helper;

/**
 * A utility class to temporarily queue API requests for batch logging.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

@Helper(APIRequest.class)
public class APILogger {
	
	private static final BlockingQueue<APIRequest> _queue = new LinkedBlockingQueue<APIRequest>();

	// static class
	private APILogger() {
		super();
	}

	/**
	 * Adds an API request to the queue.
	 * @param req an APIRequest
	 */
	public static void add(APIRequest req) {
		_queue.add(req);
	}
	
	/**
	 * Returns whether there are any entries in the request queue.
	 * @return TRUE if the queue is not empty, otherwise FALSE
	 */
	public static boolean hasData() {
		return (_queue.size() > 0);
	}

	/**
	 * Drains the request queue.
	 * @return a Collection of APIRequest beans
	 */
	public static Collection<APIRequest> drain() {
		Collection<APIRequest> results = new ArrayList<APIRequest>();
		_queue.drainTo(results);
		return results;
	}
}