package org.javaz.xml;

/**
 * This clas can be used as substitution of XpathSaxHandler.RESULTS.
 *
 */
public interface HashObjectAccepter {
    public void acceptObject(Object value);
}
