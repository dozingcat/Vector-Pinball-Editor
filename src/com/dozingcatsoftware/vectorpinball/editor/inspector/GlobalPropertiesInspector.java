package com.dozingcatsoftware.vectorpinball.editor.inspector;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableField;

public class GlobalPropertiesInspector extends ElementInspector {

    @Override void drawInPane(Pane pane) {
        VBox box = new VBox();

        box.getChildren().addAll(
                createDecimalStringFieldWithLabel(EditableField.WIDTH_PROPERTY, "Width"),
                createDecimalStringFieldWithLabel(EditableField.HEIGHT_PROPERTY, "Height"),
                createDecimalStringFieldWithLabel(EditableField.GRAVITY_PROPERTY, "Gravity"),
                createDecimalStringFieldWithLabel(EditableField.TARGET_TIME_RATIO_PROPERTY, "Timescale"),
                createIntegerFieldWithLabel(EditableField.NUM_BALLS_PROPERTY, "# Balls"),
                createDecimalStringFieldWithLabel(EditableField.BALL_RADIUS_PROPERTY, "Ball radius"),
                createColorSelectorWithLabel(EditableField.BALL_COLOR_PROPERTY, "Ball color"),
                createColorSelectorWithLabel(EditableField.SECONDARY_BALL_COLOR_PROPERTY, "Secondary color"),
                createPositionStringFieldsWithLabel(EditableField.LAUNCH_POSITION_PROPERTY, "Launch position"),
                createPositionStringFieldsWithLabel(EditableField.LAUNCH_VELOCITY_PROPERTY, "Launch velocity"),
                createPositionStringFieldsWithLabel(EditableField.LAUNCH_RANDOM_VELOCITY_PROPERTY, "Velocity delta")
        );

        box.getChildren().add(new Label("TODO: dead zone"));

        pane.getChildren().add(box);
    }

}
