// Copyright 2008 The Weather Channel Interactive. All Rights Reserved.
package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import junit.framework.TestCase;

import org.deltava.util.ThreadUtils;
import org.deltava.util.tile.*;

import com.ice.tar.*;

public class TestPlotPositions extends TestCase {

	private static final String URL = "jdbc:mysql://polaris.sce.net/acars?user=luke&password=14072";

	protected final MercatorProjection _mp = new MercatorProjection(3);
	protected final static int MAX_PPP = 48;

	protected final Queue<Integer> _work = new ConcurrentLinkedQueue<Integer>();
	protected final Queue<TileAddress> _imgWork = new ConcurrentLinkedQueue<TileAddress>();
	
	protected final ConcurrentMap<Point, Integer> _points = new ConcurrentHashMap<Point, Integer>(16384000);
	protected final Map<TileAddress, Collection<Point>> _tileMap = new HashMap<TileAddress, Collection<Point>>();
	
	protected int[] _colors;

	protected void setUp() throws Exception {
		super.setUp();
		Class.forName("com.mysql.jdbc.Driver");
	}

	private class WriteWorker extends Thread {
		private TarOutputStream _out;
		
		WriteWorker(int id, TarOutputStream out) {
			super("WriteWorker-" + id);
			setDaemon(true);
			_out = out;
		}
		
		public void run() {
			while (!_imgWork.isEmpty() && !isInterrupted()) {
				TileAddress addr = _imgWork.poll();
				if (addr != null) {
					SingleTile st = new SingleTile(addr);
					BufferedImage img = new BufferedImage(Tile.WIDTH, Tile.HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
					st.setImage(img);
					
					Collection<Point> points = _tileMap.get(addr);
					for (Iterator<Point> pi = points.iterator(); pi.hasNext(); ) {
						Point p = pi.next();
						int cnt = Math.min(MAX_PPP, _points.get(p).intValue());
						_points.remove(p);

						// Draw the pixel
						int tx = p.x - addr.getPixelX();
						int ty = p.y - addr.getPixelY();
						if ((tx >= 0) && (tx < Tile.WIDTH) && (ty>= 0) && (ty < Tile.HEIGHT) && (cnt > 5))
							img.setRGB(tx, ty, _colors[cnt]);
					}
					
					// Save the image
					_tileMap.remove(addr);
					ByteArrayOutputStream buf = new ByteArrayOutputStream();
					TarEntry entry = new TarEntry(st.getName() + ".png");
					try {
						ImageIO.write(img, "png", buf);
						entry.setSize(buf.size());
						entry.setUnixTarFormat();
						entry.setIds(500, 500);
						synchronized (_out) {
							_out.putNextEntry(entry);
							_out.write(buf.toByteArray());
							_out.closeEntry();
						}
					} catch (IOException ie) {
						System.err.println("Error writing " + entry.getName() + " " + ie.getMessage());
					}
				}
			}
		}
	}
	
	private class ReadWorker extends Thread {
		private Connection _c;

		ReadWorker(int id, String url) throws SQLException {
			super("ReadWorker-" + id);
			setDaemon(true);
			_c = DriverManager.getConnection(url);
		}

		public void run() {
			try {
				PreparedStatement ps = _c.prepareStatement("SELECT LAT, LNG FROM TRACKPOS WHERE (ID=?)");	
				//PreparedStatement ps = _c.prepareStatement("SELECT LAT, LNG FROM TRACKPOS WHERE (ID=?) AND "
					//	+ "((LAT < 36) AND (LAT >= 32)) AND ((LNG > -87) AND (LNG <= -82))");
				ps.setFetchSize(1600);
				while (!_work.isEmpty() && !isInterrupted()) {
					Integer id = _work.poll();
					if (id != null) {
						ps.setInt(1, id.intValue());
						ResultSet rs = ps.executeQuery();			
						while (rs.next()) {
							Point pt = _mp.getPixelAddress(rs.getDouble(1), rs.getDouble(2));
							Integer cnt = _points.putIfAbsent(pt, Integer.valueOf(1));
							if (cnt != null)
								_points.put(pt, Integer.valueOf(cnt.intValue() + 1));
						}
						
						// Write the rows
						rs.close();
						if ((id.intValue() % 2500) == 0)
							System.out.println("Processed " + id);
					}
				}			
			
				// Clean up
				ps.close();
			} catch (SQLException se) {
				se.printStackTrace(System.err);
			} finally {
				try {
					_c.close();
				} catch (Exception e) {
					// empty
				}
			}
		}
	}

	public void testLoadPositions() throws Exception {

		Connection c = DriverManager.getConnection(URL);
		Statement s = c.createStatement();
		ResultSet rs = s.executeQuery("SELECT ID FROM IDS");
		while (rs.next())
			_work.add(new Integer(rs.getInt(1)));

		rs.close();
		s.close();
		c.close();

		// Spawn the workers
		Collection<Thread> workers = new ArrayList<Thread>(16);
		for (int x = 1; x <= 14; x++) {
			ReadWorker worker = new ReadWorker(x, URL);
			worker.start();
			workers.add(worker);
		}

		// Wait for the pool to finish
		ThreadUtils.waitOnPool(workers);
		assertFalse(_points.isEmpty());

		// Get the maximum value
		float maxValue = 0; int pixels = _points.size();
		Map<Integer, Integer> distrib = new TreeMap<Integer, Integer>();
		for (Iterator<Map.Entry<Point, Integer>> i = _points.entrySet().iterator(); i.hasNext();) {
			Map.Entry<Point, Integer> e = i.next();
			Point pt = e.getKey();
			Integer cnt = e.getValue();
			maxValue = Math.max(maxValue, cnt.intValue());
			if (distrib.containsKey(cnt))
				distrib.put(cnt, Integer.valueOf(distrib.get(cnt).intValue() + 1));
			else
				distrib.put(cnt, Integer.valueOf(1));
			
			// Save the address
			TileAddress addr = TileAddress.fromPixel(pt.x, pt.y, _mp.getZoomLevel());
			Collection<Point> points = _tileMap.get(addr);
			if (points == null) {
				points = new ArrayList<Point>();
				_tileMap.put(addr, points);
			}
			
			points.add(pt);
		}

		// Load color maps
		maxValue = Math.min(MAX_PPP, maxValue);
		System.out.println("Max is " + maxValue);
		_colors = new int[Math.round(maxValue) + 1];
		for (int x = 0; x <= maxValue; x++) {
			float value = (x / maxValue) * 0.8f + 0.2f;
			Color cl = new Color(value, value, value);
			_colors[x] = cl.getRGB();
		}

		// Build the Tiles
		System.out.println("Loaded " + pixels + " points, " + _tileMap.size() + " tiles");
		TarGzOutputStream tar = new TarGzOutputStream(new FileOutputStream("c:\\temp\\" + _mp.getZoomLevel() + ".tar.gz"));

		// Build the tiles
		_imgWork.addAll(_tileMap.keySet());
		
		// Spawn the workers
		workers.clear();
		for (int x = 1; x <= 3; x++) {
			WriteWorker worker = new WriteWorker(x, tar);
			worker.start();
			workers.add(worker);
		}
		
		// Wait for the pool to finish
		ThreadUtils.waitOnPool(workers);
		tar.close();
	}
}