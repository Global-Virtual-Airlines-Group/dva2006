// Copyright 2011, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

/**
 * An interface for version/build/beta tuples. 
 * @author Luke
 * @version 7.5
 * @since 4.1
 */

public interface ClientVersion {

	/**
	 * Returns the version number.
	 * @return the version
	 */
	public int getVersion();
		
	/**
	 * Returns the build number.
	 * @return the build
	 */
	public int getClientBuild();
	
	/**
	 * Returns the beta version.
	 * @return the beta, or zero if non-beta
	 */
	public int getBeta();
	
	/**
	 * Returns the client type.
	 * @return the type
	 */
	public ClientType getClientType();
	
	/**
	 * Returns whether this is a beta build.
	 * @return TRUE if beta, otherwise FALSE
	 */
	default boolean isBeta() {
		return ((getBeta() > 0) && (getBeta() < Integer.MAX_VALUE));
	}
}