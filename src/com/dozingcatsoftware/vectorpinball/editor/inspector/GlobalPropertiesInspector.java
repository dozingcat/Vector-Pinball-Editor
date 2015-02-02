package com.dozingcatsoftware.vectorpinball.editor.inspector;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableField;

public class GlobalPropertiesInspector extends ElementInspector {

    @Override void drawInPane(Pane pane) {
        VBox box = new VBox();

        box.getChildren().add(createDecimalStringFieldWithLabel("Width", EditableField.WIDTH_PROPERTY));
        box.getChildren().add(createDecimalStringFieldWithLabel("Height", EditableField.HEIGHT_PROPERTY));
        box.getChildren().add(createDecimalStringFieldWithLabel("Gravity", EditableField.GRAVITY_PROPERTY));
        box.getChildren().add(createDecimalStringFieldWithLabel("Timescale", EditableField.TIME_RATIO_PROPERTY));
        box.getChildren().add(createIntegerFieldWithLabel("# Balls", EditableField.NUM_BALLS_PROPERTY));
        box.getChildren().add(createDecimalStringFieldWithLabel("Ball radius", EditableField.BALL_RADIUS_PROPERTY));

        box.getChildren().add(new Label("TODO: Ball color, launch params"));

        pane.getChildren().add(box);
    }

}
