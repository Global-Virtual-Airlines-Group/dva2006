// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.navdata;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.text.*;
import java.time.*;
import java.nio.file.attribute.FileTime;

import org.jdom2.*;
import org.apache.log4j.Logger;

import org.deltava.beans.navdata.Gate;
import org.deltava.beans.schedule.Airport;

import org.deltava.crypt.MessageDigester;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display Gate data to ACARS clients.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class XMLGateService extends DownloadService {
	
	private static final Logger log = Logger.getLogger(XMLGateService.class);
	private static final String XML_ZIP = "xmlgates.zip";
	
	private Metadata _md;

	/* (non-Javadoc)
	 * @see org.deltava.service.WebService#execute(org.deltava.service.ServiceContext)
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Check if the file exists
		File cacheDir = new File(SystemData.get("schedule.cache"));
		File f = new File(cacheDir, "gates.zip");
		Duration d = null;
		if (f.exists()) {
			Instant fileAge = Instant.ofEpochMilli(f.lastModified());
			d = Duration.between(fileAge, Instant.now());
			if ((_md != null) && fileAge.isAfter(_md.getCreatedOn())) {
				log.warn("Gates updated, clearing metadata");
				_md = null;
			}
		} else if (_md != null) {
			log.warn("Gates deleted, clearing metadata");
			_md = null;
		}
		
		// Check the cache
		if ((d != null) && (d.toHours() < 48) && (f.length() > 10240) && (_md != null)) {
			ctx.setHeader("Content-disposition", String.format("attachment; filename=%s", XML_ZIP));
			ctx.setContentType("application/zip");
			ctx.setHeader("max-age", 1800);
			ctx.setHeader("X-Airport-Count", _md.getAirportCount());
			ctx.setHeader("X-Signature", _md.getHash());
			ctx.setHeader("X-Signature-Type", _md.getHashType());
			sendFile(f, ctx.getResponse());
			return SC_OK;
		}
		
		// Get the DAO and the Gates
		Map<Airport, Collection<Gate>> allGates = new HashMap<Airport, Collection<Gate>>();
		try {
			GetGates navdao = new GetGates(ctx.getConnection());
			Collection<Gate> gates = navdao.getAll();
			for (Gate g: gates) {
				Airport a = SystemData.getAirport(g.getCode());
				if ((a == null) || StringUtils.isEmpty(a.getRegion())) continue;
				CollectionUtils.addMapCollection(allGates, a, g, ArrayList::new);
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Convert to XML
		Map<String, String> docs = new TreeMap<String, String>();
		allGates.entrySet().parallelStream().map(XMLGateService::formatXML).forEach(me -> docs.put(me.getKey(), me.getValue()));
		allGates.clear();
		
		try {
			int apCount = 0;
			try (ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(f), 262144))) {
				for (Iterator<Map.Entry<String, String>> i = docs.entrySet().iterator(); i.hasNext();) {
					Map.Entry<String, String> me = i.next();
					ZipEntry ze = new ZipEntry("gate_" + me.getKey() + ".xml");
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
			try (InputStream is = new BufferedInputStream(new FileInputStream(f), 65536)) {
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
	
	private static Map.Entry<String, String> formatXML(Map.Entry<Airport, Collection<Gate>> me) {
		final NumberFormat df = new DecimalFormat("#0.000000");
		Airport a = me.getKey();
		Document doc = new Document();
		Element re = new Element("gates");
		re.setAttribute("icao", a.getICAO());
		re.setAttribute("region", a.getRegion());
		doc.setRootElement(re);

		for (Gate g : me.getValue()) {
			Element ge = new Element("gate");
			ge.setAttribute("name", g.getName());
			ge.setAttribute("sim", g.getSimulator().toString());
			ge.setAttribute("hdg", String.valueOf(g.getHeading()));
			ge.setAttribute("lat", df.format(g.getLatitude()));
			ge.setAttribute("lng", df.format(g.getLongitude()));
			ge.setAttribute("zone", g.getZone().name());
			g.getAirlines().forEach(al -> ge.addContent(XMLUtils.createElement("airline", al.getCode(), false)));
			re.addContent(ge);
		}
		
		return Map.entry(a.getICAO().toLowerCase(), XMLUtils.format(doc, "UTF-8"));
	}

	@Override
	public boolean isSecure() {
		return true;
	}
}