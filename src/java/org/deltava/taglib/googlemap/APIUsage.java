// Copyright 2015, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import java.util.concurrent.atomic.LongAdder;

import org.deltava.beans.Helper;
import org.deltava.beans.system.*;

/**
 * A utility class to track Google Maps API usage.
 * @author Luke
 * @version 9.0
 * @since 6.0
 */

@Helper(InsertGoogleAPITag.class)
class APIUsage {

	/**
	 * Google Maps API types.
	 */
	enum Type {
		STATIC, DYNAMIC;
	}
	
	private static final LongAdder DYN_COUNT = new LongAdder();
	private static final LongAdder STATIC_COUNT = new LongAdder();
	
	// static class
	private APIUsage() {
		super();
	}

	/**
	 * Track Google Maps API usage.
	 * @param t the API type
	 * @param isAnonymous TRUE if an unauthenticated request, otherwise FALSE
	 */
	static void track(Type t, boolean isAnonymous) {
		LongAdder add = (t == Type.STATIC) ? STATIC_COUNT : DYN_COUNT;
		add.increment();
		APILogger.add(new APIRequest(API.GoogleMaps.createName(t.name()), isAnonymous));
	}
	
	/**
	 * Returns the number of times the API has been invoked.
	 * @param t the API type
	 * @return the number of invocation times
	 */
	static long get(Type t) {
		LongAdder add = (t == Type.STATIC) ? STATIC_COUNT : DYN_COUNT;
		return add.sum();
	}
}