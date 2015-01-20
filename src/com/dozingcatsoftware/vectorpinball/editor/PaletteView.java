package com.dozingcatsoftware.vectorpinball.editor;

import java.util.function.Consumer;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableBumperElement;
import com.dozingcatsoftware.vectorpinball.editor.elements.EditableFieldElement;

public class PaletteView extends VBox {

    Consumer<Class<? extends EditableFieldElement>> createCallback;

    public PaletteView(Consumer<Class<? extends EditableFieldElement>> createCallback) {
        super();
        this.createCallback = createCallback;
        Button bumperButton = new Button("Add Bumper");
        this.getChildren().add(bumperButton);
        bumperButton.setOnAction((event) -> createCallback.accept(EditableBumperElement.class));
    }

}
