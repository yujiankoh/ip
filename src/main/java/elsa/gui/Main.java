package elsa.gui;

import java.util.Objects;

import elsa.Elsa;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
 * <p>The window is a {@link BorderPane}: the conversation fills the centre and
 * the place to type sits along the bottom. A BorderPane gives its centre
 * whatever room the other edges leave, which is what lets the window be resized
 * without any of the sizes here being worked out again. Its centre is a
 * {@link ScrollPane} over a {@link VBox}, a column the conversation is added to
 * one {@link DialogBox} at a time; the column grows as the conversation does and
 * the scroll pane scrolls it, which is why the two are separate controls.
 *
 * <p>Colours, spacing and corners are in {@code /css/elsa.css} rather than here,
 * so that changing how the window looks does not mean changing what it does.
 */
public class Main extends Application {
    /** The size the window opens at, in pixels. */
    private static final double WINDOW_WIDTH = 400.0;
    private static final double WINDOW_HEIGHT = 600.0;

    /**
     * The smallest the window may be dragged to, in pixels.
     * Below this the place to type is squeezed out and the conversation is too
     * narrow to read, so the window is stopped rather than allowed to become
     * useless.
     */
    private static final double MIN_WIDTH = 360.0;
    private static final double MIN_HEIGHT = 480.0;

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
        BorderPane root = new BorderPane();
        root.setCenter(buildConversation());
        root.setBottom(buildInputRow());

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(stylesheet());

        stage.setScene(scene);
        stage.setTitle("Elsa");
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);

        handleEvents();

        // The chatbot speaks first, as it does in the terminal. This is also what
        // reads the saved tasks, so it has to happen before any command runs.
        addToConversation(DialogBox.getElsaDialog(elsa.startSession(), elsaImage));

        stage.show();

        // Asked for after the window is shown, because a control cannot take the
        // focus until it is on screen. runLater puts the request behind the work
        // JavaFX is already doing to show the window.
        Platform.runLater(userInput::requestFocus);
    }

    /**
     * Returns the scrollable column the conversation is added to.
     *
     * @return the scroll pane to put in the middle of the window.
     */
    private ScrollPane buildConversation() {
        dialogContainer = new VBox();
        dialogContainer.setId("dialog-container");

        scrollPane = new ScrollPane(dialogContainer);
        scrollPane.setId("scroll-pane");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        // Always shown, rather than appearing once the conversation outgrows the
        // window. A bar that comes and goes moves the text under it as it does,
        // and leaves the user no sign that there is anywhere to scroll to.
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        // Makes the column as wide as the scroll pane, so a dialog box knows how
        // much width it has and can be pushed to the left or the right of it.
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    /**
     * Returns the row along the bottom where the user types.
     *
     * @return the row to put at the bottom of the window.
     */
    private HBox buildInputRow() {
        userInput = new TextField();
        userInput.setId("user-input");
        userInput.setPromptText("Type a command, or \"help\"");

        sendButton = new Button("Send");
        sendButton.setId("send-button");

        HBox inputRow = new HBox(userInput, sendButton);
        inputRow.setId("input-row");
        // The button keeps the width its text needs and the text field takes
        // everything else, so widening the window widens the field alone.
        HBox.setHgrow(userInput, Priority.ALWAYS);
        return inputRow;
    }

    /**
     * Returns where the window's stylesheet is, ready for JavaFX to load.
     *
     * @return the stylesheet's location.
     * @throws NullPointerException if the stylesheet is missing from the build,
     *     which is worth failing loudly for: JavaFX would otherwise draw an
     *     unstyled window and say nothing about why.
     */
    private String stylesheet() {
        return Objects.requireNonNull(getClass().getResource("/css/elsa.css"),
                "/css/elsa.css is missing from the build").toExternalForm();
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
     * A line with nothing in it is ignored, so that pressing Enter on an empty
     * field does not put a blank entry in the conversation.
     */
    private void handleUserInput() {
        String userText = userInput.getText();
        if (userText.isBlank()) {
            return;
        }

        addToConversation(DialogBox.getUserDialog(userText, userImage));
        addToConversation(DialogBox.getElsaDialog(elsa.getResponse(userText), elsaImage));
        userInput.clear();

        if (elsa.isExiting()) {
            closeAfterFarewell();
        }
    }

    /**
     * Adds one entry to the conversation.
     *
     * @param dialogBox the entry to add.
     */
    private void addToConversation(DialogBox dialogBox) {
        dialogContainer.getChildren().add(dialogBox);
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
}
