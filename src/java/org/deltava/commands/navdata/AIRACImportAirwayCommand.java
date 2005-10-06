// Copyright (c) 2005 Delta Virtual Airlines. All Rights Reserved.
package org.deltava.commands.navdata;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import org.deltava.beans.FileUpload;
import org.deltava.beans.navdata.*;

import org.deltava.commands.*;

import org.deltava.dao.SetNavData;
import org.deltava.dao.DAOException;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to import AIRAC Airway/SID/STAR data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AIRACImportAirwayCommand extends AbstractCommand {

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
			result.setURL("/jsp/schedule/navDataImport.jsp");
			result.setSuccess(true);
			return;
		}
		
		// Get the navaid type
		int navaidType = StringUtils.arrayIndexOf(AIRACImportCommand.AIRWAY_NAMES, navData.getName());
		if (navaidType == -1)
			throw new CommandException("Unknown Data File - " + navData.getName());
		
		List errors = new ArrayList();
		int entryCount = 0;
		try {
			Connection con = ctx.getConnection();
			
			// Get the write DAO
			SetNavData dao = new SetNavData(con);
			
			// Get the file - skipping the first line
			InputStream is = new ByteArrayInputStream(navData.getBuffer());
			LineNumberReader br = new LineNumberReader(new InputStreamReader(is));
			
			// Save beans - several lines map to one bean
			Airway aw = null;
			Airway lastAirway = null;
			String lastCode = null;
			
			// Iterate through the file
			while (br.ready()) {
				String txtData = br.readLine();
				if ((!txtData.startsWith(";")) && (txtData.length() > 5)) {
					StringTokenizer tkns = null;
					
					// Parse each line differently depending on the filename
					switch (navaidType) {
						case 0: // Airway
							tkns = new StringTokenizer(txtData, ",");
							if (tkns.countTokens() < 4) {
								errors.add("Error at line " + br.getLineNumber() + ": tokenCount=" + tkns.countTokens());
							} else {
								tkns.nextToken(); // skip region
								String code = tkns.nextToken().trim();
								if (!code.equals(lastCode)) {
									lastCode = code;
									lastAirway = aw;
									aw = new Airway(code);
								}
								
								tkns.nextToken(); // skip index
								aw.addWaypoint(tkns.nextToken());
							}
							
							break;
							
						case 1: // SID
							if (txtData.startsWith("[")) {
								tkns = new StringTokenizer(txtData.substring(1, txtData.length() - 1), "/");
								TerminalRoute tr = new TerminalRoute(tkns.nextToken(), tkns.nextToken(), TerminalRoute.SID);
								tr.setRunway(tkns.nextToken());
								tr.setTransition(tkns.nextToken());
								lastAirway = aw;
								aw = tr;
							} else {
								tkns = new StringTokenizer(txtData, ",");
								if (tkns.countTokens() >= 4) {
									for (int x = 0; x < 3; x++)
										tkns.nextElement();
									
									// Get the code, but only add it if it is not the runway or empty
									String code = tkns.nextToken().trim();
									if (!StringUtils.isEmpty(code) && (!code.startsWith("RW")))
										aw.addWaypoint(code);
								} else {
									errors.add("Error at line " + br.getLineNumber() + ": tokenCount=" + tkns.countTokens());
								}
							}
							
							break;
							
						case 2: // STAR
							if (txtData.startsWith("[")) {
								tkns = new StringTokenizer(txtData.substring(1, txtData.length() - 1), "/");
								TerminalRoute tr = new TerminalRoute(tkns.nextToken(), tkns.nextToken(), TerminalRoute.STAR);
								tr.setRunway(tkns.nextToken());
								tr.setTransition(tkns.nextToken());
								lastAirway = aw;
								aw = tr;
							} else {
								tkns = new StringTokenizer(txtData, ",");
								if (tkns.countTokens() >= 4) {
									for (int x = 0; x < 3; x++)
										tkns.nextElement();

									// Add the waypoint if it is not empty
									String code = tkns.nextToken();
									if (!StringUtils.isEmpty(code))
										aw.addWaypoint(code);
								} else {
									errors.add("Error at line " + br.getLineNumber() + ": tokenCount=" + tkns.countTokens());
								}
							}
							
							break;
					}
					
					// Write the bean, and log any errors
					if (lastAirway != null) {
						try {
							if (lastAirway instanceof TerminalRoute) {
								dao.writeRoute((TerminalRoute) lastAirway);
							} else {
								dao.write(lastAirway);
							}
						} catch (DAOException de) {
							errors.add("Error at line " + br.getLineNumber() + ": " + de.getMessage());
						}
						
						// Reset the counters
						entryCount++;
						lastAirway = null;
					}
				}
			}
			
			// Close down the stream
			is.close();
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