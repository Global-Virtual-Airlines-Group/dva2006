// Copyright 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;

import org.deltava.beans.acars.ArchivedRoute;
import org.deltava.beans.navdata.*;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to read serialized route data.
 * @author Luke
 * @version 8.6
 * @since 7.0
 */

public class GetSerializedRoute extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param is the InputStream to read from
	 */
	public GetSerializedRoute(InputStream is) {
		super(is);
	}

	/**
	 * Deserializes ACARS route entries.
	 * @return an ArchivedRoute bean
	 * @throws DAOException if an I/O error occurs
	 */
	public ArchivedRoute read() throws DAOException {
		try (DataInputStream in = new DataInputStream(new BufferedInputStream(getStream(), 4096))) {
			short ver = in.readShort();
			int flightID = in.readInt(); // flight ID
			int airacVersion = (ver > 2) ? in.readInt() : -1;
			ArchivedRoute rt = new ArchivedRoute(flightID, airacVersion);
			if (ver == 0)
				return rt;

			int size = in.readInt();
			for (int x = 0; x < size; x++) {
				Navaid nt = Navaid.values()[in.readShort()];
				NavigationDataBean ndb = NavigationDataBean.create(nt, in.readDouble(), in.readDouble());
				ndb.setCode(in.readUTF());
				if (nt != Navaid.INT)
					ndb.setName(in.readUTF());
				ndb.setAirway(in.readUTF());
				if (ndb instanceof NavigationFrequencyBean)
					((NavigationFrequencyBean) ndb).setFrequency(in.readUTF());
				
				rt.addWaypoint(ndb);
			}
				
			return rt;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}