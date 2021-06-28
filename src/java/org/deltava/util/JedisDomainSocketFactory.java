// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.io.*;
import java.net.Socket;

import org.newsclub.net.unix.*;

import redis.clients.jedis.*;

/**
 * A Jedis socket factory to create domain socket connections.  
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

class JedisDomainSocketFactory implements JedisSocketFactory {
	
	private String _socketFile;

	JedisDomainSocketFactory(String addr) {
		_socketFile = addr;
	}
	
	/**
	 * Creates a socket to the given endpoint.
	 * @throws IOException if the socket cannot be found or connected to
	 */
	@Override
	public Socket createSocket() throws IOException {
		
		// Check for the socket
		File f = new File(_socketFile);
		if (!f.exists())
			throw new FileNotFoundException(f.getAbsolutePath());
		
		AFUNIXSocket sock = AFUNIXSocket.newInstance();
		sock.connect(new AFUNIXSocketAddress(f));
		return sock;
	}

	@Deprecated
	@Override
	public int getConnectionTimeout() {
		return 0;
	}

	@Deprecated
	@Override
	public String getDescription() {
		return String.format("Socket Factory - %s", _socketFile);
	}

	/* (non-Javadoc)
	 * @see redis.clients.jedis.JedisSocketFactory#getHost()
	 */
	@Deprecated
	@Override
	public String getHost() {
		return _socketFile;
	}

	/* (non-Javadoc)
	 * @see redis.clients.jedis.JedisSocketFactory#getPort()
	 */
	@Override
	@Deprecated
	public int getPort() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see redis.clients.jedis.JedisSocketFactory#getSoTimeout()
	 */
	@Deprecated
	@Override
	public int getSoTimeout() {
		return 0;
	}

	@Deprecated
	@Override
	public void setConnectionTimeout(int timeout) {
		// empty
	}

	@Deprecated
	@Override
	public void setHost(String host) {
		_socketFile = host;
	}

	@Override
	@Deprecated
	public void setPort(int p) {
		// empty
	}

	@Override
	@Deprecated
	public void setSoTimeout(int arg0) {
		// empty
	}

	@Override
	public void updateHostAndPort(HostAndPort hp) {
		_socketFile = hp.getHost();
	}
}