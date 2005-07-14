package org.deltava.taglib.view;

import org.deltava.util.system.SystemData;

/**
 * A JSP tag to support database view tables.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TableTag extends org.deltava.taglib.html.TableTag {

    private String _viewCommandName;
    private int _viewSize;
    
    public TableTag() {
        super();
        _viewSize = SystemData.getInt("html.table.viewSize");
    }

    public void setCmd(String name) {
        _viewCommandName = name.toLowerCase(); 
    }
    
    public void setSize(String rawValue) {
        try {
            int tmpValue = Integer.parseInt(rawValue);
            if (tmpValue > 0)
                _viewSize = tmpValue;
        } catch (NumberFormatException nfe) { }
    }
    
    String getCmd() {
        return _viewCommandName;
    }
    
    int size() {
        return _viewSize;
    }
}