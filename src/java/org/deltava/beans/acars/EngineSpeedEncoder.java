// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.io.*;

import org.deltava.beans.Helper;

/**
 * A utility class to encode engine speeds into an array. 
 * @author Luke
 * @version 9.1
 * @since 9.1
 */

@Helper(ACARSRouteEntry.class)
public class EngineSpeedEncoder {

	// static class
	private EngineSpeedEncoder() {
		super();
	}
	
	/**
	 * Encodes N1/N2 values into an array.
	 * @param engineCount the number of engines
	 * @param nx the N1/N2 values
	 * @return the encoded data
	 */
	public static byte[] encode(int engineCount, double[] nx) {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream(64)) {
			try (DataOutputStream ds = new DataOutputStream(os)) {
				ds.write(engineCount);
				for (int x = 0; x < engineCount; x++)
					ds.writeDouble(nx[x]);
			}
			
			return os.toByteArray();
		} catch (IOException ie) {
			return null;
		}
	}
	
	/**
	 * Decodes encoded N1/N2 data.
	 * @param data the data array
	 * @return an array of N1/N2 values, or an empty array
	 */
	public static double[] decode(byte[] data) {
		if ((data == null) || (data.length < 5)) return new double[0];
		try (DataInputStream ds = new DataInputStream(new ByteArrayInputStream(data))) {
			int engCount = ds.read();
			double[] results = new double[engCount];
			for (int x = 0; x < engCount; x++)
				results[x] = ds.readDouble();

			return results;
		} catch (IOException ie) {
			return new double[0];
		}
	}
}