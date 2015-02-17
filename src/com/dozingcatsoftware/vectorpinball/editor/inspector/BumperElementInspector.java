package com.dozingcatsoftware.vectorpinball.editor.inspector;

import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableBumperElement;

public class BumperElementInspector extends ElementInspector {

    @Override public String getLabel() {
        return "Bumper";
    }

    @Override void drawInPane(Pane pane) {
        VBox box = new VBox();
        box.getChildren().add(createStringFieldWithLabel(EditableBumperElement.ID_PROPERTY, "ID"));
        box.getChildren().add(createColorSelectorWithLabel(EditableBumperElement.COLOR_PROPERTY, "Color"));
        box.getChildren().add(createDecimalStringFieldWithLabel(EditableBumperElement.RADIUS_PROPERTY, "Radius"));
        box.getChildren().add(createPositionStringFieldsWithLabel(EditableBumperElement.POSITION_PROPERTY, "Center"));
        box.getChildren().add(createIntegerFieldWithLabel(EditableBumperElement.SCORE_PROPERTY, "Score"));
        box.getChildren().add(createDecimalStringFieldWithLabel(EditableBumperElement.KICK_PROPERTY, "Kick"));

        pane.getChildren().add(box);
    }

}
