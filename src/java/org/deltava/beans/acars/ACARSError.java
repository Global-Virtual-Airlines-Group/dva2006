// Copyright 2006, 2009, 2012, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.time.Instant;

import org.deltava.beans.*;

/**
 * A bean to store ACARS client error logs.
 * @author Luke
 * @version 8.6
 * @since 1.0
 */

public class ACARSError extends DatabaseBlobBean implements ClientVersion, AuthoredBean, ViewEntry {
	
	private int _userID;
	private Instant _createdOn;
	private String _remoteAddr;
	private String _remoteHost;
	private int _version;
	private int _clientBuild;
	private int _beta;
	
	private Simulator _sim;
	private String _pluginVersion;
	private String _bridgeVersion;
	
	private ClientType _type;
	
	private String _msg;
	private String _stackDump;
	private String _stateData;
	
	private String _osVersion;
	private boolean _is64Bit;
	
	private String _clrVersion;
	private String _locale;
	private String _tz;
	
	private boolean _isInfo;
	
	/**
	 * Creates a new error data bean.
	 * @param userID the User's database ID
	 * @param msg the error message
	 * @throws IllegalArgumentException if userID is zero or negative 
	 */
	public ACARSError(int userID, String msg) {
		super();
		setAuthorID(userID);
		setMessage(msg);
	}
	
	/**
	 * Returns the creation date of this error.
	 * @return the error date/time
	 * @see ACARSError#setCreatedOn(Instant)
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}
	
	@Override
	public int getAuthorID() {
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
	 * Returns the operating system version.
	 * @return the OS version
	 * @see ACARSError#setOSVersion(String)
	 */
	public String getOSVersion() {
		return _osVersion;
	}
	
	/**
	 * Returns the .NET Common Language Runtime version.
	 * @return the CLR version
	 * @see ACARSError#setCLRVersion(String)
	 */
	public String getCLRVersion() {
		return _clrVersion;
	}
	
	/**
	 * Returns whether the ACARS client was running on a 64-bit operating system.
	 * @return TRUE if 64-bit, otherwise FALSE
	 * @see ACARSError#setIs64Bit(boolean)
	 */
	public boolean getIs64Bit() {
		return _is64Bit;
	}
	
	/**
	 * Returns the user's locale.
	 * @return the locale name
	 * @see ACARSError#setLocale(String)
	 */
	public String getLocale() {
		return _locale;
	}
	
	/**
	 * Returns the user's time zone name.
	 * @return the <i>OS-specific</i> time zone name
	 * @see ACARSError#setTimeZone(String)
	 */
	public String getTimeZone() {
		return _tz;
	}
	
	/**
	 * Returns the ACARS Client build that generated this error.
	 * @return the client build number
	 * @see ACARSError#setClientBuild(int)
	 */
	@Override
	public int getClientBuild() {
		return _clientBuild;
	}
	
	/**
	 * Returns the ACARS Client beta build that generated this error.
	 * @return the beta number
	 * @see ACARSError#setBeta(int) 
	 */
	@Override
	public int getBeta() {
		return _beta;
	}
	
	/**
	 * Returns whether this is an informational submission only.
	 * @return TRUE if informational, otherwise FALSE
	 */
	public boolean getIsInfo() {
		return _isInfo;
	}
	
	/**
	 * Returns the ACARS client type.
	 * @return the ClienTtype
	 * @see ACARSError#setClientType(ClientType)
	 */
	@Override
	public ClientType getClientType() {
		return _type;
	}
	
	/**
	 * Returns the ACARS major version.
	 * @return the major version number
	 * @see ACARSError#setVersion(int)
	 */
	@Override
	public int getVersion() {
		return _version;
	}
	
	/**
	 * Returns the Simulator used by the client.
	 * @return the Simulator
	 * @see ACARSError#setSimulator(Simulator)
	 */
	public Simulator getSimulator() {
		return _sim;
	}
	
	/**
	 * Returns the version of simulator plugin used by the client.
	 * @return the version
	 * @see ACARSError#setPluginVersion(String)
	 */
	public String getPluginVersion() {
		return _pluginVersion;
	}
	
	/**
	 * Returns the version of simulator bridge used by the client.
	 * @return the version
	 * @see ACARSError#setBridgeVersion(String)
	 */
	public String getBridgeVersion() {
		return _bridgeVersion;
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
	 * Returns the state data.
	 * @return the state data
	 * @see ACARSError#setStateData(String)
	 */
	public String getStateData() {
		return _stateData;
	}
	
	/**
	 * Returns the log data.
	 * @return the log data, or null if none
	 */
	public byte[] getLogData() {
		return _buffer;
	}

	@Override
	public void setAuthorID(int id) {
		validateID(_userID, id);
		_userID = id;
	}
	
	/**
	 * Updates whether this is an informational log message.
	 * @param isInfo TRUE if informational, otherwise FALSE
	 */
	public void setIsInfo(boolean isInfo) {
		_isInfo = isInfo;
	}
	
	/**
	 * Updates the creation date of this error.
	 * @param dt the error date/time
	 * @see ACARSError#getCreatedOn()
	 */
	public void setCreatedOn(Instant dt) {
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
		_clientBuild = Math.max(1, ver);
	}
	
	/**
	 * Updates the ACARS Client beta version that generated this error.
	 * @param beta the beta version
	 * @see ACARSError#getBeta()
	 */
	public void setBeta(int beta) {
		_beta = Math.max(0, beta);
	}
	
	/**
	 * Updates the Simulator used by the client.
	 * @param sim the Simulator
	 * @see ACARSError#getSimulator()
	 */
	public void setSimulator(Simulator sim) {
		_sim = sim;
	}
	
	/**
	 * Updates the version of simulator plugin used by the client.
	 * @param ver the plugin version
	 * @see ACARSError#getPluginVersion()
	 */
	public void setPluginVersion(String ver) {
		_pluginVersion = ver;
	}
	
	/**
	 * Updates the version of the Brdige used by the client.
	 * @param ver the Bridge version
	 * @see ACARSError#getBridgeVersion()
	 */
	public void setBridgeVersion(String ver) {
		_bridgeVersion = ver;
	}
	
	/**
	 * Sets the ACARS client type.
	 * @param ct the ClientType
	 * @see ACARSError#getClientType()
	 */
	public void setClientType(ClientType ct) {
		_type = ct;
	}
	
	/**
	 * Sets the ACARS client major version.
	 * @param ver the major version
	 * @see ACARSError#getVersion()
	 */
	public void setVersion(int ver) {
		_version = ver;
	}
	
	/**
	 * Sets the operating system version.
	 * @param v the version
	 * @see ACARSError#getOSVersion()
	 */
	public void setOSVersion(String v) {
		_osVersion = String.valueOf(v);
	}
	
	/**
	 * Sets whether the operating system is 64-bit.
	 * @param is64 TRUE if 64-bit, otherwsie FALSE
	 * @see ACARSError#getIs64Bit()
	 */
	public void setIs64Bit(boolean is64) {
		_is64Bit = is64;
	}
	
	/**
	 * Sets the .NET CLR version.
	 * @param v the version
	 * @see ACARSError#getCLRVersion()
	 */
	public void setCLRVersion(String v) {
		_clrVersion = String.valueOf(v);
	}
	
	/**
	 * Sets the client locale name.
	 * @param l the locale
	 * @see ACARSError#getLocale()
	 */
	public void setLocale(String l) {
		_locale = String.valueOf(l);
	}
	
	/**
	 * Updates the client time zone.
	 * @param tz the <i>OS-specific</i> time zone name
	 * @see ACARSError#getTimeZone()
	 */
	public void setTimeZone(String tz) {
		_tz = String.valueOf(tz);
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
	
	/**
	 * Updates the state data.
	 * @param data the state data
	 * @see ACARSError#getStateData()
	 */
	public void setStateData(String data) {
		_stateData = data;
	}

	@Override
	public String getRowClassName() {
		return _isInfo ? "opt2" : null;
	}
}