package com.dozingcatsoftware.vectorpinball.editor.inspector;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableWallPathElement;

public class WallPathElementInspector extends ElementInspector<EditableWallPathElement> {

    @Override void drawInPane(Pane pane) {
        VBox box = new VBox();
        box.getChildren().add(new Label("TODO"));
        pane.getChildren().add(box);
    }

}
