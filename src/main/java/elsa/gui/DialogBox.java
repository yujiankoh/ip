package elsa.gui;

import javafx.geometry.Pos;
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
 */
public class DialogBox extends HBox {
    /** How wide and tall the picture is drawn, in pixels. */
    private static final double PICTURE_SIZE = 100.0;

    /** What was said. */
    private final Label text;

    /** The picture of whoever said it. */
    private final ImageView displayPicture;

    /**
     * Creates a dialog box showing a message beside a picture.
     *
     * @param message the words to show.
     * @param picture the picture of whoever said them.
     */
    public DialogBox(String message, Image picture) {
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
}
