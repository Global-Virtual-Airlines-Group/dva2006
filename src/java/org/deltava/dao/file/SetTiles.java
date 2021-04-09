// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.nio.file.*;
import java.util.Map;

import org.deltava.dao.DAOException;

import org.deltava.util.*;
import org.deltava.util.tile.SeriesWriter;
import org.deltava.util.system.SystemData;

import org.gvagroup.tile.*;

/**
 * A Data Access Object to write map tiles to the filesystem.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class SetTiles implements SeriesWriter {
	
	@Override
	public void write(ImageSeries is) throws DAOException {
		
		Path sPath = Path.of(SystemData.get("path.tile"), is.getType(), StringUtils.format(is.getDate(), "yyyyMMdd-HHmm"));
		try {
			Files.createDirectories(sPath);
			try (DataOutputStream mos = new DataOutputStream(Files.newOutputStream(sPath.resolve("img.data"), StandardOpenOption.CREATE, StandardOpenOption.WRITE))) {
				mos.write(1); // version
				mos.writeLong(is.getDate().toEpochMilli());
				mos.writeUTF(is.getType());
				mos.write(is.size());
			}
			
			for (Map.Entry<TileAddress, PNGTile> te : is.entrySet()) {
				Path tp = sPath.resolve(String.format("%s.png", te.getKey().getName()));
				Files.write(tp, te.getValue().getData(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			}
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}

	@Override
	public void purge(ImageSeries is) throws DAOException {
		
		// Find the directory
		Path sPath = Path.of(SystemData.get("path.tile"), is.getType(), StringUtils.format(is.getDate(), "yyyyMMdd-HHmm"));
		if (!Files.isDirectory(sPath)) return;
		try {
			try (DirectoryStream<Path> ds = Files.newDirectoryStream(sPath, FileUtils.IS_FILE)) {
				for (Path f : ds)
					Files.delete(f);
			}
			
			Files.delete(sPath);
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}