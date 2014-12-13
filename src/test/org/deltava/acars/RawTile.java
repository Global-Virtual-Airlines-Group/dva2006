package org.deltava.acars;

public abstract class RawTile {

	public abstract void increment(int x, int y);
	
	public abstract void set(int x, int y, int cnt);
	
	public abstract int getCount(int x, int y);

	public abstract void writeExternal(java.io.ObjectOutput out) throws java.io.IOException;
	
	public abstract void readExternal(java.io.ObjectInput in) throws java.io.IOException;
	
	public static RawTile getTile(int max) {
		return (max > 255) ? new LargeRawTile() : new SmallRawTile();
	}
}