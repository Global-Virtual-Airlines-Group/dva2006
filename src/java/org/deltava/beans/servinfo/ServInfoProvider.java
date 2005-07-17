// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.servinfo;

/**
 * A marker interface to let external classes (like in ACARS) provide ServInfo data to
 * the web site application.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */
public interface ServInfoProvider {

	public NetworkInfo getNetworkInfo();
	public NetworkStatus getNetworkStatus();
}