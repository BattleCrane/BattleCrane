package gameInterface.windows;

import controllers.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import org.jetbrains.annotations.Contract;

import java.io.IOException;


public final class BWindow {
    private final double SCENE_WIDTH = 1280.0;
    private final double SCENE_HEIGHT = 720.0;


    private Scene scene;
    private Controller controller;

    public BWindow(FXMLLoader fxmlLoader) {
        try {
            scene = new Scene(fxmlLoader.load());
            controller = fxmlLoader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Contract(pure = true)
    public Scene getScene() {
        return scene;
    }

    @Contract(pure = true)
    public Controller getController() {
        return controller;
    }
}
