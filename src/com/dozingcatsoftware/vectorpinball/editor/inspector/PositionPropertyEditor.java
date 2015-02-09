package com.dozingcatsoftware.vectorpinball.editor.inspector;

import java.util.Arrays;
import java.util.List;

import javafx.scene.layout.HBox;

public class PositionPropertyEditor extends PropertyEditor<List<Object>> {

    DecimalTextField xField, yField;

    public PositionPropertyEditor() {
        HBox box = new HBox(5);
        xField = new DecimalTextField();
        xField.setChangeHandler(this::runChangeHandler);
        yField = new DecimalTextField();
        yField.setChangeHandler(this::runChangeHandler);

        box.getChildren().addAll(xField, yField);
        setContainer(box);
    }

    @Override List<Object> getValue() {
        // Should check for valid values.
        return Arrays.asList(xField.getText(), yField.getText());
    }

    @Override void updateFromValue(List<Object> value) {
        Object xval = (value!=null && value.size()>0) ? value.get(0) : "";
        xField.setText(xval!=null ? xval.toString() : "");

        Object yval = (value!=null && value.size()>1) ? value.get(1) : "";
        yField.setText(yval!=null ? yval.toString() : "");
    }

}
