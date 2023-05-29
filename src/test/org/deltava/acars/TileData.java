package org.deltava.acars;

import java.io.*;

import org.apache.logging.log4j.*;

class TileData {
	
	private static final Logger log = LogManager.getLogger(TileData.class);
	private static File _saveRoot; 
	
	private RawTile _img;
	private File _file;
	
	public TileData() {
		this(new SmallRawTile());
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
			try (BufferedInputStream bi = new BufferedInputStream(new FileInputStream(_file), 65536)) {
				try (ObjectInput oi = new ObjectInputStream(bi)) {
					int tileType = oi.readInt();
					_img = (tileType == 0) ? new SmallRawTile() : new LargeRawTile();
					_img.readExternal(oi);
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
			int tileType = (_img instanceof SmallRawTile) ? 0 : 1;
			try (BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(_file), 65536)) {
				try (ObjectOutput oo = new ObjectOutputStream(bo)) {
					oo.writeInt(tileType);
					_img.writeExternal(oo);
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