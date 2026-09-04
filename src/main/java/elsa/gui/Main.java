package elsa.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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

        // Two dialog boxes, so that the layout can be seen to work, and both
        // pictures seen to load, before part three gives the window anything
        // real to say.
        dialogContainer.getChildren().addAll(
                new DialogBox("Hello!", userImage),
                new DialogBox("Do you want to build a snowman?", elsaImage));

        stage.show();
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
