package com.dozingcatsoftware.vectorpinball.editor.inspector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableFieldElement;

public abstract class ElementInspector<T extends EditableFieldElement> {

    public static final int DEFAULT_HBOX_SPACING = 5;
    public static final Insets DEFAULT_HBOX_INSETS = new Insets(4, 5, 4, 10);
    public static final int DEFAULT_LABEL_WIDTH = 60;

    T element;
    Runnable changeCallback;
    boolean updatingFromExternalChange = false;

    Map<String, TextField> decimalPropertyToTextField = new HashMap<>();
    Map<String, List<TextField>> positionPropertyToTextFields = new HashMap<>();
    Map<String, CheckBox> booleanPropertyToCheckBox = new HashMap<>();

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
            Object value = getElement().getProperty(prop);
            decimalPropertyToTextField.get(prop).setText(value!=null ? value.toString() : "");
        }
        for (String prop : positionPropertyToTextFields.keySet()) {
            List<?> values = (List<?>)getElement().getProperty(prop);
            List<TextField> textFields = positionPropertyToTextFields.get(prop);
            for (int i=0; i<textFields.size(); i++) {
                textFields.get(i).setText(values.get(i).toString());
            }
        }
        for (String prop : booleanPropertyToCheckBox.keySet()) {
            Boolean value = (Boolean)getElement().getProperty(prop);
            booleanPropertyToCheckBox.get(prop).setSelected(Boolean.TRUE.equals(value));
        }
        updateCustomControlValues();
        updatingFromExternalChange = false;
    }

    // Override to update UI components that aren't handled by the standard
    // cases in updateControlValuesFromElement.
    protected void updateCustomControlValues() {
    }

    HBox createHBoxWithLabel(String text) {
        HBox box = new HBox(DEFAULT_HBOX_SPACING);
        box.setPadding(DEFAULT_HBOX_INSETS);
        box.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label(text);
        label.setPrefWidth(DEFAULT_LABEL_WIDTH);
        box.getChildren().add(label);
        return box;
    }

    HBox createDecimalStringFieldWithLabel(String label, String propertyName) {
        HBox box = createHBoxWithLabel(label);
        DecimalTextField textField = new DecimalTextField();
        textField.textProperty().addListener((event) -> updateDecimalValue(propertyName, textField));
        box.getChildren().add(textField);
        decimalPropertyToTextField.put(propertyName, textField);
        return box;
    }

    HBox createPositionStringFieldsWithLabel(String label, String propertyName) {
        HBox box = createHBoxWithLabel(label);
        DecimalTextField xField = new DecimalTextField();
        DecimalTextField yField = new DecimalTextField();
        box.getChildren().addAll(xField, yField);

        List<TextField> positionTextFields = Arrays.asList(xField, yField);
        positionPropertyToTextFields.put(propertyName, positionTextFields);
        InvalidationListener changeHandler = (event) -> updatePositionValue(propertyName, positionTextFields);
        positionTextFields.forEach((field) -> field.textProperty().addListener(changeHandler));
        return box;
    }

    HBox createBooleanCheckBoxFieldWithLabel(String label, String propertyName) {
        HBox box = createHBoxWithLabel(label);
        CheckBox checkbox = new CheckBox();
        checkbox.selectedProperty().addListener((event) -> updateBooleanCheckBoxValue(propertyName, checkbox));
        box.getChildren().add(checkbox);
        booleanPropertyToCheckBox.put(propertyName, checkbox);
        return box;
    }

    void updateDecimalValue(String propertyName, TextField textField) {
        if (updatingFromExternalChange) return;
        String stringValue = textField.getText();
        if (stringValue==null || stringValue.length()==0) {
            getElement().removeProperty(propertyName);
        }
        else {
            getElement().setProperty(propertyName, stringValue);
        }
        notifyChanged();
    }

    void updatePositionValue(String propertyName, List<TextField> textFields) {
        if (updatingFromExternalChange) return;
        List<String> position = Arrays.asList(textFields.get(0).getText(), textFields.get(1).getText());
        getElement().setProperty(propertyName, position);
        notifyChanged();
    }

    void updateBooleanCheckBoxValue(String propertyName, CheckBox cbox) {
        if (updatingFromExternalChange) return;
        getElement().setProperty(propertyName, cbox.isSelected());
        notifyChanged();
    }
}
