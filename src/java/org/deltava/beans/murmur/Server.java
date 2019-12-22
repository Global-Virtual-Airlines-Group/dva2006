// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.murmur;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to store information about a Murmur virtual server. 
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class Server extends DatabaseBean {
	
	private final String _name;
	private int _port;
	
	/**
	 * Creates the bean.
	 * @param name the server name 
	 */
	public Server(String name) {
		super();
		_name = name;
	}

	/**
	 * Returns the server name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the server UDP port.
	 * @return the port number
	 */
	public int getPort() {
		return _port;
	}
	
	/**
	 * Updates the server UDP port.
	 * @param port the port number
	 */
	public void setPort(int port) {
		_port = port;
	}
}