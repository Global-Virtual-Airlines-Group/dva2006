// Copyright 2013, 2015, 2016, 2017, 2021 Global Virtual Airlines Group. All Rights Reserved.
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
 * A Data Access Object to load WAFS GRIB2 winds aloft data. The field format for the GRIB file is located
 * at {@link "https://www.nco.ncep.noaa.gov/pmb/products/gfs/gfs.t00z.pgrb2.0p25.f000.shtml"}
 * @author Luke
 * @version 10.0
 * @since 5.2
 */

public class GetWAFSData extends DAO implements Closeable {
	
	// GRIB layer offets - temp, U, V
	// LOW(850, 5000), MIDLOW(650, 12000), MID(500, 18000), LOJET(300, 30000), JET(250, 34000), HIGH(200, 38600);
	private static final int[][] RECORDS = {{462, 468, 469}, {398, 404, 405}, {350, 356, 357}, {286, 292, 293}, {270, 276, 277}, {254, 260, 261}};
	
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
			final int[] offsets = RECORDS[lvl.ordinal()];
			List<Grib2Record> records = gi.getRecords();
			Grib2Record rc = records.get(offsets[0]);
			Grib2GDSVariables gds = rc.getGDS().getGdsVars();
			GRIBResult<WindData> gr = new GRIBResult<WindData>(gds.getNx(), gds.getNy(), gds.getDx(), gds.getDy());
			gr.setStart(gds.getLa1(), gds.getLo1() - 180);
			WindData[] results = initObs(lvl, gds);
			
			// Go through the records
			for (int l = 0; l < offsets.length; l++) {
				int layer = offsets[l];
				if (layer >= records.size()) {
					log.warn(String.format("GRIB2 file is only %d records long (idx=%d)", Integer.valueOf(records.size()), Integer.valueOf(layer)));
					continue;
				}
				
				// Get the data
				Grib2Data gd = new Grib2Data(_raf);
				rc = records.get(layer-1);
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
						wd.setTemperature(Math.round(v - 273.15f));
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