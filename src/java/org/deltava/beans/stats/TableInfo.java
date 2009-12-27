// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

/**
 * A system bean to store JDBC table data.
 * @author Luke
 * @version 2.7
 * @since 1.0
 */

public class TableInfo implements java.io.Serializable, Comparable<TableInfo> {

    private String _tableName;
    private long _rows;
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
     * @see TableInfo#setRows(long) 
     */
    public long getRows() {
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
     * @see TableInfo#getRows()
     */
    public void setRows(long rows) {
        _rows = Math.max(0, rows);
    }
    
    /**
     * Updates the size of the table.
     * @param tableSize the size of the table in bytes
     * @see TableInfo#getSize()
     */
    public void setSize(long tableSize) {
        _dataLength = Math.max(0, tableSize);
    }
    
    /**
     * Updates the size of the table's indices.
     * @param idxSize the size of the indices in bytes
     * @see TableInfo#getIndexSize()
     */
    public void setIndexSize(long idxSize) {
        _idxLength = Math.max(0, idxSize);
    }
    
    /**
     * Returns the table name's hash code.
     */
    public int hashCode() {
       return _tableName.hashCode();
    }
    
    /**
     * Compares two TableInfo objects by comparing the table names.
     */
    public int compareTo(TableInfo ti2) {
    	return _tableName.compareTo(ti2._tableName);
    }
    
    /**
     * Overrides equality by using the compareTo method.
     */
    public boolean equals(Object o2) {
       return (o2 instanceof TableInfo) ? (compareTo((TableInfo) o2) == 0) : false;
    }
}