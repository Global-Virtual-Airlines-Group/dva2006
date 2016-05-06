// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;
import java.time.Instant;

import org.deltava.beans.servinfo.PositionData;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to read serialized online track data.
 * @author Luke
 * @version 7.0
 * @since 7.0
 */

public class GetSerializedOnline extends DAO {

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
		try (DataInputStream in = new DataInputStream(new BufferedInputStream(getStream(), 4096))) {
			short ver = in.readShort();
			in.readInt(); // flight ID
			if (ver == 0) 
				return Collections.emptyList();	
				
			int size = in.readInt();
			List<PositionData> results = new ArrayList<PositionData>(size + 2);
			for (int x = 0; x < size; x++) {
				PositionData pd = new PositionData(Instant.ofEpochMilli(in.readLong()));
				pd.setPosition(in.readDouble(), in.readDouble(), in.readInt());
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