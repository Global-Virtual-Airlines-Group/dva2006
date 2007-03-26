package org.deltava.taskman;

import junit.framework.*;

import org.apache.log4j.PropertyConfigurator;

import org.hansel.CoverageDecorator;

public class TestTaskInfo extends TestCase {
	
	private TaskInfo _info;
	private MockTask _task;
	
	private class MockTask extends Task {
		MockTask(String name) {
			super(name, MockTask.class);
		}

		protected void execute(TaskContext ctx) {
			// noop
		}
	}
	
    public static Test suite() {
        return new CoverageDecorator(TestTaskInfo.class, new Class[] { TaskInfo.class } );
    }
    
    protected void setUp() throws Exception {
    	super.setUp();
    	PropertyConfigurator.configure("data/log4j.test.properties");
    	_task = new MockTask("Test Task");
    }

	protected void tearDown() throws Exception {
		_task = null;
		_info = null;
		super.tearDown();
	}

	public void testEmptyTask() {
		_info = new TaskInfo(_task);
		assertEquals(_task.getID(), _info.getID());
		assertEquals(_task.getName(), _info.getName());
		assertEquals(_task.getClass().getName(), _info.getClassName());
		assertEquals(_task.getStartTime(), _info.getLastStartTime());
		assertEquals(_task.getLastRunTime(), _info.getLastRunTime());
		assertEquals(_task.getEnabled(), _info.getEnabled());
		assertEquals(_task.getRunCount(), _info.getRunCount());
	}
	
	public void testPopulatedTask() {
		_task.setEnabled(true);
		_task.setID("ID");
		_task.run();
		assertTrue(_task.getRunCount() > 0);
		
		_info = new TaskInfo(_task);
		assertEquals(_task.getID(), _info.getID());
		assertEquals(_task.getName(), _info.getName());
		assertEquals(_task.getClass().getName(), _info.getClassName());
		assertEquals(_task.getStartTime(), _info.getLastStartTime());
		assertEquals(_task.getLastRunTime(), _info.getLastRunTime());
		assertEquals(_task.getEnabled(), _info.getEnabled());
		assertEquals(_task.getRunCount(), _info.getRunCount());
	}
	
	public void testCompareTo() throws InterruptedException {
		_task.setEnabled(true);
		_task.setID("ID");
		_task.run();
		assertTrue(_task.getRunCount() > 0);

		_info = new TaskInfo(_task);
		Thread.sleep(50);
		_task.run();
		TaskInfo info2 = new TaskInfo(_task);
		assertNotNull(info2);
		TaskInfo info3 = new TaskInfo(_task);
		assertNotNull(info3);
		
		assertTrue(info2.getRunCount() > _info.getRunCount());
		assertTrue(_info.compareTo(info2) < 0);
		assertTrue(info2.compareTo(_info) > 0);
		assertEquals(info2.getLastStartTime(), info3.getLastStartTime());
		assertEquals(0, info2.compareTo(info3));
	}
}