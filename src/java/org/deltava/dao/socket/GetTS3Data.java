// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.socket;

import java.io.*;
import java.util.*;

import org.deltava.beans.ts3.*;

import org.deltava.dao.DAOException;
import org.deltava.util.StringUtils;

/**
 * A Data Access Object to query the TS3 Server Query interface. 
 * @author Luke
 * @version 6.1
 * @since 6.1
 */

public class GetTS3Data extends TS3ServerQueryDAO {

	/**
	 * Returns Teamspeak 3 server information.
	 * @return a Server
	 * @throws DAOException if an error occurs
	 */
	public Server getServerInfo() throws DAOException {
		try {
			login();
			ServerQueryResponse rsp = exec("serverlist");
			Server srv = new Server(rsp.get("virtualserver_name"));
			srv.setPort(StringUtils.parse(rsp.get("virtualserver_port"), 0));
			srv.setUptime(StringUtils.parse(rsp.get("virtualserver_uptime"), 0));
			srv.setClients(StringUtils.parse(rsp.get("virtualserver_clients"), 0));
			srv.setID(StringUtils.parse(rsp.get("virtualserver_id"), 1));
			rsp = exec("serverinfo");
			srv.setPacketsSent(StringUtils.parse(rsp.get("connection_packets_sent_total"), 0));
			srv.setPacketsSent(StringUtils.parse(rsp.get("connection_packets_received_total"), 0));
			srv.setBytesSent(StringUtils.parse(rsp.get("connection_bytes_sent_total"), 0));
			srv.setBytesRecv(StringUtils.parse(rsp.get("connection_bytes_received_total"), 0));
			logout();
			return srv;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
	
	/**
	 * Returns the Channels on a Server.
	 * @param s the Server
	 * @return a Collection of Channels
	 * @throws DAOException if an error occurs
	 */
	public Collection<Channel> getChannels(Server s) throws DAOException {
		try {
			login();
			ServerQueryResponse rsp = exec("use {0}", Integer.valueOf(s.getID()));
			rsp = exec("channellist");
			Channel c = new Channel(rsp.get("name"));
			logout();
			return Collections.singleton(c);
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}