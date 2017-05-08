package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

import org.nocrala.tools.gis.data.esri.shapefile.*;
import org.nocrala.tools.gis.data.esri.shapefile.shape.*;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolygonShape;

import com.hexiong.jdbf.DBFReader;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKTWriter;

import junit.framework.TestCase;

public class CountryShapeLoader extends TestCase {
	
	private Connection _c;
	
	private static final String JDBC_URL ="jdbc:mysql://sirius.sce.net/common?useCompression=true&useSSL=false";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Connect to the database
		Class.forName("com.mysql.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "luke", "test");
		assertNotNull(_c);
	}

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		super.tearDown();
	}
	
	public void testLoadCounties() throws Exception {
		
		Collection<String> data = new ArrayList<String>();
		DBFReader dbr = new DBFReader("d:\\shp\\TM_WORLD_BORDERS-0.3.dbf");
		while (dbr.hasNextRecord()) {
			Object o[] = dbr.nextRecord(StandardCharsets.UTF_8); 
			String iso = String.valueOf(o[1]);
			
			if (data.contains(iso))
				System.out.println("Duplicate shape for " + iso);
			else
				data.add(iso);
		}

		dbr.close();
		assertFalse(data.isEmpty());
		
		// Load the coordinates
		Map<String, Geometry> geo = new LinkedHashMap<String, Geometry>();
		try (InputStream is = new FileInputStream("d:\\shp\\TM_WORLD_BORDERS-0.3.shp")) {
			ValidationPreferences prefs = new ValidationPreferences();
			prefs.setMaxNumberOfPointsPerShape(528000);
			ShapeFileReader r = new ShapeFileReader(is, prefs);

			AbstractShape s = null;
			Iterator<String> ii = data.iterator();
			Collection<Point> pts = new HashSet<Point>();
			GeometryFactory gf = new GeometryFactory();
			while (((s = r.next()) != null) && ii.hasNext()) {
				switch (s.getShapeType()) {
				case POLYGON:
					PolygonShape pg = (PolygonShape) s;
					String iso  = ii.next();
					System.out.println("Processing " + iso + " " + pg.getNumberOfParts() + " parts");
					List<LinearRing> bnds = new ArrayList<LinearRing>();
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

					Polygon[] polys = new Polygon[bnds.size()];
					for (int x = 0; x < bnds.size(); x++)
						polys[x] = gf.createPolygon(bnds.get(x), null);

					geo.put(iso, gf.createMultiPolygon(polys));
					break;
					
				default:
					System.out.println("Read other shape - " + s.getShapeType().toString());
				}
				
				pts.clear();
			}
		}
		
		try (PreparedStatement ps = _c.prepareStatement("UPDATE COUNTRY SET DATA=ST_GeomFromText(?, ?) WHERE (CODE=?)")) {
			int cnt = 0;
			WKTWriter ww = new WKTWriter();
			for (Map.Entry<String, Geometry> me : geo.entrySet()) {
				Geometry g = me.getValue();
				String iso = me.getKey();
				ps.setString(1, ww.write(g));
				ps.setInt(2,  3587);
				ps.setString(3, iso);
				ps.addBatch();
				System.out.println("Wrote " + me.getKey()); cnt++;
				if ((cnt % 25) == 24) ps.executeBatch();
			}
			
			ps.executeBatch();
		}
	}
}