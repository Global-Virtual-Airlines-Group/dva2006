// Copyright 2013, 2014, 2015, 2016, 2017, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.wx.*;

import org.deltava.util.tile.*;
import org.gvagroup.tile.*;

/**
 * A worker thread to generate GFS jet stream tiles.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

class GFSTileWorker extends Thread {
	private final BlockingQueue<TileAddress> _work;
	private final GRIBResult<WindData> _data;
	private final ImageSeries _out;
	private final TilePlotter _plt = new GFSTilePlotter();
	
	/**
	 * Creates the worker.
	 * @param id the worker ID
	 * @param work the tile addresses to generate
	 * @param data the wind data
	 * @param out the ImageSeries with the data
	 */
	GFSTileWorker(int id, BlockingQueue<TileAddress> work, GRIBResult<WindData> data, ImageSeries out) {
		super("TileWorker-"+ id);
		setPriority(MIN_PRIORITY);
		setDaemon(true);
		_work = work;
		_data = data;
		_out = out;
	}

	@Override
	public void run() {
		TileAddress addr = _work.poll();
		while (addr != null) {
			Projection p = new MercatorProjection(addr.getLevel());

			// Plot the pixels
			int pX = addr.getPixelX(); int pY = addr.getPixelY();
			BufferedImage img = new BufferedImage(Tile.WIDTH, Tile.HEIGHT, BufferedImage.TYPE_INT_ARGB);
			SingleTile st = new SingleTile(addr, img);
			boolean hasData = false;
			for (int x = 0; x < Tile.WIDTH; x++) {
				for (int y = 0; y < Tile.HEIGHT; y++) {
					GeoLocation loc = p.getGeoPosition(pX + x, pY + y);
					if (Math.abs(loc.getLatitude()) > 80.5) continue;
					WindData wd = _data.getResult(loc);
					if (wd.getJetStreamSpeed() < 25) continue;

					img.setRGB(x, y, _plt.plot(wd.getJetStreamSpeed()));
					hasData = true;
				}
			}

			// Convert to PNG
			if (hasData) {
				PNGTile png = new PNGTile(st.getAddress(), st.getImage());
				synchronized (_out) {
					_out.put(png.getAddress(), png);
				}
			}

			addr = _work.poll();
		}
	}
}