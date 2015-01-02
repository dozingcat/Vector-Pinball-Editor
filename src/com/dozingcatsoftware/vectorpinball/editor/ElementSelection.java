package com.dozingcatsoftware.vectorpinball.editor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableFieldElement;

public class ElementSelection {

    Set<EditableFieldElement> selectedElements = new HashSet<>();
    Runnable changeCallback;


    public void setSelectionChangeCallback(Runnable callback) {
        changeCallback = callback;
    }

    void runChangeCallback() {
        if (changeCallback != null) {
            changeCallback.run();
        }
    }

    public void clearSelection() {
        selectedElements.clear();
    }

    public boolean isElementSelected(EditableFieldElement element) {
        return selectedElements.contains(element);
    }

    public void selectElement(EditableFieldElement element) {
        selectedElements.clear();
        selectedElements.add(element);
        runChangeCallback();
    }

    public void setSelectedElements(Collection<EditableFieldElement> elements) {
        selectedElements.clear();
        selectedElements.addAll(elements);
        runChangeCallback();
    }

    public boolean hasSelection() {
        return !selectedElements.isEmpty();
    }

    public Set<EditableFieldElement> getSelectedElements() {
        return Collections.unmodifiableSet(selectedElements);
    }
}
