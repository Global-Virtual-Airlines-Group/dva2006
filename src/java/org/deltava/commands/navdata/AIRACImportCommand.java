// Copyright 2005, 2006, 2007, 2008, 2009, 2012, 2013, 2015, 2018, 2019, 2020, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import org.deltava.beans.FileUpload;
import org.deltava.beans.navdata.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.*;
import org.deltava.util.cache.CacheManager;

/**
 * A Web Site Command to import Navigation data in PSS format.
 * @author Luke
 * @version 10.6
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
		FileUpload navData = ctx.getFile("navData", 0);
		if (navData == null) {
			result.setURL("/jsp/navdata/navDataImport.jsp");
			result.setSuccess(true);
			return;
		}

		// Strip out .gz extension
		String name = navData.getName();
		if (name.endsWith(".gz") || name.endsWith(".bz2") || name.endsWith(".xz"))
			name = name.substring(0, name.lastIndexOf('.'));

		// Get the navaid type
		int navaidType = StringUtils.arrayIndexOf(UPLOAD_NAMES, name);
		if ((navaidType == -1) || (navaidType >= Navaid.values().length))
			throw notFoundException("Unknown Data File - " + navData.getName());
		
		Navaid nt = Navaid.values()[navaidType];
		List<String> errors = new ArrayList<String>(); 
		Map<String, Long> timings = new LinkedHashMap<String, Long>();
		int entryCount = 0; int regionCount = 0; CycleInfo newCycle = null;
		try (InputStream is = navData.getInputStream(); LineNumberReader br = new LineNumberReader(new InputStreamReader(is))) {
			Connection con = ctx.getConnection();
			ctx.startTX();
			
			// Get the write DAO
			TaskTimer tt = new TaskTimer();
			SetNavData dao = new SetNavData(con);
			dao.setQueryTimeout(90);
			ctx.setAttribute("purgeCount", Integer.valueOf(dao.purge(nt)), REQUEST);
			timings.put("Purge", Long.valueOf(tt.stop()));
			dao.setQueryTimeout(30); tt.start();
			ctx.setAttribute("legacyCount", Integer.valueOf(dao.updateLegacy(nt)), REQUEST);
			timings.put("UpdateLegacy", Long.valueOf(tt.stop()));

			// Iterate through the file
			Collection<NavigationDataBean> nds = new ArrayList<NavigationDataBean>();
			String txtData = br.readLine(); tt.start();
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
					NavigationDataBean nd = null;

					// Parse each line differently depending on the filename
					try {
						switch (nt) {
						case AIRPORT:
							AirportLocation al = new AirportLocation(Double.parseDouble(txtData.substring(5, 15).trim()), Double.parseDouble(txtData.substring(15, 26).trim()));
							al.setCode(txtData.substring(0, 5));
							al.setAltitude(StringUtils.parse(txtData.substring(27, 32).trim(), 0));
							al.setName(txtData.substring(34));
							nd = al;
							break;

						case NDB:
							NDB ndb = new NDB(Double.parseDouble(txtData.substring(5, 15).trim()), Double.parseDouble(txtData.substring(15, 26).trim()));
							ndb.setCode(txtData.substring(0, 4));
							ndb.setFrequency(txtData.substring(28, 34).trim());
							ndb.setName(txtData.substring(36));
							nd = ndb;
							break;

						case RUNWAY:
							Runway rwy = new Runway(Double.parseDouble(txtData.substring(9, 19).trim()), Double.parseDouble(txtData.substring(19, 30).trim()));
							rwy.setCode(txtData.substring(0, 5));
							rwy.setName(txtData.substring(5, 8).toUpperCase());
							rwy.setLength((int) Math.round(Integer.parseInt(txtData.substring(35, 40).trim()) * 3.2808399)); // Convert runway length from meters to feet
							if (txtData.length() < 47) {
								rwy.setFrequency("-");
								rwy.setHeading(Integer.parseInt(txtData.substring(31, 34).trim()));
							} else {
								rwy.setFrequency(txtData.substring(41, 47).trim());
								rwy.setHeading(Integer.parseInt(txtData.substring(48, 51).trim()));
							}
							
							nd = rwy;
							break;

						case VOR:
							VOR vor = new VOR(Double.parseDouble(txtData.substring(5, 15).trim()), Double.parseDouble(txtData.substring(15, 26).trim()));
							vor.setCode(txtData.substring(0, 5));
							vor.setFrequency(txtData.substring(28, 34).trim());
							vor.setName(txtData.substring(36));
							nd = vor;
							break;

						case INT:
							StringTokenizer tkns = new StringTokenizer(txtData, " ");
							int cnt = tkns.countTokens();
							if (cnt == 3)
								nd = new Intersection(tkns.nextToken(), Double.parseDouble(tkns.nextToken()), Double.parseDouble(tkns.nextToken()));
							else if ((cnt == 2) && (txtData.indexOf('-') < 8)) {
								String codeLat = tkns.nextToken();
								int pos = txtData.indexOf('-');
								String code = codeLat.substring(0, pos);
								nd = new Intersection(code, Double.parseDouble(codeLat.substring(pos).trim()), Double.parseDouble(tkns.nextToken()));
							} else
								nd = new Intersection(txtData.substring(0, 5), Double.parseDouble(txtData.substring(5, 15).trim()), Double.parseDouble(txtData.substring(16).trim()));
								
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
						nds.add(nd);
						if (nds.size() > 40) {
							entryCount += nds.size();
							try {
								dao.write(nds);
								nds.clear();
							} catch (DAOException de) {
								errors.add("Error at line " + br.getLineNumber() + ": " + de.getMessage());
							}
						}
					}
				}
				
				txtData = br.readLine();
			}
			
			// Flush
			if (!nds.isEmpty()) {
				dao.write(nds);
				entryCount += nds.size();
			}

			// Update the regions
			timings.put("Load", Long.valueOf(tt.stop()));
			dao.setQueryTimeout(150); tt.start();
			regionCount = dao.updateRegions(nt);
			timings.put("UpdateRegions", Long.valueOf(tt.stop()));
			
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
		ctx.setAttribute("timings", timings, REQUEST);
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