package org.deltava.acars;

class ProjectInfo {
	private final int _zoom;
	private final int _max;
	private final int _min;
	
	ProjectInfo(int zoom, int maxPPP, int minPPP) {
		super();
		_zoom = Math.max(1, zoom);
		_min = Math.max(1, minPPP);
		_max = Math.max(_min, maxPPP);
	}
	
	public int getZoom() {
		return _zoom;
	}
	
	public int getMax() {
		return _max;
	}
	
	public int getMin() {
		return _min;
	}
}