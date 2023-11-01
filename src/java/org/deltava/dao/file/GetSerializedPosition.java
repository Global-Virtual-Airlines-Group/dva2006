// Copyright 2012, 2014, 2016, 2017, 2018, 2019, 2020, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import static org.deltava.beans.acars.SerializedDataVersion.*;

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
 * @version 11.1
 * @since 4.1
 */

public class GetSerializedPosition extends DAO {
	
	private SerializedDataVersion _v;

	/**
	 * Initializes the Data Access Object.
	 * @param is the InputStream to read from
	 */
	public GetSerializedPosition(InputStream is) {
		super(is);
	}
	
	/**
	 * Reads the data format used to serialize a set of position entries.
	 * @return a SerializedDataFormat enum, or null if unknown
	 * @throws DAOException if an I/O error occurs
	 */
	public SerializedDataVersion getFormat() throws DAOException {
		if (_v != null) return _v;
		try (DataInputStream in = new DataInputStream(getStream())) {
			_v = SerializedDataVersion.fromCode(in.readShort());
			return _v;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}

	/**
	 * Deserializes ACARS/XACARS position reports.
	 * @return a Collection of RouteEntry beans
	 * @throws DAOException if an I/O error occurs
	 */
	public SequencedCollection<? extends RouteEntry> read() throws DAOException {
		try (DataInputStream in = new DataInputStream(getStream())) {
			_v = SerializedDataVersion.fromCode(in.readShort());
			in.readInt(); // flight ID
			return _v.isXACARS() ? loadXACARS(in) : loadACARS(in);
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
	
	private SequencedCollection<ACARSRouteEntry> loadACARS(DataInputStream in) throws IOException {
		int size = in.readInt();
		SequencedCollection<ACARSRouteEntry> results = new ArrayList<ACARSRouteEntry>(size + 2);
		for (int x = 0; x < size; x++) {
			GeoPosition gp = new GeoPosition(in.readDouble(), in.readDouble());
			ACARSRouteEntry re = new ACARSRouteEntry(Instant.ofEpochMilli(in.readLong()), gp);
			re.setPhase(FlightPhase.values()[in.readShort()]);
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
			if (_v.atLeast(ACARSv9)) {
				re.setEngineCount(in.readShort());
				re.setN1(in.readDouble());
				re.setN2(in.readDouble());
				for (int eng = 0; eng < re.getEngineCount(); eng++) {
					re.setN1(eng, in.readDouble());
					re.setN2(eng, in.readDouble());
				}
			} else {
				re.setN1(in.readFloat());
				re.setN2(in.readFloat());
			}
			
			re.setVisibility(in.readFloat());
			re.setFuelFlow(in.readInt());
			re.setFlaps(in.readShort());
			re.setFrameRate(in.readShort());
			re.setSimRate(in.readShort());
			
			// Load weather and sim time
			if (_v.atLeast(ACARSv3)) {
				re.setTemperature(in.readShort());
				re.setPressure(in.readInt());
				re.setSimUTC(Instant.ofEpochMilli(in.readLong()));
				if (_v.atLeast(ACARSv4))
					re.setVASFree(in.readInt());
				if (_v.atLeast(ACARSv41))
					re.setAirspace(AirspaceType.values()[in.readShort()]);
				if (_v.atLeast(ACARSv5))
					re.setWeight(in.readInt());
			} else
				re.setSimUTC(re.getDate()); // ensure non-null
			
			// Load NAV1/NAV2
			if (_v.atLeast(ACARSv2)) {
				String n1 = in.readUTF();
				String n2 = in.readUTF();
				re.setNAV1(StringUtils.isEmpty(n1) ? null : n1);
				re.setNAV2(StringUtils.isEmpty(n2) ? null : n2);
			}
			
			if (_v.atLeast(ACARSv6)) {
				String adf1 = in.readUTF();
				re.setADF1(StringUtils.isEmpty(adf1) ? null : adf1);
			}
			
			if (_v.atLeast(ACARSv7)) {
				byte b = in.readByte();
				re.setNetworkConnected((b & 0x1) > 0);
				re.setACARSConnected((b & 0x2) == 0);
			}
				
			if (_v.atLeast(ACARSv8)) {
				re.setGroundOperations(in.readInt());
				re.setCG(in.readFloat());
			}
			
			if (_v.atLeast(ACARSv91))
				re.setRestoreCount(in.readShort());
			
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
			String com2 = _v.atLeast(ACARSv2) ? in.readUTF() : null;
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
	
	private static SequencedCollection<XARouteEntry> loadXACARS(DataInputStream in) throws IOException {
		int size = in.readInt();
		SequencedCollection<XARouteEntry> results = new ArrayList<XARouteEntry>(size + 2);
		for (int x = 0; x < size; x++) {
			GeoPosition gp = new GeoPosition(in.readDouble(), in.readDouble());
			XARouteEntry re = new XARouteEntry(gp, Instant.ofEpochMilli(in.readLong()));
			re.setPhase(FlightPhase.values()[in.readShort()]);
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