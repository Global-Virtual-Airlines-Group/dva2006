// Copyright 2010, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

/**
 * A bean to store information about Online Network FSD Servers. 
 * @author Luke
 * @version 9.0
 * @since 3.4
 */

public class Server implements java.io.Serializable, Comparable<Server> {
	
	private final String _name;
	private String _addr;
	
	private String _location;
	private String _comment;
	
	private int _connections;

	/**
	 * Creates a new Server bean.
	 * @param name the server name
	 * @throws NullPointerException if name is null 
	 */
	public Server(String name) {
		super();
		_name = name.toUpperCase();
	}

	/**
	 * Returns the server name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Returns the server address.
	 * @return the IP address
	 */
	public String getAddress() {
		return _addr;
	}
	
	/**
	 * Returns the server location.
	 * @return the location
	 */
	public String getLocation() {
		return _location;
	}

	/**
	 * Returns the server comment.
	 * @return the comment
	 */
	public String getComment() {
		return _comment;
	}
	
	/**
	 * Returns the number of connections to this FSD server.
	 * @return the number of connections
	 */
	public int getConnections() {
		return _connections;
	}
	
	/**
	 * Updates the server address.
	 * @param addr the IP address
	 */
	public void setAddress(String addr) {
		_addr = addr;
	}
	
	/**
	 * Updates the server location.
	 * @param loc the location
	 */
	public void setLocation(String loc) {
		_location = loc;
	}
	
	/**
	 * Updates the server comment.
	 * @param comment the comment
	 */
	public void setComment(String comment) {
		_comment = comment;
	}
	
	/**
	 * Updates the number of connections to this FSD server.
	 * @param cons the number of connections
	 */
	public void setConnections(int cons) {
		_connections = Math.max(0, cons);
	}
	
	@Override
	public int hashCode() {
		return _name.hashCode();
	}

	/**
	 * Compares two servers by comparing their names.
	 */
	@Override
	public int compareTo(Server s2) {
		return _name.compareTo(s2._name);
	}
}