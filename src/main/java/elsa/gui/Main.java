package elsa.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
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
 * <p>The window is built from three pieces that nest inside one another: a
 * {@link Label} holds the text, a {@link Scene} holds the label and is
 * everything drawn inside the window, and the {@link Stage} is the window
 * itself, frame and all.
 */
public class Main extends Application {
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
        Label helloWorld = new Label("Hello World!");
        Scene scene = new Scene(helloWorld);

        stage.setScene(scene);
        stage.show();
    }
}
