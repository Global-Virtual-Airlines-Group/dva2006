// Copyright 2008, 2010, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

/**
 * An abstract class to for objects that can return ACARS connection statistics.
 * @author Luke
 * @version 6.4
 * @since 2.1
 */

public abstract class ConnectionStats implements java.io.Serializable, Comparable<ConnectionStats> {
	
	private String _id;
	
	protected int _bufferReads;
	protected int _bufferWrites;
	
	protected int _msgsIn;
	protected int _msgsOut;
	
	protected long _bytesIn;
	protected long _bytesOut;
	protected long _bytesSaved;
	
	protected int _writeErrors;
	
	/**
	 * Creates a new statistics object.
	 * @param id the ID
	 */
	protected ConnectionStats(String id) {
		super();
		_id = id;
	}
	
	protected ConnectionStats(ConnectionStats cs) {
		this(cs.getID());
		_msgsIn = cs._msgsIn;
		_msgsOut = cs._msgsOut;
		_bufferReads = cs._bufferReads;
		_bufferWrites = cs._bufferWrites;
		_bytesIn = cs._bytesIn;
		_bytesOut = cs._bytesOut;
		_bytesSaved = cs._bytesSaved;
		_writeErrors = cs._writeErrors;
	}
	
	/**
	 * Returns the ACARS connection ID.
	 * @return the protocol and connection ID
	 */
	public String getID() {
		return _id;
	}

	/**
	 * Returns the number of messages received on this connection.
	 * @return the number of messages
	 */
	public int getMsgsIn() {
		return _msgsIn;
	}
	
	/**
	 * Returns the number of messages sent on this connection.
	 * @return the number of messages
	 */
	public int getMsgsOut() {
		return _msgsOut;
	}
	
	/**
	 * Returns the number of buffer reads made on this connection.
	 * @return the number of reads
	 */
	public int getBufferReads() {
		return _bufferReads;
	}

	/**
	 * Returns the number of buffer writes made on this connection.
	 * @return the number of writes
	 */
	public int getBufferWrites() {
		return _bufferWrites;
	}
	
	/**
	 * Returns the number of bytes received on this connection.
	 * @return the number of bytes
	 */
	public long getBytesIn() {
		return _bytesIn;
	}
	
	/**
	 * Returns the number of bytes sent on this connection.
	 * @return the number of bytes
	 */
	public long getBytesOut() {
		return _bytesOut;
	}
	
	/**
	 * Returns the number of bytes saved via compression.
	 * @return the number of bytes
	 */
	public long getBytesSaved() {
		return _bytesSaved;
	}

	/**
	 * Returns the number of write errors on this connection.
	 * @return the number of timed out writes
	 */
	public int getWriteErrors() {
		return _writeErrors;
	}

	@Override
	public int hashCode() {
		return _id.hashCode();
	}
	
	@Override
	public int compareTo(ConnectionStats cs2) {
		int tmpResult = _id.compareTo(cs2._id);
		return (tmpResult == 0) ? getClass().getSimpleName().compareTo(cs2.getClass().getSimpleName()) : tmpResult;
	}
}