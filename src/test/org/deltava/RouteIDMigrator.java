package org.deltava;

import java.sql.*;
import java.util.*;

public class RouteIDMigrator {

	public static void main(String[] args) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection c = DriverManager.getConnection("jdbc:mysql://127.0.0.1/events", "luke", "14072");
			
			// Get max route_ID
			Map<Integer, Integer> maxIDs = new HashMap<Integer, Integer>();
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery("SELECT ID, MAX(ROUTE_ID) FROM EVENT_AIRPORTS GROUP BY ID");
			while (rs.next())
				maxIDs.put(Integer.valueOf(rs.getInt(1)), Integer.valueOf(rs.getInt(2)));
			// Clean up
			rs.close();
			s.close();
			// Build the update statement
			PreparedStatement ps = c.prepareStatement("UPDATE EVENT_AIRPORTS SET ROUTE_ID=? WHERE (ID=?) AND "
					+ "(ROUTE_ID=0) AND (AIRPORT_D=?) AND (AIRPORT_A=?)");

			// Create the statement
			s = c.createStatement();
			rs = s.executeQuery("SELECT ID, AIRPORT_D, AIRPORT_A FROM EVENT_AIRPORTS WHERE (ROUTE_ID=0)"
					+ "ORDER BY ID, AIRPORT_D, AIRPORT_A");
			while (rs.next()) {
				Integer eventID = Integer.valueOf(rs.getInt(1));
				int maxID = maxIDs.get(eventID).intValue() + 1;
				maxIDs.put(eventID, Integer.valueOf(maxID));
				System.out.println("Set Route #" + maxID + " for Event #" + eventID.toString());

				// Update the data
				ps.setInt(1, maxID);
				ps.setInt(2, eventID.intValue());
				ps.setString(3, rs.getString(2));
				ps.setString(4, rs.getString(3));
				ps.executeUpdate();
			}

			// Clean up
			rs.close();
			s.close();
			ps.close();
			c.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}