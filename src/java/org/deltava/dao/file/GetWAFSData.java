// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 5.2
 * @since 5.2
 */

public class GetWAFSData extends DAO {
	
	// GRIB layer offets - temp, U, V
	private static final int[] RECORDS = {67, 68};
	
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
	private static WindData[] initObs(Grib2GDSVariables grid) {
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
				WindData wd = new WindData(lat, lng);
				results[ofs] = wd;
				lng += grid.getDx();
				ofs++;
			}
			
			lat += grid.getDy();
			lng = startlng;
		}
		
		return results;
	}

	public GRIBResult<WindData> load() throws DAOException {
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
			WindData[] results = initObs(gds);
			
			// Go through the records
			for (int l = 0; l < RECORDS.length; l++) {
				int layer = RECORDS[l];
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
					case 0:
						wd.setJetStreamU(v);
						break;
						
					case 1:
						wd.setJetStreamV(v);
						break;
					}
				}
			}
			
			_raf.close();
			gr.addAll(Arrays.asList(results));
			return gr;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
	
	/**
	 * Finalizer to clean up the file handle.
	 */
	@Override
	protected void finalize() {
		try {
			if (_raf != null) {
				_raf.close();
				_raf = null;
			}
		} catch (Exception e) {
			// empty
		}
	}
}
