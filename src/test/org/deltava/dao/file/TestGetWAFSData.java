package org.deltava.dao.file;

import java.io.*;
import java.util.*;
import java.awt.Color;
import java.awt.image.*;
import java.util.concurrent.*;

import org.apache.log4j.*;

import junit.framework.TestCase;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.wx.*;
import org.deltava.util.ThreadUtils;
import org.deltava.util.tile.*;

public class TestGetWAFSData extends TestCase {

	Logger log;
	
	private class TileWorker extends Thread {
		private final BlockingQueue<TileAddress> _work;
		private final GRIBResult<WindData> _data;
		private final Projection _p;

		TileWorker(int id, BlockingQueue<TileAddress> work, GRIBResult<WindData> data, Projection p) {
			super("TileWorker-"+ id);
			setDaemon(true);
			_work = work;
			_data = data;
			_p = p;
		}

		@Override
		public void run() {
			TileAddress addr = _work.poll();
			while (addr != null) {
				log.info(getName() + " generating tile " + addr);

				// Plot the pixels
				int pX = addr.getPixelX(); int pY = addr.getPixelY();
				BufferedImage img = new BufferedImage(Tile.WIDTH, Tile.HEIGHT, BufferedImage.TYPE_INT_ARGB);
				SingleTile st = new SingleTile(addr);
				st.setImage(img); boolean hasData = false;
				for (int x = 0; x < Tile.WIDTH; x++) {
					for (int y = 0; y < Tile.HEIGHT; y++) {
						GeoLocation loc = _p.getGeoPosition(pX + x, pY + y);
						if (Math.abs(loc.getLatitude()) > 81)
							continue;

						WindData wd = _data.getResult(loc);
						if (wd.getJetStreamSpeed() < 40)
							continue;
						
						int c = Math.min(255, wd.getJetStreamSpeed() + 40);
						if (wd.getJetStreamSpeed() > 99) {
							int r = Math.min(c+32, 255);
							int g = (wd.getJetStreamSpeed() > 150) ? Math.min(255, c+36): c;
							Color rgb = new Color(r,g,c);
							img.setRGB(x, y, rgb.getRGB());
						} else
							img.setRGB(x, y, new Color(c, c, c).getRGB());
						
						hasData = true;
					}
				}

				// Convert to PNG
				if (hasData) {
					File tf = new File("/Users/luke/tiles", st.getName() + ".png");
					try (FileOutputStream fo = new FileOutputStream(tf)) {
						PNGTile png = new PNGTile(st);
						fo.write(png.getData());
					} catch (IOException ie) {
						log.error(ie.getMessage(), ie);
					}
				}

				addr = _work.poll();
			}
		}
	}

	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("etc/log4j.test.properties");
		log = Logger.getLogger(GetWAFSData.class);
	}

	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}

	public void testLoadGRIB2() throws Exception {

		File f = new File("data/gfs.t12z.pgrb2bf00.grib");
		assertTrue(f.exists());

		// Load the data
		GetWAFSData dao = new GetWAFSData(f.getAbsolutePath());
		assertNotNull(dao);
		GRIBResult<WindData> data = dao.load();
		assertNotNull(data);
		assertTrue(data.size() > 0);
		
		int wMin = 500; int wMax = 0;
		for (WindData wd : data) {
			wMin = Math.min(wMin, wd.getJetStreamSpeed());
			wMax = Math.max(wMax, wd.getJetStreamSpeed());
		}
		
		log.info("Jet Stream max = " +wMax +", min = " + wMin);
		
		// Get threads
		int threads = Math.max(2, Runtime.getRuntime().availableProcessors() - 2);

		// Plot the tiles
		for (int zoom = 5; zoom > 1; zoom--) {
			Projection p = new MercatorProjection(zoom);
			TileAddress nw = p.getAddress(data.getNW()); TileAddress se = p.getAddress(data.getSE());

			// Make work
			BlockingQueue<TileAddress> work = new LinkedBlockingQueue<TileAddress>();
			for (int tx = nw.getX(); tx <= se.getX(); tx++) {
				for (int ty = nw.getY(); ty <= se.getY(); ty++)
					work.add(new TileAddress(tx, ty, zoom));
			}
			
			// Fire off the threads
			Collection<TileWorker> workers = new ArrayList<TileWorker>();
			for (int x = 0; x < threads; x++) {
				TileWorker tw = new TileWorker(x+1, work, data, p);
				workers.add(tw);
				tw.start();
			}
			
			ThreadUtils.waitOnPool(workers);
		}
	}
}