// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.Date;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to store ACARS error dumps.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ACARSError extends DatabaseBean {
	
	private int _userID;
	private Date _createdOn;
	private String _remoteAddr;
	private String _remoteHost;
	private int _clientBuild;
	private int _fsVersion;
	private String _fsuipcVersion;
	
	private String _msg;
	private String _stackDump;

	/**
	 * Creates a new error data bean.
	 * @param userID the User's database ID
	 * @param msg the error message
	 * @throws IllegalArgumentException if userID is zero or negative 
	 */
	public ACARSError(int userID, String msg) {
		super();
		setUserID(userID);
		setMessage(msg);
	}
	
	/**
	 * Returns the creation date of this error.
	 * @return the error date/time
	 * @see ACARSError#setCreatedOn(Date)
	 */
	public Date getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Returns the User who logged this error message. 
	 * @return the User's database ID
	 * @see ACARSError#setUserID(int)
	 */
	public int getUserID() {
		return _userID;
	}
	
	/**
	 * Returns the IP address of the ACARS client.
	 * @return the IP address
	 * @see ACARSError#setRemoteAddr(String)
	 */
	public String getRemoteAddr() {
		return _remoteAddr;
	}

	/**
	 * Returns the host name of the ACARS client.
	 * @return the host name
	 * @see ACARSError#setRemoteHost(String)
	 */
	public String getRemoteHost() {
		return _remoteHost;
	}
	
	/**
	 * Returns the ACARS Client build that generated this error.
	 * @return the client build number
	 * @see ACARSError#setClientBuild(int)
	 */
	public int getClientBuild() {
		return _clientBuild;
	}
	
	/**
	 * Returns the version of Flight Simulator used by the client.
	 * @return the FS version code
	 * @see ACARSError#setFSVersion(int)
	 */
	public int getFSVersion() {
		return _fsVersion;
	}
	
	/**
	 * Returns the version of FSUIPC used by the client.
	 * @return the FSUIPC version
	 * @see ACARSError#setFSUIPCVersion(String)
	 */
	public String getFSUIPCVersion() {
		return _fsuipcVersion;
	}
	
	/**
	 * Returns the error message.
	 * @return the message
	 * @see ACARSError#setMessage(String)
	 */
	public String getMessage() {
		return _msg;
	}
	
	/**
	 * Returns the stack dump information.
	 * @return the stack dump
	 * @see ACARSError#setStackDump(String)
	 */
	public String getStackDump() {
		return _stackDump;
	}

	/**
	 * Updates the User who logged this error message. 
	 * @param id the User's database ID
	 * @throws IllegalArgumentException if id is zero or negative
	 * @see ACARSError#getUserID()
	 */
	public void setUserID(int id) {
		validateID(_userID, id);
		_userID = id;
	}
	
	/**
	 * Updates the creation date of this error.
	 * @param dt the error date/time
	 * @see ACARSError#getCreatedOn()
	 */
	public void setCreatedOn(Date dt) {
		_createdOn = dt;
	}
	
	/**
	 * Updates the IP address of the ACARS client.
	 * @param addr the IP address
	 * @see ACARSError#getRemoteAddr()
	 */
	public void setRemoteAddr(String addr) {
		_remoteAddr = addr;
	}
	
	/**
	 * Updates the host name of the ACARS client.
	 * @param host the host name
	 * @see ACARSError#getRemoteHost()
	 */
	public void setRemoteHost(String host) {
		_remoteHost = host;
	}

	/**
	 * Updates the ACARS Client build that generated this error.
	 * @param ver the client build number
	 * @see ACARSError#getClientBuild()
	 */
	public void setClientBuild(int ver) {
		_clientBuild = (ver < 1) ? 1 : ver;
	}
	
	/**
	 * Updates the version of Flight Simulator used by the client.
	 * @param ver the FS version code
	 * @see ACARSError#getFSVersion()
	 */
	public void setFSVersion(int ver) {
		_fsVersion = ver;
	}
	
	/**
	 * Updates the version of FSUIPC used by the client.
	 * @param ver the FSUIPC version
	 * @see ACARSError#getFSUIPCVersion()
	 */
	public void setFSUIPCVersion(String ver) {
		_fsuipcVersion = ver;
	}
	
	/**
	 * Updates the error message.
	 * @param msg the message
	 * @see ACARSError#getMessage()
	 */
	public void setMessage(String msg) {
		_msg = msg;
	}
	
	/**
	 * Updates the stack dump information.
	 * @param info the stack dump
	 * @see ACARSError#getStackDump()
	 */
	public void setStackDump(String info) {
		_stackDump = info;
	}
}