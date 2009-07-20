package org.deltava.beans;

import java.lang.reflect.*;
import junit.framework.TestCase;

/**
 * This class lets you test property getter/setters; just pass the bean in using setBean() and
 * then call checkProperty() for each given property. If the property takes a primitive number
 * pass in a wrapper object like Integer or Double.
 */

public abstract class AbstractBeanTestCase extends TestCase {
    
    private Object _bean;
    
    protected void setBean(Object bean) {
        assertNotNull(bean);
        _bean = bean;
    }
    
    protected void toDo() {
        fail("Test Not Implemented");
    }
    
    // Get the name of a get/set method from a property name
    private String getMethod(String propertyName, boolean isSet) {
        StringBuffer buf = new StringBuffer(isSet ? "set" : "get");
        buf.append(Character.toUpperCase(propertyName.charAt(0)));
        buf.append(propertyName.substring(1));
        return buf.toString();
    }
    
    // If the Object is a numeric primitive wrapper, return the result of the static TYPE field
    private Class<?> getNumericPrimitiveWrapper(Object value) {
        try {
            Field f = value.getClass().getField("TYPE");
            return (Class<?>) f.get(null);
        } catch (Exception e) {
            return null;
        }
    }
    
    private Method getSetter(String pName, Object value) throws NoSuchMethodException {
        Class<?> primitiveClass = getNumericPrimitiveWrapper(value);
        if (primitiveClass != null) {
            return _bean.getClass().getMethod(getMethod(pName, true), new Class[] { primitiveClass });
        } else if (value == null) {
            return _bean.getClass().getMethod(getMethod(pName, true), new Class[] { String.class });
        } else {
            return _bean.getClass().getMethod(getMethod(pName, true), new Class[] { value.getClass() });
        }
    }

    protected void checkProperty(String pName, Object testValue) {
        
        // Validate we can get/set the property
        Method setProperty = null;
        Method getProperty = null;
        try {
            setProperty = getSetter(pName, testValue);
            getProperty = _bean.getClass().getMethod(getMethod(pName, false), (Class []) null);
        } catch (NoSuchMethodException nsme) {
            fail("Cannot find get/set methods for " + pName + " - " + nsme.getMessage());
        }
        
        // Set the property
        try {
            setProperty.invoke(_bean, new Object[] { testValue });
        } catch (InvocationTargetException ite) {
            Throwable te = ite.getTargetException();
            fail("Cannot set property " + pName + " - " + te.getClass().getName());
        } catch (Exception e) {
            fail("Cannot set property " + pName + " - " + e.getClass().getName());
        }
        
        // Get the property and compare the results
        try {
            Object retValue = getProperty.invoke(_bean, (Object []) null);
            assertEquals(testValue, retValue);
        } catch (Exception e) {
            fail("Cannot get property " + pName + " - " + e.getClass().getName());
        }
    }
    
    protected void validateInput(String pName, Object testValue, Class<?>[] exClasses) {

        Method setProperty = null;
        try {
            setProperty = getSetter(pName, testValue);
        } catch (NoSuchMethodException nsme) {
            fail("Cannot find set method for " + pName + " - " + nsme.getMessage());
        }
        
        try {
            setProperty.invoke(_bean, new Object[] { testValue });
            if (exClasses != null)
                fail("Exception exepected on " + setProperty.getName());
        } catch (InvocationTargetException ite) {
            Throwable te = ite.getTargetException();
            if (exClasses == null)
                fail("Unexpected exception - " + te.getClass().getName());

            // Check if the exception is one we expected
            for (int x = 0; x < exClasses.length; x++) {
                if (exClasses[x].equals(te.getClass()))
                    return;
            }
            
            fail("Unexpected exception - " + te.getClass().getName());
        } catch (Exception e) {
            if (exClasses == null)
                fail("Unexpected exception - " + e.getClass().getName());
        }
    }
    
    protected void validateInput(String pName, Object testValue, Class<?> exClass) {
        validateInput(pName, testValue, new Class[] { exClass } );
    }
}