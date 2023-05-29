package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.logging.log4j.*;

import org.json.*;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKTWriter;

import junit.framework.TestCase;

public class TZGeoJSONLoader extends TestCase {
	
	private Logger log; 
	
	private static final int WGS84_SRID = 4326;
	
	private static final GeometryFactory GF = new GeometryFactory();
	
	private static final String JDBC_URL ="jdbc:mysql://sirius.sce.net/geoip?useCompression=true&useSSL=false";
	private static final String JDBC_USER = "luke";
	private static final String JDBC_PWD = "test";
	private static final String JSON_ROOT ="combined";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
		log = LogManager.getLogger(TZGeoJSONLoader.class);
		
		// Connect to the database
		Class<?> c = Class.forName("com.mysql.cj.jdbc.Driver");
		assertNotNull(c);
		
	}

	public void testLoadZones() throws Exception {
		
		File f = new File(String.format("E:\\Temp\\tz\\%s.json", JSON_ROOT));
		assertTrue(f.exists());
		
		// Load the JSON
		JSONObject jo = null;
		try (InputStream is = new BufferedInputStream(new FileInputStream(f), 262144)) {
			jo = new JSONObject(new JSONTokener(is));
			assertNotNull(jo);
		}
		
		// Load the coordinates
		assertTrue(jo.has("features"));
		Map<String, MultiPolygon> geo = new LinkedHashMap<String, MultiPolygon>();
		JSONArray fa = jo.getJSONArray("features");
		for (int x = 0; x < fa.length(); x++) {
			JSONObject fo = fa.getJSONObject(x);
			assertTrue(fo.has("properties"));
			assertTrue(fo.has("geometry"));
			
			String id = fo.getJSONObject("properties").getString("tzid");
			JSONObject go = fo.getJSONObject("geometry");
			String gt = go.getString("type");
			JSONArray ca = go.getJSONArray("coordinates");
			assertNotNull(id);
			assertNotNull(gt);
			boolean isMulti = "MultiPolygon".equals(gt);
			assertTrue("Polygon".equals(gt) || isMulti);
			
			if (isMulti) {
				MultiPolygon mp = loadMulti(ca); int geoCount = 0;
				for (int p = 0; p < mp.getNumGeometries(); p++) {
					Geometry g = mp.getGeometryN(p);
					assertTrue(g instanceof Polygon);
					geoCount += g.getNumGeometries();
					
					// Check overlap
					for (int pp = 0; pp < mp.getNumGeometries(); pp++) {
						Geometry g2 = mp.getGeometryN(pp);
						if (pp != p)
							assertFalse(g2.overlaps(g));
					}
				}
				
				log.info(String.format("Loaded %s, shape = %s, has %d rings and %d sub-rings", id, gt, Integer.valueOf(mp.getNumGeometries()), Integer.valueOf(geoCount)));
				geo.put(id, mp);				
			} else {
				Polygon p = loadPolygon(ca);
				log.info(String.format("Loaded %s, shape = %s, has %d rings", id, gt, Integer.valueOf(p.getNumGeometries())));
				MultiPolygon mp = GF.createMultiPolygon(new Polygon[] { p });
				geo.put(id, mp);
			}
		}
		
		try (Connection c = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PWD)) {
			c.setAutoCommit(false);
			try (PreparedStatement ps = c.prepareStatement("INSERT INTO TZ (NAME, DATA) VALUES (?, ST_GeomFromText(?,?))")) {
				WKTWriter ww = new WKTWriter();
				for (Map.Entry<String, MultiPolygon> me : geo.entrySet()) {
					Geometry g = me.getValue();
					String wkt = ww.write(g);
					ps.setString(1, me.getKey());
					ps.setString(2, wkt);
					ps.setInt(3, WGS84_SRID);
					ps.executeUpdate();
					log.info("Wrote " + me.getKey());
				}
			}
		
			c.commit();
		}
	}
	
	private static MultiPolygon loadMulti(JSONArray ca) {
		List<Polygon> pgs = new ArrayList<Polygon>();
		
		for (int x = 0; x < ca.length(); x++) {
			JSONArray pa = ca.getJSONArray(x);
			Polygon p = loadPolygon(pa);
			pgs.add(p);
		}
		
		return GF.createMultiPolygon(pgs.toArray(new Polygon[0]));
	}
	
	private static Polygon loadPolygon(JSONArray ca) {
		
		// Add outer ring
		JSONArray oa = ca.getJSONArray(0);
		LinearRing or = GF.createLinearRing(loadLine(oa));
		
		// Get inner ring(s) for holes, if present
		List<LinearRing> rngs = new ArrayList<LinearRing>();
		for (int x = 1; x < ca.length(); x++) {
			JSONArray ha = ca.getJSONArray(x);
			LinearRing lr = GF.createLinearRing(loadLine(ha));
			rngs.add(lr);
		}
		
		// Convert the rings into a Polygon
		return GF.createPolygon(or, rngs.toArray(new LinearRing[0]));
	}
	
	private static Coordinate[] loadLine(JSONArray la) {
		List<Coordinate> pts = new ArrayList<Coordinate>();
		for (int x = 0; x < la.length(); x++) {
			JSONArray pca = la.getJSONArray(x);
			Coordinate pt = new Coordinate(pca.getDouble(1), pca.getDouble(0)); // swap since we do lat/lon
			pts.add(pt);
		}
		
		return pts.toArray(new Coordinate[0]);
	}
}