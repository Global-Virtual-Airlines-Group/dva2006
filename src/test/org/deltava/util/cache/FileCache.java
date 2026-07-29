package org.deltava.util.cache;

import java.io.*;
import java.util.*;

import org.apache.logging.log4j.*;

import org.deltava.util.JedisUtils;

public class FileCache<T extends Cacheable> extends AgingCache<T> {
	
	private static final Logger log = LogManager.getLogger(FileCache.class);
	
	private final File _f;

	public FileCache(int maxSize, File f) {
		super(maxSize);
		_f = f;
		load();
	}
	
	@Override
	protected void addEntry(T obj) {
		super.addEntry(obj);
		save();
	}
	
	@Override
	public void clear() {
		super.clear();
		_f.delete();
	}
	
	private void save() {
		Map<Object, CacheEntry<T>> data = new LinkedHashMap<Object, CacheEntry<T>>(_cache);
		try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(_f), 65536))) {
			dos.writeInt(data.size());
			for (CacheEntry<T> ce : data.values()) {
				byte[] k = JedisUtils.write(ce.getKey());
				byte[] v = JedisUtils.write(ce.get());
				dos.writeInt(k.length);
				dos.write(k);
				dos.writeInt(v.length);
				dos.write(v);
			}
		} catch (IOException ie) {
			log.atError().withThrowable(ie).log(ie.getMessage());
		}
	}
	
	private void load() {
		if (!_f.exists()) return;
		
		try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(_f), 65536))) {
			int size = dis.readInt();
			for (int x = 0; x < size; x++) {
				int kl = dis.readInt();
				byte[] kd = dis.readNBytes(kl);
				int vl = dis.readInt();
				byte[] vd = dis.readNBytes(vl);
				
				Object k = JedisUtils.read(kd);
				@SuppressWarnings("unchecked")
				T v = (T)JedisUtils.read(vd);
				_cache.put(k, new AgingCacheEntry<T>(v));
			}
		} catch (IOException ie) {
			log.atError().withThrowable(ie).log(ie.getMessage());
		}
	}
}