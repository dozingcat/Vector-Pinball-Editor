package com.dozingcatsoftware.vectorpinball.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableField;
import com.dozingcatsoftware.vectorpinball.elements.FieldElement;
import com.dozingcatsoftware.vectorpinball.model.Field;
import com.dozingcatsoftware.vectorpinball.model.FieldDriver;

// Need to edit project as described in
// http://stackoverflow.com/questions/24467931/using-javafx-jdk-1-8-0-05-in-eclipse-luna-does-not-work

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    enum EditorState {
        EDITING,
        SAMPLE_BALL,
        SAMPLE_GAME,
    }

    ScrollPane fieldScroller;
    Canvas fieldCanvas;
    Field field;
    FxCanvasRenderer renderer;
    FieldDriver fieldDriver;
    EditorState editorState = EditorState.EDITING;
    Map<String, Object> fieldMap = null;

    @Override public void start(Stage primaryStage) {
        /*
         * primaryStage.setTitle("Hello World!"); Button btn = new Button();
         * btn.setText("Say 'Hello World'"); btn.setOnAction((event) ->
         * System.out.println("Hello Lambda!"));
         *
         * int width = 1000; int height = 1000;
         *
         * Canvas canvas = new Canvas(width, height);
         * drawMandelbrot(canvas.getGraphicsContext2D(), width, height);
         *
         *
         * StackPane root = new StackPane(); root.getChildren().add(canvas);
         * //root.getChildren().add(btn);
         */
        int width = 1100;
        int height = 1100;

        GridPane root = new GridPane();

        ColumnConstraints col1 = new ColumnConstraints(300);
        ColumnConstraints col2 = new ColumnConstraints(0, 700, Double.MAX_VALUE);
        col2.setHgrow(Priority.ALWAYS);
        root.getColumnConstraints().addAll(col1, col2);

        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(50);
        RowConstraints row2 = new RowConstraints();
        row2.setPercentHeight(50);
        root.getRowConstraints().addAll(row1, row2);

        VBox palette = new VBox();
        palette.setBackground(new Background(new BackgroundFill(Color.RED, null, null)));
        GridPane.setConstraints(palette, 0, 0);

        VBox inspector = new VBox();
        inspector.setBackground(new Background(new BackgroundFill(Color.BLUE, null, null)));
        GridPane.setConstraints(inspector, 0, 1);

        VBox fieldBox = new VBox();
        HBox fieldControls = new HBox(10);
        fieldControls.setPrefHeight(60);
        fieldControls.setBackground(new Background(new BackgroundFill(Color.GREEN, null, null)));

        Button launchBallButton = new Button("Launch Ball");
        launchBallButton.setOnAction((event) -> launchSingleBall());
        fieldControls.getChildren().add(launchBallButton);

        Button endGameButton = new Button("Stop Game");
        endGameButton.setOnAction((event) -> stopGame());
        fieldControls.getChildren().add(endGameButton);


        fieldScroller = new ScrollPane();
        fieldScroller.setStyle("-fx-background: black;");
        VBox.setVgrow(fieldScroller, Priority.ALWAYS);

        createCanvas(700, 1000);

        fieldBox.getChildren().addAll(fieldControls, fieldScroller);

        GridPane.setConstraints(fieldBox, 1, 0, 1, 2);

        root.getChildren().addAll(palette, inspector, fieldBox);

        primaryStage.setScene(new Scene(root, width, height));
        primaryStage.show();

        loadBuiltInLevel(3);

    }

    void createCanvas(int width, int height) {
        fieldCanvas = new Canvas(width, height);
        fieldScroller.setContent(fieldCanvas);
        fieldCanvas.setOnMousePressed(this::handleCanvasMousePressed);
        fieldCanvas.setOnMouseReleased(this::handleCanvasMouseReleased);
        fieldCanvas.setOnMouseDragged(this::handleCanvasMouseDragged);
    }

    void loadBuiltInLevel(int level) {
        System.out.println("Reading table");
        JarFileFieldReader fieldReader = new JarFileFieldReader();
        fieldMap = fieldReader.layoutMapForLevel(level);
        displayForEditing();
    }

    void displayForEditing() {
        renderer = new FxCanvasRenderer();
        renderer.setCanvas(fieldCanvas);

        EditableField editableField = EditableField.createFromPropertyMap(fieldMap);
        renderer.setEditableField(editableField);
        /*
        field = new Field();
        field.resetForLevel(fieldMap);
        renderer.setField(field);
        */
        renderer.doDraw();
        editorState = EditorState.EDITING;
    }

    void launchSingleBall() {
        if (fieldDriver==null) {
            regenerateFieldMap();
            field = new Field();
            field.resetForLevel(fieldMap);
            renderer.setField(field);

            fieldDriver = new FieldDriver();
            fieldDriver.setFieldRenderer(renderer);
            fieldDriver.setField(field);
            fieldDriver.start();
        }
        field.getDelegate().gameStarted(field);
        field.launchBall();
        editorState = EditorState.SAMPLE_BALL;

        // Start polling every second to detect lost ball?
    }

    void stopGame() {
        if (fieldDriver != null) {
            fieldDriver.stop();
        }
        fieldDriver = null;
        displayForEditing();
    }

    void regenerateFieldMap() {
        List<Map<String, Object>> newElementMaps = new ArrayList<>();
        for (FieldElement elem : field.getFieldElements()) {
            newElementMaps.add(elem.getPropertyMap());
        }
        Map<String, Object> newFieldMap = new HashMap<String, Object>(fieldMap);
        newFieldMap.put("elements", newElementMaps);
        fieldMap = newFieldMap;
    }

    void handleCanvasMousePressed(MouseEvent event) {
        switch (editorState) {
            case SAMPLE_GAME:
            case SAMPLE_BALL:
                field.setAllFlippersEngaged(true);
                break;
            case EDITING:
                renderer.handleEditorMouseDown(event);
                break;
        }
    }

    void handleCanvasMouseReleased(MouseEvent event) {
        switch (editorState) {
            case SAMPLE_GAME:
            case SAMPLE_BALL:
                field.setAllFlippersEngaged(false);
                break;
            case EDITING:
                renderer.handleEditorMouseUp(event);
                break;
        }
    }

    void handleCanvasMouseDragged(MouseEvent event) {
        switch (editorState) {
            case EDITING:
                renderer.handleEditorMouseDrag(event);
                break;
            default:
                break;
        }
    }
}
