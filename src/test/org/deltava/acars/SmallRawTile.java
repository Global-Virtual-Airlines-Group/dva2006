package org.deltava.acars;

import java.io.*;

public class SmallRawTile extends RawTile {

	private final byte[][] _data = new byte[256][256];
	
	@Override
	public synchronized void increment(int x, int y) {
		byte cnt = _data[x][y];
		if (cnt != -1)
			_data[x][y] = ++cnt;
	}
	
	@Override
	public synchronized void set(int x, int y, int cnt) {
		_data[x][y] = (byte) Math.min(255, cnt);
	}
	
	@Override
	public int getCount(int x, int y) {
		int b = _data[x][y];
		return (short) ((b > -1) ? b : (b+256));
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		for (int x = 0; x < 256; x++) {
			for (int y = 0; y < 256; y++)
				out.writeByte(_data[x][y]);
		}
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException {
		for (int x = 0; x < 256; x++) {
			for (int y = 0; y < 256; y++)
				_data[x][y] = in.readByte();
		}
	}
}