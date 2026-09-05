package elsa.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
 * <p>Who spoke is shown two ways: which side of the window the entry sits on,
 * and how the bubble is coloured. A dialog box is therefore made through
 * {@link #getUserDialog(String, Image)} or {@link #getElsaDialog(String, Image)}
 * rather than with {@code new}, so that every entry is turned the right way
 * round and coloured to match, and no caller has to remember to do it.
 *
 * <p>The colours themselves are in {@code /css/elsa.css}. This class only says
 * which style class an entry belongs to.
 */
public class DialogBox extends HBox {
    /** How wide and tall the picture is drawn, in pixels. */
    private static final double PICTURE_SIZE = 100.0;

    /**
     * How much of the width available a bubble may take up.
     * Without a limit a long message stretches the whole way across a widened
     * window, giving lines too long to read comfortably and losing the sense
     * that the two speakers are on opposite sides.
     */
    private static final double MAX_BUBBLE_SHARE = 0.72;

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

        this.getStyleClass().add("dialog-box");
        text.getStyleClass().add("dialog-label");
        displayPicture.getStyleClass().add("avatar");

        // Without this a long message is drawn as one line running off the side
        // of the window, because a Label does not wrap by default.
        text.setWrapText(true);
        // Bound rather than set, so the limit follows the window as it is
        // resized instead of being worked out once when the entry is made.
        text.maxWidthProperty().bind(this.widthProperty().multiply(MAX_BUBBLE_SHARE));

        displayPicture.setFitWidth(PICTURE_SIZE);
        displayPicture.setFitHeight(PICTURE_SIZE);
        // Cropped to a circle, which is why the picture is not asked to keep its
        // proportions: inside a round crop a picture very slightly wider than it
        // is tall reads no differently, and both speakers get the same shape
        // whatever they hand in.
        displayPicture.setClip(new Circle(PICTURE_SIZE / 2, PICTURE_SIZE / 2, PICTURE_SIZE / 2));

        // Sat against the middle of the picture rather than its top. A short
        // message pinned to the top leaves most of the picture's height beside
        // it as a gap, which reads as though the two were not part of the same
        // entry.
        this.setAlignment(Pos.CENTER_RIGHT);
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
        DialogBox dialogBox = new DialogBox(message, picture);
        dialogBox.text.getStyleClass().add("user-bubble");
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
        dialogBox.text.getStyleClass().add("elsa-bubble");
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
