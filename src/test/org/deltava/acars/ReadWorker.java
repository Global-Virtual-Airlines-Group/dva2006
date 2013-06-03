package org.deltava.acars;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import org.deltava.beans.acars.RouteEntry;
import org.deltava.dao.file.GetSerializedPosition;

class ReadWorker implements Runnable, Comparable<ReadWorker> {

	private final Connection _c;
	private final int _id;
	private final BlockingQueue<Integer> _work;
	private final SparseGlobalTile _gt;
	private final Logger log;
	private final RouteEntryFilter _filter;

	public ReadWorker(int id, RouteEntryFilter f, Connection c, SparseGlobalTile out, BlockingQueue<Integer> work) {
		super();
		_id = id;
		_c = c;
		_work = work;
		_gt = out;
		_filter = f;
		log = Logger.getLogger(ReadWorker.class.getPackage().getName() + "." + toString());
	}

	public String toString() {
		return "ReadWorker-" + _id;
	}

	public int compareTo(ReadWorker rw2) {
		return Integer.valueOf(_id).compareTo(Integer.valueOf(rw2._id));
	}

	@Override
	public void run() {
		int cnt = 0;
		try {
			try (PreparedStatement ps = _c.prepareStatement("SELECT DATA FROM POS_ARCHIVE WHERE (ID=?)")) {
				Integer id = _work.poll();
				while (id != null) {
					ps.setInt(1, id.intValue());
					try (ResultSet rs = ps.executeQuery()) {
						if (rs.next()) {
							InputStream in = new ByteArrayInputStream(rs.getBytes(1));
							GetSerializedPosition psdao = new GetSerializedPosition(in);
							Collection<? extends RouteEntry> entries = psdao.read();
							for (RouteEntry re : entries) {
								java.awt.Point pt = _filter.filter(re);
								if (pt != null)
									_gt.plot(pt.x, pt.y);
							}
						}
					}

					cnt++;
					if ((id.intValue() % 2500) == 0)
						log.info("Processed " + id);
					
					id = _work.poll();
				}
			}
			
			_c.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			log.info("Completed - " + cnt + " flights");
		}
	}
}