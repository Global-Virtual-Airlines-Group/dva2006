// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;

import org.deltava.beans.navdata.*;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to read serialized route data.
 * @author Luke
 * @version 7.0
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
	 * Deserializes simFDR route entries.
	 * @return a Collection of NavigationDataBeans
	 * @throws DAOException if an I/O error occurs
	 */
	public List<NavigationDataBean> read() throws DAOException {
		try (DataInputStream in = new DataInputStream(new BufferedInputStream(getStream(), 4096))) {
			short ver = in.readShort();
			in.readInt(); // flight ID
			if (ver == 0)
				return Collections.emptyList();

			int size = in.readInt();
			List<NavigationDataBean> results = new ArrayList<NavigationDataBean>(size + 2);
			for (int x = 0; x < size; x++) {
				Navaid nt = Navaid.values()[in.readShort()];
				NavigationDataBean ndb = NavigationDataBean.create(nt, in.readDouble(), in.readDouble());
				ndb.setCode(in.readUTF());
				if (nt != Navaid.INT)
					ndb.setName(in.readUTF());
				ndb.setAirway(in.readUTF());
				if (ndb instanceof NavigationFrequencyBean)
					((NavigationFrequencyBean) ndb).setFrequency(in.readUTF());
				
				results.add(ndb);
			}
				
			return results;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}