// Copyright 2012, 2014, 2016, 2017, 2018, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;

import org.deltava.beans.acars.*;
import org.deltava.beans.servinfo.Controller;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to serialize ACARS position records.
 * @author Luke
 * @version 9.1
 * @since 4.1
 */

public class SetSerializedPosition extends WriteableDAO {

	/**
	 * Initializes the Data Access Object.
	 * @param os the OutputStream to write to
	 */
	public SetSerializedPosition(OutputStream os) {
		super(os);
	}

	/**
	 * Serializes ACARS position records.
	 * @param flightID the ACARS Flight ID
	 * @param positions a Collection of ACARSRouteEntry beans
	 * @return the SerializedDataVersion used to archive these position records, or null if none
	 * @throws DAOException if an I/O error occurs
	 */
	public SerializedDataVersion archivePositions(int flightID, Collection<? extends RouteEntry> positions) throws DAOException {
		if (positions.isEmpty()) return null;
		RouteEntry re = positions.iterator().next();
		SerializedDataVersion ver = (re instanceof ACARSRouteEntry) ? SerializedDataVersion.ACARSv9 : SerializedDataVersion.XACARS;
		try (DataOutputStream out = new DataOutputStream(_os)) {
			out.writeShort(ver.ordinal());
			out.writeInt(flightID);
			out.writeInt(positions.size());
			for (RouteEntry rte : positions) {
				if (ver.isXACARS())
					write((XARouteEntry) rte, out);
				else
					write((ACARSRouteEntry) rte, out);
			}
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
		
		return ver;
	}
	
	/*
	 * Helper method to write an ACARS position entry.
	 */
	private static void write(ACARSRouteEntry re, DataOutputStream out) throws IOException {
		out.writeDouble(re.getLatitude());
		out.writeDouble(re.getLongitude());
		out.writeLong(re.getDate().toEpochMilli());
		out.writeShort(re.getPhase().ordinal());
		out.writeInt(re.getFlags());
		out.writeInt(re.getAltitude());
		out.writeShort(re.getHeading());
		out.writeShort(re.getAirSpeed());
		out.writeShort(re.getGroundSpeed());
		out.writeFloat((float) re.getMach());
		out.writeShort(re.getWindSpeed());
		out.writeShort(re.getWindHeading());
		out.writeInt(re.getFuelRemaining());
		out.writeInt(re.getRadarAltitude());
		out.writeFloat((float) re.getPitch());
		out.writeFloat((float) re.getBank());
		out.writeShort(re.getVerticalSpeed());
		out.writeFloat((float) re.getAOA());
		out.writeFloat((float) re.getG());
		out.writeShort((short)re.getEngineCount()); // v9
		out.writeDouble(re.getN1()); // was float in v8-
		out.writeDouble(re.getN2()); // was float in v8-
		for (int x = 0; x < re.getEngineCount(); x++) { // v9
			out.writeDouble(re.getN1(x));
			out.writeDouble(re.getN2(x));
		}
		
		out.writeFloat((float) re.getVisibility());
		out.writeInt(re.getFuelFlow());
		out.writeShort(re.getFlaps());
		out.writeShort(re.getFrameRate());
		out.writeShort(re.getSimRate());
		out.writeShort(re.getTemperature()); // v3
		out.writeInt(re.getPressure()); // v3
		out.writeLong(re.getSimUTC().toEpochMilli()); // v3
		out.writeInt(re.getVASFree()); // v4
		out.writeShort(re.getAirspace().ordinal()); // v41
		out.writeInt(re.getWeight()); // v42
		out.writeUTF((re.getNAV1() == null) ? "" : re.getNAV1());
		out.writeUTF((re.getNAV2() == null) ? "" : re.getNAV2());
		out.writeUTF((re.getADF1() == null) ? "" : re.getADF1()); // v6
		
		// encode two booleans into a single byte, second bit is set if DISCONNECTED
		int b = re.getNetworkConnected() ? 1 : 0;
		if (!re.getACARSConnected())
			b += 2;
		
		out.write(b); // v7
		out.writeInt(re.getGroundOperations()); // v8
		out.writeFloat((float) re.getCG());
		
		// Write ATC1
		Controller atc = re.getATC1();
		if (atc != null) {
			out.writeUTF(re.getCOM1());
			out.writeInt(atc.getID());
			out.writeShort(atc.getFacility().ordinal());
			out.writeUTF(atc.getCallsign());
			out.writeFloat((float) atc.getLatitude());
			out.writeFloat((float) atc.getLongitude());
		} else
			out.writeUTF("");
		
		// Write ATC2
		atc = re.getATC2();
		if (atc != null) {
			out.writeUTF(re.getCOM2());
			out.writeInt(atc.getID());
			out.writeShort(atc.getFacility().ordinal());
			out.writeUTF(atc.getCallsign());
			out.writeFloat((float) atc.getLatitude());
			out.writeFloat((float) atc.getLongitude());
		} else
			out.writeUTF("");
	}
	
	/*
	 * Helper method to write an XACARS position entry.
	 */
	private static void write(XARouteEntry re, DataOutputStream out) throws IOException {
		out.writeDouble(re.getLatitude());
		out.writeDouble(re.getLongitude());
		out.writeLong(re.getDate().toEpochMilli());
		out.writeShort(re.getPhase().ordinal());
		out.writeInt(re.getFlags());
		out.writeInt(re.getAltitude());
		out.writeShort(re.getHeading());
		out.writeShort(re.getAirSpeed());
		out.writeShort(re.getGroundSpeed());
		out.writeFloat((float) re.getMach());
		out.writeShort(re.getWindSpeed());
		out.writeShort(re.getWindHeading());
		out.writeInt(re.getFuelRemaining());
		out.writeShort(re.getVerticalSpeed());
	}
}