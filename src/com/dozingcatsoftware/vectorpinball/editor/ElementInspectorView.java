package com.dozingcatsoftware.vectorpinball.editor;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableFieldElement;

public class ElementInspectorView extends VBox {

    ElementSelection elementSelection;
    Label selectionLabel;

    public ElementInspectorView() {
        selectionLabel = new Label("");
        this.getChildren().add(selectionLabel);
        update();
    }

    public void setElementSelection(ElementSelection selection) {
        elementSelection = selection;
    }

    public void update() {
        if (elementSelection != null && elementSelection.hasSelection()) {
            EditableFieldElement elem = elementSelection.getSelectedElements().iterator().next();
            String className = elem.getClass().getName();
            selectionLabel.setText(className.substring(className.lastIndexOf('.')+1));
        }
        else {
            selectionLabel.setText("No selection");
        }
    }
}
