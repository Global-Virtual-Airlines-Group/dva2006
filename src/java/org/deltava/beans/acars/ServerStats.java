// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.*;

/**
 * A class to store ACARS Server statistics.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public final class ServerStats implements java.io.Serializable {
	
	private static Date _startDate;
	private static int _authCount;
	
	private static int _maxConnects;
	private static int _connectCount;
	
	private static long _bytesIn;
	private static long _bytesOut;
	
	private static long _msgsIn;
	private static long _msgsOut;

	// Singleton
	private ServerStats() {
	}
	
	public static ServerStats getInstance() {
		return new ServerStats();
	}
	
	/**
	 * Returns the ACARS server start date.
	 * @return the date/time the server was started
	 * @see ServerStats#authenticate()
	 */
	public static Date getStartDate() {
		return _startDate;
	}
	
	/**
	 * Returns the number of authentications the server has performed.
	 * @return the authentication count
	 * @see ServerStats#authenticate()
	 */
	public static int getAuthentications() {
		return _authCount;
	}
	
	/**
	 * Returns the number of current ACARS connections.
	 * @return the number of connections
	 */
	public static int getConnections() {
		return _connectCount;
	}
	
	/**
	 * Returns the maximum number of concurrent ACARS connections.
	 * @return the maximum number of connections
	 */
	public static int getMaxConnections() {
		return _maxConnects;
	}
	
	public static long getBytesIn() {
		return _bytesIn;
	}
	
	public static long getBytesOut() {
		return _bytesOut;
	}
	
	public static long getMsgsIn() {
		return _msgsIn;
	}
	
	public static long getMsgsOut() {
		return _msgsOut;
	}
	
	/**
	 * Marks the ACARS server as started.
	 */
	public static void start() {
		_startDate = new Date();
	}
	
	/**
	 * Logs a connection.
	 */
	public static synchronized void connect() {
		_connectCount++;
		if (_connectCount > _maxConnects)
			_maxConnects = _connectCount;
	}

	/**
	 * Logs an authentication.
	 */
	public static synchronized void authenticate() {
		_authCount++;
	}

	/**
	 * Logs a disconnection.
	 */
	public static synchronized void disconnect() {
		if (_connectCount > 0)
			_connectCount--;
	}
	
	/**
	 * Logs an inbound message.
	 * @param length the message length in bytes
	 */
	public static synchronized void msgIn(int length) {
		_msgsIn++;
		_bytesIn += length;
	}

	/**
	 * Logs an outbound message.
	 * @param length the message length in bytes
	 */
	public static synchronized void msgOut(int length) {
		_msgsOut++;
		_bytesOut += length;
	}
}