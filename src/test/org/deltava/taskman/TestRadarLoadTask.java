package org.deltava.taskman;

import java.io.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import org.deltava.dao.DAOException;
import org.deltava.dao.mc.MemcachedDAO;

import org.deltava.util.tile.*;

import org.deltava.tasks.RadarLoadTask;
import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class TestRadarLoadTask extends TestCase {

	private final class MockRadarLoadTask extends RadarLoadTask {
		
		MockRadarLoadTask() {
			super(new NullTileWriter());
		}
		
		@Override
		public void run() {
			super.execute(new TaskContext());
		}
	}
	
	final class NullTileWriter implements SeriesWriter {
		
		public void write(ImageSeries is) {
			// empty
		}
	}
	
	final class FileSystemTileWriter implements SeriesWriter {
		private final Logger log = Logger.getLogger(TestRadarLoadTask.class);
		private final File _root;
		
		FileSystemTileWriter(String rootPath) {
			super();
			_root = new File(rootPath);
		}
		
		@Override
		public void write(ImageSeries is) throws DAOException {
			for (PNGTile pt : is) {
				TileAddress addr = pt.getAddress();
				File tp = new File(_root, String.valueOf(addr.getLevel()));
				tp.mkdirs();
				File tF = new File(tp, addr.toString() + ".png");
				try (FileOutputStream fos = new FileOutputStream(tF)) {
					fos.write(pt.getData());
					log.info("Wrote " + tF.getAbsolutePath());
				} catch (IOException ie) {
					throw new DAOException(ie);
				}
			}
		}
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.test.properties");
		SystemData.init();
		
		// Override numbers
		//SystemData.add("weather.radar.se.lat", Double.valueOf(41));
		//SystemData.add("weather.radar.se.lng", Double.valueOf(-101));
	}
	
	protected void tearDown() throws Exception {
		MemcachedDAO.shutdown();
		super.tearDown();
	}

	public void testRadarLoad() {
		
		TaskContext ctx = new TaskContext();
		assertNotNull(ctx);
		
		RadarLoadTask task = new MockRadarLoadTask();
		assertNotNull(task);
		task.run();
	}
}