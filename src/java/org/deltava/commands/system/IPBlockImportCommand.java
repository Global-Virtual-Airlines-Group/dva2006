// Copyright 2013, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.sql.Connection;

import org.deltava.beans.schedule.Country;
import org.deltava.beans.system.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to import IP netblock geolocation data.
 * @author Luke
 * @version 8.7
 * @since 5.2
 */

public class IPBlockImportCommand extends AbstractCommand {

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
			result.setURL("/jsp/admin/ipImport.jsp");
			result.setSuccess(true);
			return;
		}

		// Load the data
		boolean isGZIP = fName.toLowerCase().endsWith(".gz");
		Collection<String> msgs = new ArrayList<String>(); int blockID = 1;
		try {
			Connection con = ctx.getConnection();
			ctx.startTX();

			SetIPLocation ipwdao = new SetIPLocation(con);
			try (InputStream fis = new FileInputStream(f)) {
				try (InputStream is = isGZIP ? new GZIPInputStream(fis, 32768) : new BufferedInputStream(fis, 32768)) {
					try (LineNumberReader lr = new LineNumberReader(new InputStreamReader(is))) {
						String data = lr.readLine(); Collection<IPBlock> blocks = new ArrayList<IPBlock>(32); 
						while (data != null) {
							data = lr.readLine();
							if (data == null)
								break;
							
							List<String> tkns = StringUtils.split(data, ",");
							if (tkns.size() < 9) {
								msgs.add("Invalid token count (" + tkns.size() + ", expected 10) at Line " + lr.getLineNumber());
								continue;
							}

							// Build the object
							IPBlock ip = new IPBlock(++blockID, tkns.get(0));
							ip.setCity(tkns.get(1)); // genoName ID
							ip.setLocation(StringUtils.parse(tkns.get(7), 0.0d), StringUtils.parse(tkns.get(8), 0.0d));
							if (tkns.size() > 9)
								ip.setRadius(StringUtils.parse(tkns.get(9), 50));
							if (!StringUtils.isEmpty(ip.getCity()))
								blocks.add(ip);
							if (blocks.size() > 30) {
								ipwdao.write(blocks);
								blocks.clear();
							}
						}
					}
				}
			}
			
			// Add link-local IPv block
			IPBlock ip6ll = new IPBlock(++blockID, "fe80::/10");
			ip6ll.setCountry(Country.UNKNOWN);
			ip6ll.setCity(String.valueOf(Integer.MAX_VALUE));

			// Add localhost
			IPBlock l4 = new IPBlock(++blockID, "127.0.0.0/8");
			l4.setCountry(Country.UNKNOWN);
			l4.setCity(String.valueOf(Integer.MAX_VALUE));
			IPBlock l6 = new IPBlock(++blockID, "::1/128");
			l6.setCountry(Country.UNKNOWN);
			l6.setCity(String.valueOf(Integer.MAX_VALUE));
			
			// Write special blocks
			ipwdao.write(List.of(ip6ll, l4, l6));
			ctx.commitTX();
		} catch (DAOException | IOException ie) {
			ctx.rollbackTX();
			throw new CommandException(ie);
		} finally {
			ctx.release();
			if (f.exists())
				f.delete();
		}

		// Purge the IP block caches
		CacheManager.invalidate("IPInfo");
		CacheManager.invalidate("IPBlock");

		// Set status updates
		ctx.setAttribute("msgs", msgs, REQUEST);
		ctx.setAttribute("isBlock", Boolean.TRUE, REQUEST);
		ctx.setAttribute("entryCount", Integer.valueOf(blockID), REQUEST);

		// Forward to the JSP
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/admin/ipUpdate.jsp");
		result.setSuccess(true);
	}
}