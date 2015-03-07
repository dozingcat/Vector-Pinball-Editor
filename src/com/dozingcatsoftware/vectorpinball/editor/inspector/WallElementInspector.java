package com.dozingcatsoftware.vectorpinball.editor.inspector;

import static com.dozingcatsoftware.vectorpinball.util.Localization.localizedString;

import java.util.Arrays;

import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableWallElement;

public class WallElementInspector extends ElementInspector {

    @Override public String getLabel() {
        return "Wall";
    }

    @Override void drawInPane(Pane pane) {
        VBox box = new VBox();
        box.getChildren().add(createStringFieldWithLabel(EditableWallElement.ID_PROPERTY, "ID"));
        box.getChildren().add(createColorSelectorWithLabel(EditableWallElement.COLOR_PROPERTY, "Color"));
        // For position, use 2x2 grid of text fields which produce a value of a 4-element list.
        box.getChildren().add(createMultiRowDecimalArrayFieldWithLabels(
                EditableWallElement.POSITION_PROPERTY,
                Arrays.asList(localizedString("Start"), localizedString("End")),
                2, 2));

        box.getChildren().add(createIntegerFieldWithLabel(EditableWallElement.SCORE_PROPERTY, "Score"));
        box.getChildren().add(createDecimalStringFieldWithLabel(
                EditableWallElement.KICK_PROPERTY, localizedString("Kick")));

        box.getChildren().add(createBooleanCheckBoxFieldWithLabel(
                EditableWallElement.IGNORE_BALL_PROPERTY, localizedString("Ignore ball")));
        box.getChildren().add(createBooleanCheckBoxFieldWithLabel(
                EditableWallElement.RETRACT_WHEN_HIT_PROPERTY, localizedString("Retract when hit")));
        box.getChildren().add(createBooleanCheckBoxFieldWithLabel(
                EditableWallElement.KILL_PROPERTY, localizedString("Kill ball")));

        pane.getChildren().add(box);
    }

}
