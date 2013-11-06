// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.sql.Connection;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.deltava.beans.FileUpload;
import org.deltava.beans.schedule.Country;
import org.deltava.beans.system.*;
import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.StringUtils;
import org.deltava.util.cache.CacheManager;

/**
 * A Web Site Command to import IPv6 netblock data. 
 * @author Luke
 * @version 5.2
 * @since 5.2
 */

public class IP6BlockImportCommand extends AbstractCommand {
	
	private static final Logger log = Logger.getLogger(IP6BlockImportCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the Command result
		CommandResult result = ctx.getResult();
		
		// If we're doing a GET, then redirect to the JSP
		FileUpload ipData = ctx.getFile("netblockData");
		if (ipData == null) {
			result.setURL("/jsp/admin/ip6Import.jsp");
			result.setSuccess(true);
			return;
		}
		
		try (InputStream is = ipData.getInputStream()) {
			Connection con = ctx.getConnection();
			ctx.startTX();
			
			// Purge the table
			SetIPLocation ipwdao = new SetIPLocation(con);
			ipwdao.purge(IPAddress.IPV6);
			
			int entryCount = 0;
			try (LineNumberReader lr = new LineNumberReader(new InputStreamReader(is))) {
				String data = lr.readLine();
				while (data != null) {
					StringTokenizer st = new StringTokenizer(data, ",");
					if (st.countTokens() < 9)
						continue;
					
					// Build the object
					IP6Block ip = createBlock(lr.getLineNumber(), st.nextToken(), st.nextToken());
					st.nextToken(); st.nextToken();
					
					// Get country
					String cc = st.nextToken();
					Country c = Country.get(cc);
					if (c == null) {
						log.warn("Unknown country code - " + cc);
						c = Country.get("US");
					}
					
					ip.setCountry(c);
					ip.setLocation(StringUtils.parse(st.nextToken(), 0.0), StringUtils.parse(st.nextToken(), 0.0));
					ipwdao.write(ip);
					
					entryCount++;
					data = lr.readLine();
				}
			}
			
			// Add link-local block
			IP6Block ip = createBlock(++entryCount, "fe80::", "fe80::ffff:ffff:ffff:ffff");
			ip.setCountry(Country.get("US"));
			ipwdao.write(ip);
			
			// Save attribute and commit
			ctx.commitTX();
			ctx.setAttribute("entryCount", Integer.valueOf(entryCount), REQUEST);
		} catch (IOException | DAOException ide) {
			throw new CommandException(ide);
		} finally {
			ctx.release();
		}

		// Purge the IP block caches
		CacheManager.invalidate("IPInfo");
		CacheManager.invalidate("IPBlock");
		
		// Forward to the JSP
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/admin/ip6Update.jsp");
		result.setSuccess(true);
	}

	/*
	 * Helper method to calculate block sizes.
	 */
	private static IP6Block createBlock(int id, String startAddr, String endAddr) throws IOException {
		InetAddress sa = InetAddress.getByName(startAddr);
		InetAddress ea = InetAddress.getByName(endAddr);
		BigInteger eda = new BigInteger(ea.getAddress());
		BigInteger sz = eda.subtract(new BigInteger(sa.getAddress()));
		int bits = (int) (Math.log(sz.doubleValue()) / Math.log(2));
		return new IP6Block(id, sa.getHostAddress(), ea.getHostAddress(), bits);
	}
}