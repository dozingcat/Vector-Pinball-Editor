package com.dozingcatsoftware.vectorpinball.editor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;
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
import com.dozingcatsoftware.vectorpinball.editor.elements.EditableFieldElement;
import com.dozingcatsoftware.vectorpinball.model.Field;
import com.dozingcatsoftware.vectorpinball.model.FieldDriver;
import com.dozingcatsoftware.vectorpinball.util.CollectionUtils;

// Need to edit project as described in
// http://stackoverflow.com/questions/24467931/using-javafx-jdk-1-8-0-05-in-eclipse-luna-does-not-work

public class Main extends Application {
    public static void main(String[] args) {
        //test();
        launch(args);
    }

    static void test() {
        Map<String, Object> m = new HashMap<>();
        m.put("foo", Arrays.asList(1, 2, 3));
        System.out.println(m);
        Map<String, Object> m2 = CollectionUtils.mutableDeepCopyOfMap(m);
        ((List)m2.get("foo")).add(4);
        System.out.println(m);
        System.out.println(m2);
        System.exit(0);
    }

    enum EditorState {
        EDITING,
        SAMPLE_BALL,
        SAMPLE_GAME,
    }

    ScrollPane fieldScroller;
    Canvas fieldCanvas;
    PaletteView palette;
    ElementInspectorView inspector;

    Field field;
    EditableField editableField;
    FxCanvasRenderer renderer;
    UndoStack undoStack;

    FieldDriver fieldDriver;
    EditorState editorState = EditorState.EDITING;
    Map<String, Object> fieldMap = null;

    @Override public void start(Stage primaryStage) {
        editableField = new EditableField();
        editableField.setElementChangedCallback(this::handleElementChangeFromField);
        editableField.setSelectionChangedCallback(this::handleSelectionChange);

        undoStack = new UndoStack();
        undoStack.setEditableField(editableField);

        renderer = new FxCanvasRenderer();
        renderer.setEditableField(editableField);
        renderer.setUndoStack(undoStack);

        palette = new PaletteView(this::createElement);

        inspector = new ElementInspectorView();
        inspector.setChangeCallback(this::handleElementChangeFromInspector);
        inspector.setEditableField(editableField);
        inspector.setUndoStack(undoStack);

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

        palette.setBackground(new Background(new BackgroundFill(Color.RED, null, null)));
        GridPane.setConstraints(palette, 0, 0);

        inspector.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, null, null)));
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

        Button undoButton = new Button("Undo");
        undoButton.setOnAction((event) -> undoEdit());
        fieldControls.getChildren().add(undoButton);

        Button redoButton = new Button("Redo");
        redoButton.setOnAction((event) -> redoEdit());
        fieldControls.getChildren().add(redoButton);


        fieldScroller = new ScrollPane();
        fieldScroller.setStyle("-fx-background: black;");
        VBox.setVgrow(fieldScroller, Priority.ALWAYS);

        createCanvas(700, 1000);

        fieldBox.getChildren().addAll(fieldControls, fieldScroller);

        GridPane.setConstraints(fieldBox, 1, 0, 1, 2);

        MenuBar menuBar = buildMenuBar();
        root.getChildren().addAll(menuBar, palette, inspector, fieldBox);

        primaryStage.setScene(new Scene(root, width, height));
        primaryStage.show();

        loadBuiltInLevel(1);
    }

    MenuBar buildMenuBar() {
        Menu fileMenu = new Menu("File");
        MenuItem newItem = new MenuItem("New Table");
        newItem.setAccelerator(new KeyCharacterCombination("N", KeyCombination.SHORTCUT_DOWN));

        Menu newFromTemplateItem = new Menu("New From Template");
        MenuItem newFromTable1Item = new MenuItem("Table 1");
        newFromTable1Item.setOnAction((event) -> loadBuiltInLevel(1));
        MenuItem newFromTable2Item = new MenuItem("Table 2");
        newFromTable2Item.setOnAction((event) -> loadBuiltInLevel(2));
        MenuItem newFromTable3Item = new MenuItem("Table 3");
        newFromTable3Item.setOnAction((event) -> loadBuiltInLevel(3));
        newFromTemplateItem.getItems().addAll(newFromTable1Item, newFromTable2Item, newFromTable3Item);

        MenuItem openItem = new MenuItem("Open");
        openItem.setAccelerator(new KeyCharacterCombination("O", KeyCombination.SHORTCUT_DOWN));
        MenuItem saveItem = new MenuItem("Save");
        saveItem.setAccelerator(new KeyCharacterCombination("S", KeyCombination.SHORTCUT_DOWN));
        fileMenu.getItems().addAll(newItem, newFromTemplateItem, openItem, new SeparatorMenuItem(), saveItem);

        MenuBar mbar = new MenuBar();
        mbar.getMenus().addAll(fileMenu);
        mbar.setUseSystemMenuBar(true);
        System.out.println(mbar.isUseSystemMenuBar());
        return mbar;
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

        undoStack.clearStack();
        undoStack.pushSnapshot();
    }

    void displayForEditing() {
        renderer.setCanvas(fieldCanvas);
        editableField.initFromProperties(fieldMap);
        renderer.doDraw();
        editorState = EditorState.EDITING;
    }

    void launchSingleBall() {
        if (fieldDriver==null) {
            field = new Field();
            field.resetForLevel(editableField.getPropertyMapSnapshot());
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

        renderer.setEditableField(editableField);
        renderer.doDraw();
        editorState = EditorState.EDITING;
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

    void handleSelectionChange() {
        inspector.update();
    }

    void handleElementChangeFromField() {
        inspector.updateInspectorValues();
    }

    void handleElementChangeFromInspector() {
        renderer.doDraw();
        undoStack.pushSnapshot();
    }

    void createElement(Class<? extends EditableFieldElement> elementClass) {
        if (editableField!=null && fieldDriver==null) {
            EditableFieldElement newElement = editableField.addNewElement(elementClass);
            editableField.selectElement(newElement);
            renderer.doDraw();
            undoStack.pushSnapshot();
        }
    }

    void undoEdit() {
        if (undoStack.canUndo()) {
            undoStack.undo();
            renderer.doDraw();
        }
    }

    void redoEdit() {
        if (undoStack.canRedo()) {
            undoStack.redo();
            renderer.doDraw();
        }
    }
}
