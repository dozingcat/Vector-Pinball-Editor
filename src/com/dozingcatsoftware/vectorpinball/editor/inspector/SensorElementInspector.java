package com.dozingcatsoftware.vectorpinball.editor.inspector;

import java.util.Arrays;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableFieldElement;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableSensorElement;

public class SensorElementInspector extends ElementInspector {

    @Override public String getLabel() {
        return "Sensor";
    }

    @Override void drawInPane(Pane pane) {
        VBox box = new VBox();
        box.getChildren().add(createStringFieldWithLabel(EditableFieldElement.ID_PROPERTY, "ID"));
        box.getChildren().add(createMultiRowDecimalArrayFieldWithLabels(
                EditableSensorElement.RECT_PROPERTY,
                Arrays.asList("Left/Bottom", "Right/Top"),
                2, 2));
        box.getChildren().add(new Label("Change ball layer:"));
        box.getChildren().add(createIntegerFieldWithLabel(
                EditableSensorElement.BALL_LAYER_TO_PROPERTY, "To layer"));
        box.getChildren().add(createIntegerFieldWithLabel(
                EditableSensorElement.BALL_LAYER_FROM_PROPERTY, "If at layer"));
        pane.getChildren().add(box);
    }

}
