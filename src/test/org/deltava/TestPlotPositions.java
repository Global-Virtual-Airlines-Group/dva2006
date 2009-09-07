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

	protected final Queue<Integer> _IDwork = new ConcurrentLinkedQueue<Integer>();
	protected final Queue<Map.Entry<TileAddress, Collection<Point>>> _imgWork = new ConcurrentLinkedQueue<Map.Entry<TileAddress, Collection<Point>>>();
	
	protected final ConcurrentMap<Point, Integer> _points = new ConcurrentHashMap<Point, Integer>(10240000);
	protected final Map<TileAddress, Collection<Point>> _tileMap = new HashMap<TileAddress, Collection<Point>>(1024);
	
	private final SortedSet<ProjectInfo> _zooms = new TreeSet<ProjectInfo>(Collections.reverseOrder()); 
	
	protected int[] _colors;

	protected void setUp() throws Exception {
		super.setUp();
		Class<?> c= Class.forName("com.mysql.jdbc.Driver");
		assertNotNull(c);
		
		// Build Zoom levels
		_zooms.add(new ProjectInfo(3, 96, 6));
		_zooms.add(new ProjectInfo(4, 80, 5));
		_zooms.add(new ProjectInfo(5, 64, 4));
		_zooms.add(new ProjectInfo(6, 52, 3));
		_zooms.add(new ProjectInfo(7, 48, 3));
		_zooms.add(new ProjectInfo(8, 42, 3));
		_zooms.add(new ProjectInfo(9, 40, 3));
	}
	
	private class ProjectInfo implements Comparable<ProjectInfo> {
		private int _zoom;
		private int _max;
		private int _min;
		
		ProjectInfo(int zoom, int maxPPP, int minPPP) {
			super();
			_zoom = Math.max(1, zoom);
			_min = Math.max(1, minPPP);
			_max = Math.max(_min, maxPPP);
		}
		
		public int getZoom() {
			return _zoom;
		}
		
		public int getMax() {
			return _max;
		}
		
		public int getMin() {
			return _min;
		}
		
		public int compareTo(ProjectInfo pi2) {
			return Integer.valueOf(_zoom).compareTo(Integer.valueOf(pi2._zoom));
		}
	}

	private class ZoomData implements Comparable<ZoomData> {
		private int _zoom;
		private int _max;
		private final ConcurrentMap<Point, Integer> _pts = new ConcurrentHashMap<Point, Integer>(1024000);
		private final Map<TileAddress, Collection<Point>> _tiles = new HashMap<TileAddress, Collection<Point>>();
		
		ZoomData(int zoomLevel) {
			super();
			_zoom = zoomLevel;
		}
		
		public ConcurrentMap<Point, Integer> getPoints() {
			return _pts;
		}
		
		public Map<TileAddress, Collection<Point>> getTileMap() {
			return _tiles;
		}
		
		public int getMaxValue() {
			return _max;
		}
		
		/**
		 * Converts a set of points up one zoom level
		 * @param points
		 */
		public void convert(Map<Point, Integer> points) {
			for (Iterator<Map.Entry<Point, Integer>> i = points.entrySet().iterator(); i.hasNext();) {
				Map.Entry<Point, Integer> e = i.next();
				Point pt = e.getKey();
				Integer cnt = e.getValue();
				
				// Zoom up a level
				Point pt2 = new Point(pt.x >> 1, pt.y >> 1);
				Integer cnt2 = _pts.putIfAbsent(pt2, cnt);
				if (cnt2 != null) {
					int newCount = cnt2.intValue() + cnt.intValue();
					_pts.put(pt2, Integer.valueOf(newCount));
					_max = Math.max(_max, newCount);
				} else
					_max = Math.max(_max, cnt.intValue());
				
				// Save the address
				TileAddress addr = TileAddress.fromPixel(pt2.x, pt2.y, _zoom);
				Collection<Point> addrPoints = _tiles.get(addr);
				if (addrPoints == null) {
					addrPoints = new ArrayList<Point>();
					_tiles.put(addr, addrPoints);
				}
				
				addrPoints.add(pt2);
				i.remove();
			}
		}
		
		public int compareTo(ZoomData zd2) {
			return Integer.valueOf(_zoom).compareTo(Integer.valueOf(zd2._zoom));
		}
	}
	
	private class WriteWorker extends Thread {
		private TarOutputStream _out;
		private int _maxPPP;
		private int _minPPP;
		
		WriteWorker(int id, TarOutputStream out, int max, int min) {
			super("WriteWorker-" + id);
			setDaemon(true);
			_out = out;
			_maxPPP = max;
			_minPPP = min;
		}
		
		public void run() {
			while (!_imgWork.isEmpty() && !isInterrupted()) {
				Map.Entry<TileAddress, Collection<Point>> me = _imgWork.poll();
				if (me != null) {
					TileAddress addr = me.getKey();
					SingleTile st = new SingleTile(me.getKey());
					BufferedImage img = new BufferedImage(Tile.WIDTH, Tile.HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
					st.setImage(img);
					
					Collection<Point> points = me.getValue();
					for (Iterator<Point> pi = points.iterator(); pi.hasNext(); ) {
						Point p = pi.next();
						int cnt = Math.min(_maxPPP, _points.get(p).intValue());

						// Draw the pixel
						int tx = p.x - addr.getPixelX();
						int ty = p.y - addr.getPixelY();
						if (cnt >= _minPPP)
							img.setRGB(tx, ty, _colors[cnt]);
					}
					
					// Save the image
					_tileMap.remove(addr);
					TarEntry entry = new TarEntry(st.getName() + ".png");
					try {
						ByteArrayOutputStream buf = new ByteArrayOutputStream();
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
		private MercatorProjection _mp;

		ReadWorker(int id, String url, int zoom) throws SQLException {
			super("ReadWorker-" + id);
			setDaemon(true);
			_c = DriverManager.getConnection(url);
			_mp = new MercatorProjection(zoom);
		}

		public void run() {
			try {
				PreparedStatement ps = _c.prepareStatement("SELECT LAT, LNG FROM TRACKPOS WHERE (ID=?)");	
				//PreparedStatement ps = _c.prepareStatement("SELECT LAT, LNG FROM TRACKPOS WHERE (ID=?) AND "
					//	+ "((LAT < 36) AND (LAT >= 32)) AND ((LNG > -87) AND (LNG <= -82))");
				ps.setFetchSize(1600);
				while (!_IDwork.isEmpty() && !isInterrupted()) {
					Integer id = _IDwork.poll();
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
			_IDwork.add(new Integer(rs.getInt(1)));

		rs.close();
		s.close();
		c.close();
		
		// Get the first zoom level
		ProjectInfo pInf = _zooms.first();

		// Spawn the workers
		Collection<Thread> workers = new ArrayList<Thread>(16);
		for (int x = 1; x <= 14; x++) {
			ReadWorker worker = new ReadWorker(x, URL, pInf.getZoom() + 1);
			worker.start();
			workers.add(worker);
		}

		// Wait for the pool to finish
		ThreadUtils.waitOnPool(workers);
		assertFalse(_points.isEmpty());
		System.out.println("Loaded " + _points.size() + " points at Zoom " + (pInf.getZoom() + 1));
		
		// Go through each level
		for (Iterator<ProjectInfo> i = _zooms.iterator(); i.hasNext(); ) {
			ProjectInfo pi = i.next();
			
			// Convert to this zoom level
			ZoomData zd = new ZoomData(pi.getZoom());
			zd.convert(_points);
			Map<Point, Integer> pts = zd.getPoints();
			_points.clear();
			_points.putAll(pts);
			
			// Get colors
			float maxValue = Math.min(pi.getMax(), zd.getMaxValue());
			_colors = new int[Math.round(maxValue) + 1];
			for (int x = 0; x <= maxValue; x++) {
				float value = (x / maxValue) * 0.8f + 0.2f;
				Color cl = new Color(value, value, value);
				_colors[x] = cl.getRGB();
			}
			
			// Build the Tiles
			_tileMap.clear();
			_tileMap.putAll(zd.getTileMap());
			System.out.println("Loaded " + pts.size() + " points, " + _tileMap.size() + " tiles  at Zoom " + pi.getZoom());
			File tarGZ = new File(new File("c:\\temp"), pi.getZoom() + ".tar.gz");
			TarGzOutputStream tar = new TarGzOutputStream(new FileOutputStream(tarGZ));

			// Build the tiles
			_imgWork.addAll(_tileMap.entrySet());

			// Spawn the workers
			workers.clear();
			for (int x = 1; x <= 5; x++) {
				WriteWorker worker = new WriteWorker(x, tar, pi.getMax(), pi.getMin());
				worker.start();
				workers.add(worker);
			}
			
			// Wait for the pool to finish
			ThreadUtils.waitOnPool(workers);
			tar.close();
		}
	}
}