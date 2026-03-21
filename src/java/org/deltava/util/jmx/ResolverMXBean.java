// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.jmx;

/**
 * A JMX interface for DNS resolvers.
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

public interface ResolverMXBean {
	
	/**
	 * Returns the application code.
	 * @return the code
	 */
	public String getCode();

	/**
	 * Returns the number of resolver threads.
	 * @return the number of threads
	 */
	public Integer getThreads();
	
	/**
	 * Returns the number of requests made to this resolver.
	 * @return the number of requests
	 */
	public Long getRequests();
	
	/**
	 * Returns the number of successful requests made to this resolver.
	 * @return the number of hits
	 */
	public Long getHits();
	
	/**
	 * Returns the number of unsuccessful requests made to this resolver.
	 * @return the number of misses
	 */
	public Long getMisses();
	
	/**
	 * Returns the number of rejected requests made to this resolve.
	 * @return the number of rejected requests
	 */
	public Long getRejected();
	
	/**
	 * Returns the cache hit rate percentage.
	 * @return the percentage, or zero if no requests have been made
	 */
	public Float getHitRate();
}