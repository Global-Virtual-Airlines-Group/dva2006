package org.deltava.acars;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import org.deltava.beans.acars.RouteEntry;

import org.deltava.dao.file.*;

class ReadWorker implements Runnable, Comparable<ReadWorker> {

	private final Connection _c;
	private final int _id;
	private final BlockingQueue<Integer> _work;
	private final SparseGlobalTile _gt;
	private final Logger log;
	private final RouteEntryFilter _filter;
	
	private final File DIR = new File("D:\\Temp\\TrackData");

	public ReadWorker(int id, RouteEntryFilter f, Connection c, SparseGlobalTile out, BlockingQueue<Integer> work) {
		super();
		_id = id;
		_c = c;
		_work = work;
		_gt = out;
		_filter = f;
		log = Logger.getLogger(ReadWorker.class.getPackage().getName() + "." + toString());
	}

	@Override
	public String toString() {
		return "ReadWorker-" + _id;
	}

	@Override
	public int compareTo(ReadWorker rw2) {
		return Integer.valueOf(_id).compareTo(Integer.valueOf(rw2._id));
	}
	
	private File getFile(int id) {
		int hash = id & 0xFF;
		return new File(new File(DIR, String.valueOf(hash)), String.valueOf(id) + ".dat");
	}
	
	@Override
	public void run() {
		int cnt = 0;
		try {
			try (PreparedStatement ps = _c.prepareStatement("SELECT DATA FROM POS_ARCHIVE WHERE (ID=?)")) {
				Integer id = _work.poll();
				while (id != null) {
					File dat = getFile(id.intValue()); InputStream is = null;
					if (!dat.exists()) {
						ps.setInt(1, id.intValue()); byte[] data = null;
						try (ResultSet rs = ps.executeQuery()) {
							if (rs.next())
								data = rs.getBytes(1);
						}
						
						if (data != null) {
							is = new ByteArrayInputStream(data); dat.getParentFile().mkdirs();
							try (OutputStream out = new BufferedOutputStream(new FileOutputStream(dat))) {
								out.write(data);
							}
						}
					} else
						is = new BufferedInputStream(new FileInputStream(dat), 512 * 1024);
					
					if (is != null) {
						GetSerializedPosition psdao = new GetSerializedPosition(is);
						Collection<? extends RouteEntry> entries = psdao.read();
						is.close();
						for (RouteEntry re : entries) {
							java.awt.Point pt = _filter.filter(re);
							if (pt != null)
								_gt.plot(pt.x, pt.y);
						}
					}

					cnt++;
					if ((cnt % 250) == 0)
						log.info("Processed " + id);
					
					id = _work.poll();
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			log.info("Completed - " + cnt + " flights");
			try {
				_c.close();
			} catch (Exception e) { 
				// empty
			}
		}
	}
}