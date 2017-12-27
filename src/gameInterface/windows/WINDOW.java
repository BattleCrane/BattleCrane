package gameInterface.windows;

import main.BGame;
import org.jetbrains.annotations.Contract;

import java.net.URL;

public enum WINDOW {

    INITIALIZATION(BGame.class.getResource(""))
    , MENU(BGame.class.getResource("../fxmlFiles/Menu.fxml"))
    , AUTHORIZATION(BGame.class.getResource(""))
    , PROFILE(BGame.class.getResource(""))
    , CHOICE_BONUS(BGame.class.getResource(""))
    , CHOICE_HERO(BGame.class.getResource(""))
    , MATCHMAKING(BGame.class.getResource("../fxmlFiles/MatchMaking.fxml"));

    private final URL url;

    WINDOW(URL url){
        this.url = url;
    }

    @Contract(pure = true)
    public final URL URL() {
        return url;
    }
}