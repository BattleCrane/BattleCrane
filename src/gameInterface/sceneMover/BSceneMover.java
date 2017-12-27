package gameInterface.sceneMover;
import gameInterface.windows.WINDOW;
import main.BGame;
import com.google.inject.Inject;
import javafx.scene.Scene;
public final class BSceneMover {
    @Inject
    private BGame bGame;

    public final void moveToScene(WINDOW window) {
        Scene scene = bGame.getWindowMap().get(window).getScene();
        bGame.getStage().setScene(scene);
    }
}