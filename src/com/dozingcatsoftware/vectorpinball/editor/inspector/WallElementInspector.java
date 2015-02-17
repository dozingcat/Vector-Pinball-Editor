package com.dozingcatsoftware.vectorpinball.editor.inspector;

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
                Arrays.asList("Start", "End"),
                2, 2));

        box.getChildren().add(createDecimalStringFieldWithLabel(
                EditableWallElement.KICK_PROPERTY, "Kick"));

        box.getChildren().add(createBooleanCheckBoxFieldWithLabel(
                EditableWallElement.RETRACT_WHEN_HIT_PROPERTY, "Retract when hit"));

        pane.getChildren().add(box);
    }

}
