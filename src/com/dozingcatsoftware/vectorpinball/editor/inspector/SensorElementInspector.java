package com.dozingcatsoftware.vectorpinball.editor.inspector;

import java.util.Arrays;

import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableSensorElement;

public class SensorElementInspector extends ElementInspector {

    @Override public String getLabel() {
        return "Sensor";
    }

    @Override void drawInPane(Pane pane) {
        VBox box = new VBox();
        box.getChildren().add(createStringFieldWithLabel(EditableSensorElement.ID_PROPERTY, "ID"));
        box.getChildren().add(createMultiRowDecimalArrayFieldWithLabels(
                EditableSensorElement.RECT_PROPERTY,
                Arrays.asList("Left/Bottom", "Right/Top"),
                2, 2));
        pane.getChildren().add(box);
    }

}
