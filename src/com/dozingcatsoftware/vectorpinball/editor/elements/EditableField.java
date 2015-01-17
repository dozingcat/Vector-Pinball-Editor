package com.dozingcatsoftware.vectorpinball.editor.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dozingcatsoftware.vectorpinball.util.CollectionUtils;

public class EditableField {

    public static final String ELEMENTS_PROPERTY = "elements";

    Map<String, Object> properties;
    List<EditableFieldElement> elements;
    Runnable changeHandler;

    void initFromProperties(Map<String, Object> props, Runnable changeHandler) {
        this.changeHandler = changeHandler;
        properties = CollectionUtils.mutableDeepCopyOfMap(props);
        if (!properties.containsKey(ELEMENTS_PROPERTY)) {
            properties.put(ELEMENTS_PROPERTY, new ArrayList<Object>());
        }
        elements = new ArrayList<>();
        List<Map<String, Object>> elementMaps = (List<Map<String, Object>>)properties.get(ELEMENTS_PROPERTY);
        // Strings are allowed in the elements array, should really be fields in element maps.
        for (Object emap : elementMaps) {
            if (emap instanceof Map) {
                EditableFieldElement elem = EditableFieldElement.createFromParameters((Map)emap);
                elem.setChangeHandler(changeHandler);
                elements.add(elem);
            }
        }
    }

    public static EditableField createFromPropertyMap(Map<String, Object> props, Runnable changeHandler) {
        EditableField field = new EditableField();
        field.initFromProperties(props, changeHandler);
        return field;
    }

    public EditableFieldElement addNewElement(Class<? extends EditableFieldElement> elementClass) {
        EditableFieldElement elem;
        try {
            elem = elementClass.newInstance();
            elem.initAsNewElement(this);
            elem.setChangeHandler(changeHandler);
            elements.add(elem);
            return elem;
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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
