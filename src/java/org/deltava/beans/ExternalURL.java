// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface to describe objects linking to an external resource. 
 * @author Luke
 * @version 10.4
 * @since 10.4
 */

public interface ExternalURL {

	/**
	 * Returns the URL.
	 * @return the URL
	 */
	public String getURL();
	
	/**
	 * Returns the URL Title.
	 * @return the title
	 */
	public String getTitle();
}