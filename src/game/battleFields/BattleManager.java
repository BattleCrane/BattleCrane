package game.battleFields;


import game.adjutants.*;
import game.players.Player;
import game.unities.Unity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс ControlBattler контролирует внутреннее состояние процесса игры.
 * Он хранит в себе все двух игроков, все типы юнитов.
 * Выполняет методы:
 * 1.) Инициализация (стартовая позиция игроков);
 * 2.) Размещение юнитов на поле боя;
 * --- Проверка территории на пустоту;
 * 3.) Удаление юнитов с поля боя;
 * 4.) Создание нового хода:
 * --- Смена игрока;
 * --- Отображение активных строений;
 * --- Отображение активных боевых единиц;
 * --- Отображение возможных построек;
 * --- Отображение возможных автоматчиков;
 * --- Отображение возможных танков.
 * 5.) Отрисовка территорий
 */
//Контроллер игры, делегат
// TODO: 16.12.2017 make enum units & move methods callable from units, extending their from Unity
public class BattleManager {

    //Основные переменные:
    private AdjutantWakeUpper adjutantWakeUpper = new AdjutantWakeUpper();
    private AdjutantReporter adjutantReporter = new AdjutantReporter();
    private AdjutantAttacker adjutantAttacker = new AdjutantAttacker();
    private AdjutantFielder adjutantFielder = new AdjutantFielder();

    private BattleField battleField = new BattleField();
    private IdentificationField identificationField = new IdentificationField();
    private int turn = 1;

    //Игроки:
    private Player playerBlue = new Player(0, -1, -1, "+");
    private Player playerRed = new Player(1, 0, 0, "-");
    private Player player;


    private Player opponentPlayer;

    //Юниты:
    //Строения:
    private int howICanBuild = 1;
    //Штаб:
    private Unity headquarters = new Unity(2, 2, "h", 8);
    //Бараки:
    private Unity barracks = new Unity(1, 2, "b", 1);
    //Генератор:
    private Unity generator = new Unity(2, 2, "g", 1);
    private boolean isConstructedGenerator = false;
    //Завод:
    private Unity factory = new Unity(2, 3, "f", 1);
    //Турель:
    private Unity turret = new Unity(1, 1, "t", 2);
    //Стена:
    private Unity wall = new Unity(1, 1, "w", 4);

    //Армия:
    private int howICanProductArmyLevel1 = 0;
    private int howICanProductTanksLevel1 = 0;

    private int howICanProductArmyLevel2 = 0;
    private int howICanProductTanksLevel2 = 0;
    private int howICanProductArmyLevel3 = 0;
    private int howICanProductTanksLevel3 = 0;
    private int howICanBuildFactories = 0;
    private Unity gunner = new Unity(1, 1, "G", 1);
    private Unity tank = new Unity(1, 1, "T", 2);


    public BattleManager(BattleField battleField) {
        this.battleField = battleField;
    }

    public BattleManager() {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void initializeField() {
        //Размещение штабов:
        putUnity(playerRed, new Point(0, 0), headquarters);
        putUnity(playerBlue, new Point(14, 14), headquarters);

        //Размещение стен:
        for (int j = 0; j < 5; j++) {
            putUnity(playerRed, new Point(j, 4), wall);
            putUnity(playerRed, new Point(4, j), wall);

            putUnity(playerBlue, new Point(15 - j, 11), wall);
            putUnity(playerBlue, new Point(11, 15 - j), wall);
        }
        nextTurnOfCurrentPlayer();
    }

    public void initializeFieldSlava() {
        //Размещение штабов:
        putUnity(playerRed, new Point(0, 0), headquarters);
        putUnity(playerBlue, new Point(14, 14), headquarters);

        //Размещение стен:
        for (int j = 0; j < 5; j++) {
            putUnity(playerRed, new Point(j, 4), wall);
            putUnity(playerRed, new Point(4, j), wall);

            putUnity(playerBlue, new Point(15 - j, 11), wall);
            putUnity(playerBlue, new Point(11, 15 - j), wall);
        }
        playerBlue.setEnergy(10);
//        for (int j = 0; j < 16; j++){
//            putUnity(playerBlue, new Point(15 -j, j), barracks);
//        }
//        putUnity(playerBlue, new Point(14, 3), factory);
//        putUnity(playerBlue, new Point(4, 13), factory);
        nextTurnOfCurrentPlayer();
//        for (int j = 0; j < 16; j++){
//            putUnity(playerBlue, new Point(14, j), turret);
//            putUnity(playerBlue, new Point(j, 14), turret);
//        }
    }

    public boolean putUnity(Player player, Point point, Unity unity) {
        if (isEmptyTerritory(point, unity)) {
            for (int i = point.X(); i < point.X() + unity.getWidth(); i++) {
                for (int j = point.Y(); j < point.Y() + unity.getHeight(); j++) {
                    String newUnity = unity.getHitPoints() + "^" + "?" + player.getColorType() + unity.getId();
                    if (i == point.X() && j == point.Y()) {
                        newUnity = newUnity + "'";
                    } else {
                        newUnity = newUnity + ".";
                    }
                    identificationField.getMatrix().get(i).set(j, "" + identificationField.getNumberUnity());
                    battleField.getMatrix().get(i).set(j, newUnity);
                }
            }
            identificationField.setNumberUnity(identificationField.getNumberUnity() + 1);
            return true;
        } else {
            return false;
        }
    }

    public void removeUnity(Point point, Unity unity, String field) {
        for (int i = point.X(); i < point.X() + unity.getWidth(); i++) {
            for (int j = point.Y(); j < point.Y() + unity.getHeight(); j++) {
                identificationField.getMatrix().get(i).set(j, "     0");
                battleField.getMatrix().get(i).set(j, field + "    0");
            }
        }
        identificationField.setNumberUnity(identificationField.getNumberUnity() - 1);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean putDoubleWall(Player player, Point point, Unity unity) {
        Point pointShiftRight = new Point(point.X(), point.Y());
        pointShiftRight.setY(pointShiftRight.Y() + 1);
        if (isEmptyTerritory(point, unity) && isEmptyTerritory(pointShiftRight, unity)) {
            for (int i = point.X(); i < point.X() + unity.getWidth(); i++) {
                for (int j = point.Y(); j < point.Y() + unity.getHeight(); j++) {
                    String newUnity = unity.getHitPoints() + "^" + "?" + player.getColorType() + unity.getId();
                    if (i == point.X() && j == point.Y()) {
                        newUnity = newUnity + "'";
                    }
                    identificationField.getMatrix().get(i).set(j, "" + identificationField.getNumberUnity());
                    identificationField.setNumberUnity(identificationField.getNumberUnity() + 1);
                    identificationField.getMatrix().get(i).set(j + 1, "" + identificationField.getNumberUnity());
                    identificationField.setNumberUnity(identificationField.getNumberUnity() + 1);
                    battleField.getMatrix().get(i).set(j, newUnity);
                    battleField.getMatrix().get(i).set(j + 1, newUnity);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @NotNull
    public Boolean isEmptyTerritory(Point point, Unity unity) {
        if (point.X() + unity.getWidth() > 16 || point.Y() + unity.getHeight() > 16) {
            return false;
        }
        for (int i = point.X(); i < point.X() + unity.getWidth(); i++) {
            for (int j = point.Y(); j < point.Y() + unity.getHeight(); j++) {
                if (!battleField.getMatrix().get(i).get(j).equals("     0") &&
                        !battleField.getMatrix().get(i).get(j).equals("+    0") &&
                        !battleField.getMatrix().get(i).get(j).equals("-    0")) {
                    return false;
                }
            }
        }
        return true;
    }

    //Проверка расположения рядом строения:
    public boolean canConstructBuilding(Point point, Unity unity, Player player) {
        int startX = point.X() - 1; //Сдвигаем начальную точку в левый верхний угол (Тут ошибка в проектировании осей координат)
        int startY = point.Y() - 1;
        Pattern pattern = Pattern.compile("[hgbfwt]");
        Pattern patternBonus = Pattern.compile("[ui]");
        for (int i = startX; i <= startX + unity.getWidth() + 1; i++) {
            for (int j = startY; j <= startY + unity.getHeight() + 1; j++) {
                if (i >= 0 && i < 16 && j >= 0 && j < 16) {
                    Matcher matcher = pattern.matcher(battleField.getMatrix().get(i).get(j));
                    Matcher matcherBonus = patternBonus.matcher(battleField.getMatrix().get(i).get(j));
                    if (battleField.getMatrix().get(i).get(j).contains(player.getColorType()) && (matcher.find() || matcherBonus.find())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean upgradeBuilding(Point point, Player player) {
        boolean isUpgraded = false;
        if (battleField.getMatrix().get(point.X()).get(point.Y()).contains(player.getColorType())) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    String unityBuild = battleField.getMatrix().get(i).get(j);
                    if (identificationField.getMatrix().get(i).get(j).equals(String.valueOf(getIdentification(point)))) {
                        try {
                            switch (unityBuild.substring(4, 5)) { //Смотрим строение:
                                case "g": //Улучшение генератора: -> Опускаемся в бараки:
                                case "b": //Улучшение бараков:
                                    if (!unityBuild.contains(">")) {
                                        unityBuild = levelUp(unityBuild);
                                        if (unityBuild != null && unityBuild.contains("<")) {
                                            unityBuild = increaseHitPoints(unityBuild, 1);
                                        }
                                        if (unityBuild != null && unityBuild.contains(">")) {
                                            unityBuild = increaseHitPoints(unityBuild, 2);
                                        }
                                        isUpgraded = true;
                                    }
                                    break;
                                case "f": //Улучшение завода:
                                    if (!unityBuild.contains(">")) {
                                        unityBuild = levelUp(unityBuild);
                                        if (unityBuild != null && unityBuild.contains("<")) {
                                            unityBuild = increaseHitPoints(unityBuild, 3);
                                        }
                                        if (unityBuild != null && unityBuild.contains(">")) {
                                            unityBuild = increaseHitPoints(unityBuild, 2);
                                        }
                                        isUpgraded = true;
                                    }
                                    break;
                                case "t": //Улучшение туррели:
                                    if (unityBuild.contains("^")) {
                                        unityBuild = levelUp(unityBuild);
                                        unityBuild = increaseHitPoints(unityBuild, 2);
                                    }
                                    isUpgraded = true;
                                    break;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    battleField.getMatrix().get(i).set(j, unityBuild);
                }
            }
        }
        return isUpgraded;
    }

    private int getIdentification(Point point) {
        return Integer.parseInt(identificationField.getMatrix().get(point.X()).get(point.Y()));
    }

    @NotNull
    public String increaseHitPoints(String unityBuild, int quantity) {
        int upgradedHitPoints = Integer.parseInt(unityBuild.substring(0, 1)) + quantity;
        return upgradedHitPoints + unityBuild.substring(1);
    }


    @Nullable
    public String levelUp(String unity) {
        switch (unity.substring(1, 2)) {
            case "^":
                return unity.substring(0, 1) + '<' + unity.substring(2);
            case "<":
                return unity.substring(0, 1) + '>' + unity.substring(2);
        }
        return unity;
    }

    public void aggravateUnit(Point point, Unity unity) {
        for (int i = point.X(); i < point.X() + unity.getWidth(); i++) {
            for (int j = point.Y(); j < point.Y() + unity.getHeight(); j++) {
                String e = battleField.getMatrix().get(i).get(j);
                e = levelDown(e);
                battleField.getMatrix().get(i).set(j, e);
            }
        }
    }

    @Nullable
    private String levelDown(String unity) {
        int decrease;
        switch (unity.substring(1, 2)) {
            case "<":
                if (unity.substring(4, 5).equals("g")) {
                    decrease = Integer.parseInt(unity.substring(0, 1)) - 1;
                    return "" + decrease + '^' + unity.substring(2);
                }
                if (unity.substring(4, 5).equals("b")) {
                    decrease = Integer.parseInt(unity.substring(0, 1)) - 1;
                    return "" + decrease + '^' + unity.substring(2);
                }
                if (unity.substring(4, 5).equals("f")) {
                    decrease = Integer.parseInt(unity.substring(0, 1)) - 3;
                    return "" + decrease + '^' + unity.substring(2);
                }
                if (unity.substring(4, 5).equals("t")) {
                    decrease = Integer.parseInt(unity.substring(0, 1)) - 2;
                    return "" + decrease + '^' + unity.substring(2);
                }
            case ">":
                if (unity.substring(4, 5).equals("g")) {
                    decrease = Integer.parseInt(unity.substring(0, 1)) - 2;
                    return "" + decrease + '<' + unity.substring(2);
                }
                if (unity.substring(4, 5).equals("b")) {
                    decrease = Integer.parseInt(unity.substring(0, 1)) - 2;
                    return "" + decrease + '<' + unity.substring(2);
                }
                if (unity.substring(4, 5).equals("f")) {
                    decrease = Integer.parseInt(unity.substring(0, 1)) - 2;
                    return "" + decrease + '<' + unity.substring(2);
                }
        }
        return unity;
    }

    public void checkDestroyedUnities() {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                if (battleField.getMatrix().get(i).get(j).substring(0, 1).equals("0")) {
                    battleField.getMatrix().get(i).set(j, "XXXXXX");
                }
            }
        }
    }

    //Следующий ход:
    public void nextTurnOfCurrentPlayer() {
        turn = (turn + 1) % 2;
        whatIsTurn();
        adjutantWakeUpper.wakeUpUnities(this);
        adjutantReporter.getReportAboutUnities(this);
        howICanBuildFactories = howICanProductArmyLevel1 + howICanProductArmyLevel2 + howICanProductArmyLevel3 - howICanProductTanksLevel1 - howICanProductTanksLevel2 - howICanProductTanksLevel3;
        if (player.getSupplyEnergy() < 20) {
            player.setEnergy(player.getEnergy() + 1);
            player.setSupplyEnergy(player.getSupplyEnergy() + 1);
        } else {
            getAdjutantWakeUpper().wakeUpHeadquarters(this);
            adjutantAttacker.setCountShortsForHeadquarters(3);
        }
        isConstructedGenerator = false;
    }

    //Определяет чей ход:
    private void whatIsTurn() {
        if (turn == playerBlue.getTurn()) {
            player = playerBlue;
            opponentPlayer = playerRed;
        } else {
            player = playerRed;
            opponentPlayer = playerBlue;
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public BattleField getBattleField() {
        return battleField;
    }

    public Player getPlayerBlue() {
        return playerBlue;
    }

    public Player getPlayerRed() {
        return playerRed;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getOpponentPlayer() {
        return opponentPlayer;
    }

    public void setOpponentPlayer(Player opponentPlayer) {
        this.opponentPlayer = opponentPlayer;
    }

    public Unity getHeadquarters() {
        return headquarters;
    }

    public Unity getBarracks() {
        return barracks;
    }

    public Unity getGenerator() {
        return generator;
    }

    public Unity getFactory() {
        return factory;
    }

    public Unity getTurret() {
        return turret;
    }

    public Unity getWall() {
        return wall;
    }

    public Unity getGunner() {
        return gunner;
    }

    public Unity getTank() {
        return tank;
    }

    public int getHowCanBuildFactories() {
        return howICanBuildFactories;
    }

    public void setHowICanBuildFactories(int howICanBuildFactories) {
        this.howICanBuildFactories = howICanBuildFactories;
    }

    public int getHowICanBuild() {
        return howICanBuild;
    }

    public void setHowICanBuild(int howICanBuild) {
        this.howICanBuild = howICanBuild;
    }

    public int getHowICanProductArmyLevel1() {
        return howICanProductArmyLevel1;
    }

    public void setHowICanProductArmyLevel1(int howICanProductArmyLevel1) {
        this.howICanProductArmyLevel1 = howICanProductArmyLevel1;
    }

    public int getHowICanProductTanksLevel1() {
        return howICanProductTanksLevel1;
    }

    public void setHowICanProductTanksLevel1(int howICanProductTanksLevel1) {
        this.howICanProductTanksLevel1 = howICanProductTanksLevel1;
    }

    public int getHowICanProductArmyLevel2() {
        return howICanProductArmyLevel2;
    }

    public void setHowICanProductArmyLevel2(int howICanProductArmyLevel2) {
        this.howICanProductArmyLevel2 = howICanProductArmyLevel2;
    }

    public int getHowICanProductTanksLevel2() {
        return howICanProductTanksLevel2;
    }

    public void setHowICanProductTanksLevel2(int howICanProductTanksLevel2) {
        this.howICanProductTanksLevel2 = howICanProductTanksLevel2;
    }

    public int getHowICanProductArmyLevel3() {
        return howICanProductArmyLevel3;
    }

    public void setHowICanProductArmyLevel3(int howICanProductArmyLevel3) {
        this.howICanProductArmyLevel3 = howICanProductArmyLevel3;
    }

    public int getHowICanProductTanksLevel3() {
        return howICanProductTanksLevel3;
    }

    public void setHowICanProductTanksLevel3(int howICanProductTanksLevel3) {
        this.howICanProductTanksLevel3 = howICanProductTanksLevel3;
    }


    public boolean isConstructedGenerator() {
        return isConstructedGenerator;
    }

    public void setConstructedGenerator(boolean constructedGenerator) {
        isConstructedGenerator = constructedGenerator;
    }

    public IdentificationField getIdentificationField() {
        return identificationField;
    }

    public AdjutantWakeUpper getAdjutantWakeUpper() {
        return adjutantWakeUpper;
    }

    public AdjutantAttacker getAdjutantAttacker() {
        return adjutantAttacker;
    }

    public void setBattleField(BattleField battleField) {
        this.battleField = battleField;
    }
}