// Copyright 2007, 2009, 2010, 2017, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.io.*;
import java.util.*;

import org.apache.logging.log4j.*;

import org.gvagroup.ipc.IPCInfo;

/**
 * A utility class to handle deserializing IPC data.
 * @author Luke
 * @version 11.0
 * @since 1.0
 */

public class IPCUtils {
	
	private static final Logger log = LogManager.getLogger(IPCUtils.class); 

	// singleton
	private IPCUtils() {
		super();
	}
	
	/**
	 * Reserializes a shared object to allow local access if loaded by a different class loader.
	 * @param data the shared object
	 * @return a local class version of the shared object
	 */
	public static Serializable reserialize(Serializable data) {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(512); ObjectOutputStream oos = new ObjectOutputStream(bos)) {
			oos.writeObject(data);
			try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
				return (Serializable) in.readObject();
			}
		} catch (ClassNotFoundException cnfe) {
			log.error("Unknown class " + cnfe.getMessage());
		} catch (IOException ie) {
			log.error("I/O exception - " + ie.getMessage(), ie);
		}
		
		return null;
	}
	
	/**
	 * Deserializes shared data.
	 * @param data the object that can pass serialized data
	 * @return a Collection of beans
	 */
	public static <T extends Serializable> Collection<T> deserialize(IPCInfo<T> data) {
		return deserialize(data.getSerializedInfo());
	}

	/**
	 * Deserializes shared data.
	 * @param data the object that can pass serialized data
	 * @return a Collection of beans
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Serializable> Collection<T> deserialize(Collection<byte[]> data) {
		Collection<T> results = new ArrayList<T>(data.size());
		for (Iterator<byte[]> i = data.iterator(); i.hasNext(); ) {
			try (ByteArrayInputStream is = new ByteArrayInputStream(i.next())) {
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
		for (Serializable entry : data) {
			try (ByteArrayOutputStream bos = new ByteArrayOutputStream(512); ObjectOutputStream oos = new ObjectOutputStream(bos)) {
				oos.writeObject(entry);
				results.add(bos.toByteArray());
			} catch (IOException ie) {
				log.error(ie.getClass().getSimpleName() + " - " + ie.getMessage());
			}
		}
		
		return results;
	}
}