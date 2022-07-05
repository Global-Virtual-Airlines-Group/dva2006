// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.jmx;

import org.gvagroup.tomcat.SharedTask;

/**
 * A shared task to auotmatically refresh JMX statistics. 
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public class JMXRefreshTask implements SharedTask {
	
	private final JMXRefresh _jmx;
	private final int _refreshInterval;
	private boolean _isStopped;
	
	/**
	 * Initializes the Task.
	 * @param bean the JMXRefresh bean
	 * @param interval the execution interval in milliseconds
	 */
	public JMXRefreshTask(JMXRefresh bean, int interval) {
		super();
		_jmx = bean;
		_refreshInterval = Math.max(1000, interval);
	}
	
	@Override
	public int getInterval() {
		return _refreshInterval;
	}
	
	@Override
	public boolean isStopped() {
		return _isStopped;
	}
	
	@Override
	public void stop() {
		_isStopped = true;
	}

	@Override
	public void execute() {
		_jmx.update();
	}
	
	@Override
	public String toString() {
		return _jmx.toString();
	}
}