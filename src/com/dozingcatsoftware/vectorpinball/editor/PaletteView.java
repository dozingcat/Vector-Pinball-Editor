package com.dozingcatsoftware.vectorpinball.editor;

import java.util.function.Consumer;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableBumperElement;
import com.dozingcatsoftware.vectorpinball.editor.elements.EditableFieldElement;
import com.dozingcatsoftware.vectorpinball.editor.elements.EditableWallElement;
import com.dozingcatsoftware.vectorpinball.editor.elements.EditableWallPathElement;

public class PaletteView extends VBox {

    Consumer<Class<? extends EditableFieldElement>> createCallback;

    public PaletteView(Consumer<Class<? extends EditableFieldElement>> createCallback) {
        super();
        this.createCallback = createCallback;

        Button bumperButton = new Button("Add Bumper");
        bumperButton.setOnAction((event) -> createCallback.accept(EditableBumperElement.class));

        Button wallButton = new Button("Add Wall");
        wallButton.setOnAction((event) -> createCallback.accept(EditableWallElement.class));

        Button wallPathButton = new Button("Add Wall Path");
        wallPathButton.setOnAction((event) -> createCallback.accept(EditableWallPathElement.class));

        this.getChildren().addAll(bumperButton, wallButton, wallPathButton);
    }

}
