// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.fleet;

import java.util.Date;

/**
 * A bean to store system information reported by Fleet Installers.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SystemInformation implements java.io.Serializable, Comparable<SystemInformation> {
	
	private String _id;
	private String _code;
	private Date _created;
	private int _memory;
	private String _cpu;
	private String _gpu;
	private String _os;
	private String _dx;
	private int _fsVersion;

	/**
	 * Creates a new System Information bean.
	 * @param id the user ID
	 * @throws NullPointerException if id is null
	 * @see SystemInformation#getID()
	 */
	public SystemInformation(String id) {
		super();
		_id = id.trim();
	}
	
	/**
	 * Returns the System Information user ID.
	 * @return the user ID
	 * @see SystemInformation#SystemInformation(String)
	 */
	public String getID() {
		return _id;
	}
	
	/**
	 * Returns the Installation date.
	 * @return the date/time the installer was run
	 * @see SystemInformation#getDate()
	 */
	public Date getDate() {
		return _created;
	}
	
	/**
	 * Returns the Installer code.
	 * @return the installer code
	 * @see SystemInformation#setCode(String)
	 */
	public String getCode() {
		return _code;
	}
	
	/**
	 * Returns the Operating System name.
	 * @return the operating system name
	 * @see SystemInformation#setOS(String)
	 */
	public String getOS() {
		return _os;
	}
	
	/**
	 * Returns the name and speed of the CPU.
	 * @return the CPU data
	 * @see SystemInformation#setCPU(String)
	 */
	public String getCPU() {
		return _cpu;
	}
	
	/**
	 * Returns the name and speed of the GPU.
	 * @return the GPU data
	 * @see SystemInformation#setGPU(String)
	 */
	public String getGPU() {
		return _gpu;
	}
	
	/**
	 * Returns the size of the memory.
	 * @return the memory size in kilobytes
	 * @see SystemInformation#setRAM(int)
	 */
	public int getRAM() {
		return _memory;
	}
	
	/**
	 * Returns the DirectX version.
	 * @return the DirectX version string
	 * @see SystemInformation#setDirectX(String)
	 */
	public String getDirectX() {
		return _dx;
	}
	
	/**
	 * Returns the Microsoft Flight Simulator version.
	 * @return the Flight Simulator version
	 * @see SystemInformation#setFSVersion(int)
	 */
	public int getFSVersion() {
		return _fsVersion;
	}
	
	/**
	 * Updates the Installer code.
	 * @param code the Installer code
	 * @throws NullPointerException if code is null
	 * @see SystemInformation#getCode()
	 */
	public void setCode(String code) {
		_code = code.trim().toUpperCase();
	}

	/**
	 * Updates the CPU information.
	 * @param cpuID the CPU information
	 * @see SystemInformation#getCPU()
	 */
	public void setCPU(String cpuID) {
		_cpu = cpuID;
	}
	
	/**
	 * Updates the GPU information.
	 * @param gpuID the GPU information
	 * @see SystemInformation#getGPU()
	 */
	public void setGPU(String gpuID) {
		_gpu = gpuID;
	}
	
	/**
	 * Updates the Installation Date.
	 * @param dt the date/time the Installer was executed
	 * @see SystemInformation#getDate()
	 */
	public void setDate(Date dt) {
		_created = dt;
	}
	
	/**
	 * Updates the DirectX version.
	 * @param dxID the DirectX version string
	 * @see SystemInformation#getDirectX()
	 */
	public void setDirectX(String dxID) {
		_dx = dxID;
	}
	
	/**
	 * Updates the Operating System name.
	 * @param osName the operating system name
	 * @see SystemInformation#getOS()
	 */
	public void setOS(String osName) {
		_os = osName;
	}
	
	/**
	 * Updates the memory size.
	 * @param memSize the memory size in kilobytes
	 * @throws IllegalArgumentException if memSize is zero or negative
	 * @see SystemInformation#getRAM()
	 */
	public void setRAM(int memSize) {
		if (memSize <= 0)
			throw new IllegalArgumentException("Invalid Memory Size - " + memSize);
		
		_memory = memSize;
	}
	
	/**
	 * Updates the Microsoft Flight Simulator version.
	 * @param version the version number
	 * @throws IllegalArgumentException if version is negative
	 * @see SystemInformation#getFSVersion()
	 */
	public void setFSVersion(int version) {
		if (version < 0)
			throw new IllegalArgumentException("Invalid Flight Simulator version - " + version);
		
		_fsVersion = version;
	}

	/**
	 * Compares to another SystemInformation bean by comparing the ID and the date.
	 */
	public int compareTo(SystemInformation si2) {
		int tmpResult = _id.compareTo(si2._id);
		return (tmpResult == 0) ? _created.compareTo(si2._created) : tmpResult;
	}
}