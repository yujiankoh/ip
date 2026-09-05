package elsa.gui;

import elsa.Elsa;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * The JavaFX application that shows the chatbot's window.
 *
 * <p>A JavaFX application is not written as a method that draws a window and
 * returns. JavaFX itself creates this object, starts a thread to draw on, and
 * then calls {@link #start(Stage)} once the toolkit is ready. Everything that
 * builds the window therefore goes in that method rather than in a constructor
 * or in {@code main}.
 *
 * <p>The window is built from controls nested inside one another. An
 * {@link AnchorPane} is the root, and holds three things pinned to its edges: a
 * {@link ScrollPane} filling the top, and a {@link TextField} and {@link Button}
 * along the bottom. The scroll pane's content is a {@link VBox}, a column the
 * conversation is added to a {@link DialogBox} at a time. The column grows as
 * the conversation does and the scroll pane scrolls it, which is why the two are
 * separate controls.
 */
public class Main extends Application {
    /** The size of the window, in pixels, which the user cannot change. */
    private static final double WINDOW_WIDTH = 400.0;
    private static final double WINDOW_HEIGHT = 600.0;

    /** The size of the conversation area, leaving room for the input row below. */
    private static final double SCROLL_PANE_WIDTH = 385.0;
    private static final double SCROLL_PANE_HEIGHT = 535.0;

    /** How the bottom row is divided between the text field and the button. */
    private static final double INPUT_WIDTH = 325.0;
    private static final double SEND_BUTTON_WIDTH = 55.0;

    /** How far each control sits from the edge it is pinned to, in pixels. */
    private static final double EDGE_GAP = 1.0;

    /**
     * How long the farewell stays on screen before the window closes.
     * Closing the moment the reply is added would take the farewell away before
     * it could be read.
     */
    private static final Duration FAREWELL_PAUSE = Duration.seconds(1.5);

    /** Scrolls the conversation once it is taller than the window. */
    private ScrollPane scrollPane;

    /** The conversation itself, one dialog box per line spoken. */
    private VBox dialogContainer;

    /** Where the user types. */
    private TextField userInput;

    /** What the user presses to send what they typed. */
    private Button sendButton;

    /** The picture shown beside what the user says. */
    private final Image userImage = new Image(this.getClass().getResourceAsStream("/images/OlafPortrait.png"));

    /** The picture shown beside what Elsa says. */
    private final Image elsaImage = new Image(this.getClass().getResourceAsStream("/images/ElsaPortrait.png"));

    /** The chatbot the window is a face for. */
    private final Elsa elsa = new Elsa();

    /**
     * Creates the application object.
     * JavaFX makes this object itself, by reflection, so it needs a constructor
     * that takes no arguments. Java supplies one to any class that declares
     * none, but declaring it says out loud that JavaFX depends on it and that
     * removing it would break the application at run time rather than at
     * compile time.
     */
    public Main() {
    }

    @Override
    public void start(Stage stage) {
        scrollPane = new ScrollPane();
        dialogContainer = new VBox();
        scrollPane.setContent(dialogContainer);

        userInput = new TextField();
        sendButton = new Button("Send");

        AnchorPane mainLayout = new AnchorPane();
        mainLayout.getChildren().addAll(scrollPane, userInput, sendButton);

        Scene scene = new Scene(mainLayout);
        stage.setScene(scene);

        styleWindow(stage, mainLayout);
        anchorControls();

        handleEvents();

        // The chatbot speaks first, as it does in the terminal. This is also what
        // reads the saved tasks, so it has to happen before any command runs.
        dialogContainer.getChildren().add(
                DialogBox.getElsaDialog(elsa.startSession(), elsaImage));

        stage.show();
    }

    /**
     * Says what the window should do when the user acts.
     * JavaFX does not ask the program what happened; the program says in advance
     * which method answers which event, and JavaFX calls it when the event
     * arrives. Both the button and the Enter key are wired to the same method,
     * so that either way of sending a line does the same thing.
     */
    private void handleEvents() {
        sendButton.setOnMouseClicked(event -> handleUserInput());
        userInput.setOnAction(event -> handleUserInput());

        // The conversation is taller every time a line is added, and the scroll
        // pane does not follow on its own, so each change in height scrolls it
        // back to the bottom. 1.0 is the bottom.
        dialogContainer.heightProperty().addListener(observable -> scrollPane.setVvalue(1.0));
    }

    /**
     * Shows what the user typed, and what Elsa says back, then empties the text
     * field ready for the next line.
     */
    private void handleUserInput() {
        String userText = userInput.getText();
        String elsaText = elsa.getResponse(userText);

        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(userText, userImage),
                DialogBox.getElsaDialog(elsaText, elsaImage));

        userInput.clear();

        if (elsa.isExiting()) {
            closeAfterFarewell();
        }
    }

    /**
     * Closes the window once the farewell has been on screen long enough to read.
     * The wait cannot be a sleep: that would stop the thread JavaFX draws on, so
     * the farewell would never appear and the window would freeze instead. A
     * PauseTransition instead asks JavaFX to call back later, leaving it free to
     * draw in the meantime.
     */
    private void closeAfterFarewell() {
        userInput.setDisable(true);
        sendButton.setDisable(true);

        PauseTransition pause = new PauseTransition(FAREWELL_PAUSE);
        pause.setOnFinished(event -> Platform.exit());
        pause.play();
    }

    /**
     * Sets the size of the window and of the controls inside it.
     *
     * @param stage the window being shown.
     * @param mainLayout the pane holding every control in that window.
     */
    private void styleWindow(Stage stage, AnchorPane mainLayout) {
        stage.setTitle("Elsa");
        stage.setResizable(false);
        stage.setMinHeight(WINDOW_HEIGHT);
        stage.setMinWidth(WINDOW_WIDTH);

        mainLayout.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        scrollPane.setPrefSize(SCROLL_PANE_WIDTH, SCROLL_PANE_HEIGHT);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        // 1.0 is the bottom, so the newest line is the one on screen.
        scrollPane.setVvalue(1.0);
        scrollPane.setFitToWidth(true);

        // The column is as tall as the dialog boxes in it, rather than a fixed
        // height, so it grows as the conversation does.
        dialogContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);

        userInput.setPrefWidth(INPUT_WIDTH);
        sendButton.setPrefWidth(SEND_BUTTON_WIDTH);
    }

    /**
     * Pins each control to an edge of the window.
     * An AnchorPane does not lay its children out in rows or columns; each child
     * stays the stated distance from the edges it is anchored to. The scroll pane
     * is pinned to the top, and the text field and button to the bottom corners,
     * which is what keeps the input row below the conversation.
     */
    private void anchorControls() {
        AnchorPane.setTopAnchor(scrollPane, EDGE_GAP);
        AnchorPane.setBottomAnchor(sendButton, EDGE_GAP);
        AnchorPane.setRightAnchor(sendButton, EDGE_GAP);
        AnchorPane.setLeftAnchor(userInput, EDGE_GAP);
        AnchorPane.setBottomAnchor(userInput, EDGE_GAP);
    }
}
