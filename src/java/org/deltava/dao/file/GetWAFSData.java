// Copyright 2013, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.wx.*;
import org.deltava.dao.DAOException;

import ucar.grib.NotSupportedException;
import ucar.grib.grib2.*;
import ucar.unidata.io.RandomAccessFile;

/**
 * A Data Access Object to load WAFS GRIB2 winds aloft data. 
 * @author Luke
 * @version 8.1
 * @since 5.2
 */

public class GetWAFSData extends DAO implements Closeable {
	
	// GRIB layer offets - temp, U, V
	// LOW(875, 4000), MIDLOW(625, 12000), MID(475, 20000), LOJET(325, 28000), JET(275, 32000), HIGH(225, 38000);
	private static final int[][] RECORDS = {{183, 187, 188}, {143, 146, 147}, {113, 117, 118}, {83, 87, 88}, {73, 77, 78}, {63, 67, 68}};
	
	private static final Logger log = Logger.getLogger(GetWAFSData.class);

	private final String _fileName;
	private RandomAccessFile _raf;
	
	/**
	 * Initializes the Data Access Object.
	 * @param file the GRIB2 file name
	 */
	public GetWAFSData(String file) {
		super(null);
		_fileName = file;
	}
	
	/*
	 * Initializes the Observation array.
	 */
	private static WindData[] initObs(PressureLevel pl, Grib2GDSVariables grid) {
		int gridW = grid.getNx(); int gridH = grid.getNy();
		log.info("Generating " + gridW + "x" + gridH + " grid, dLat = " + grid.getDx() + ", dLng = " + grid.getDy());
		
		WindData[] results = new WindData[gridW * gridH];
		float lat = grid.getLa1();
		float lng = grid.getLo1();
		if (lng > 180)
			lng -= 360;
		
		// Init the observations and their lat/long
		int ofs = 0; float startlng = lng;
		for (int y = 0; y < gridH; y++) {
			for (int x = 0; x < gridW; x++) {
				results[ofs] = new WindData(pl, lat, lng);
				lng += grid.getDx();
				ofs++;
			}
			
			lat += grid.getDy();
			lng = startlng;
		}
		
		return results;
	}

	/**
	 * Loads GFS wind/temperature data for a given Pressure Level.
	 * @param lvl the PressureLevel
	 * @return a GRIBResult object
	 * @throws DAOException if an I/O error occurs
	 */
	public GRIBResult<WindData> load(PressureLevel lvl) throws DAOException {
		try {
			_raf = new RandomAccessFile(_fileName, "r");
			_raf.order(RandomAccessFile.BIG_ENDIAN);
			Grib2Input gi = new Grib2Input(_raf);
			try {
				gi.scan(false, false);
			} catch (NotSupportedException nse) {
				throw new DAOException(nse.getMessage());
			}

			// Save total number of observations - assume all layers are equivalent
			List<Grib2Record> records = gi.getRecords();
			Grib2Record rc = records.get(0);
			Grib2GDSVariables gds = rc.getGDS().getGdsVars();
			GRIBResult<WindData> gr = new GRIBResult<WindData>(gds.getNx(), gds.getNy(), gds.getDx(), gds.getDy());
			gr.setStart(gds.getLa1(), gds.getLo1() - 180);
			WindData[] results = initObs(lvl, gds);
			
			// Go through the records
			final int[] ofsetts = RECORDS[lvl.ordinal()];
			for (int l = 0; l < ofsetts.length; l++) {
				int layer = ofsetts[l];
				if (layer >= records.size()) {
					log.warn("GRIB2 file is only " + records.size() + " records long");
					continue;
				}
				
				rc = records.get(layer-1);

				// Get the data
				Grib2Data gd = new Grib2Data(_raf);
				float[] data = gd.getData(rc.getGdsOffset(), rc.getPdsOffset(), rc.getId().getRefTime());
				
				// Populate the data
				for (int x = 0; x < data.length; x++) {
					float v = data[x];
					WindData wd = results[x];
					
					switch (l) {
					case 1:
						wd.setJetStreamU(v);
						break;
						
					case 2:
						wd.setJetStreamV(v);
						break;
						
					case 0:
					default:
						wd.setTemperature(Math.round(v));
						break;
					}
				}
			}
			
			gr.addAll(Arrays.asList(results));
			return gr;
		} catch (IOException ie) {
			throw new DAOException(ie);
		} finally {
			close();
		}
	}
	
	/**
	 * Finalizer to clean up the file handle.
	 */
	@Override
	public void close() {
		try {
			if (_raf != null)
				_raf.close();
		} catch (Exception e) {
			// empty
		} finally {
			_raf = null;
		}
	}
}