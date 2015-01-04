package com.dozingcatsoftware.vectorpinball.editor.inspector;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableFieldElement;

public abstract class ElementInspector<T extends EditableFieldElement> {

    T element;
    Runnable changeCallback;

    public void initialize(Pane pane, T element, Runnable changeCallback) {
        this.element = element;
        this.changeCallback = changeCallback;
        drawInPane(pane);
    }

    abstract void drawInPane(Pane pane);

    public T getElement() {
        return element;
    }

    protected void notifyChanged() {
        if (changeCallback != null) {
            changeCallback.run();
        }
    }

    HBox createDecimalTextFieldWithLabel(String label, String propertyName) {
        HBox box = new HBox();
        box.getChildren().add(new Label(label));
        DecimalTextField textField = new DecimalTextField();
        Number initialValue = (Number)getElement().getProperty(propertyName);
        if (initialValue != null) {
            textField.setText(initialValue.toString());
        }
        textField.textProperty().addListener((event) -> updateDecimalValue(propertyName, textField.getText()));
        box.getChildren().add(textField);
        return box;
    }

    void updateDecimalValue(String propertyName, String stringValue) {
        if (stringValue==null || stringValue.length()==0) {
            // TODO: support removing property
        }
        else {
            getElement().setProperty(propertyName, Double.valueOf(stringValue));
            notifyChanged();
        }
    }
}
