// Copyright 2016, 2023, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;
import java.time.Instant;

import org.apache.logging.log4j.*;

import org.deltava.beans.schedule.GeoPosition;
import org.deltava.beans.servinfo.PositionData;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to read serialized online track data.
 * @author Luke
 * @version 12.4
 * @since 7.0
 */

public class GetSerializedOnline extends DAO {
	
	private static final Logger log = LogManager.getLogger(GetSerializedOnline.class);

	/**
	 * Initializes the Data Access Object.
	 * @param is the InputStream to read from
	 */
	public GetSerializedOnline(InputStream is) {
		super(is);
	}

	/**
	 * Deserializes simFDR route entries.
	 * @return a Collection of PositionData beans 
	 * @throws DAOException if an I/O error occurs
	 */
	public List<PositionData> read() throws DAOException {
		try (DataInputStream in = new DataInputStream(new BufferedInputStream(getStream()))) {
			short ver = in.readShort();
			int flightID = in.readInt();
			if (ver == 0) 
				return Collections.emptyList();	
				
			int size = in.readInt();
			if (size > 1024) {
				log.warn("Possibly corrupt online positions - ID={}, couunt={}", Integer.valueOf(flightID), Integer.valueOf(size));
				size = 1024;
			}
			
			List<PositionData> results = new ArrayList<PositionData>(size + 2);
			for (int x = 0; (x < size) && (in.available() > 24); x++) {
				PositionData pd = new PositionData(Instant.ofEpochMilli(in.readLong()), new GeoPosition(in.readDouble(), in.readDouble(), in.readInt()));
				pd.setAirSpeed(in.readInt());
				pd.setHeading(in.readShort());
				results.add(pd);
			}
			
			return results;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}