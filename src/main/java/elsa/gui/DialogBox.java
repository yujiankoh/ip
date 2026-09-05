package elsa.gui;

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;

/**
 * One entry in the conversation: what was said, beside the picture of whoever
 * said it.
 *
 * <p>This is a control the project defines rather than one JavaFX supplies.
 * Extending {@link HBox} means a dialog box is itself a layout that arranges its
 * children in a row, so it can be dropped straight into the conversation the way
 * a Label or a Button could, and the container holding it needs to know nothing
 * about what is inside.
 *
 * <p>What it contains is described in {@code /view/DialogBox.fxml} rather than
 * built here. That file uses the {@code fx:root} construct, which is what suits
 * a control made over and over: this object hands itself to the loader as both
 * the root to fill in and the controller to fill in, so each dialog box gets its
 * own copy of the children the file describes.
 *
 * <p>Who spoke is shown two ways: which side of the window the entry sits on,
 * and how the bubble is coloured. A dialog box is therefore made through
 * {@link #getUserDialog(String, Image)} or {@link #getElsaDialog(String, Image)}
 * rather than with {@code new}, so that every entry is turned the right way
 * round and coloured to match, and no caller has to remember to do it.
 *
 * <p>The colours themselves are in {@code /css/elsa.css}. This class and the
 * FXML only say which style class an entry belongs to.
 */
public class DialogBox extends HBox {
    /** How wide and tall the picture is drawn, in pixels. Matches the FXML. */
    private static final double PICTURE_SIZE = 100.0;

    /**
     * How much of the width available a bubble may take up.
     * Without a limit a long message stretches the whole way across a widened
     * window, giving lines too long to read comfortably and losing the sense
     * that the two speakers are on opposite sides.
     */
    private static final double MAX_BUBBLE_SHARE = 0.72;

    /** What was said. Filled in by the loader from the matching fx:id. */
    @FXML
    private Label dialog;

    /** The picture of whoever said it. Filled in by the loader likewise. */
    @FXML
    private ImageView displayPicture;

    /**
     * Creates a dialog box showing a message beside a picture, with the picture
     * on the right.
     *
     * @param message the words to show.
     * @param picture the picture of whoever said them.
     */
    private DialogBox(String message, Image picture) {
        loadFxml();

        dialog.setText(message);
        displayPicture.setImage(picture);

        // The two things below cannot be written in the FXML, which can only set
        // a property to a value, not tie one property to another or hand over an
        // object it would have to build itself.

        // Bound rather than set, so the limit follows the window as it is
        // resized instead of being worked out once when the entry is made.
        dialog.maxWidthProperty().bind(this.widthProperty().multiply(MAX_BUBBLE_SHARE));

        // Cropped to a circle, which is why the FXML does not ask the picture to
        // keep its proportions: inside a round crop a picture very slightly
        // wider than it is tall reads no differently, and both speakers get the
        // same shape whatever they hand in.
        displayPicture.setClip(new Circle(PICTURE_SIZE / 2, PICTURE_SIZE / 2, PICTURE_SIZE / 2));
    }

    /**
     * Fills this dialog box in from the layout described in the FXML.
     * Setting the root and the controller to this object is what the
     * {@code fx:root} construct expects: the file describes children to add to
     * something that already exists, rather than a new object to build.
     */
    private void loadFxml() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/DialogBox.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            // Not recoverable and not the user's doing: the layout is part of the
            // program, so a copy that cannot be read means the build is wrong.
            // Thrown rather than printed, so it cannot be missed.
            throw new IllegalStateException("/view/DialogBox.fxml could not be read", e);
        }
    }

    /**
     * Returns a dialog box for something the user said, picture on the right.
     *
     * @param message the words the user typed.
     * @param picture the picture shown beside them.
     * @return the entry to add to the conversation.
     */
    public static DialogBox getUserDialog(String message, Image picture) {
        DialogBox dialogBox = new DialogBox(message, picture);
        dialogBox.dialog.getStyleClass().add("user-bubble");
        return dialogBox;
    }

    /**
     * Returns a dialog box for something Elsa said, picture on the left.
     *
     * @param message the words Elsa replied with.
     * @param picture the picture shown beside them.
     * @return the entry to add to the conversation.
     */
    public static DialogBox getElsaDialog(String message, Image picture) {
        DialogBox dialogBox = new DialogBox(message, picture);
        dialogBox.dialog.getStyleClass().add("elsa-bubble");
        dialogBox.flip();
        return dialogBox;
    }

    /**
     * Turns this dialog box round, so the picture is on the left of the words
     * and the whole entry sits against the left edge, still level with the
     * middle of the picture.
     * Reversing the children is what moves the picture, and changing the
     * alignment is what moves the entry; both are needed, because one decides
     * the order within the row and the other where the row sits in the width
     * available to it.
     */
    private void flip() {
        this.setAlignment(Pos.CENTER_LEFT);
        ObservableList<Node> children = FXCollections.observableArrayList(this.getChildren());
        FXCollections.reverse(children);
        this.getChildren().setAll(children);
    }
}
