// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.navdata;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import org.deltava.beans.FileUpload;
import org.deltava.beans.navdata.*;

import org.deltava.commands.*;

import org.deltava.dao.SetNavData;
import org.deltava.dao.DAOException;

import org.deltava.security.command.RouteAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to import Navigation data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AIRACImportCommand extends AbstractCommand {

	private static final String[] UPLOAD_NAMES = { "pssapt.dat", "pssndb.dat", "pssrwy.dat", "pssvor.dat", "psswpt.dat" };

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the Command result
		CommandResult result = ctx.getResult();

		// If we're doing a GET, then redirect to the JSP
		FileUpload navData = ctx.getFile("navData");
		if (navData == null) {
			result.setURL("/jsp/schedule/navDataImport.jsp");
			result.setSuccess(true);
			return;
		}

		// Get the navaid type
		int navaidType = StringUtils.arrayIndexOf(UPLOAD_NAMES, navData.getName());
		if (navaidType == -1)
			throw new CommandException("Unknown Data File - " + navData.getName());

		// Check our access level
		RouteAccessControl access = new RouteAccessControl(ctx);
		access.validate();
		if (!access.getCanImport())
			throw new CommandSecurityException("Cannot import Navigation Data");

		List errors = new ArrayList();
		int entryCount = 0;
		try {
			Connection con = ctx.getConnection();

			// Get the write DAO
			SetNavData dao = new SetNavData(con);
			
			// Get the file - skipping the first line
			InputStream is = new ByteArrayInputStream(navData.getBuffer());
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
							al.setCode(txtData.substring(0, 6));
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
							lon = Double.parseDouble(txtData.substring(19, 32).trim());
							
							Runway rwy = new Runway(lat, lon);
							rwy.setCode(txtData.substring(0, 5));
							rwy.setHeading(Integer.parseInt(txtData.substring(31, 34).trim()));
							rwy.setLength(Integer.parseInt(txtData.substring(35, 40).trim()));
							if (txtData.length() > 40)
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
							
							Intersection i = new Intersection(lat, lon);
							i.setCode(txtData.substring(0, 5));
							nd = i;
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
		} catch (IOException ie) {
			throw new CommandException(ie);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set status attributes
		ctx.setAttribute("entryCount", new Integer(entryCount), REQUEST);
		ctx.setAttribute("isImport", Boolean.TRUE, REQUEST);
		
		// Save error messages
		if (!errors.isEmpty())
			ctx.setAttribute("errors", errors, REQUEST);
		
		// Forward to the JSP
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/schedule/navDataUpdate.jsp");
		result.setSuccess(true);
	}
}