package com.dozingcatsoftware.vectorpinball.editor.inspector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import com.dozingcatsoftware.vectorpinball.editor.elements.PropertyContainer;

public abstract class ElementInspector {

    public static final int DEFAULT_HBOX_SPACING = 5;
    public static final Insets DEFAULT_HBOX_INSETS = new Insets(4, 5, 4, 10);
    public static final int DEFAULT_LABEL_WIDTH = 80;

    PropertyContainer propertyContainer;
    Runnable changeCallback;
    boolean updatingFromExternalChange = false;

    Map<String, DecimalStringPropertyEditor> decimalPropertyToEditor= new HashMap<>();
    Map<String, IntegerPropertyEditor> integerPropertyToEditor = new HashMap<>();
    Map<String, PositionPropertyEditor> positionPropertyToEditor = new HashMap<>();
    Map<String, BooleanPropertyEditor> booleanPropertyToEditor = new HashMap<>();
    Map<String, ColorPropertyEditor> colorPropertyToSelector = new HashMap<>();

    public void initialize(Pane pane, PropertyContainer propertyContainer, Runnable changeCallback) {
        this.propertyContainer = propertyContainer;
        this.changeCallback = changeCallback;
        drawInPane(pane);
        updateControlValuesFromElement();
    }

    abstract void drawInPane(Pane pane);

    public PropertyContainer getPropertyContainer() {
        return propertyContainer;
    }

    protected void notifyChanged() {
        if (changeCallback != null && !updatingFromExternalChange) {
            changeCallback.run();
        }
    }

    public void updateControlValuesFromElement() {
        updatingFromExternalChange = true;
        for (String prop : decimalPropertyToEditor.keySet()) {
            Object value = getPropertyContainer().getProperty(prop);
            decimalPropertyToEditor.get(prop).updateFromValue(value);
        }
        for (String prop : integerPropertyToEditor.keySet()) {
            Number value = (Number)getPropertyContainer().getProperty(prop);
            integerPropertyToEditor.get(prop).updateFromValue(value);
        }
        for (String prop : positionPropertyToEditor.keySet()) {
            List<Object> values = (List<Object>)getPropertyContainer().getProperty(prop);
            positionPropertyToEditor.get(prop).updateFromValue(values);
        }
        for (String prop : booleanPropertyToEditor.keySet()) {
            Boolean value = (Boolean)getPropertyContainer().getProperty(prop);
            booleanPropertyToEditor.get(prop).updateFromValue(value);
        }
        for (String prop : colorPropertyToSelector.keySet()) {
            List<Number> value = (List<Number>)getPropertyContainer().getProperty(prop);
            colorPropertyToSelector.get(prop).updateFromValue(value);
        }
        updateCustomControlValues();
        updatingFromExternalChange = false;
    }

    // Override to update UI components that aren't handled by the standard
    // cases in updateControlValuesFromElement.
    protected void updateCustomControlValues() {
    }

    static HBox createHBoxWithLabel(String text) {
        HBox box = new HBox(DEFAULT_HBOX_SPACING);
        box.setPadding(DEFAULT_HBOX_INSETS);
        box.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label(text);
        label.setPrefWidth(DEFAULT_LABEL_WIDTH);
        box.getChildren().add(label);
        return box;
    }

    void setOrRemoveProperty(String propertyName, Object value) {
        if (updatingFromExternalChange) return;
        getPropertyContainer().setOrRemoveProperty(propertyName, value);
        notifyChanged();
    }

    HBox createDecimalStringFieldWithLabel(String label, String propertyName) {
        HBox box = createHBoxWithLabel(label);
        DecimalStringPropertyEditor editor = new DecimalStringPropertyEditor();
        editor.setOnChange(() -> setOrRemoveProperty(propertyName, editor.getValue()));
        box.getChildren().add(editor.getContainer());
        decimalPropertyToEditor.put(propertyName, editor);
        return box;
    }

    HBox createIntegerFieldWithLabel(String label, String propertyName) {
        HBox box = createHBoxWithLabel(label);
        IntegerPropertyEditor editor = new IntegerPropertyEditor();
        editor.setOnChange(() -> setOrRemoveProperty(propertyName, editor.getValue()));
        box.getChildren().add(editor.getContainer());
        integerPropertyToEditor.put(propertyName, editor);
        return box;
    }

    HBox createPositionStringFieldsWithLabel(String label, String propertyName) {
        HBox box = createHBoxWithLabel(label);
        PositionPropertyEditor editor = new PositionPropertyEditor();
        editor.setOnChange(() -> setOrRemoveProperty(propertyName, editor.getValue()));
        box.getChildren().add(editor.getContainer());
        positionPropertyToEditor.put(propertyName, editor);
        return box;
    }

    HBox createBooleanCheckBoxFieldWithLabel(String label, String propertyName) {
        HBox box = createHBoxWithLabel(label);
        BooleanPropertyEditor editor = new BooleanPropertyEditor();
        editor.setOnChange(() -> setOrRemoveProperty(propertyName, editor.getValue()));
        box.getChildren().add(editor.getContainer());
        booleanPropertyToEditor.put(propertyName, editor);
        return box;
    }

    HBox createColorSelectorWithLabel(String label, String propertyName) {
        HBox box = createHBoxWithLabel(label);
        ColorPropertyEditor selector = new ColorPropertyEditor();
        selector.setOnChange(() -> setOrRemoveProperty(propertyName, selector.getValue()));
        box.getChildren().add(selector.getContainer());
        colorPropertyToSelector.put(propertyName, selector);
        return box;
    }

}
