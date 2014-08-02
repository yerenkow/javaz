package org.javaz.jdbc.base;

import java.io.Serializable;

/**
 * This interface declares that this entity can be converted to/from Map
 */
public interface MapConvertibleI extends Serializable, Cloneable {
    Comparable getPrimaryKey();
    void setGeneratedPrimaryKey(Comparable key);
}
