package com.dozingcatsoftware.vectorpinball.editor.inspector;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableField;
import com.dozingcatsoftware.vectorpinball.editor.elements.EditableFieldElement;

public interface PropertyContainer {

    Object getOwner();

    Object getProperty(String propertyName);

    void setProperty(String propertyName, Object value);

    void removeProperty(String propertyName);

    static PropertyContainer forFieldElement(EditableFieldElement element) {
        return new PropertyContainer() {
            @Override public Object getOwner() {
                return element;
            }

            @Override public Object getProperty(String propertyName) {
                return element.getProperty(propertyName);
            }

            @Override public void setProperty(String propertyName, Object value) {
                element.setProperty(propertyName, value);
            }

            @Override public void removeProperty(String propertyName) {
                element.removeProperty(propertyName);
            }
        };
    }

    static PropertyContainer forField(EditableField field) {
        return new PropertyContainer() {
            @Override public Object getOwner() {
                return field;
            }

            @Override public Object getProperty(String propertyName) {
                return field.getProperty(propertyName);
            }

            @Override public void setProperty(String propertyName, Object value) {
                field.setProperty(propertyName, value);
            }

            @Override public void removeProperty(String propertyName) {
                field.removeProperty(propertyName);
            }
        };
    }
}
