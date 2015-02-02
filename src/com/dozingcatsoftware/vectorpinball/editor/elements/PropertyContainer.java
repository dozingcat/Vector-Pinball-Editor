package com.dozingcatsoftware.vectorpinball.editor.elements;


public interface PropertyContainer {

    boolean hasProperty(String propertyName);

    Object getProperty(String propertyName);

    void setProperty(String propertyName, Object value);

    void removeProperty(String propertyName);
}
