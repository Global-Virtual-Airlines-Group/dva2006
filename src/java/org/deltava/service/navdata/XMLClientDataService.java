// Copyright 2007, 2008, 2009, 2012, 2015, 2019, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.navdata;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.time.*;
import java.sql.Connection;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.ConcurrentHashMap;

import static javax.servlet.http.HttpServletResponse.*;

import org.apache.log4j.Logger;

import org.deltava.beans.navdata.*;

import org.deltava.crypt.MessageDigester;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to serve Terminal Route/Gate/Runway data to ACARS clients.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class XMLClientDataService extends DownloadService {
	
	private static final Logger log = Logger.getLogger(XMLClientDataService.class);
	
	private static final String XML_ZIP = "xmldata.zip";
	private static final String XML_LEGACY_ZIP = "xmlsidstar.zip";
	
	private final Map<String, Metadata> _md = new HashMap<String, Metadata>();

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Determine if we are pulling old/new format data
		boolean isOld = Boolean.valueOf(ctx.getParameter("legacy")).booleanValue();
		final String ZIP_NAME = isOld ? XML_LEGACY_ZIP : XML_ZIP;
		Metadata md = _md.get(ZIP_NAME);

		// Check if the file exists
		File cacheDir = new File(SystemData.get("schedule.cache"));
		File f = new File(cacheDir, ZIP_NAME);
		Duration d = null;
		if (f.exists()) {
			Instant fileAge = Instant.ofEpochMilli(f.lastModified());
			d = Duration.between(fileAge, Instant.now());
			if ((md != null) && fileAge.isAfter(md.getCreatedOn())) {
				log.warn("Terminal Routes updated, clearing metadata");
				_md.remove(ZIP_NAME);
			}
		} else if (md != null) {
			log.warn("Terminal Routes deleted, clearing metadata");
			_md.remove(ZIP_NAME);
		}
		
		// Check the cache
		if ((d != null) && (d.toHours() < 8) && (f.length() > 10240) && (md != null)) {
			ctx.setHeader("Content-disposition", String.format("attachment; filename=%s", ZIP_NAME));
			ctx.setContentType("application/zip");
			ctx.setHeader("max-age", 1800);
			ctx.setHeader(isOld ? "X-Airport-Count" : "X-File-Count", md.getAirportCount());
			ctx.setHeader("X-Signature", md.getHash());
			ctx.setHeader("X-Signature-Type", md.getHashType());
			sendFile(f, ctx.getResponse());
			return SC_OK;
		}

		// Get the data
		Map<String, String> docs = new ConcurrentHashMap<String, String>();
		try {
			Connection con = ctx.getConnection();
			
			// Load Runways
			GetNavRoute navdao = new GetNavRoute(con);
			if (!isOld) {
				Map<String, Collection<Runway>> rwys = new TreeMap<String, Collection<Runway>>();
				navdao.getRunways().forEach(r -> CollectionUtils.addMapCollection(rwys, r.getCode(), r, ArrayList::new));
				rwys.entrySet().parallelStream().map(XMLFormatter::formatRunway).filter(Objects::nonNull).forEach(me -> docs.put(me.getKey(), me.getValue())); rwys.clear();
			
				// Load Gates
				GetGates gdao = new GetGates(con);
				Map<String, Collection<Gate>> gates = new TreeMap<String, Collection<Gate>>();
				gdao.getAll().forEach(g -> CollectionUtils.addMapCollection(gates, g.getCode(), g, ArrayList::new));
				gates.entrySet().parallelStream().map(XMLFormatter::formatGate).filter(Objects::nonNull).forEach(me -> docs.put(me.getKey(), me.getValue())); gates.clear();
			}
			
			// Load Terminal Routes
			Map<String, Collection<TerminalRoute>> routes = new TreeMap<String, Collection<TerminalRoute>>();
			navdao.getAll().forEach(tr -> CollectionUtils.addMapCollection(routes, tr.getICAO(), tr));
			routes.entrySet().parallelStream().map(XMLFormatter::formatTR).filter(Objects::nonNull).forEach(me -> docs.put(me.getKey(), me.getValue())); routes.clear();
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}

		// Convert to XML
		try {
			int apCount = 0; FileTime now = FileTime.from(Instant.now());
			try (ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(f), 262144))) {
				for (Iterator<Map.Entry<String, String>> i = docs.entrySet().iterator(); i.hasNext(); ) {
					Map.Entry<String, String> me = i.next();
					ZipEntry ze = new ZipEntry(me.getKey() + ".xml");
					ze.setMethod(ZipEntry.DEFLATED);
					ze.setCreationTime(now);
					zout.putNextEntry(ze);
					PrintWriter pw = new PrintWriter(zout);
					pw.print(me.getValue());
					pw.flush();
					i.remove();
					apCount++;
				}
			}
			
			// Calculate the metadata
			MessageDigester mdg = new MessageDigester("SHA-256", 8192);
			try (InputStream is = new BufferedInputStream(new FileInputStream(f), 131072)) {
				md = new Metadata(MessageDigester.convert(mdg.digest(is)), mdg.getAlgorithm());
				md.setAirportCount(apCount);
			}

			// Format and write
			ctx.setHeader("Content-disposition", String.format("attachment; filename=%s", ZIP_NAME));
			ctx.setContentType("application/zip");
			ctx.setExpiry(1800);
			ctx.setHeader(isOld ? "X-Airport-Count" : "X-File-Count", md.getAirportCount());
			ctx.setHeader("X-Signature", md.getHash());
			ctx.setHeader("X-Signature-Type", md.getHashType());
			sendFile(f, ctx.getResponse());
		} catch (Exception e) {
			throw error(SC_INTERNAL_SERVER_ERROR, "I/O Error", false);
		}

		return SC_OK;
	}
	
	@Override
	public boolean isSecure() {
		return true;
	}
}