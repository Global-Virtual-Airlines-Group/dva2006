// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

/**
 * An interface for version/build/beta tuples. 
 * @author Luke
 * @version 4.1
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
}