package bInterface.bSceneMover;
import main.BGame;
import com.google.inject.Inject;
import javafx.scene.Scene;
public class BSceneMover {
    @Inject
    private BGame bGame;

    public void moveToScene(String nameOfWindow) {
        Scene scene = bGame.getMapOfWindows().get(nameOfWindow).getScene();
        bGame.getStage().setScene(scene);
    }
}