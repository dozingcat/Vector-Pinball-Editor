package com.dozingcatsoftware.vectorpinball.editor;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class ScoreView extends HBox {
    
    Label messageLabel;

    public ScoreView() {
        setMinHeight(40);
        setPrefHeight(40);
        setPrefWidth(200);
        setAlignment(Pos.CENTER);
        setBackground(new Background(new BackgroundFill(Color.rgb(32, 32, 32), null, null)));
        
        messageLabel = new Label("12345");
        messageLabel.setFont(new Font(24));
//        messageLabel.setPrefWidth(300);
//        messageLabel.setMaxWidth(300);
        messageLabel.setTextFill(Color.YELLOW);
        messageLabel.setAlignment(Pos.CENTER);
        HBox.setHgrow(messageLabel, Priority.ALWAYS);
        this.getChildren().add(messageLabel);
    }
    
    public void setMessage(String message) {
        messageLabel.setText(message);
    }
}
