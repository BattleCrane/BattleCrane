package main;

import gameInterface.sceneMover.BSceneMover;
import gameInterface.windows.BWindow;
import gameInterface.windows.WINDOW;
import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jetbrains.annotations.Contract;

import java.net.URL;
import java.util.EnumMap;
import java.util.function.Function;

/**
 * Класс BGame является стартом приложения.
 * У него существует injector, который привязывает классы к переменным.
 * В реализации метода start, stage ведет себя как аргумент,
 * Реальный объект на входе Stage unused игнорируется.
 */

public final class BGame extends Application {
    private final String ICON_PATH = "file:src\\Resources\\Icon.png";
    private final String TITLE = "BattleCrane";

    private final Injector injector = Guice.createInjector(new BGameModule(this));

    private final Stage stage = new Stage();

    private final EnumMap<WINDOW, BWindow> windowMap;

    {
        final Function<URL, FXMLLoader> makeFXMLLoader = url -> {
            final FXMLLoader result = new FXMLLoader(url);
            result.setControllerFactory(injector::getInstance);
            return result;
        };

        windowMap = new EnumMap<WINDOW, BWindow>(WINDOW.class){{
            put(WINDOW.MENU, new BWindow(makeFXMLLoader.apply(WINDOW.MENU.URL())));
            put(WINDOW.MATCHMAKING, new BWindow(makeFXMLLoader.apply(WINDOW.MATCHMAKING.URL())));
        }};
    }

    private final BSceneMover sceneMover = injector.getInstance(BSceneMover.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public final void start(Stage unused) throws Exception {
        initialize();
    }

    private void initialize(){
        stage.getIcons().add(new Image(ICON_PATH));
        stage.setResizable(false);
        stage.setTitle(TITLE);
        sceneMover.moveToScene(WINDOW.MATCHMAKING);
        stage.show();
    }

    @Contract(pure = true)
    public final Stage getStage() {
        return stage;
    }

    @Contract(pure = true)
    public final EnumMap<WINDOW, BWindow> getWindowMap() {
        return windowMap;
    }
}
