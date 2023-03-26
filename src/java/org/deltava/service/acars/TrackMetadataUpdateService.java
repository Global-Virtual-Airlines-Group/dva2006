// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.*;
import java.sql.Connection;
import java.time.Instant;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.deltava.beans.Compression;
import org.deltava.beans.acars.*;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.dao.file.GetSerializedPosition;

import org.deltava.util.StringUtils;

/**
 * A Web Service to update serialized ACARS track metadata if needed.
 * @author Luke
 * @version 10.5
 * @since 10.5
 */

public class TrackMetadataUpdateService extends WebService {
	
	private static final Logger log = Logger.getLogger(ArchiveMetadata.class);

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Check role
		if (!ctx.isUserInRole("Developer"))
			return SC_FORBIDDEN;
		
		// Check for the archive
		int acarsID = StringUtils.parse(ctx.getParameter("id"), -1);
		File f = ArchiveHelper.getPositions(acarsID);
		
		// Get the metadata
		boolean isUpdated = false;
		try {
			Connection con = ctx.getConnection();
			SetACARSArchive awdao = new SetACARSArchive(con);
			if (!f.exists()) {
				awdao.delete(acarsID);
				return SC_NOT_FOUND;
			}
			
			GetACARSData dao = new GetACARSData(con);
			ArchiveMetadata md = dao.getArchiveInfo(acarsID);
			if (md == null)
				md = new ArchiveMetadata(acarsID);
			
			// Update metadata
			Compression c = Compression.detect(f);
			try (InputStream is = c.getStream(new FileInputStream(f))) {
				GetSerializedPosition psdao = new GetSerializedPosition(is);
				Collection<? extends RouteEntry> positions = psdao.read();
				SerializedDataVersion v = psdao.getFormat();
				if (md.getFormat() != v) {
					if (md.getFormat() != null)
						log.warn(String.format("Updating format for %d from %s to %s", Integer.valueOf(acarsID), md.getFormat(), v));
					
					isUpdated = true;
					md.setFormat(v);
					md.setPositionCount(positions.size());
					md.setSize((int) f.length());
					if (md.getArchivedOn() == null)
						md.setArchivedOn(Instant.ofEpochMilli(f.lastModified()));
				}
			}
			
			// Write update
			if (isUpdated)
				awdao.update(md);
		} catch (DAOException | IOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		return isUpdated ? SC_OK : SC_NOT_MODIFIED;
	}

	@Override
	public boolean isSecure() {
		return true;
	}
	
	@Override
	public boolean isLogged() {
		return false;
	}
}