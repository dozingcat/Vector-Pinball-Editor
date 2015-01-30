package com.dozingcatsoftware.vectorpinball.editor.inspector;

import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableBumperElement;

public class BumperElementInspector extends ElementInspector<EditableBumperElement> {

    @Override void drawInPane(Pane pane) {
        VBox box = new VBox();
        box.getChildren().add(createHBoxWithLabel("Bumper"));
        box.getChildren().add(createDecimalStringFieldWithLabel("Radius", EditableBumperElement.RADIUS_PROPERTY));
        box.getChildren().add(createPositionStringFieldsWithLabel("Center", EditableBumperElement.POSITION_PROPERTY));
        box.getChildren().add(createIntegerFieldWithLabel("Score", EditableBumperElement.SCORE_PROPERTY));
        box.getChildren().add(createIntegerFieldWithLabel("Kick", EditableBumperElement.KICK_PROPERTY));

        pane.getChildren().add(box);
    }

}
