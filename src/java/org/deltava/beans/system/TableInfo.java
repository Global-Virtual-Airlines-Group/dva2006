// Copyright (c) 2005 Delta Virtual Airlines. All Rights Reserved.
package org.deltava.beans.system;

import java.io.Serializable;

/**
 * A system bean to store JDBC table data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TableInfo implements Serializable, Comparable {

    private String _tableName;
    private int _rows;
    private long _dataLength;
    private long _idxLength;
    
    /**
     * Creates a new TableInfo bean for the specified database table.
     * @param tableName the name of the table
     * @throws NullPointerException if tableName is null
     * @see TableInfo#getName() 
     */
    public TableInfo(String tableName) {
        super();
        _tableName = tableName.trim().toUpperCase();
    }
    
    /**
     * Returns the table name.
     * @return the table name
     */
    public String getName() {
        return _tableName;
    }

    /**
     * Returns the number of rows in the table.
     * @return the number of rows
     * @see TableInfo#setRows(int) 
     */
    public int getRows() {
        return _rows;
    }
    
    /**
     * Returns the average row size.
     * @return the average bytes per row
     */
    public int getAverageRowLength() {
        return (_rows == 0) ? 0 : (int) (_dataLength / _rows);
    }
    
    /**
     * Returns the size of the table.
     * @return the size of the table in bytes
     * @see TableInfo#setSize(long)
     */
    public long getSize() {
        return _dataLength;
    }
    
    /**
     * Returns the size of the table's indices.
     * @return the size of the indices in bytes
     * @see TableInfo#setIndexSize(long)
     */
    public long getIndexSize() {
        return _idxLength;
    }
    
    /**
     * Updates the number of rows in the table.
     * @param rows the number of rows
     * @throws IllegalArgumentException if rows is negative
     * @see TableInfo#getRows()
     */
    public void setRows(int rows) {
        if (rows < 0)
            throw new IllegalArgumentException("Rows cannot be negative");
        
        _rows = rows;
    }
    
    /**
     * Updates the size of the table.
     * @param tableSize the size of the table in bytes
     * @throws IllegalArgumentException if tableSize is negative
     * @see TableInfo#getSize()
     */
    public void setSize(long tableSize) {
        if (tableSize < 0)
            throw new IllegalArgumentException("Table Size cannot be negative");
      
        _dataLength = tableSize;
    }
    
    /**
     * Updates the size of the table's indices.
     * @param idxSize the size of the indices in bytes
     * @throws IllegalArgumentException if idxSize is negative
     * @see TableInfo#getIndexSize()
     */
    public void setIndexSize(long idxSize) {
        if (idxSize < 0)
            throw new IllegalArgumentException("Table Index Size cannot be negative");
        
        _idxLength = idxSize;
    }
    
    /**
     * Returns the table name's hash code.
     */
    public int hashCode() {
       return _tableName.hashCode();
    }
    
    /**
     * Compares two TableInfo objects by comparing the table names.
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object o2) {
    	TableInfo ti2 = (TableInfo) o2;
    	return _tableName.compareTo(ti2.getName());
    }
    
    /**
     * Overrides equality by using the compareTo method.
     */
    public boolean equals(Object o2) {
       return (o2 instanceof TableInfo) ? (compareTo(o2) == 0) : false;
    }
}