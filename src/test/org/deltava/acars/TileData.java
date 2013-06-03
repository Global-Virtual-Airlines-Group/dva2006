package org.deltava.acars;

import java.io.*;
import java.util.zip.*;

import org.apache.log4j.Logger;

class TileData {
	
	private static final Logger log = Logger.getLogger(TileData.class);
	private static File _saveRoot; 
	
	private RawTile _img;
	private File _file;
	
	public TileData() {
		this(new RawTile());
	}
	
	public TileData(RawTile img) {
		super();
		_img = img;
	}
	
	public RawTile getImage() {
		return _img;
	}
	
	public boolean isSwapped() {
		return (_img == null);
	}
	
	public void load() {
		if (_file == null) throw new IllegalStateException("Not Saved");
		try {
			_img = new RawTile();
			try (BufferedInputStream bi = new BufferedInputStream(new FileInputStream(_file))) {
				try (GZIPInputStream gi = new GZIPInputStream(bi)) {
					try (ObjectInput oi = new ObjectInputStream(gi)) {
						_img.readExternal(oi);
					}
				}
			}
			
			_file.delete();
			_file = null;
		} catch (IOException ie) {
			log.error(ie.getMessage());
		}
	}
	
	public void save() {
		if (_img == null) throw new IllegalStateException("Already Saved");
		
		File intPath = new File(_saveRoot, String.valueOf(_img.hashCode() & 0xF));
		intPath.mkdir();
		try {
			_file = File.createTempFile("img", ".bmp", intPath);
			try (BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(_file))) {
				try (GZIPOutputStream go = new GZIPOutputStream(bo)) {
					try (ObjectOutput oo = new ObjectOutputStream(go)) {
						_img.writeExternal(oo);
					}
				}
			}
			
			_img = null;
		} catch (IOException ie) {
			log.error(ie.getMessage());
		}
	}
	
	public void flush() {
		_img = null;
		if ((_file != null) && _file.exists()) {
			_file.delete();
			_file = null;
		}
	}
	
	public static synchronized void init(String savePath) {
		_saveRoot = new File(savePath);
		if (!_saveRoot.isDirectory())
			throw new IllegalArgumentException(savePath + " is not a directory");
		
		for (File c : _saveRoot.listFiles()) clearDirectory(c);
	}
	
	private static void clearDirectory(File d) {
		if ((d == null) || !d.isDirectory() || (d.getName().startsWith("."))) return;
		File[] children = d.listFiles();
		for (File c : children) {
			if (c.isDirectory()) clearDirectory(c);
			else if (c.isFile()) c.delete();
		}
		
		d.delete();
	}
}