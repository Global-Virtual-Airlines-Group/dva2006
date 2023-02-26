package org.deltava;

import java.util.Comparator;

import org.deltava.beans.navdata.Gate;

class GeoGateComparator implements Comparator<Gate> {
	
	private final Gate _g;
	
	GeoGateComparator(Gate g) {
		_g = g;
	}
	
	@Override
	public int compare(Gate g1, Gate g2) {
		int d1 = _g.distanceFeet(g1);
		int d2 = _g.distanceFeet(g2);
		int tmpResult = Integer.compare(d1, d2);
		return (tmpResult != 0) ? tmpResult : g1.getName().compareTo(g2.getName());
	}
}