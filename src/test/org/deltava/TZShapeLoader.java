package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.nio.charset.StandardCharsets;

import org.nocrala.tools.gis.data.esri.shapefile.*;
import org.nocrala.tools.gis.data.esri.shapefile.shape.*;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolygonShape;

import com.hexiong.jdbf.DBFReader;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKTWriter;

import junit.framework.TestCase;

public class TZShapeLoader extends TestCase {
	
	private Connection _c;
	
	private static final String JDBC_URL ="jdbc:mysql://dev.gvagroup.org/geoip?useCompression=true";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Connect to the database
		Class.forName("com.mysql.cj.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "luke", "test");
		assertNotNull(_c);
	}

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		super.tearDown();
	}
	
	public void testLoadCounties() throws Exception {
		
		List<String> data = new ArrayList<String>();
		Map<String, LongAdder> count = new HashMap<String, LongAdder>();
		DBFReader dbr = new DBFReader("d:\\shp\\tz_world.dbf");
		while (dbr.hasNextRecord()) {
			Object o[] = dbr.nextRecord(StandardCharsets.UTF_8); String tz = String.valueOf(o[0]);
			LongAdder cnt = count.get(tz);
			if (cnt == null) {
				cnt = new LongAdder();
				count.put(tz, cnt);
			}
			
			cnt.increment();
			data.add(tz + "$" + cnt.intValue());
		}

		dbr.close();
		assertFalse(data.isEmpty());
		
		// Load the coordinates
		Map<String, Geometry> geo = new LinkedHashMap<String, Geometry>();
		try (InputStream is = new FileInputStream("d:\\shp\\tz_world.shp")) {
			ValidationPreferences prefs = new ValidationPreferences();
			prefs.setMaxNumberOfPointsPerShape(528000);
			ShapeFileReader r = new ShapeFileReader(is, prefs);

			AbstractShape s = null;
			Iterator<String> ii = data.iterator();
			Collection<Point> pts = new HashSet<Point>();
			GeometryFactory gf = new GeometryFactory();
			while ((s = r.next()) != null) {
				switch (s.getShapeType()) {
				case POLYGON:
					PolygonShape pg = (PolygonShape) s;
					String tzName = ii.next(); List<LinearRing> bnds = new ArrayList<LinearRing>();
					for (int i = 0; i < pg.getNumberOfParts(); i++) {
						PointData[] points = pg.getPointsOfPart(i);
						List<Coordinate> cts = new ArrayList<Coordinate>();
						for (PointData pd : points)
							cts.add(new Coordinate(pd.getY(), pd.getX()));
						
						cts.add(cts.get(0));
						if (cts.size() > 3) {
							LinearRing lr = gf.createLinearRing(cts.toArray(new Coordinate[0]));
							bnds.add(lr);
						}
					}

					if (bnds.size() == 1)
						geo.put(tzName, gf.createPolygon(bnds.get(0), null));
					else
						geo.put(tzName, gf.createPolygon(bnds.get(0), bnds.subList(1, bnds.size()).toArray(new LinearRing[0])));
					
					break;
					
				default:
					System.out.println("Read other shape - " + s.getShapeType().toString());
				}
				
				pts.clear();
			}
		}
		
		try (PreparedStatement ps = _c.prepareStatement("INSERT INTO TZ (name, id, lat, lng, ctr, data) VALUES (?, ?, ?, ?, ST_GeomFromText(?, ?), ST_GeomFromText(?, ?))")) {
			int cnt = 0;
			WKTWriter ww = new WKTWriter();
			for (Map.Entry<String, Geometry> me : geo.entrySet()) {
				Geometry g = me.getValue();
				
				String tz = me.getKey(); int pos = tz.indexOf('$');
				//if (me.getValue().getSize() < 3) continue;
				
				Point cp = g.getCentroid();
				ps.setString(1, tz.substring(0,  pos));
				ps.setInt(2, Integer.parseInt(tz.substring(pos + 1)));
				ps.setDouble(3, cp.getY());
				ps.setDouble(4, cp.getX());
				ps.setString(5, "POINT(" + cp.getX() + " " + cp.getY() + ")");
				ps.setInt(6,  3587);
				ps.setString(7, ww.write(g));
				ps.setInt(8,  3587);
				ps.addBatch();
				System.out.println("Wrote " + me.getKey()); cnt++;
				if ((cnt % 25) == 24) ps.executeBatch();
			}
			
			ps.executeBatch();
		}
	}
}