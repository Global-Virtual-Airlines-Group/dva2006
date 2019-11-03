// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.sql.Connection;

import org.deltava.beans.schedule.Country;
import org.deltava.beans.system.IPLocation;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to load IP GeoLocation data.
 * @author Luke
 * @version 8.7
 * @since 8.7
 */

public class IPGeoImportCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the Command result
		CommandResult result = ctx.getResult();

		// Get the uploaded file - look for a file
		String fName = ctx.getParameter("id");
		File f = new File(SystemData.get("path.upload"), StringUtils.isEmpty(fName) ? "" : fName);

		// If we're doing a GET, then redirect to the JSP
		if (!f.exists() || !f.isFile() || StringUtils.isEmpty(fName)) {
			result.setURL("/jsp/admin/ipGeoImport.jsp");
			result.setSuccess(true);
			return;
		}

		// Load the data
		boolean isGZIP = fName.toLowerCase().endsWith(".gz");
		Collection<String> msgs = new ArrayList<String>(); int locCount = 0;
		try {
			Connection con = ctx.getConnection();
			ctx.startTX();

			SetIPLocation ipwdao = new SetIPLocation(con);
			try (InputStream fis = new FileInputStream(f)) {
				try (InputStream is = isGZIP ? new GZIPInputStream(fis, 32768) : new BufferedInputStream(fis)) {
					try (LineNumberReader lr = new LineNumberReader(new InputStreamReader(is))) {
						String data = lr.readLine();
						while (data != null) {
							data = lr.readLine();
							if (data == null)
								break;

							List<String> tkns = StringUtils.split(data, ",");
							if (tkns.size() < 11) {
								msgs.add("Invalid token count (" + tkns.size() + ", expected 14) at Line " + lr.getLineNumber());
								continue;
							}

							// Build the object
							IPLocation loc = new IPLocation(StringUtils.parse(tkns.get(0), 0));
							loc.setCountry(Country.get(tkns.get(4)));

							if (tkns.get(6).length() > 3) {
								msgs.add("Invalid region code at Line " + lr.getLineNumber() + " - " + tkns.get(6));
								loc.setRegionCode("");
							} else {
								loc.setRegionCode(tkns.get(6));
								loc.setRegion(removeCSVQuotes(tkns.get(7)));
							}

							loc.setCityName(removeCSVQuotes(tkns.get(10)));
							ipwdao.write(loc);
							locCount++;
						}
					}
				}
			}

			// Create localhost location
			IPLocation loc = new IPLocation(Integer.MAX_VALUE);
			loc.setCountry(Country.UNKNOWN);
			loc.setCityName("");
			ipwdao.write(loc);
			ctx.commitTX();
		} catch (IOException | DAOException ie) {
			ctx.rollbackTX();
			throw new CommandException(ie);
		} finally {
			ctx.release();
			if (f.exists())
				f.delete();
		}
		
		// Purge the IP info cache
		CacheManager.invalidate("IPInfo");

		// Set status attributes
		ctx.setAttribute("msgs", msgs, REQUEST);
		ctx.setAttribute("isLocation", Boolean.TRUE, REQUEST);
		ctx.setAttribute("locationCount", Integer.valueOf(locCount), REQUEST);

		// Forward to the JSP
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/admin/ipUpdate.jsp");
		result.setSuccess(true);
	}
	
	/*
	 * Helper method to parse CSV-quoted strings.
	 */
	private static String removeCSVQuotes(String s) {
		if ((s == null) || (s.length() < 2)) return s;
		boolean leadingQ = (s.charAt(0) == '"');
		return s.substring(leadingQ ? 1 : 0, s.length() - (leadingQ ? 2 : 1));
	}
}