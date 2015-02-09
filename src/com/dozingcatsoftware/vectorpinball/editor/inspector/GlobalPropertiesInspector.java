package com.dozingcatsoftware.vectorpinball.editor.inspector;

import java.util.Arrays;
import java.util.List;

import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableField;

public class GlobalPropertiesInspector extends ElementInspector {

    List<TextField> deadZoneTextFields;

    @Override void drawInPane(Pane pane) {
        VBox box = new VBox();

        box.getChildren().addAll(
                createStringFieldWithLabel(EditableField.NAME_PROPERTY, "Name"),
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

        // TODO: This is basically the same as the 4 text fields in WallElementInspector,
        // figure out how to remove duplication.
        deadZoneTextFields = Arrays.asList(
                createDeadZoneTextField(),
                createDeadZoneTextField(),
                createDeadZoneTextField(),
                createDeadZoneTextField());

        HBox startBox = createHBoxWithLabel("Dead zone start");
        startBox.getChildren().addAll(deadZoneTextFields.get(0), deadZoneTextFields.get(1));
        box.getChildren().add(startBox);

        HBox endBox = createHBoxWithLabel("Dead zone end");
        endBox.getChildren().addAll(deadZoneTextFields.get(2), deadZoneTextFields.get(3));
        box.getChildren().add(endBox);

        pane.getChildren().add(box);
    }

    TextField createDeadZoneTextField() {
        DecimalTextField field = new DecimalTextField();
        field.setChangeHandler(this::updateDeadZone);
        return field;
    }

    void updateDeadZone() {
        if (updatingFromExternalChange) return;
        List<String> deadZone = Arrays.asList(
                deadZoneTextFields.get(0).getText(),
                deadZoneTextFields.get(1).getText(),
                deadZoneTextFields.get(2).getText(),
                deadZoneTextFields.get(3).getText());
        getPropertyContainer().setProperty(EditableField.LAUNCH_DEAD_ZONE_PROPERTY, deadZone);
        notifyChanged();
    }

    @Override public void updateCustomControlValues() {
        List<?> deadZone = (List<?>)getPropertyContainer().getProperty(EditableField.LAUNCH_DEAD_ZONE_PROPERTY);
        for (int i=0; i<4; i++) {
            String val = (deadZone!=null && deadZone.size()>i) ? deadZone.get(i).toString() : "";
            deadZoneTextFields.get(i).setText(val);
        }
    }

}
