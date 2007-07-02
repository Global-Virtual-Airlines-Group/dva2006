// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.gvagroup.ipc.IPCInfo;

/**
 * A utility class to handle deserializing IPC data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class IPCUtils {
	
	private static final Logger log = Logger.getLogger(IPCUtils.class); 

	// singleton
	private IPCUtils() {
	}
	
	/**
	 * Deserializes
	 * @param data the object that can pass serialized data
	 * @return a Collection of beans
	 */
	public static <T extends Serializable> Collection<T> deserialize(IPCInfo<T> data) {
		return deserialize(data.getSerializedInfo());
	}

	/**
	 * Deserializes
	 * @param data the object that can pass serialized data
	 * @return a Collection of beans
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Serializable> Collection<T> deserialize(Collection<byte[]> data) {
		Collection<T> results = new ArrayList<T>(data.size());
		for (Iterator<byte[]> i = data.iterator(); i.hasNext(); ) {
			ByteArrayInputStream is = new ByteArrayInputStream(i.next());
			try {
				ObjectInputStream ois = new ObjectInputStream(is);
				T entry = (T) ois.readObject();
				results.add(entry);
			} catch (IOException ie) {
				log.error(ie.getClass().getSimpleName() + " - " + ie.getMessage(), ie);
			} catch (ClassNotFoundException cnfe) {
				log.error("Cannot find class " + cnfe.getMessage());
			}
		}
		
		return results;
	}
	
	/**
	 * Serializes IPC data.
	 * @param data a Collection of data elements
	 * @return a Collection of byte arrays
	 */
	public static Collection<byte[]> serialize(Collection<? extends Serializable> data) {
		Collection<byte[]> results = new ArrayList<byte[]>();
		for (Iterator<? extends Serializable> i = data.iterator(); i.hasNext(); ) {
			Serializable entry = i.next();
			ByteArrayOutputStream bos = new ByteArrayOutputStream(512);
			try {
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				oos.writeObject(entry);
				results.add(bos.toByteArray());
			} catch (IOException ie) {
				log.error(ie.getClass().getSimpleName() + " - " + ie.getMessage(), ie);
			}
		}
		
		return results;
	}
}