// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.ts3;

/**
 * A bean to store data about a TeamSpeak 3 server instance. This combines
 * data about the Server and the Virtual Server.
 * @author Luke
 * @version 6.1
 * @since 6.1
 */

public class Server {
	
	private int _id;
	private String _name;
	private int _port;
	private int _uptime;
	private int _clientsOnline;
	
	private int _pktsSent;
	private int _pktsRecv;
	private long _bytesSent;
	private long _bytesRecv;
	
	/**
	 * Creates the bean.
	 * @param name the Server name  
	 */
	public Server(String name) {
		super();
		_name = name;
	}
	
	/**
	 * Returns the virtual server ID.
	 * @return the ID
	 */
	public int getID() {
		return _id;
	}
	
	/**
	 * Returns the Server name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Returtns the server port number.
	 * @return the TCP/IP port
	 */
	public int getPort() {
		return _port;
	}
	
	/**
	 * Returtns the server port uptime.
	 * @return the uptime in seconds
	 */
	public int getUptime() {
		return _uptime;
	}
	
	/**
	 * Returtns the current number of clients.
	 * @return the number of clients
	 */
	public int getClients() {
		return _clientsOnline;
	}
	
	/**
	 * Returtns the number of packets sent by the server.
	 * @return the number of packets
	 */
	public int getPacketsSent() {
		return _pktsSent;
	}

	/**
	 * Returtns the number of packets received by the server.
	 * @return the number of packets
	 */
	public int getPacketsRecv() {
		return _pktsRecv;
	}
	
	/**
	 * Returtns the number of bytes sent by the server.
	 * @return the number of bytes
	 */
	public long getBytesSent() {
		return _bytesSent;
	}
	
	/**
	 * Returtns the number of bytes received by the server.
	 * @return the number of bytes
	 */
	public long getBytesRecv() {
		return _bytesRecv;
	}
	
	/**
	 * Updates the virtual server ID.
	 * @param id the ID
	 */
	public void setID(int id) {
		_id = Math.max(0, id);
	}
	
	/**
	 * Updates the server port.
	 * @param p the TCP/IP port number
	 */
	public void setPort(int p) {
		_port = Math.max(0, p);
	}
	
	/**
	 * Updates the server uptime.
	 * @param ut the uptime in seconds
	 */
	public void setUptime(int ut) {
		_uptime = Math.max(0, ut);
	}
	
	/**
	 * Updates the number of currently connected clients.
	 * @param cnt the number of clients
	 */
	public void setClients(int cnt) {
		_clientsOnline = Math.max(0, cnt);
	}
	
	/**
	 * Updates the number of packets sent by the server.
	 * @param pkts the number of packets
	 */
	public void setPacketsSent(int pkts) {
		_pktsSent = Math.max(0, pkts);
	}
	
	/**
	 * Updates the number of packets received by the server.
	 * @param pkts the number of packets
	 */
	public void setPacketsRecv(int pkts) {
		_pktsRecv = Math.max(0, pkts);
	}

	/**
	 * Updates the number of bytes sent by the server.
	 * @param b the number of bytes
	 */
	public void setBytesSent(long b) {
		_bytesSent = Math.max(0, b);
	}

	/**
	 * Updates the number of bytes received by the server.
	 * @param b the number of bytes
	 */
	public void setBytesRecv(long b) {
		_bytesRecv = Math.max(0, b);
	}
}