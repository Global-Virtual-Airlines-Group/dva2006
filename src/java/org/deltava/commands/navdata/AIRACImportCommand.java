// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import org.deltava.beans.FileUpload;
import org.deltava.beans.navdata.*;

import static org.deltava.beans.navdata.NavigationDataBean.*;
import org.deltava.service.navdata.DispatchDataService;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to import Navigation data in PSS format.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class AIRACImportCommand extends AbstractCommand {

	private static final String[] UPLOAD_NAMES = {"pssapt.dat", "pssndb.dat", "pssrwy.dat", "pssvor.dat", "psswpt.dat"};
	private static final int[] NAVAID_TYPES = { AIRPORT, NDB, RUNWAY, VOR, INT };
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the Command result
		CommandResult result = ctx.getResult();
		
		// Check our access level
		ScheduleAccessControl access = new ScheduleAccessControl(ctx);
		access.validate();
		if (!access.getCanImport())
			throw securityException("Cannot import Navigation Data");

		// If we're doing a GET, then redirect to the JSP
		FileUpload navData = ctx.getFile("navData");
		if (navData == null) {
			result.setURL("/jsp/navdata/navDataImport.jsp");
			result.setSuccess(true);
			return;
		}
		
		// Strip out .gz extension
		String name = navData.getName();
		if (name.endsWith(".gz"))
			name = name.substring(0, name.lastIndexOf('.'));
		
		// Get the navaid type
		int navaidType = StringUtils.arrayIndexOf(UPLOAD_NAMES, name);
		if (navaidType == -1)
			throw notFoundException("Unknown Data File - " + navData.getName());
		
		List<String> errors = new ArrayList<String>();
		int entryCount = 0; int regionCount = 0;
		try {
			Connection con = ctx.getConnection();
			ctx.startTX();

			// Get the write DAO
			SetNavData dao = new SetNavData(con);
			dao.purge(NAVAID_TYPES[navaidType]);
			
			// Get the file - skipping the first line
			InputStream is = navData.getInputStream();
			LineNumberReader br = new LineNumberReader(new InputStreamReader(is));

			// Iterate through the file
			while (br.ready()) {
				String txtData = br.readLine();
				if ((!txtData.startsWith(";")) && (txtData.length() > 5)) {
					double lat, lon;
					NavigationDataBean nd = null;
					
					// Parse each line differently depending on the filename
					switch (navaidType) {
						case 0: // Airport
							lat = Double.parseDouble(txtData.substring(5, 15).trim());
							lon = Double.parseDouble(txtData.substring(15, 26).trim());
							
							AirportLocation al = new AirportLocation(lat, lon);
							al.setCode(txtData.substring(0, 5));
							al.setAltitude(Integer.parseInt(txtData.substring(27, 32).trim()));
							al.setName(txtData.substring(34));
							nd = al;
							break;

						case 1: // NDB
							lat = Double.parseDouble(txtData.substring(5, 15).trim());
							lon = Double.parseDouble(txtData.substring(15, 26).trim());
							
							NDB ndb = new NDB(lat, lon);
							ndb.setCode(txtData.substring(0, 4));
							ndb.setFrequency(txtData.substring(28, 34).trim());
							ndb.setName(txtData.substring(36));
							nd = ndb;
							break;

						case 2: // Runway
							lat = Double.parseDouble(txtData.substring(9, 19).trim());
							lon = Double.parseDouble(txtData.substring(19, 30).trim());
							
							// Convert runway length from meters to feet
							int lenM = Integer.parseInt(txtData.substring(35, 40).trim());
							
							Runway rwy = new Runway(lat, lon);
							rwy.setCode(txtData.substring(0, 5));
							rwy.setName(txtData.substring(5, 8).toUpperCase());
							rwy.setHeading(Integer.parseInt(txtData.substring(31, 34).trim()));
							rwy.setLength((int) Math.round(lenM * 3.2808399));
							rwy.setFrequency("-");
							if (txtData.length() > 46) 
								rwy.setFrequency(txtData.substring(41, 47).trim());
							
							nd = rwy;
							break;

						case 3: // VOR
							lat = Double.parseDouble(txtData.substring(5, 15).trim());
							lon = Double.parseDouble(txtData.substring(15, 26).trim());
							
							VOR vor = new VOR(lat, lon);
							vor.setCode(txtData.substring(0, 5));
							vor.setFrequency(txtData.substring(28, 34).trim());
							vor.setName(txtData.substring(36));
							nd = vor;
							break;

						case 4: // Intersection
							lat = Double.parseDouble(txtData.substring(5, 15).trim());
							lon = Double.parseDouble(txtData.substring(16).trim());
							nd = new Intersection(txtData.substring(0, 5), lat, lon);
							break;
					}
					
					// Write the bean, and log any errors
					if (nd != null) {
						try {
							dao.write(nd);
							entryCount++;
						} catch (DAOException de) {
							errors.add("Error at line " + br.getLineNumber() + ": " + de.getMessage());
						}
					}
				}
			}
			
			// Update the regions
			regionCount = dao.updateRegions(NAVAID_TYPES[navaidType]);
			
			// Commit and close down the stream
			ctx.commitTX();
			is.close();
		} catch (IOException ie) {
			ctx.rollbackTX();
			throw new CommandException(ie);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Purge the dispatch web service file cache
		DispatchDataService.invalidate();

		// Set status attributes
		ctx.setAttribute("entryCount", new Integer(entryCount), REQUEST);
		ctx.setAttribute("regionCount", new Integer(regionCount), REQUEST);
		ctx.setAttribute("isImport", Boolean.TRUE, REQUEST);
		ctx.setAttribute("navData", Boolean.TRUE, REQUEST);
		
		// Save error messages
		if (!errors.isEmpty())
			ctx.setAttribute("errors", errors, REQUEST);
		
		// Forward to the JSP
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/navdata/navDataUpdate.jsp");
		result.setSuccess(true);
	}
}