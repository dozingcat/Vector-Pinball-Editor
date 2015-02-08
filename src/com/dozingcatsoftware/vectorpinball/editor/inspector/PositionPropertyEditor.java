package com.dozingcatsoftware.vectorpinball.editor.inspector;

import java.util.Arrays;
import java.util.List;

import javafx.scene.layout.HBox;

public class PositionPropertyEditor extends PropertyEditor<List<Object>> {

    DecimalTextField xField, yField;

    public PositionPropertyEditor() {
        HBox box = new HBox();
        xField = new DecimalTextField();
        yField = new DecimalTextField();

        Arrays.asList(xField, yField).forEach((field) -> {
            field.setOnAction((event) -> runChangeHandler());
            field.focusedProperty().addListener((target, wasFocused, isFocused) -> {
                if (!isFocused) runChangeHandler();
            });
        });
        box.getChildren().addAll(xField, yField);
        setContainer(box);
    }

    @Override List<Object> getValue() {
        // Should check for valid values.
        return Arrays.asList(xField.getText(), yField.getText());
    }

    @Override void updateFromValue(List<Object> value) {
        Object xval = value.get(0);
        xField.setText(xval!=null ? xval.toString() : "");

        Object yval = value.get(1);
        yField.setText(yval!=null ? yval.toString() : "");
    }

}
