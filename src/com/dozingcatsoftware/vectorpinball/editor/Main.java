package com.dozingcatsoftware.vectorpinball.editor;

import static com.dozingcatsoftware.vectorpinball.util.Localization.localizedString;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.dozingcatsoftware.vectorpinball.editor.elements.EditableField;
import com.dozingcatsoftware.vectorpinball.editor.elements.EditableFieldElement;
import com.dozingcatsoftware.vectorpinball.model.Field;
import com.dozingcatsoftware.vectorpinball.model.FieldDriver;
import com.dozingcatsoftware.vectorpinball.model.GameMessage;
import com.dozingcatsoftware.vectorpinball.util.JSONUtils;

// Need to edit project as described in
// http://stackoverflow.com/questions/24467931/using-javafx-jdk-1-8-0-05-in-eclipse-luna-does-not-work

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    enum EditorState {
        EDITING,
        SAMPLE_GAME,
    }

    static String WINDOW_TITLE_PREFIX = localizedString("Vector Pinball: ");

    static int WINDOW_WIDTH = 1000;
    static int WINDOW_HEIGHT = 860;
    static int BASE_CANVAS_WIDTH = 560;
    static int BASE_CANVAS_HEIGHT = 810;

    static int TOOLS_COLUMN_WIDTH = 360;
    static int SCRIPT_COLUMN_WIDTH = 450;

    Stage mainStage;
    VBox fieldBox;
    ScrollPane fieldScroller;
    Canvas fieldCanvas;
    ElementInspectorView inspectorView;
    ScoreView scoreView;
    Timer scoreViewTimer;

    ColumnConstraints scriptColumnConstraints;
    ScriptEditorView scriptView;
    Button showScriptButton;

    Field field;
    EditableField editableField;
    FxCanvasRenderer renderer;
    UndoStack undoStack;

    FieldDriver fieldDriver;
    EditorState editorState = EditorState.EDITING;
    Map<String, Object> fieldMap = null;

    FileSystem fileSystem = FileSystems.getDefault();
    Path savedFilePath;

    @Override public void start(Stage primaryStage) {
        this.mainStage = primaryStage;

        editableField = new EditableField();
        editableField.setElementChangedCallback(this::handleElementChangeFromField);
        editableField.setSelectionChangedCallback(this::handleSelectionChange);

        undoStack = new UndoStack();
        undoStack.setEditableField(editableField);

        renderer = new FxCanvasRenderer();
        renderer.setEditableField(editableField);
        renderer.setUndoStack(undoStack);

        inspectorView = new ElementInspectorView();
        inspectorView.setChangeCallback(this::handleElementChangeFromInspector);
        inspectorView.setEditableField(editableField);
        inspectorView.setUndoStack(undoStack);

        GridPane root = new GridPane();

        Insets leftColumnInsets = new Insets(20, 0, 20, 20);

        ColumnConstraints toolsCol = new ColumnConstraints(TOOLS_COLUMN_WIDTH);
        scriptColumnConstraints = new ColumnConstraints(0);
        scriptColumnConstraints.setHgrow(Priority.ALWAYS);
        ColumnConstraints fieldCol = new ColumnConstraints(0, 700, Double.MAX_VALUE);
        fieldCol.setHgrow(Priority.ALWAYS);
        root.getColumnConstraints().addAll(toolsCol, scriptColumnConstraints, fieldCol);

        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints();
        row2.setVgrow(Priority.ALWAYS);
        root.getRowConstraints().addAll(row1, row2);

        VBox topLeft = new VBox(5);
        topLeft.setPadding(leftColumnInsets);
        topLeft.getChildren().add(new ElementPaletteView(this::createElement));

        Region spacer = new Region();
        spacer.setMinHeight(15);
        Label simLabel = new Label(localizedString("Simulation"));
        simLabel.setFont(new Font(16));
        HBox simButtonRow = new HBox(5);
        Button startGameButton = new Button(localizedString("Start Game"));
        startGameButton.setOnAction((event) -> startGame());
        Button endGameButton = new Button(localizedString("Stop Game"));
        endGameButton.setOnAction((event) -> stopGame());
        this.showScriptButton = new Button(localizedString("Show Script"));
        showScriptButton.setOnAction((event) -> showScriptView());
        simButtonRow.getChildren().addAll(startGameButton, endGameButton, showScriptButton);
        topLeft.getChildren().addAll(spacer, simLabel, simButtonRow);

        topLeft.setBackground(new Background(new BackgroundFill(Color.rgb(240, 240, 240), null, null)));
        GridPane.setConstraints(topLeft, 0, 0);

        ScrollPane inspectorScroller = new ScrollPane();
        inspectorScroller.setContent(inspectorView);
        inspectorScroller.setStyle("-fx-background: #bdf;");
        inspectorScroller.setPadding(leftColumnInsets);
        GridPane.setConstraints(inspectorScroller, 0, 1);

        scriptView = new ScriptEditorView();
        scriptView.setEditableField(editableField);
        scriptView.setChangeHandler(this::handleScriptChange);
        GridPane.setConstraints(scriptView, 1, 0, 1, 2);

        fieldBox = new VBox();
        scoreView = new ScoreView();

        fieldScroller = new ScrollPane();
        fieldScroller.setStyle("-fx-background: #222;");
        VBox.setVgrow(fieldScroller, Priority.ALWAYS);

        createCanvas(BASE_CANVAS_WIDTH, BASE_CANVAS_HEIGHT);

        fieldBox.getChildren().addAll(fieldScroller);

        GridPane.setConstraints(fieldBox, 2, 0, 1, 2);

        MenuBar menuBar = buildMenuBar();
        root.getChildren().addAll(menuBar, topLeft, inspectorScroller, scriptView, fieldBox);

        primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.setMinWidth(640);
        primaryStage.setMinHeight(500);
        primaryStage.show();

        loadStarterField();
    }

    void showScoreView() {
        fieldBox.getChildren().add(0, scoreView);
        if (scoreViewTimer == null) {
            scoreViewTimer = new Timer(true); // Set daemon to not stop quitting the app.
            scoreViewTimer.schedule(new TimerTask() {
                @Override public void run() {
                    updateScoreView();
                }
            }, 0, 100);
        }
    }

    void hideScoreView() {
        if (scoreViewTimer != null) {
            scoreViewTimer.cancel();
            scoreViewTimer = null;
        }
        fieldBox.getChildren().remove(scoreView);
    }

    void updateScoreView() {
        Field f = this.field;
        if (f == null) return;
        GameMessage gameMessage = f.getGameMessage();
        String msg = (gameMessage != null) ? gameMessage.text : String.valueOf(f.getGameState().getScore());
        Platform.runLater(() -> scoreView.setMessage(msg));
    }

    void showScriptView() {
        scriptColumnConstraints.setPrefWidth(SCRIPT_COLUMN_WIDTH);
        showScriptButton.setText(localizedString("Hide Script"));
        showScriptButton.setOnAction((event) -> hideScriptView());
    }

    void hideScriptView() {
        scriptColumnConstraints.setPrefWidth(0);
        showScriptButton.setText(localizedString("Show Script"));
        showScriptButton.setOnAction((event) -> showScriptView());
    }

    MenuItem createMenuItem(String label, String shortcutChar, Runnable onAction) {
        MenuItem item = new MenuItem(label);
        if (shortcutChar != null) {
            item.setAccelerator(new KeyCharacterCombination(shortcutChar, KeyCombination.SHORTCUT_DOWN));
        }
        if (onAction != null) {
            item.setOnAction((event) -> onAction.run());
        }
        return item;
    }

    MenuBar buildMenuBar() {
        Menu fileMenu = new Menu("File");

        Menu newFromTemplateMenu = new Menu("New From Template");
        newFromTemplateMenu.getItems().addAll(
                createMenuItem("Table 1", null, () -> loadBuiltInField(1)),
                createMenuItem("Table 2", null, () -> loadBuiltInField(2)),
                createMenuItem("Table 3", null, () -> loadBuiltInField(3))
                );

        fileMenu.getItems().addAll(
                createMenuItem("New Table", "N", () -> loadStarterField()),
                newFromTemplateMenu,
                new SeparatorMenuItem(),
                createMenuItem("Open", "O", this::openFile),
                createMenuItem("Save", "S", this::saveFile)
                );

        Menu editMenu = new Menu("Edit");
        MenuItem undoItem = createMenuItem("Undo", "Z", this::undoEdit);
        MenuItem redoItem = createMenuItem("Redo", null, this::redoEdit);
        redoItem.setAccelerator(new KeyCharacterCombination("Z", KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        editMenu.getItems().addAll(undoItem, redoItem);

        Menu viewMenu = new Menu("View");
        viewMenu.getItems().addAll(
                createMenuItem("Zoom In", "+", this::zoomIn),
                createMenuItem("Zoom Out", "-", this::zoomOut));

        MenuBar mbar = new MenuBar();
        mbar.getMenus().addAll(fileMenu, editMenu, viewMenu);
        mbar.setUseSystemMenuBar(true);
        return mbar;
    }

    void createCanvas(double width, double height) {
        fieldCanvas = new Canvas(width, height);
        fieldCanvas.addEventFilter(MouseEvent.ANY, (e) -> fieldCanvas.requestFocus()); // To handle key events.
        fieldScroller.setContent(fieldCanvas);
        fieldCanvas.setOnMousePressed(this::handleCanvasMousePressed);
        fieldCanvas.setOnMouseReleased(this::handleCanvasMouseReleased);
        fieldCanvas.setOnMouseDragged(this::handleCanvasMouseDragged);
        fieldCanvas.setOnKeyPressed(this::handleCanvasKeyPressed);
    }

    void zoomIn() {
        renderer.zoomIn();
        createCanvas(BASE_CANVAS_WIDTH * renderer.getRelativeScale(), BASE_CANVAS_HEIGHT * renderer.getRelativeScale());
        renderer.setCanvas(fieldCanvas);
        renderer.doDraw();
    }

    void zoomOut() {
        renderer.zoomOut();
        createCanvas(BASE_CANVAS_WIDTH * renderer.getRelativeScale(), BASE_CANVAS_HEIGHT * renderer.getRelativeScale());
        renderer.setCanvas(fieldCanvas);
        renderer.doDraw();
    }

    void loadFieldMap(Map<String, Object> map) {
        fieldMap = map;
        displayForEditing();

        undoStack.clearStack();
        undoStack.pushSnapshot();
        savedFilePath = null;
    }

    void loadBuiltInField(int fieldNum) {
        JarFileFieldReader fieldReader = new JarFileFieldReader();
        loadFieldMap(fieldReader.layoutMapForBuiltInField(fieldNum));
        // TODO: localize
        mainStage.setTitle(WINDOW_TITLE_PREFIX + "Table Template " + fieldNum);
    }

    void loadStarterField() {
        JarFileFieldReader fieldReader = new JarFileFieldReader();
        loadFieldMap(fieldReader.layoutMapForStarterField());
        // TODO: localize
        mainStage.setTitle(WINDOW_TITLE_PREFIX + "New Table");
    }

    void displayForEditing() {
        renderer.setCanvas(fieldCanvas);
        editableField.initFromProperties(fieldMap);
        renderer.doDraw();
        inspectorView.updateInspectorValues();
        scriptView.updateScriptText();
        editorState = EditorState.EDITING;
    }

    void startGame() {
        if (fieldDriver==null) {
            field = new Field();
            field.resetForLevel(editableField.getPropertyMapSnapshot());
            renderer.setField(field);

            fieldDriver = new FieldDriver();
            fieldDriver.setFieldRenderer(renderer);
            fieldDriver.setField(field);
            fieldDriver.start();
            showScoreView();
        }
        field.startGame();
        field.removeDeadBalls();
        field.launchBall();
        editorState = EditorState.SAMPLE_GAME;

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
        hideScoreView();
    }

    void handleCanvasMousePressed(MouseEvent event) {
        switch (editorState) {
        case SAMPLE_GAME:
            if (!field.getGameState().isGameInProgress()) {
                field.startGame();
            }
            field.removeDeadBalls();
            if (field.getBalls().size()==0) field.launchBall();
            field.setAllFlippersEngaged(true);
            // TODO: Launch next ball if needed.
            break;
        case EDITING:
            renderer.handleEditorMouseDown(event);
            break;
        }
    }

    void handleCanvasMouseReleased(MouseEvent event) {
        switch (editorState) {
        case SAMPLE_GAME:
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

    void handleCanvasKeyPressed(KeyEvent event) {
        System.out.println("key pressed: " + event.getCode());
        KeyCode code = event.getCode();
        if (KeyCode.DELETE.equals(code) || KeyCode.BACK_SPACE.equals(code)) {
            // Possibly cheating, but the logic is already there.
            inspectorView.deleteSelectedElements();
        }
        // For up/down/left/right, add methods to Editable*FieldElements,
        // and call event.consume() so it won't scroll.
    }

    void handleSelectionChange() {
        inspectorView.update();
    }

    void handleElementChangeFromField() {
        inspectorView.updateInspectorValues();
    }

    void handleElementChangeFromInspector() {
        renderer.doDraw();
        undoStack.pushSnapshot();
    }

    void handleScriptChange() {
        undoStack.pushSnapshot();
    }

    void createElement(Class<? extends EditableFieldElement> elementClass) {
        if (editableField!=null && fieldDriver==null) {
            EditableFieldElement newElement = editableField.addNewElement(elementClass);
            // Push to undo stack before selecting the new element, so that the previous
            // selection will be restored if we undo.
            undoStack.pushSnapshot();
            editableField.selectElement(newElement);
            renderer.doDraw();
        }
    }

    void undoEdit() {
        if (undoStack.canUndo()) {
            undoStack.undo();
            renderer.doDraw();
            inspectorView.updateInspectorValues();
        }
    }

    void redoEdit() {
        if (undoStack.canRedo()) {
            undoStack.redo();
            renderer.doDraw();
            inspectorView.updateInspectorValues();
        }
    }

    void openFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Table");
        File selectedFile = chooser.showOpenDialog(mainStage);
        if (selectedFile != null) {
            Path openFilePath = selectedFile.toPath();
            // TODO: Check for implausibly large files.
            byte[] contents = null;
            Map<String, Object> map = null;
            try {
                contents = Files.readAllBytes(openFilePath);
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
            try {
                map = JSONUtils.mapFromJSONString(new String(contents, StandardCharsets.UTF_8));
            }
            catch (JSONUtils.ParsingException ex) {
                ex.printStackTrace();
                return;
            }
            loadFieldMap(map);
            savedFilePath = openFilePath;
            mainStage.setTitle(WINDOW_TITLE_PREFIX + savedFilePath.toString());
        }
    }

    void saveFile() {
        if (savedFilePath != null) {
            writeToSavedFilePath();
        }
        else {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save Table");
            File selectedFile = chooser.showSaveDialog(mainStage);
            if (selectedFile != null) {
                savedFilePath = selectedFile.toPath();
                writeToSavedFilePath();
                mainStage.setTitle("Vector Pinball: " + savedFilePath.toString());
            }
        }
    }

    void writeToSavedFilePath() {
        String fileText = JSONUtils.jsonStringFromObject(editableField.getPropertyMapSnapshot());
        try {
            Files.write(savedFilePath, fileText.getBytes(StandardCharsets.UTF_8));
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
