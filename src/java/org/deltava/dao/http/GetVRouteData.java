// Copyright 2009, 2010, 2012, 2016, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.util.*;
import java.net.SocketTimeoutException;

import org.apache.logging.log4j.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.servinfo.PositionData;

import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object to load VATSIM track data from VRoute.
 * @author Luke
 * @version 11.0
 * @since 2.4
 */

public class GetVRouteData extends DAO {

	private static final Logger log = LogManager.getLogger(GetVRouteData.class);

	/**
	 * Initializes the Data Access Object.
	 */
	public GetVRouteData() {
		super();
		setReturnErrorStream(false);
	}

	/**
	 * Retrieves a Pilot's flight data from VRoute.
	 * @param usr the Pilot
	 * @param aD the departure Airport
	 * @param aA the arrival Airport
	 * @return a Collection of PositionData beans
	 * @throws DAOException if an I/O error occurs
	 */
	public Collection<PositionData> getPositions(Pilot usr, Airport aD, Airport aA) throws DAOException {

		// Build the URL
		String id = usr.getNetworkID(OnlineNetwork.VATSIM);
		if (id == null)
			return Collections.emptyList();

		String url = "http://data.vroute.net/services/flthistory.php?pid=" + id;
		try {
			init(url);
			Collection<PositionData> results = new ArrayList<PositionData>();
			try (LineNumberReader lr = new LineNumberReader(new InputStreamReader(getIn()))) {
				while (lr.ready()) {
					String data = lr.readLine();
					List<String> tkns = StringUtils.split(data, "\t");
					if ((tkns.size() == 10) && (tkns.get(8).equals(aD.getICAO())) && (tkns.get(9).equals(aA.getICAO()))) {
						try {
							PositionData pd = new PositionData(StringUtils.parseInstant(tkns.get(0), "yyyy-MM-dd HH:mm:ss"));
							pd.setPilotID(usr.getID());
							pd.setPosition(StringUtils.parse(tkns.get(3), 0.0d), StringUtils.parse(tkns.get(4), 0.0d), StringUtils.parse(tkns.get(7), 0));
							pd.setAirSpeed(StringUtils.parse(tkns.get(5), 0));
							pd.setHeading(StringUtils.parse(tkns.get(6), 0));
							results.add(pd);
						} catch (Exception pe) {
							log.warn("Error parsing " + data + " - " + pe.getMessage());
						}
					}
				}
			}

			return results;
		} catch (SocketTimeoutException ste) {
			log.warn("Socket Timeout - " + url);
			return Collections.emptyList();
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}