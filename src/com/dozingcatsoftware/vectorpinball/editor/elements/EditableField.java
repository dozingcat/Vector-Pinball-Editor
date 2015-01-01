package com.dozingcatsoftware.vectorpinball.editor.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dozingcatsoftware.vectorpinball.util.CollectionUtils;

public class EditableField {

    public static final String ELEMENTS_PROPERTY = "elements";

    Map<String, Object> properties;
    List<EditableFieldElement> elements;

    void initFromProperties(Map<String, Object> props) {
        properties = CollectionUtils.mutableDeepCopyOfMap(props);
        if (!properties.containsKey(ELEMENTS_PROPERTY)) {
            properties.put(ELEMENTS_PROPERTY, new ArrayList<Object>());
        }
        elements = new ArrayList<>();
        List<Map<String, Object>> elementMaps = (List<Map<String, Object>>)properties.get(ELEMENTS_PROPERTY);
        // Strings are allowed in the elements array, should really be fields in element maps.
        for (Object emap : elementMaps) {
            if (emap instanceof Map) {
                elements.add(EditableFieldElement.createFromParameters((Map)emap));
            }
        }
    }

    public static EditableField createFromPropertyMap(Map<String, Object> props) {
        EditableField field = new EditableField();
        field.initFromProperties(props);
        return field;
    }

    public List<EditableFieldElement> getElements() {
        return elements;
    }

    public Map<String, Object> getPropertyMapSnapshot() {
        List<Map<String, Object>> elementMaps = new ArrayList<>();
        for (EditableFieldElement elem : elements) {
            elementMaps.add(elem.getPropertyMap());
        }
        Map<String, Object> mapCopy = CollectionUtils.mutableDeepCopyOfMap(properties);
        mapCopy.put(ELEMENTS_PROPERTY, CollectionUtils.mutableDeepCopyOfList(elementMaps));
        return mapCopy;
    }
}
