// Copyright 2006, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * A bean to store codeshare flight number data.
 * @author Luke
 * @version 3.0
 * @since 1.0
 */

public class PartnerAirline {

	private Airline _a;
	private int _start;
	private int _end;
	
	private String _fileName;

	/**
	 * Special entry to ignore/discard imported schedule data.
	 */
	public static final PartnerAirline IGNORE = new PartnerAirline(new Airline("IGNORE"), 0, 0);
	
	/**
	 * Populates the bean.
	 * @param a the Airline bean
	 * @param startFlight the first flight number served by this Airline
	 * @param endFlight the first flight number served by this Airline
	 * @param fileName the import file name to apply this partner airline mapping to
	 */
	public PartnerAirline(Airline a, int startFlight, int endFlight, String fileName) {
		super();
		_a = a;
		_start = startFlight;
		_end = endFlight;
		_fileName = fileName;
	}
	
	/**
	 * Populates the bean for alll import filenames.
	 * @param a the Airline bean
	 * @param startFlight the first flight number served by this Airline
	 * @param endFlight the first flight number served by this Airline
	 */
	PartnerAirline(Airline a, int startFlight, int endFlight) {
		this(a, startFlight, endFlight, "*");
	}

	/**
	 * Returns wether a flight number maps to this Airline.
	 * @param flightNumber the flight number
	 * @return TRUE if the Airline serves this flight, otherwise FALSE
	 */
	public boolean contains(int flightNumber) {
		return ((flightNumber >= _start) && (flightNumber <= _end));
	}

	/**
	 * Returns the Airline bean.
	 * @return the Airline
	 */
	public Airline getAirline() {
		return _a;
	}
	
	/**
	 * Returns wether this Partner Airline data applies to a particular import file.
	 * @param fileName the import file name
	 * @return TRUE if this data applies to the import file, otherwise FALSE
	 */
	public boolean includesFile(String fileName) {
		return "*".equals(_fileName) || _fileName.equalsIgnoreCase(fileName);
	}
	
	/**
	 * Sets the import file name associated with this partner airline mapping.
	 * @param fName the file name, or null/* if applicable to all
	 */
	public void setFileName(String fName) {
		_fileName = (fName == null) ? "*" : fName;
	}

	/**
	 * Compares two Partner Airlines by comparing the airline codes.
	 */
	public boolean equals(Object o2) {
		if (o2 instanceof PartnerAirline) {
			PartnerAirline pa2 = (PartnerAirline) o2;
			return _a.equals(pa2._a);
		} else if (o2 instanceof String)
			return _a.getCode().equals(o2);
		
		return false;
	}
	
	public int hashCode() {
		return _a.hashCode();
	}
	
	/**
	 * Returns the airline code, flight range and file name.
	 */
	public String toString() {
		StringBuilder buf = new StringBuilder(_a.getCode());
		buf.append(' ');
		buf.append(_start);
		buf.append('-');
		buf.append(_end);
		buf.append(", ");
		buf.append(_fileName);
		return buf.toString();
	}
}