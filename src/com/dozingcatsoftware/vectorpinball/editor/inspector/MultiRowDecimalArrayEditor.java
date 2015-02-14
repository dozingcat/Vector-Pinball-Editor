package com.dozingcatsoftware.vectorpinball.editor.inspector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MultiRowDecimalArrayEditor extends PropertyEditor<List<String>> {

    List<DecimalTextField> textFields;

    public MultiRowDecimalArrayEditor (List<String> rowLabels, int numFieldsPerRow, int numRows) {
        VBox vbox = new VBox();
        textFields = new ArrayList<DecimalTextField>(numFieldsPerRow * numRows);

        for (int row=0; row<numRows; row++) {
            HBox hbox = ElementInspector.createHBoxWithLabel(rowLabels.get(row));
            for (int i=0; i<numFieldsPerRow; i++) {
                DecimalTextField field = new DecimalTextField();
                field.setChangeHandler(this::runChangeHandler);
                textFields.add(field);
                hbox.getChildren().add(field);
            }
            vbox.getChildren().add(hbox);
        }
        setContainer(vbox);
    }

    @Override List<String> getValue() {
        // Should check for valid values.
        return textFields.stream().map(DecimalTextField::getText).collect(Collectors.toList());
    }

    @Override void updateFromValue(List<String> value) {
        for (int i=0; i<value.size(); i++) {
            Object val = (value!=null && value.size()>i) ? value.get(i) : "";
            textFields.get(i).setText(val.toString());
        }
    }

}
