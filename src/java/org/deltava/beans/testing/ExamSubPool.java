// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import org.deltava.beans.*;
import org.deltava.util.cache.Cacheable;

/**
 * A bean to store Examination question sub-pool information.
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public class ExamSubPool extends DatabaseBean implements Cacheable, ComboAlias {
	
	private String _examName;
	private String _name;
	
	private int _size;
	private int _poolSize;

	/**
	 * Initializes the bean.
	 * @param eName the Examination name
	 * @param name the pool name
	 */
	public ExamSubPool(String eName, String name) {
		super();
		setExamName(eName);
		setName(name);
	}
	
	public String getComboName() {
		return toString();
	}
	
	public String getComboAlias() {
		return _examName + "-" + String.valueOf(getID()); 
	}
	
	/**
	 * Returns the examination name for this pool.
	 * @return the examination name
	 * @see ExamSubPool#setExamName(String)
	 */
	public String getExamName() {
		return _examName;
	}
	
	/**
	 * Returns the sub-pool name.
	 * @return the name
	 * @see ExamSubPool#setName(String)
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the number of questions from this pool that should be placed into the exam.
	 * @return the number of questions
	 * @see ExamSubPool#setSize(int)
	 */
	public int getSize() {
		return _size;
	}
	
	/**
	 * Returns the number of questions in the sub-pool. This may not be populated.
	 * @return the number of questions
	 * @see ExamSubPool#setPoolSize(int)
	 */
	public int getPoolSize() {
		return _poolSize;
	}
	
	/**
	 * Updates the examination name.
	 * @param eName the examination name
	 * @throws NullPointerException if eName is null
	 * @see ExamSubPool#getExamName()
	 */
	public void setExamName(String eName) {
		_examName = eName.trim();
	}
	
	/**
	 * Updates the sub-pool name.
	 * @param name the name
	 * @throws NullPointerException if name is null
	 * @see ExamSubPool#getName()
	 */
	public void setName(String name) {
		_name = name.trim().replace("-", "");
	}
	
	/**
	 * Updates the number of questions from this pool that should be placed into the exam.
	 * @param size the number of questions
	 * @see ExamSubPool#getSize()
	 */
	public void setSize(int size) {
		_size = Math.max(1, size);
	}
	
	/**
	 * Updates the number of questions in this sub-pool.
	 * @param size the number of questions
	 * @see ExamSubPool#getPoolSize()
	 */
	public void setPoolSize(int size) {
		_poolSize = Math.max(0, size);
	}
	
	public Object cacheKey() {
		return toString();
	}
	
	public int hashCode() {
		return toString().hashCode();
	}
	
	public String toString() {
		return _examName + "-" + _name;
	}

	/**
	 * Compares two sub-pools by comparing their exam and pool names.
	 */
	public int compareTo(Object o2) {
		if (o2 instanceof ExamSubPool) {
			ExamSubPool sp2 = (ExamSubPool) o2;
			int tmpResult = _examName.compareTo(sp2._examName);
			return (tmpResult == 0) ? _name.compareTo(sp2._name) : tmpResult;
		}
		
		return super.compareTo(o2);
	}
}