package org.deltava.acars;

import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;

import org.deltava.beans.acars.RouteEntry;

import org.deltava.dao.file.*;

class ReadWorker implements Runnable, Comparable<ReadWorker> {

	private final int _id;
	private final BlockingQueue<Integer> _work;
	private final SparseGlobalTile _gt;
	private final Logger log;
	private final RouteEntryFilter _filter;
	
	private final File DIR = new File("D:\\Temp\\TrackData");

	public ReadWorker(int id, RouteEntryFilter f, SparseGlobalTile out, BlockingQueue<Integer> work) {
		super();
		_id = id;
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
		return new File(new File(DIR, Integer.toHexString(id % 2048)), Integer.toHexString(id) + ".dat");
	}
	
	@Override
	public void run() {
		int cnt = 0; Integer id = _work.poll();
		while (id != null) {
			File dat = getFile(id.intValue());
			if (dat.exists()) {
				try (InputStream is = new FileInputStream(dat)) {
					try (InputStream gis = new GZIPInputStream(is, 20480)) {
						GetSerializedPosition psdao = new GetSerializedPosition(gis);
						Collection<? extends RouteEntry> entries = psdao.read();
						entries.stream().map(re -> _filter.filter(re)).filter(Objects::nonNull).forEach(pt -> _gt.plot(pt.x, pt.y));
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					throw new RuntimeException(e);
				}
			}
					
			cnt++;
			if ((cnt % 500) == 0)
				log.info("Processed " + id);
					
			id = _work.poll();
		} 

		log.info("Completed - " + cnt + " flights");
	}
}