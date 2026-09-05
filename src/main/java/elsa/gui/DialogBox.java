package elsa.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

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
 * <p>Who spoke is shown by which side of the window the entry sits on: the user
 * on the right, Elsa on the left. A dialog box is therefore made through
 * {@link #getUserDialog(String, Image)} or {@link #getElsaDialog(String, Image)}
 * rather than with {@code new}, so that every entry is turned the right way
 * round and no caller has to remember to do it.
 */
public class DialogBox extends HBox {
    /** How wide and tall the picture is drawn, in pixels. */
    private static final double PICTURE_SIZE = 100.0;

    /** What was said. */
    private final Label text;

    /** The picture of whoever said it. */
    private final ImageView displayPicture;

    /**
     * Creates a dialog box showing a message beside a picture, with the picture
     * on the right.
     *
     * @param message the words to show.
     * @param picture the picture of whoever said them.
     */
    private DialogBox(String message, Image picture) {
        text = new Label(message);
        displayPicture = new ImageView(picture);

        // Without this a long message is drawn as one line running off the side
        // of the window, because a Label does not wrap by default.
        text.setWrapText(true);
        displayPicture.setFitWidth(PICTURE_SIZE);
        displayPicture.setFitHeight(PICTURE_SIZE);
        // An ImageView stretches a picture to fill the box it is given unless
        // told otherwise, so a picture that is not square arrives squashed.
        displayPicture.setPreserveRatio(true);
        this.setAlignment(Pos.TOP_RIGHT);

        this.getChildren().addAll(text, displayPicture);
    }

    /**
     * Returns a dialog box for something the user said, picture on the right.
     *
     * @param message the words the user typed.
     * @param picture the picture shown beside them.
     * @return the entry to add to the conversation.
     */
    public static DialogBox getUserDialog(String message, Image picture) {
        return new DialogBox(message, picture);
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
        dialogBox.flip();
        return dialogBox;
    }

    /**
     * Turns this dialog box round, so the picture is on the left of the words
     * and the whole entry sits against the left edge.
     * Reversing the children is what moves the picture, and changing the
     * alignment is what moves the entry; both are needed, because one decides
     * the order within the row and the other where the row sits in the width
     * available to it.
     */
    private void flip() {
        this.setAlignment(Pos.TOP_LEFT);
        ObservableList<Node> children = FXCollections.observableArrayList(this.getChildren());
        FXCollections.reverse(children);
        this.getChildren().setAll(children);
    }
}
