package com.dozingcatsoftware.vectorpinball.editor.elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dozingcatsoftware.vectorpinball.util.CollectionUtils;

/**
 * Contains the field elements and global field properties, and manages the selected items.
 **/
public class EditableField {

    public static final String ELEMENTS_PROPERTY = "elements";

    Map<String, Object> properties;
    List<EditableFieldElement> elements = new ArrayList<>();
    Set<EditableFieldElement> selectedElements = new HashSet<>();

    Runnable elementChangedCallback;
    Runnable selectionChangedCallback;

    public void setElementChangedCallback(Runnable callback) {
        elementChangedCallback = callback;
    }

    public void setSelectionChangedCallback(Runnable callback) {
        selectionChangedCallback = callback;
    }

    public void initFromProperties(Map<String, Object> props) {
        clearSelection();
        properties = CollectionUtils.mutableDeepCopyOfMap(props);
        if (!properties.containsKey(ELEMENTS_PROPERTY)) {
            properties.put(ELEMENTS_PROPERTY, new ArrayList<Object>());
        }
        elements.clear();
        List<Map<String, Object>> elementMaps = (List<Map<String, Object>>)properties.get(ELEMENTS_PROPERTY);
        // Strings are allowed in the elements array, should really be fields in element maps.
        for (Object emap : elementMaps) {
            if (emap instanceof Map) {
                EditableFieldElement elem = EditableFieldElement.createFromParameters((Map)emap);
                elem.setChangeHandler(elementChangedCallback);
                elements.add(elem);
            }
        }
    }

    public EditableFieldElement addNewElement(Class<? extends EditableFieldElement> elementClass) {
        EditableFieldElement elem;
        try {
            elem = elementClass.newInstance();
            elem.initAsNewElement(this);
            elem.setChangeHandler(elementChangedCallback);
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

    public void removeElement(EditableFieldElement element) {
        this.elements.remove(element);
    }

    public void removeElements(Collection<EditableFieldElement> elements) {
        this.elements.removeAll(elements);
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

    public void restoreFromPropertyMapSnapshot(Map<String, Object> snapshot) {
        this.initFromProperties(snapshot);
    }

    void runSelectionChangeCallback() {
        if (selectionChangedCallback != null) {
            selectionChangedCallback.run();
        }
    }

    public void clearSelection() {
        selectedElements.clear();
        runSelectionChangeCallback();
    }

    public boolean isElementSelected(EditableFieldElement element) {
        return selectedElements.contains(element);
    }

    public void selectElement(EditableFieldElement element) {
        selectedElements.clear();
        selectedElements.add(element);
        runSelectionChangeCallback();
    }

    public void setSelectedElements(Collection<EditableFieldElement> elements) {
        selectedElements.clear();
        selectedElements.addAll(elements);
        runSelectionChangeCallback();
    }

    public boolean hasSelection() {
        return !selectedElements.isEmpty();
    }

    public Set<EditableFieldElement> getSelectedElements() {
        return new HashSet(selectedElements);
    }

    public Set<Integer> getSelectedElementIndexes() {
        Set<Integer> indexes = new HashSet<>();
        for(int i=0; i<elements.size(); i++) {
            if (selectedElements.contains(elements.get(i))) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    public void setSelectedElementIndexes(Collection<Integer> indexes) {
        List<EditableFieldElement> selected = new ArrayList<>();
        for (int i : indexes) {
            selected.add(elements.get(i));
        }
        setSelectedElements(selected);
    }
}
