package org.deltava.acars;

import java.io.*;

public class LargeRawTile extends RawTile {

	private final short[][] _data = new short[256][256];
	
	@Override
	public synchronized void increment(int x, int y) {
		short cnt = _data[x][y];
		_data[x][y] = ++cnt;
	}
	
	@Override
	public synchronized void set(int x, int y, int cnt) {
		_data[x][y] = (short) Math.max(0, cnt);
	}
	
	@Override
	public int getCount(int x, int y) {
		return _data[x][y];
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		for (int x = 0; x < 256; x++) {
			for (int y = 0; y < 256; y++)
				out.writeShort(_data[x][y]);
		}
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException {
		for (int x = 0; x < 256; x++) {
			for (int y = 0; y < 256; y++)
				_data[x][y] = in.readShort();
		}
	}
}