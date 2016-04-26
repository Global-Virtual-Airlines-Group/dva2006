// Copyright 2005, 2009, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.time.Instant;

import org.deltava.beans.*;

/**
 * A bean to store system information.
 * @author Luke
 * @version 7.0
 * @since 6.4
 */

public class SystemInformation extends DatabaseBean {
	
	private Instant _created;
	
	private String _os;
	private String _clr;
	private String _net;
	private int _ram;
	
	private String _locale;
	private String _tz;
	
	private String _cpu;
	private int _cpuSpeed;
	private int _sockets;
	private int _cores;
	private int _threads;
	private boolean _is64Bit;
	
	private String _gpu;
	private String _driver;
	private boolean _isSLI;
	private int _vram;
	private int _x;
	private int _y;
	private int _bpp;
	private int _screens;

	/**
	 * Creates a new System Information bean.
	 * @param userID the user ID
	 */
	public SystemInformation(int userID) {
		super();
		setID(userID);
	}
	
	/**
	 * Returns the Installation date.
	 * @return the date/time the installer was run
	 */
	public Instant getDate() {
		return _created;
	}
	
	/**
	 * Returns the Operating System name.
	 * @return the operating system name
	 */
	public String getOSVersion() {
		return _os;
	}
	
	/**
	 * Returns the Microsoft .NET Common Language Runtime version.
	 * @return the CLR version
	 */
	public String getCLRVersion() {
		return _clr;
	}
	
	/**
	 * Returns the Microsoft .NET version.
	 * @return the .NET version
	 */
	public String getDotNETVersion() {
		return _net;
	}
	
	/**
	 * Returns the user locale.
	 * @return the locale
	 */
	public String getLocale() {
		return _locale;
	}
	
	/**
	 * Returns the user's <i>OS-specific</i> time zone.
	 * @return the time zone name
	 */
	public String getTimeZone() {
		return _tz;
	}
	
	/**
	 * Returns the name and speed of the CPU.
	 * @return the CPU data
	 */
	public String getCPU() {
		return _cpu;
	}
	
	/**
	 * Returns the CPU speed.
	 * @return the speed in megahertz
	 */
	public int getCPUSpeed() {
		return _cpuSpeed;
	}
	
	/**
	 * Returns the number of CPU sockets in the machine.
	 * @return the number of sockets
	 */
	public int getSockets() {
		return _sockets;
	}
	
	/**
	 * Returns the total number of cores in the machine.
	 * @return the number of cores
	 */
	public int getCores() {
		return _cores;
	}
	
	/**
	 * Returns the total number of logical cores in the machine.
	 * @return the number of logical cores
	 */
	public int getThreads() {
		return _threads;
	}
	
	/**
	 * Returns whether the machine uses a 64-bit operating system.
	 * @return TRUE if 64-bit, otherwise FALSE
	 */
	public boolean is64Bit() {
		return _is64Bit;
	}
	
	/**
	 * Returns whether the machine has more than 1 GPU.
	 * @return TRUE if multiple GPUs, otherwise FALSE
	 */
	public boolean isSLI() {
		return _isSLI;
	}
	
	/**
	 * Returns the name of the GPU.
	 * @return the GPU name
	 * @see SystemInformation#setGPU(String)
	 */
	public String getGPU() {
		return _gpu;
	}
	
	/**
	 * Returns the GPU driver version.
	 * @return the version
	 */
	public String getGPUDriverVersion() {
		return _driver;
	}
	
	/**
	 * Returns the video memory size.
	 * @return the memory size, in megabytes
	 */
	public int getVideoMemorySize() {
		return _vram;
	}
	
	/**
	 * Returns the screen width.
	 * @return the width in pixels
	 */
	public int getWidth() {
		return _x;
	}
	
	/**
	 * Returns the screen height.
	 * @return the height in pixels
	 */
	public int getHeight() {
		return _y;
	}
	
	/**
	 * Returns the screen color depth.
	 * @return the depth in bits per pixel
	 */
	public int getColorDepth() {
		return _bpp;
	}
	
	/**
	 * Returns the number of screens attached to the machine.
	 * @return the number of screens
	 */
	public int getScreenCount() {
		return _screens;
	}
	
	/**
	 * Returns the size of the memory.
	 * @return the memory size in megabytes
	 */
	public int getMemorySize() {
		return _ram;
	}
	
	/**
	 * Updates the CPU information.
	 * @param cpuID the CPU information
	 */
	public void setCPU(String cpuID) {
		_cpu = cpuID;
	}
	
	/**
	 * Updates the GPU information.
	 * @param gpuID the GPU information
	 */
	public void setGPU(String gpuID) {
		_gpu = gpuID;
	}
	
	/**
	 * Updates the Installation Date.
	 * @param dt the date/time the Installer was executed
	 */
	public void setDate(Instant dt) {
		_created = dt;
	}
	
	/**
	 * Updates the Operating System name.
	 * @param osName the operating system name
	 */
	public void setOSVersion(String osName) {
		_os = osName;
	}

	/**
	 * Updates the Microsoft .NET Common Language Runtime version.
	 * @param v the CLR version
	 */
	public void setCLRVersion(String v) {
		_clr = v;
	}
	
	/**
	 * Updates the Microsoft .NET Framework version.
	 * @param v the version
	 */
	public void setDotNETVersion(String v) {
		_net = v;
	}

	/**
	 * Updates the user Locale.
	 * @param l the locale
	 */
	public void setLocale(String l) {
		_locale = l;
	}
	
	/**
	 * Updates the user's time zone.
	 * @param tz the time zone name
	 */
	public void setTimeZone(String tz) {
		_tz = tz;
	}
	
	/**
	 * Updates the memory size.
	 * @param memSize the memory size in kilobytes
	 */
	public void setMemorySize(int memSize) {
		_ram = Math.max(0, memSize);
	}
	
	/**
	 * Updates the CPU speed.
	 * @param mhz the speed in megahertz
	 */
	public void setCPUSpeed(int mhz) {
		_cpuSpeed = Math.max(0,  mhz);
	}
	
	/**
	 * Updates the number of CPU sockets.
	 * @param s the number of scokets
	 */
	public void setSockets(int s) {
		_sockets = Math.max(1,  s);
	}
	
	/**
	 * Updates the number of CPU cores.
	 * @param c the number of cores
	 */
	public void setCores(int c) {
		_cores = Math.max(_sockets, c);
	}

	/**
	 * Updates the number of CPU logical cores.
	 * @param t the number of logical cores
	 */
	public void setThreads(int t) {
		_threads = Math.max(_cores, t);
	}
	
	/**
	 * Updates whether the machine is running a 64-bit Operating System.
	 * @param is64 TRUE if using a 64-bit operating system, otherwise FALSE
	 */
	public void setIs64Bit(boolean is64) {
		_is64Bit = is64;
	}

	/**
	 * Updates whether there are multiple GPUs in the machine.
	 * @param sli TRUE if multiple, otherwise FALSE
	 */
	public void setIsSLI(boolean sli) {
		_isSLI = sli;
	}

	/**
	 * Updates the GPU driver version.
	 * @param v the version
	 */
	public void setGPUDriverVersion(String v) {
		_driver = v;
	}
	
	/**
	 * Updates the video memory size.
	 * @param kb the size in megabytes
	 */
	public void setVideoMemorySize(int kb) {
		_vram = Math.max(16, kb);
	}
	
	/**
	 * Updates the screen size.
	 * @param w the screen width in pixels
	 * @param h the screen size in pixels
	 */
	public void setScreenSize(int w, int h) {
		_x = Math.max(0, w);
		_y = Math.max(0, h);
	}

	/**
	 * Updates the screen color depth.
	 * @param bpp the color depth in bits per pixel
	 */
	public void setColorDepth(int bpp) {
		_bpp = Math.max(0,  Math.min(64, bpp));
	}

	/**
	 * Updates the number of screens attached to the machine.
	 * @param s the number of screens
	 */
	public void setScreenCount(int s) {
		_screens = Math.max(1, s);
	}
}