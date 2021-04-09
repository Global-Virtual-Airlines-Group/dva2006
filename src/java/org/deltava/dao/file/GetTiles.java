// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.time.Instant;

import org.apache.log4j.Logger;

import org.deltava.dao.DAOException;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

import org.deltava.util.tile.SeriesReader;

import org.gvagroup.tile.*;

/**
 * A Data Access Object to read image tiles from the filesystem. 
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class GetTiles extends DAO implements SeriesReader {
	
	private static final Logger log = Logger.getLogger(GetTiles.class);
	
	/**
	 * Creates the Data Access Object.
	 */
	public GetTiles() {
		super(null);
	}

	@Override
	public Collection<String> getTypes() throws DAOException {
		
		Collection<String> results = new LinkedHashSet<String>();
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(Path.of(SystemData.get("path.tile")), FileUtils.IS_DIR)) {
			ds.forEach(p -> results.add(p.getFileName().toString()));
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
		
		return results;
	}

	@Override
	public Collection<Instant> getDates(String type) throws DAOException {
		
		Collection<Instant> results = new TreeSet<Instant>(Collections.reverseOrder());
		Path td = Path.of(SystemData.get("path.tile"), type);
		if (!Files.isDirectory(td))			
			return results;
		
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(td, FileUtils.IS_DIR)) {
			for (Path d : ds) {
				try {
					Instant isdDate = StringUtils.parseInstant(d.getFileName().toString(), "yyyyMMdd-HHmm");
					results.add(isdDate);
				} catch (Exception e) {
					log.warn("Unparseable date - " + d.getFileName());
				}
			}
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
		
		return results;
	}

	@Override
	public PNGTile getTile(String imgType, Instant effDate, TileAddress addr) throws DAOException {

		Path sd = Path.of(SystemData.get("path.tile"), imgType, StringUtils.format(effDate, "yyyyMMdd-HHmm"));
		if (!Files.isDirectory(sd))
			return null;
		
		// Check the file
		Path pf = sd.resolve(String.format("%s.png", addr.getName()));
		if (!Files.isReadable(pf))
			return null;
		
		// Load and return the file
		try {
			PNGTile pt = new PNGTile(addr);
			pt.setImage(Files.readAllBytes(pf));
			return pt;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}