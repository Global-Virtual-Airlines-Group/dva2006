// Copyright 2005, 2006, 2007, 2008, 2009, 2012, 2013, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import org.deltava.beans.FileUpload;
import org.deltava.beans.navdata.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.CacheManager;

/**
 * A Web Site Command to import Navigation data in PSS format.
 * @author Luke
 * @version 6.0
 * @since 1.0
 */

public class AIRACImportCommand extends NavDataImportCommand {

	private static final String[] UPLOAD_NAMES = { "pssapt.dat", "pssvor.dat", "pssndb.dat", "psswpt.dat", "pssrwy.dat" };

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the Command result
		CommandResult result = ctx.getResult();

		// Check our access level
		ScheduleAccessControl access = new ScheduleAccessControl(ctx);
		access.validate();
		if (!access.getCanImport())
			throw securityException("Cannot import Navigation Data");
		
		// Load the data
		CycleInfo inf = getCurrrentCycle(ctx);
		ctx.setAttribute("currentNavCycle", inf, REQUEST);
		
		// If we're doing a GET, then redirect to the JSP
		FileUpload navData = ctx.getFile("navData");
		if (navData == null) {
			result.setURL("/jsp/navdata/navDataImport.jsp");
			result.setSuccess(true);
			return;
		}

		// Strip out .gz extension
		String name = navData.getName();
		if (name.endsWith(".gz") || name.endsWith(".bz2"))
			name = name.substring(0, name.lastIndexOf('.'));

		// Get the navaid type
		int navaidType = StringUtils.arrayIndexOf(UPLOAD_NAMES, name);
		if ((navaidType == -1) || (navaidType >= Navaid.values().length))
			throw notFoundException("Unknown Data File - " + navData.getName());
		
		Navaid nt = Navaid.values()[navaidType];
		List<String> errors = new ArrayList<String>();
		int entryCount = 0; int regionCount = 0; CycleInfo newCycle = null;
		try (InputStream is = navData.getInputStream()) {
			Connection con = ctx.getConnection();
			ctx.startTX();
			
			// Get the write DAO
			SetNavData dao = new SetNavData(con);
			ctx.setAttribute("purgeCount", Integer.valueOf(dao.purge(nt)), REQUEST);
			ctx.setAttribute("legacyCount", Integer.valueOf(dao.updateLegacy(nt)), REQUEST);

			// Iterate through the file
			LineNumberReader br = new LineNumberReader(new InputStreamReader(is));
			String txtData = br.readLine();
			while (txtData != null) {
				boolean isComment = txtData.startsWith(";"); 
				if ((newCycle == null) && isComment) {
					int pos = txtData.indexOf("AIRAC Cycle : ");
					if (pos != -1) {
						newCycle = new CycleInfo(txtData.substring(pos+14, pos+18));
						if ((inf != null) && (newCycle.compareTo(inf) == -1))
							throw new DAOException("Navigation Data Cycle " + newCycle + " is older than loaded cycle " + inf);
					}
				} else if (!isComment && (txtData.length() > 5)) {
					double lat, lon;
					NavigationDataBean nd = null;

					// Parse each line differently depending on the filename
					try {
						switch (nt) {
						case AIRPORT:
							lat = Double.parseDouble(txtData.substring(5, 15).trim());
							lon = Double.parseDouble(txtData.substring(15, 26).trim());

							AirportLocation al = new AirportLocation(lat, lon);
							al.setCode(txtData.substring(0, 5));
							al.setAltitude(StringUtils.parse(txtData.substring(27, 32).trim(), 0));
							al.setName(txtData.substring(34));
							nd = al;
							break;

						case NDB:
							lat = Double.parseDouble(txtData.substring(5, 15).trim());
							lon = Double.parseDouble(txtData.substring(15, 26).trim());

							NDB ndb = new NDB(lat, lon);
							ndb.setCode(txtData.substring(0, 4));
							ndb.setFrequency(txtData.substring(28, 34).trim());
							ndb.setName(txtData.substring(36));
							nd = ndb;
							break;

						case RUNWAY:
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

						case VOR:
							lat = Double.parseDouble(txtData.substring(5, 15).trim());
							lon = Double.parseDouble(txtData.substring(15, 26).trim());

							VOR vor = new VOR(lat, lon);
							vor.setCode(txtData.substring(0, 5));
							vor.setFrequency(txtData.substring(28, 34).trim());
							vor.setName(txtData.substring(36));
							nd = vor;
							break;

						case INT:
							StringTokenizer tkns = new StringTokenizer(txtData, " ");
							int cnt = tkns.countTokens();
							if (cnt == 3) {
								String code = tkns.nextToken();
								lat = Double.parseDouble(tkns.nextToken());
								lon = Double.parseDouble(tkns.nextToken());
								nd = new Intersection(code, lat, lon);
							} else if ((cnt == 2) && (txtData.indexOf('-') < 8)) {
								String codeLat = tkns.nextToken();
								int pos = txtData.indexOf('-');
								String code = codeLat.substring(0, pos);
								lat = Double.parseDouble(codeLat.substring(pos).trim());
								lon = Double.parseDouble(tkns.nextToken());
								nd = new Intersection(code, lat, lon);
							} else {
								lat = Double.parseDouble(txtData.substring(5, 15).trim());
								lon = Double.parseDouble(txtData.substring(16).trim());
								nd = new Intersection(txtData.substring(0, 5), lat, lon);
							}
							break;
							
						default:
							break;
						}
					} catch (IllegalArgumentException | StringIndexOutOfBoundsException nfe) {
						errors.add("Error at line " + br.getLineNumber() + ": " + nfe.getMessage());
						errors.add(txtData);
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
				
				txtData = br.readLine();
			}

			// Update the regions
			dao.setQueryTimeout(75);
			regionCount = dao.updateRegions(nt);
			
			// Write the cycle ID and commit
			if (newCycle != null) {
				SetMetadata mdwdao = new SetMetadata(con);
				mdwdao.write("navdata.cycle", newCycle.toString());
			}
				
			ctx.commitTX();
		} catch (IOException | DAOException ie) {
			ctx.rollbackTX();
			throw new CommandException(ie);
		} finally {
			ctx.release();
		}

		// Purge the navdata cache
		CacheManager.invalidate("NavData");
		CacheManager.invalidate("NavAirway");
		CacheManager.invalidate("NavRunway");
		CacheManager.invalidate("NavSIDSTAR");
		CacheManager.invalidate("NavRoute");
		
		// Set status attributes
		ctx.setAttribute("entryCount", Integer.valueOf(entryCount), REQUEST);
		ctx.setAttribute("regionCount", Integer.valueOf(regionCount), REQUEST);
		ctx.setAttribute("navaidType", nt, REQUEST);
		ctx.setAttribute("navCycleID", newCycle, REQUEST);
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