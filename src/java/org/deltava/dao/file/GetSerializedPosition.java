// Copyright 2012, 2014, 2016, 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.time.Instant;
import java.util.*;

import org.deltava.beans.acars.*;
import org.deltava.beans.navdata.AirspaceType;
import org.deltava.beans.servinfo.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object to deserialize ACARS/XACARS position records.  
 * @author Luke
 * @version 8.3
 * @since 4.1
 */

public class GetSerializedPosition extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param is the InputStream to read from
	 */
	public GetSerializedPosition(InputStream is) {
		super(is);
	}

	/**
	 * Deserializes ACARS/XACARS position reports.
	 * @return a Collection of RouteEntry beans
	 * @throws DAOException if an I/O error occurs
	 */
	public Collection<? extends RouteEntry> read() throws DAOException {
		try (DataInputStream in = new DataInputStream(getStream())) {
			SerializedDataVersion ver = SerializedDataVersion.values()[in.readShort()];
			in.readInt(); // flight ID
			return (ver != SerializedDataVersion.XACARS) ? loadACARS(in, ver) : loadXACARS(in);
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
	
	private static Collection<ACARSRouteEntry> loadACARS(DataInputStream in, SerializedDataVersion version) throws IOException {
		int size = in.readInt();
		Collection<ACARSRouteEntry> results = new ArrayList<ACARSRouteEntry>(size + 2);
		for (int x = 0; x < size; x++) {
			GeoPosition gp = new GeoPosition(in.readDouble(), in.readDouble());
			ACARSRouteEntry re = new ACARSRouteEntry(Instant.ofEpochMilli(in.readLong()), gp);
			re.setPhase(in.readShort());
			re.setFlags(in.readInt());
			re.setAltitude(in.readInt());
			re.setHeading(in.readShort());
			re.setAirSpeed(in.readShort());
			re.setGroundSpeed(in.readShort());
			re.setMach(in.readFloat());
			re.setWindSpeed(in.readShort());
			re.setWindHeading(in.readShort());
			re.setFuelRemaining(in.readInt());
			re.setRadarAltitude(in.readInt());
			re.setPitch(in.readFloat());
			re.setBank(in.readFloat());
			re.setVerticalSpeed(in.readShort());
			re.setAOA(in.readFloat());
			re.setG(in.readFloat());
			re.setN1(in.readFloat());
			re.setN2(in.readFloat());
			re.setVisibility(in.readFloat());
			re.setFuelFlow(in.readInt());
			re.setFlaps(in.readShort());
			re.setFrameRate(in.readShort());
			re.setSimRate(in.readShort());
			
			// Load weather and sim time
			if (version.getVersion() > 2) {
				re.setTemperature(in.readShort());
				re.setPressure(in.readInt());
				re.setSimUTC(Instant.ofEpochMilli(in.readLong()));
				if (version.getVersion() > 3)
					re.setVASFree(in.readInt());
				if ((version == SerializedDataVersion.ACARSv41) || (version.getVersion() >= 5))
					re.setAirspace(AirspaceType.values()[in.readShort()]);
				if (version.getVersion() > 4)
					re.setWeight(in.readInt());
			} else
				re.setSimUTC(re.getDate()); // ensure non-null
			
			// Load NAV1/NAV2
			if (version != SerializedDataVersion.ACARS) {
				String n1 = in.readUTF();
				String n2 = in.readUTF();
				re.setNAV1(StringUtils.isEmpty(n1) ? null : n1);
				re.setNAV2(StringUtils.isEmpty(n2) ? null : n2);
			}
			
			if (version.getVersion() > 5) {
				String adf1 = in.readUTF();
				re.setADF1(StringUtils.isEmpty(adf1) ? null : adf1);
			}
			
			// Check for ATC1
			String com1 = in.readUTF();
			if (!StringUtils.isEmpty(com1)) {
				re.setCOM1(com1);
				Controller atc = new Controller(in.readInt(), null);
				atc.setFacility(Facility.values()[in.readShort()]);
				atc.setCallsign(in.readUTF());
				atc.setPosition(in.readFloat(), in.readFloat());
				re.setATC1(atc);
			}
			
			// Check for ATC2
			String com2 = (version.getVersion() > 1) ? in.readUTF() : null;
			if (!StringUtils.isEmpty(com2)) {
				re.setCOM2(com2);
				Controller atc = new Controller(in.readInt(), null);
				atc.setFacility(Facility.values()[in.readShort()]);
				atc.setCallsign(in.readUTF());
				atc.setPosition(in.readFloat(), in.readFloat());
				re.setATC2(atc);
			}
			
			results.add(re);
		}
		
		return results;
	}
	
	private static Collection<XARouteEntry> loadXACARS(DataInputStream in) throws IOException {
		int size = in.readInt();
		Collection<XARouteEntry> results = new ArrayList<XARouteEntry>(size + 2);
		for (int x = 0; x < size; x++) {
			GeoPosition gp = new GeoPosition(in.readDouble(), in.readDouble());
			XARouteEntry re = new XARouteEntry(gp, Instant.ofEpochMilli(in.readLong()));
			re.setPhase(in.readShort());
			re.setFlags(in.readInt());
			re.setAltitude(in.readInt());
			re.setHeading(in.readShort());
			re.setAirSpeed(in.readShort());
			re.setGroundSpeed(in.readShort());
			re.setMach(in.readFloat());
			re.setWindSpeed(in.readShort());
			re.setWindHeading(in.readShort());
			re.setFuelRemaining(in.readInt());
			re.setVerticalSpeed(in.readShort());
			results.add(re);
		}
		
		return results;
	}
}