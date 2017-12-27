package main;

import bInterface.bwindows.BWindow;
import bInterface.bwindows.WINDOW;
import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jetbrains.annotations.Contract;

import java.net.URL;
import java.util.EnumMap;
import java.util.function.Function;

/**
 * Класс BGame является стартом приложения.
 * У него существует один статический экземпляр Stage, который не является аргументом метода start.
 * Но в реализации этого метода, BattleCraneStage ведет себя как аргумент,
 * а реальный объект на входе Stage unused игнорируется
 */

// TODO: 16.12.2017 make this injectable & set maven project
public final class BGame extends Application {
    private final Injector injector = Guice.createInjector(new BGameGuiceModule(this));

    private final Stage stage = new Stage();

    private final EnumMap<WINDOW, BWindow> windowMap;

    {
        final Function<URL, FXMLLoader> makeFXMLLoader = url -> {
            final FXMLLoader result = new FXMLLoader(url);
            result.setControllerFactory(injector::getInstance);
            return result;
        };

        windowMap = new EnumMap<WINDOW, BWindow>(WINDOW.class){{
//            put(WINDOW.INITIALIZATION, new BWindow(makeFXMLLoader.apply(BGame.class.getResource(""))));
//            put(WINDOW.MENU, new BWindow(makeFXMLLoader.apply(BGame.class.getResource(""))));
//            put(WINDOW.AUTHORIZATION, new BWindow(makeFXMLLoader.apply(BGame.class.getResource(""))));
//            put(WINDOW.PROFILE, new BWindow(makeFXMLLoader.apply(BGame.class.getResource(""))));
//            put(WINDOW.MATCHMAKING, new BWindow(makeFXMLLoader.apply(BGame.class.getResource(""))));
        }};
    }


    //Статическое окно, которое можно переключать
    private static Stage BattleCraneStage = new Stage();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage unused) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("../fxmlFiles/fxmlBattleFieldWindow.fxml"));
        BattleCraneStage.getIcons().add(new Image("file:src\\Resources\\Icon.png"));
        BattleCraneStage.setResizable(false);
        BattleCraneStage.setTitle("BattleCrane");
        BattleCraneStage.setScene(new Scene(root));
        BattleCraneStage.show();
    }

    @Contract(pure = true)
    public Stage getStage() {
        return stage;
    }

    @Contract(pure = true)
    public EnumMap<WINDOW, BWindow> getMapOfWindows() {
        return windowMap;
    }
}
