// Copyright 2005, 2009, 2016, 2023, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

/**
 * A system bean to store JDBC table data.
 * @author Luke
 * @version 12.5
 * @since 1.0
 */

public class TableInfo implements java.io.Serializable, Comparable<TableInfo> {

	/**
	 * InnoDB table compression types.
	 */
	public enum TableCompression {
		NONE, LZ4, ZLIB
	}

    private final String _tableName;
    private long _rows;
    private long _dataLength;
    private long _idxLength;
    private TableCompression _cmp;
    private long _diskSize;
    
    /**
     * Creates a new TableInfo bean for the specified database table.
     * @param tableName the name of the table
     * @throws NullPointerException if tableName is null
     * @see TableInfo#getName() 
     */
    public TableInfo(String tableName) {
        super();
        _tableName = tableName.toUpperCase();
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
     * Returns the on-disk size of the table.
     * @return the size of the table in bytes
     * @see TableInfo#setDiskSize(long)
     */
    public long getDiskSize() {
    	return _diskSize;
    }
    
    /**)
     * Returns the InnoDB table compression for this table.
     * @return a TableCompression enumeration value
     * @see TableInfo#setCompression(TableCompression)
     */
    public TableCompression getCompression() {
    	return _cmp;
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
     * Updates the on-disk size of the table.
     * @param size the size of the table in bytes
     * @see TableInfo#getDiskSize()
     */
    public void setDiskSize(long size) {
    	_diskSize = size;
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
     * Updates the InnoDB table compression used on this table.
     * @param cmp a TableCompression
     * @see TableInfo#getCompression()
     */
    public void setCompression(TableCompression cmp) {
    	_cmp = cmp;
    }
    
    @Override
    public int hashCode() {
       return _tableName.hashCode();
    }
    
    @Override
	public String toString() {
    	return _tableName;
    }
    
    @Override
    public int compareTo(TableInfo ti2) {
    	return _tableName.compareTo(ti2._tableName);
    }
    
    @Override
    public boolean equals(Object o2) {
       return (o2 instanceof TableInfo ti2) ? (compareTo(ti2) == 0) : false;
    }
}