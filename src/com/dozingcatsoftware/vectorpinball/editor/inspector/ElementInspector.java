package com.dozingcatsoftware.vectorpinball.editor.inspector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.InvalidationListener;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableFieldElement;

public abstract class ElementInspector<T extends EditableFieldElement> {

    T element;
    Runnable changeCallback;
    boolean updatingFromExternalChange = false;

    Map<String, TextField> decimalPropertyToTextField = new HashMap<>();
    Map<String, List<TextField>> positionPropertyToTextFields = new HashMap<>();

    public void initialize(Pane pane, T element, Runnable changeCallback) {
        this.element = element;
        this.changeCallback = changeCallback;
        drawInPane(pane);
        updateControlValuesFromElement();
    }

    abstract void drawInPane(Pane pane);

    public T getElement() {
        return element;
    }

    protected void notifyChanged() {
        if (changeCallback != null && !updatingFromExternalChange) {
            changeCallback.run();
        }
    }

    public void updateControlValuesFromElement() {
        updatingFromExternalChange = true;
        for (String prop : decimalPropertyToTextField.keySet()) {
            Number value = (Number)getElement().getProperty(prop);
            decimalPropertyToTextField.get(prop).setText(value!=null ? value.toString() : "");
        }
        for (String prop : positionPropertyToTextFields.keySet()) {
            List<Number> values = (List<Number>)getElement().getProperty(prop);
            List<TextField> textFields = positionPropertyToTextFields.get(prop);
            for (int i=0; i<textFields.size(); i++) {
                textFields.get(i).setText(values.get(i).toString());
            }
        }
        updatingFromExternalChange = false;
    }

    HBox createDecimalTextFieldWithLabel(String label, String propertyName) {
        HBox box = new HBox();
        box.getChildren().add(new Label(label));
        DecimalTextField textField = new DecimalTextField();
        textField.textProperty().addListener((event) -> updateDecimalValue(propertyName, textField));
        box.getChildren().add(textField);
        decimalPropertyToTextField.put(propertyName, textField);
        return box;
    }

    HBox createPositionTextFieldsWithLabel(String label, String propertyName) {
        HBox box = new HBox();
        box.getChildren().add(new Label(label));
        DecimalTextField xField = new DecimalTextField();
        DecimalTextField yField = new DecimalTextField();
        box.getChildren().addAll(xField, yField);

        List<TextField> positionTextFields = Arrays.asList(xField, yField);
        positionPropertyToTextFields.put(propertyName, positionTextFields);
        InvalidationListener changeHandler = (event) -> updatePositionValue(propertyName, positionTextFields);
        positionTextFields.forEach((field) -> field.textProperty().addListener(changeHandler));
        return box;
    }

    void updateDecimalValue(String propertyName, TextField textField) {
        if (updatingFromExternalChange) return;
        String stringValue = textField.getText();
        if (stringValue==null || stringValue.length()==0) {
            // TODO: support removing property
        }
        else {
            getElement().setProperty(propertyName, Double.valueOf(stringValue));
            notifyChanged();
        }
    }

    void updatePositionValue(String propertyName, List<TextField> textFields) {
        if (updatingFromExternalChange) return;
        List<Double> position = Arrays.asList(
                Double.valueOf(textFields.get(0).getText()), Double.valueOf(textFields.get(1).getText()));
        getElement().setProperty(propertyName, position);
        System.out.println("Updating " + propertyName + " to " + position);
        notifyChanged();
    }
}
