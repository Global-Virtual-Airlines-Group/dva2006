// Copyright 2007, 2008, 2009, 2012, 2015, 2019, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.navdata;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.time.*;
import java.nio.file.attribute.FileTime;

import static javax.servlet.http.HttpServletResponse.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.navdata.*;

import org.deltava.crypt.MessageDigester;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display Terminal Route data to ACARS clients.
 * @author Luke
 * @version 11.0
 * @since 2.4
 */

public class XMLTerminalRouteService extends DownloadService {
	
	private static final Logger log = LogManager.getLogger(XMLTerminalRouteService.class);
	private static final String XML_ZIP = "xmlsidstar.zip";
	
	private Metadata _md;

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Check if the file exists
		File cacheDir = new File(SystemData.get("schedule.cache"));
		File f = new File(cacheDir, "sidstar.zip");
		Duration d = null;
		if (f.exists()) {
			Instant fileAge = Instant.ofEpochMilli(f.lastModified());
			d = Duration.between(fileAge, Instant.now());
			if ((_md != null) && fileAge.isAfter(_md.getCreatedOn())) {
				log.warn("Terminal Routes updated, clearing metadata");
				_md = null;
			}
		} else if (_md != null) {
			log.warn("Terminal Routes deleted, clearing metadata");
			_md = null;
		}
		
		// Check the cache
		if ((d != null) && (d.toHours() < 8) && (f.length() > 10240) && (_md != null)) {
			ctx.setHeader("Content-disposition", String.format("attachment; filename=%s", XML_ZIP));
			ctx.setContentType("application/zip");
			ctx.setHeader("max-age", 1800);
			ctx.setHeader("X-Airport-Count", _md.getAirportCount());
			ctx.setHeader("X-Signature", _md.getHash());
			ctx.setHeader("X-Signature-Type", _md.getHashType());
			sendFile(f, ctx.getResponse());
			return SC_OK;
		}

		// Get the DAO and the SIDs/STARs
		Map<String, Collection<TerminalRoute>> routes = new TreeMap<String, Collection<TerminalRoute>>();
		try {
			GetNavRoute navdao = new GetNavRoute(ctx.getConnection());
			navdao.getAll().forEach(tr -> CollectionUtils.addMapCollection(routes, tr.getICAO(), tr));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Convert to XML
		Map<String, String> docs = new TreeMap<String, String>();
		routes.entrySet().parallelStream().map(XMLFormatter::formatTR).forEach(me -> docs.put(me.getKey(), me.getValue()));
		routes.clear();
		
		try {
			int apCount = 0;
			try (ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(f), 262144))) {
				for (Iterator<Map.Entry<String, String>> i = docs.entrySet().iterator(); i.hasNext(); ) {
					Map.Entry<String, String> me = i.next();
					ZipEntry ze = new ZipEntry("ss_" + me.getKey() + ".xml");
					ze.setMethod(ZipEntry.DEFLATED);
					ze.setCreationTime(FileTime.from(Instant.now()));
					zout.putNextEntry(ze);
					PrintWriter pw = new PrintWriter(zout);
					pw.print(me.getValue());
					pw.flush();
					i.remove();
					apCount++;
				}
			}
			
			// Calculate the metadata
			MessageDigester md = new MessageDigester("SHA-256", 8192);
			try (InputStream is = new BufferedInputStream(new FileInputStream(f), 131072)) {
				_md = new Metadata(MessageDigester.convert(md.digest(is)), md.getAlgorithm());
				_md.setAirportCount(apCount);
			}

			// Format and write
			ctx.setHeader("Content-disposition", String.format("attachment; filename=%s", XML_ZIP));
			ctx.setContentType("application/zip");
			ctx.setExpiry(1800);
			ctx.setHeader("X-Airport-Count", _md.getAirportCount());
			ctx.setHeader("X-Signature", _md.getHash());
			ctx.setHeader("X-Signature-Type", _md.getHashType());
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