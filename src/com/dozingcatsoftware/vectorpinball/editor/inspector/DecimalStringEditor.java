package com.dozingcatsoftware.vectorpinball.editor.inspector;

import javafx.scene.layout.HBox;

public class DecimalStringEditor extends PropertyEditor<String> {

    DecimalTextField textField;

    public DecimalStringEditor() {
        HBox box = new HBox();
        this.textField = new DecimalTextField();
        this.textField.setOnAction((event) -> runChangeHandler());
        this.textField.focusedProperty().addListener((target, wasFocused, isFocused) -> {
            if (!isFocused) runChangeHandler();
        });
        box.getChildren().add(textField);
        setContainer(box);
    }

    @Override public String getValue() {
        String text = textField.getText();
        return (text!=null && text.length()>0 && textField.isTextValid(text)) ? text : null;
    }

    @Override void updateFromValue(String value) {
        textField.setText((value!=null) ? value.toString() : "");
    }
}
