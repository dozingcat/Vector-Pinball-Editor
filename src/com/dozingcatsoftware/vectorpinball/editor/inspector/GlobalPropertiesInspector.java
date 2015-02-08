package com.dozingcatsoftware.vectorpinball.editor.inspector;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableField;

public class GlobalPropertiesInspector extends ElementInspector {

    @Override void drawInPane(Pane pane) {
        VBox box = new VBox();

        box.getChildren().add(createDecimalStringFieldWithLabel(EditableField.WIDTH_PROPERTY, "Width"));
        box.getChildren().add(createDecimalStringFieldWithLabel(EditableField.HEIGHT_PROPERTY, "Height"));
        box.getChildren().add(createDecimalStringFieldWithLabel(EditableField.GRAVITY_PROPERTY, "Gravity"));
        box.getChildren().add(createDecimalStringFieldWithLabel(EditableField.TIME_RATIO_PROPERTY, "Timescale"));
        box.getChildren().add(createIntegerFieldWithLabel(EditableField.NUM_BALLS_PROPERTY, "# Balls"));
        box.getChildren().add(createDecimalStringFieldWithLabel(EditableField.BALL_RADIUS_PROPERTY, "Ball radius"));

        box.getChildren().add(new Label("TODO: Ball color, launch params"));

        pane.getChildren().add(box);
    }

}
