// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

import java.util.*;
import java.io.IOException;
import java.io.StringReader;

import javax.servlet.http.*;

import org.jdom.*;
import org.jdom.input.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;
import org.deltava.dao.*;

import org.deltava.util.ACARSHelper;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to handle submitted ACARS Flight Reports.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ACARSSubmitService extends WebDataService {
	
	/**
	 * Executes the Web Service, writing an ACARS Flight Report to the database.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Build the XML document from this request
		Document doc = null;
		try {
			String rawBody = ctx.getRequest().getParameter("XML");
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(new StringReader(rawBody));
		} catch (IOException ie) {
		} catch (JDOMException je) {
			throw new ServiceException(HttpServletResponse.SC_BAD_REQUEST, "Invalid XML Request");
		}

		// Get root element and validate it
		Element root = doc.getRootElement();
		if (!"ACARSRequest".equals(root.getName()))
			throw new ServiceException(HttpServletResponse.SC_BAD_REQUEST, "Invalid XML Element name");

		// Validate the ACARS version
		int ver = Integer.parseInt(root.getAttributeValue("version"));
		if (ver != 1)
			throw new ServiceException(HttpServletResponse.SC_NOT_IMPLEMENTED, "Unknown ACARS protocol version");

		// Get the PIREP data
		Properties data = new Properties();
		Element cmdE = root.getChild("CMD");
		for (Iterator i = cmdE.getChildren().iterator(); i.hasNext();) {
			Element e = (Element) i.next();
			data.setProperty(e.getName(), e.getTextNormalize());
		}
		
		// Get the airport list and the departure/arrival airports
		Airport airportD = SystemData.getAirport(data.getProperty("dep_apt"));
		Airport airportA = SystemData.getAirport(data.getProperty("arr_apt"));

		String response = null;
		try {
			// See if we have any draft PIREPs for this route pair
			GetFlightReports prdao = new GetFlightReports(_con);
			List dFlights = prdao.getDraftReports(ctx.getUser().getID(), airportD, airportA);
			ACARSFlightReport afr = dFlights.isEmpty() ? ACARSHelper.create(data.getProperty("flight_num")) :
			   ACARSHelper.create((FlightReport) dFlights.get(0));

			// Build the PIREP
			afr.setDatabaseID(FlightReport.DBID_PILOT, ctx.getUser().getID());
			afr.setRank(ctx.getUser().getRank());
			afr.setAirportD(airportD);
			afr.setAirportA(airportA);
			
			// Check if the flight qualifies for the promotion to Captain legs
			GetEquipmentType eqdao = new GetEquipmentType(_con);
			Collection pTypes = eqdao.getPrimaryTypes(afr.getEquipmentType());
			if (!pTypes.isEmpty())
				afr.setCaptEQType(pTypes);

			// Copy XML data into the PIREP
			ACARSHelper.build(afr, data);
			
			// Start the transaction
			_con.setAutoCommit(false);
			
			// Save the PIREP
			SetFlightReport wdao = new SetFlightReport(_con);
			wdao.write(afr);
			wdao.writeACARS(afr);
			wdao.submit(afr);
			
			// Commit the transaction
			_con.commit();
			response = "200 OK";
		} catch (DAOException de) {
		   try {
		      _con.rollback();
		   } catch (Exception e2) { }
		   response = "500" + de.getMessage();
		} catch (Exception e) {
		   response = "500" + e.getMessage();
		}
		
		// Write result code
		ctx.getResponse().setContentType("text/plain");
		try {
		   ctx.getResponse().getWriter().println(response);
		} catch (IOException ie) {
		   throw new ServiceException(HttpServletResponse.SC_NO_CONTENT, ie.getMessage());
		}

		// Return success code
		return HttpServletResponse.SC_OK;
	}

	/**
	 * Queries if the web services requires authentication.
	 * @return TRUE
	 */
	public final boolean isSecure() {
		return true;
	}
}