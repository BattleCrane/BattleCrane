package bonuses;

import game.adjutants.AdjutantFielder;
import game.battleFields.BattleField;
import game.battleFields.BattleManager;
import game.battleFields.IdentificationField;
import game.battleFields.Point;
import controllers.ControllerMatchMaking;
import game.graphics.Painter;
import game.players.Player;
import game.unities.Unity;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.Contract;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс ControllerBonusCollection хранит в себе экземпляры класса Support и управляет ими
 */

// TODO: 16.12.2017 This class is so huge and difficult to understand! Transform bonuses in classes
public final class ControllerBonusesCollection {
    public void showBonuses(ControllerMatchMaking controllerMatchMaking, Player player, Pane paneControlBonus) {
        int x = 42;
        int y = 37;
        BattleManager battleManager = controllerMatchMaking.getBattleManager();
        for (Bonus bonus : player.getListOfBonuses()) {
            bonus.getSprite().setLayoutX(x);
            bonus.getSprite().setLayoutY(y);
            if (bonus.equals(obstacle)) {
                returnEnergyFromObstacle(battleManager);
            }
            if (bonus.equals(energyBattery)) {
                int collectedEnergy = 0;
                for (int i = 0; i < 16; i++) {
                    for (int j = 0; j < 16; j++) {
                        if (battleManager.getBattleField().getMatrix().get(i).get(j).contains(battleManager.getPlayer().getColorType() + "e")) {
                            collectedEnergy += returnEnergyFromEnergyBattery(battleManager.getBattleField(), controllerMatchMaking.getBattleManager().getIdentificationField(), new Point(i, j),
                                    new Unity(1, 1, "e", 1), battleManager.getPlayer());
                            battleManager.getBattleField().getMatrix().get(i).set(j,
                                    battleManager.getAdjutantAttacker().attack(controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(i).get(j), 1));
                            controllerMatchMaking.getBattleManager().checkDestroyedUnities();

                        }
                    }
                }
                controllerMatchMaking.getBattleManager().getPlayer().setEnergy(controllerMatchMaking.getBattleManager().getPlayer().getEnergy() + collectedEnergy);
                controllerMatchMaking.getBattleManager().getPlayer().setSupplyEnergy(controllerMatchMaking.getBattleManager().getPlayer().getSupplyEnergy() + collectedEnergy);
            }
            if (bonus.equals(explosive)) {
                for (int i = 0; i < 16; i++) {
                    for (int j = 0; j < 16; j++) {
                        String currentUnity = controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(i).get(j);
                        if (currentUnity.contains("!" + controllerMatchMaking.getBattleManager().getOpponentPlayer().getColorType() + "w")) {
                            controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(i).set(j,
                                    battleManager.getAdjutantAttacker().attack(controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(i).get(j), 4));
                            controllerMatchMaking.getBattleManager().checkDestroyedUnities();
                            System.out.println("Destroyed");
                        }
                    }
                }
            }

            if (bonus.equals(superMortarTurret)) {
                for (int i = 0; i < 16; i++) {
                    for (int j = 0; j < 16; j++) {
                        String currentUnity = controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(j).get(i);
                        if (currentUnity.contains(controllerMatchMaking.getBattleManager().getPlayer().getColorType() + "u")) {
                            battleManager.getAdjutantAttacker().radiusAttack(controllerMatchMaking.getBattleManager(), new Point(j, i), 5, 1);
                        }
                    }
                }
            }

            if (bonus.equals(intensiveProduction)) {
                if (player.getColorType().equals("+")) {
                    player.setEnergy(player.getEnergy() - 1 + additionalEnergyBlue);
                    player.setSupplyEnergy(player.getSupplyEnergy() - 1 + additionalEnergyBlue);
                }
                if (player.getColorType().equals("-")) {
                    player.setEnergy(player.getEnergy() - 1 + additionalEnergyRed);
                    player.setSupplyEnergy(player.getSupplyEnergy() - 1 + additionalEnergyRed);
                }
            }

            if (bonus.equals(fort)) {
                for (int i = 0; i < 16; i++) {
                    for (int j = 0; j < 16; j++) {
                        String currentUnity = controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(j).get(i);
                        if (currentUnity.contains(controllerMatchMaking.getBattleManager().getPlayer().getColorType() + "i")) {
                            currentUnity = currentUnity.substring(0, 1) + currentUnity.substring(0, 1) + currentUnity.substring(2);
                            controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(j).set(i, currentUnity);
                            battleManager.getAdjutantWakeUpper().wakeUpExactly(controllerMatchMaking.getBattleManager(), j, i);
                        }
                    }
                }
            }
            if (bonus.equals(tankBuffalo)) {
                for (int i = 0; i < 16; i++) {
                    for (int j = 0; j < 16; j++) {
                        String currentUnity = controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(j).get(i);
                        if (currentUnity.contains(controllerMatchMaking.getBattleManager().getPlayer().getColorType() + "Q")) {
                            currentUnity = currentUnity.substring(0, 1) + getBuffaloDamage(controllerMatchMaking.getBattleManager()) + currentUnity.substring(2);
                            controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(j).set(i, currentUnity);
                            if (getBuffaloDamage(controllerMatchMaking.getBattleManager()) == 0) {
                                controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(j).set(i, battleManager.getAdjutantWakeUpper().sleepUnity(currentUnity));
                            }
                        }
                    }
                }
            }
            paneControlBonus.getChildren().add(bonus.getSprite());
            if (x + 80 > 450) {
                x = 42;
                y += 80;
            } else {
                x += 80;
            }
        }
    }

    public void flush(Pane paneControlBonus, BattleManager battleManager) {
        paneControlBonus.getChildren().retainAll(paneControlBonus.getChildren().get(0));
        battleManager.getAdjutantWakeUpper().sleepHeadquarters(battleManager);
    }


    /**
     * Бонус: "Легкая преграда"
     * Стоимость: 1 ед. энергии;
     * Устанавливается на вашей и нейтральной территории;
     * Тип: "Сооружение" (не является ни строением ни армией);
     * Запас прочности 1;
     * На следующий ход уничтожается, и Вы получаете 1 ед. энергии.
     */

    private final Bonus obstacle = new Bonus(1,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Sprite\\Obstacle.png")),
                    new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        private Unity obstacle = new Unity(1, 1, "o", 1);

        public void run(ControllerMatchMaking controllerMatchMaking) {
            Pane paneControlField = controllerMatchMaking.getPaneControlField();
            BattleManager battleManager = controllerMatchMaking.getBattleManager();
            paneControlField.setOnMouseClicked(event -> {
                int currentEnergy = battleManager.getPlayer().getEnergy();
                if (controllerMatchMaking.isClick() && currentEnergy - this.getEnergy() >= 0) {
                    controllerMatchMaking.setClick(false);
                    if (battleManager.putUnity(battleManager.getPlayer(),
                            new Point((int) (event.getY() / 33.5), (int) (event.getX() / 33.5)), obstacle)) {
                        battleManager.getPlayer().setEnergy(currentEnergy - this.getEnergy());
                        Painter.drawGraphic(battleManager, controllerMatchMaking.getResource(),
                                controllerMatchMaking.getPaneControlField(), controllerMatchMaking.getResourceOfBonuses());
                    }
                }
                controllerMatchMaking.getPaneControlField().setOnMouseClicked(controllerMatchMaking.getEventHandler());
            });
        }
    };

    private void returnEnergyFromObstacle(BattleManager battleManager) {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                if (battleManager.getBattleField().getMatrix().get(i).get(j).substring(4, 5).equals("o") &&
                        battleManager.getPlayer().getColorType().equals(battleManager.getBattleField().getMatrix()
                                .get(i).get(j).substring(3, 4))) {
                    battleManager.getPlayer().setEnergy(battleManager.getPlayer().getEnergy() + 1);
                    battleManager.getPlayer().setSupplyEnergy(battleManager.getPlayer().getSupplyEnergy() + 1);
                    battleManager.getBattleField().getMatrix().get(i).set(j,
                            battleManager.getAdjutantAttacker().attack(battleManager.getBattleField().getMatrix().get(i).get(j), 1));
                    battleManager.checkDestroyedUnities();
                }
            }
        }
    }

    /**
     * Бонус: "Реактивная готовность"
     * Стоимость: 1 ед. энергии;
     * Ваш выбранный пехотинец становиться активным, после чего теряет 1 ед. здоровья
     */

    private final Bonus combatReadiness = new Bonus(1,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1CombatReadiness\\Sprite\\CombatReadiness.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            controllerMatchMaking.getPaneControlField().setOnMouseClicked(event -> {
                Pattern pattern = Pattern.compile("[G]");
                Pattern patternBonuses = Pattern.compile("[HC]");
                String currentUnit = controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get((int) (event.getY() / 33.5)).get((int) (event.getX() / 33.5));
                int currentEnergy = controllerMatchMaking.getBattleManager().getPlayer().getEnergy();
                Matcher matcher = pattern.matcher(currentUnit);
                Matcher matcherBonuses = patternBonuses.matcher(currentUnit);
                if (currentEnergy - this.getEnergy() >= 0 && (matcher.find() || matcherBonuses.find()) &&
                        !currentUnit.contains("!")) {
                    controllerMatchMaking.setClick(false);
                    controllerMatchMaking.getBattleManager().getAdjutantWakeUpper().wakeUpExactly(controllerMatchMaking.getBattleManager(), (int) (event.getY() / 33.5), (int) (event.getX() / 33.5));
                    controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get((int) (event.getY() / 33.5)).set((int) (event.getX() / 33.5),
                            controllerMatchMaking.getBattleManager().getAdjutantAttacker().attack(controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get((int) (event.getY() / 33.5)).get((int) (event.getX() / 33.5)), 1));
                    controllerMatchMaking.getBattleManager().getPlayer().setEnergy(currentEnergy - this.getEnergy());
                    Painter.drawGraphic(controllerMatchMaking.getBattleManager(), controllerMatchMaking.getResource(),
                            controllerMatchMaking.getPaneControlField(), controllerMatchMaking.getResourceOfBonuses());
                }
                controllerMatchMaking.getPaneControlField().setOnMouseClicked(controllerMatchMaking.getEventHandler());
            });
        }
    };

    /**
     * Бонус: "Скорая помощь"
     * Стоимость: 1 ед. энергии;
     * Все ваши обыкновенные автоматчики улучшаются до бронированных автоматчиков и получают +1 к здоровью;
     * Восстанавливает здоровье всем бронированным автоматчикам;
     */

    private final Bonus ambulance = new Bonus(1,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Ambulance\\Sprite\\Ambulance.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            if (controllerMatchMaking.getBattleManager().getPlayer().getEnergy() - this.getEnergy() >= 0) {
                for (int i = 0; i < 16; i++) {
                    for (int j = 0; j < 16; j++) {
                        String currentUnity = controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(i).get(j);
                        if (currentUnity.substring(3, 4).equals(controllerMatchMaking.getBattleManager().getPlayer().getColorType()) &&
                                currentUnity.substring(4, 5).equals("G") && !currentUnity.substring(0, 2).equals("2A")) {
                            currentUnity = controllerMatchMaking.getBattleManager().increaseHitPoints(currentUnity, 1); //Здесь конечно можно вызвать AdjutantAttacker и положить -1...
                            currentUnity = currentUnity.substring(0, 1) + "A" + currentUnity.substring(2);
                            controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(i).set(j, currentUnity);
                        }
                    }
                }
                Painter.drawGraphic(controllerMatchMaking.getBattleManager(), controllerMatchMaking.getResource(),
                        controllerMatchMaking.getPaneControlField(), controllerMatchMaking.getResourceOfBonuses());
            }
            controllerMatchMaking.setClick(false);
        }
    };

    /**
     * Бонус: "Тяжелые снаряды"
     * Стоимость: 1 ед. энергии;
     * Ваш выбранный автоматчик улучшается до тяжелого автоматчика и получает +2 к атаке до конца матча;
     */

    private final Bonus heavyShells = new Bonus(1,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1HeavyShells\\Sprite\\HeavyShells.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            controllerMatchMaking.getPaneControlField().setOnMouseClicked(event -> {
                String currentUnit = controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get((int) (event.getY() / 33.5)).get((int) (event.getX() / 33.5));
                int currentEnergy = controllerMatchMaking.getBattleManager().getPlayer().getEnergy();
                if (currentEnergy - this.getEnergy() >= 0 && currentUnit.substring(1, 2).equals("^") && currentUnit.substring(4, 5).equals("G")) {
                    controllerMatchMaking.setClick(false);
                    controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get((int) (event.getY() / 33.5)).set((int) (event.getX() / 33.5),
                            currentUnit.substring(0, 4) + "H" + currentUnit.substring(5));
                    controllerMatchMaking.getBattleManager().getPlayer().setEnergy(currentEnergy - this.getEnergy());
                    Painter.drawGraphic(controllerMatchMaking.getBattleManager(), controllerMatchMaking.getResource(),
                            controllerMatchMaking.getPaneControlField(), controllerMatchMaking.getResourceOfBonuses());
                }
                controllerMatchMaking.getPaneControlField().setOnMouseClicked(controllerMatchMaking.getEventHandler());
            });
        }
    };

    /**
     * Бонус: "Энергетическая батарея"
     * Стоимость: 1 ед. энергии;
     * Устанавливается на вашей и нейтральной территории;
     * Тип: "Сооружение" (не является ни строением ни армией);
     * Запас прочности 1;
     * На следующий ход уничтожается, и Вы получаете 1 ед. энергии за каждый барак, стоящий рядом.
     */

    private final Bonus energyBattery = new Bonus(1,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1EnergyBlock\\Sprite\\EnergyBlock.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        private Unity energyBattery = new Unity(1, 1, "e", 1);

        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            controllerMatchMaking.getPaneControlField().setOnMouseClicked(event -> {
                int currentEnergy = controllerMatchMaking.getBattleManager().getPlayer().getEnergy();
                if (controllerMatchMaking.isClick() && currentEnergy - this.getEnergy() >= 0) {
                    controllerMatchMaking.setClick(false);
                    controllerMatchMaking.getBattleManager().putUnity(controllerMatchMaking.getBattleManager().getPlayer(),
                            new Point((int) (event.getY() / 33.5), (int) (event.getX() / 33.5)), energyBattery);
                    controllerMatchMaking.getBattleManager().getPlayer().setEnergy(currentEnergy - this.getEnergy());
                    Painter.drawGraphic(controllerMatchMaking.getBattleManager(), controllerMatchMaking.getResource(),
                            controllerMatchMaking.getPaneControlField(), controllerMatchMaking.getResourceOfBonuses());
                }
                controllerMatchMaking.getPaneControlField().setOnMouseClicked(controllerMatchMaking.getEventHandler());
            });
        }
    };

    private int returnEnergyFromEnergyBattery(BattleField battleField, IdentificationField identificationField, Point point, Unity unity, Player player) {
        int returnedEnergy = 0;
        Set<Integer> setId = new LinkedHashSet<>();
        int startX = point.X() - 1; //Сдвигаем начальную точку в левый верхний угол (Тут ошибка в проектировании осей координат)
        int startY = point.Y() - 1;
        for (int i = startX; i <= startX + unity.getWidth() + 1; i++) {
            for (int j = startY; j <= startY + unity.getHeight() + 1; j++) {
                if (i >= 0 && i < 16 && j >= 0 && j < 16) {
                    if (battleField.getMatrix().get(i).get(j).contains(player.getColorType() + "b") &&
                            setId.add(Integer.parseInt(identificationField.getMatrix().get(i).get(j)))) {
                        returnedEnergy++;
                    }
                }
            }
        }
        return returnedEnergy;
    }

    /**
     * Бонус: "Взрывчатка"
     * Стоимость: 2 ед. энергии;
     * Устанавливается на любой вражеской стене;
     * На следующий ход уничтожает вражескую стену.
     */

    private final Bonus explosive = new Bonus(2,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\2Explosive\\Sprite\\Explosive.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            controllerMatchMaking.getPaneControlField().setOnMouseClicked(event -> {
                int currentEnergy = controllerMatchMaking.getBattleManager().getPlayer().getEnergy();
                int i = (int) (event.getX() / 33.5);
                int j = (int) (event.getY() / 33.5);
                if (controllerMatchMaking.isClick() && currentEnergy - this.getEnergy() >= 0 &&
                        controllerMatchMaking.getBattleManager().getBattleField().getMatrix().
                                get(j).get(i).contains(controllerMatchMaking.getBattleManager().getOpponentPlayer().getColorType() + "w'")) {
                    controllerMatchMaking.setClick(false);
                    if (!controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(j).get(i).contains("!")) {
                        controllerMatchMaking.getBattleManager().getAdjutantWakeUpper().wakeUpExactly(controllerMatchMaking.getBattleManager(), j, i);
                        controllerMatchMaking.getBattleManager().getPlayer().setEnergy(currentEnergy - this.getEnergy());
                        Painter.drawGraphic(controllerMatchMaking.getBattleManager(), controllerMatchMaking.getResource(),
                                controllerMatchMaking.getPaneControlField(), controllerMatchMaking.getResourceOfBonuses());
                    }
                }
                controllerMatchMaking.getPaneControlField().setOnMouseClicked(controllerMatchMaking.getEventHandler());
            });
        }
    };

    /**
     * Бонус: "Боевой штаб"
     * Стоимость: 2 ед. энергии;
     * Ваш штаб получает три выстела по 1 ед. урона до конца хода ;
     */

    private final Bonus fightingHeadquarters = new Bonus(2,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\2FightingHeadquarters\\Sprite\\FightingHeadquarters.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            BattleManager battleManager = controllerMatchMaking.getBattleManager();
            int currentEnergy = controllerMatchMaking.getBattleManager().getPlayer().getEnergy();
            if (currentEnergy - this.getEnergy() >= 0) {
                battleManager.getAdjutantWakeUpper().wakeUpHeadquarters(battleManager);
                battleManager.getAdjutantAttacker().setCountShortsForHeadquarters(3);
                controllerMatchMaking.getBattleManager().getPlayer().setEnergy(currentEnergy - this.getEnergy());
                Painter.drawGraphic(controllerMatchMaking.getBattleManager(), controllerMatchMaking.getResource(),
                        controllerMatchMaking.getPaneControlField(), controllerMatchMaking.getResourceOfBonuses());
                controllerMatchMaking.setClick(true);
            }

        }
    };


    /**
     * Бонус: "Кластерные стрелы"
     * Стоимость: 2 ед. энергии;
     * Ваш выбранный автоматчик улучшается до кластерного автоматчика и наносит ТОЛЬКО СТРОЕНИЯМ урон, равный их площади до конца матча;
     */


    private final Bonus clusterArrow = new Bonus(2,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\2ClusterArrow\\Sprite\\ClusterArrow.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            controllerMatchMaking.getPaneControlField().setOnMouseClicked(event -> {
                String currentUnit = controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get((int) (event.getY() / 33.5)).get((int) (event.getX() / 33.5));
                int currentEnergy = controllerMatchMaking.getBattleManager().getPlayer().getEnergy();
                if (currentEnergy - this.getEnergy() >= 0 && currentUnit.substring(1, 2).equals("^") && currentUnit.substring(4, 5).equals("G")) {
                    controllerMatchMaking.setClick(false);
                    controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get((int) (event.getY() / 33.5)).set((int) (event.getX() / 33.5),
                            currentUnit.substring(0, 4) + "C" + currentUnit.substring(5));
                    controllerMatchMaking.getBattleManager().getPlayer().setEnergy(currentEnergy - this.getEnergy());
                    Painter.drawGraphic(controllerMatchMaking.getBattleManager(), controllerMatchMaking.getResource(),
                            controllerMatchMaking.getPaneControlField(), controllerMatchMaking.getResourceOfBonuses());
                }
                controllerMatchMaking.getPaneControlField().setOnMouseClicked(controllerMatchMaking.getEventHandler());
            });
        }
    };


    /**
     * Бонус: "Генеральная уборка"
     * Стоимость: 2 ед. энергии;
     * Очищает битые клетки площадью 2х2 от заданного левого верхнего угла;
     */

    private final Bonus cleanup = new Bonus(2,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\2Cleanup\\Sprite\\Cleanup.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        String field = "     0";

        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            controllerMatchMaking.getPaneControlField().setOnMouseClicked(event -> {
                int x = (int) (event.getX() / 33.5);
                int y = (int) (event.getY() / 33.5);
                int currentEnergy = controllerMatchMaking.getBattleManager().getPlayer().getEnergy();
                if (controllerMatchMaking.isClick() && currentEnergy - this.getEnergy() >= 0) {
                    controllerMatchMaking.setClick(false);
                    for (int i = x; i < x + 2; i++) {
                        for (int j = y; j < y + 2; j++) {
                            if (i >= 0 && i < 16 && j >= 0 && j < 16 &&
                                    controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(j).get(i).equals("XXXXXX")) {
                                controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(j).set(i, field);
                            }
                        }
                    }
                    controllerMatchMaking.getBattleManager().getPlayer().setEnergy(currentEnergy - this.getEnergy());
                    Painter.drawGraphic(controllerMatchMaking.getBattleManager(), controllerMatchMaking.getResource(),
                            controllerMatchMaking.getPaneControlField(), controllerMatchMaking.getResourceOfBonuses());
                }
                controllerMatchMaking.getPaneControlField().setOnMouseClicked(controllerMatchMaking.getEventHandler());
            });
        }
    };

    /**
     * Бонус: "БМП Медведь"
     * Стоимость: 2 ед. энергии;
     * Устанавливается в зависимости от доступной территории;
     * Тип: "Техника";
     * Запас прочности 2;
     * Требуется завод;
     * Наносит кол-во урона, равное кол-ву ваших казарм на поле боя.
     */

    private final Bonus bear = new Bonus(2,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\2Bear\\Sprite\\Bear.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        Unity bear = new Unity(1, 1, "B", 2);

        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            controllerMatchMaking.getPaneControlField().setOnMouseClicked(event -> {
                int currentEnergy = controllerMatchMaking.getBattleManager().getPlayer().getEnergy();
                if (controllerMatchMaking.isClick() && currentEnergy - this.getEnergy() >= 0) {
                    controllerMatchMaking.setClick(false);
                    if (controllerMatchMaking.getBattleManager().getHowICanProductTanksLevel1() > 0 && controllerMatchMaking.getBattleManager().putUnity(controllerMatchMaking.getBattleManager().getPlayer(),
                            new Point((int) (event.getY() / 33.5), (int) (event.getX() / 33.5)), bear)) {
                        controllerMatchMaking.getBattleManager().getPlayer().setEnergy(currentEnergy - this.getEnergy());
                        controllerMatchMaking.getBattleManager().setHowICanProductTanksLevel1(controllerMatchMaking.getBattleManager().getHowICanProductTanksLevel1() - 1);
                    }
                    Painter.drawGraphic(controllerMatchMaking.getBattleManager(), controllerMatchMaking.getResource(),
                            controllerMatchMaking.getPaneControlField(), controllerMatchMaking.getResourceOfBonuses());
                }
                controllerMatchMaking.getPaneControlField().setOnMouseClicked(controllerMatchMaking.getEventHandler());
            });
        }
    };


    @Contract(pure = true)
    public int getBearDamage(BattleManager battleManager) {
        int bearsDamage = 0;
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                if (battleManager.getBattleField().getMatrix().get(i).get(j).contains(
                        battleManager.getPlayer().getColorType() + "b'")) {
                    bearsDamage++;
                }
            }
        }
        return bearsDamage;
    }

    /**
     * Бонус: "Тяжелый танк 'Молот'"
     * Стоимость: 3 ед. энергии;
     * Улучшает ваш обыкновенный танк до танка "Молот", становится активным и его запас прочности равен 3
     */

    private final Bonus heavyTankHammer = new Bonus(3,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\3HeavyTankHammer\\Sprite\\HeavyTankHammer.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            BattleManager battleManager = controllerMatchMaking.getBattleManager();
            controllerMatchMaking.getPaneControlField().setOnMouseClicked(event -> {
                int i = (int) (event.getX() / 33.5);
                int j = (int) (event.getY() / 33.5);
                Pattern pattern = Pattern.compile("[T]");
                Pattern patternBonuses = Pattern.compile("[EQ]");
                String currentUnit = controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(j).get(i);
                int currentEnergy = controllerMatchMaking.getBattleManager().getPlayer().getEnergy();
                Matcher matcher = pattern.matcher(currentUnit);
                Matcher matcherBonuses = patternBonuses.matcher(currentUnit);
                if (currentEnergy - this.getEnergy() >= 0 && (matcher.find() || matcherBonuses.find()) &&
                        currentUnit.substring(3, 4).equals(controllerMatchMaking.getBattleManager().getPlayer().getColorType())) {
                    controllerMatchMaking.setClick(false);
                    currentUnit = controllerMatchMaking.getBattleManager().increaseHitPoints(currentUnit, 1);
                    currentUnit = currentUnit.substring(0, 4) + "E" + currentUnit.substring(5);
                    controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(j).set(i, currentUnit);
                    battleManager.getAdjutantWakeUpper().wakeUpExactly(controllerMatchMaking.getBattleManager(), j, i);
                    controllerMatchMaking.getBattleManager().getPlayer().setEnergy(currentEnergy - this.getEnergy());
                    Painter.drawGraphic(controllerMatchMaking.getBattleManager(), controllerMatchMaking.getResource(),
                            controllerMatchMaking.getPaneControlField(), controllerMatchMaking.getResourceOfBonuses());
                }
                controllerMatchMaking.getPaneControlField().setOnMouseClicked(controllerMatchMaking.getEventHandler());
            });

        }
    };

    /**
     * Бонус: "Клонирование"
     * Стоимость: 3 ед. энергии;
     * Клонирует выбранный танк или выбранного автоматчика, и эта боевая единица становится активной.
     */

    private final Bonus cloning = new Bonus(3,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\3Cloning\\Sprite\\Cloning.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            BattleManager battleManager = controllerMatchMaking.getBattleManager();
            int currentEnergy = controllerMatchMaking.getBattleManager().getPlayer().getEnergy();
            if (currentEnergy - this.getEnergy() >= 0) {
                controllerMatchMaking.getPaneControlField().setOnMouseClicked(eventChoiceOfUnit -> {
                    int x = (int) (eventChoiceOfUnit.getX() / 33.5);
                    int y = (int) (eventChoiceOfUnit.getY() / 33.5);
                    String currentUnitOfChoice = controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(y).get(x);
                    System.out.println("!!!!!!!!!" + currentUnitOfChoice);
                    Pattern patternGunnersAndTanks = Pattern.compile("[GTAHCBEQ]");
                    Matcher matcherOfBasicArmy = patternGunnersAndTanks.matcher(currentUnitOfChoice);
                    if (matcherOfBasicArmy.find()) {
                        controllerMatchMaking.getPaneControlField().setOnMouseClicked(eventOfTarget -> {
                            Point targetPoint = new Point((int) (eventOfTarget.getY() / 33.5), (int) (eventOfTarget.getX() / 33.5));
                            Unity cloneUnit = new Unity(1, 1, currentUnitOfChoice.substring(4, 5), Integer.parseInt(currentUnitOfChoice.substring(0, 1)));
                            System.out.println(controllerMatchMaking.getBattleManager().putUnity(controllerMatchMaking.getBattleManager().getPlayer(), targetPoint, cloneUnit));
                            controllerMatchMaking.getBattleManager().putUnity(controllerMatchMaking.getBattleManager().getPlayer(), targetPoint, cloneUnit);

                            battleManager.getAdjutantWakeUpper().wakeUpExactly(controllerMatchMaking.getBattleManager(), targetPoint.X(), targetPoint.Y());
                            controllerMatchMaking.getBattleManager().getPlayer().setEnergy(currentEnergy - this.getEnergy());
                            Painter.drawGraphic(controllerMatchMaking.getBattleManager(), controllerMatchMaking.getResource(),
                                    controllerMatchMaking.getPaneControlField(), controllerMatchMaking.getResourceOfBonuses());
                            controllerMatchMaking.getPaneControlField().setOnMouseClicked(controllerMatchMaking.getEventHandler());
                            controllerMatchMaking.setClick(false);
                        });
                    } else {
                        controllerMatchMaking.getPaneControlField().setOnMouseClicked(controllerMatchMaking.getEventHandler());
                    }
                });
            }

        }
    };

    /**
     * Бонус: "Супер турель"
     * Стоимость: 3 ед. энергии;
     * Улучшает вашу турель. Её радиус равен 5, а запас прочности равен 2;
     * При модернизации делает выстрел
     */

    private Bonus superMortarTurret = new Bonus(3,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\3SuperMortar\\Sprite\\SuperMortar.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            BattleManager battleManager = controllerMatchMaking.getBattleManager();
            int currentEnergy = controllerMatchMaking.getBattleManager().getPlayer().getEnergy();
            if (currentEnergy - this.getEnergy() >= 0) {
                controllerMatchMaking.getPaneControlField().setOnMouseClicked(event -> {
                    int i = (int) (event.getX() / 33.5);
                    int j = (int) (event.getY() / 33.5);
                    String currentUnit = controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(j).get(i);
                    if (currentUnit.contains(controllerMatchMaking.getBattleManager().getPlayer().getColorType() + "t") ||
                            currentUnit.contains(controllerMatchMaking.getBattleManager().getPlayer().getColorType() + "u")) {
                        controllerMatchMaking.setClick(false);
                        currentUnit = currentUnit.substring(0, 4) + "u'";
                        controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(j).set(i, currentUnit);
                        battleManager.getAdjutantAttacker().radiusAttack(controllerMatchMaking.getBattleManager(), new Point(j, i), 5, 1);
                        controllerMatchMaking.getBattleManager().getPlayer().setEnergy(currentEnergy - this.getEnergy());
                        Painter.drawGraphic(controllerMatchMaking.getBattleManager(), controllerMatchMaking.getResource(),
                                controllerMatchMaking.getPaneControlField(), controllerMatchMaking.getResourceOfBonuses());
                    }
                    controllerMatchMaking.getPaneControlField().setOnMouseClicked(controllerMatchMaking.getEventHandler());
                });
            }
        }
    };

    /**
     * Бонус: "Засада"
     * Стоимость: 3 ед. энергии;
     * Окружает выбранный вами танк противника автоматчиками.
     */

    private final Bonus attackOfTank = new Bonus(3,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\3CapturingOfTank\\Sprite\\CapturingOfTank.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            int currentEnergy = controllerMatchMaking.getBattleManager().getPlayer().getEnergy();
            if (currentEnergy - this.getEnergy() >= 0) {
                controllerMatchMaking.getPaneControlField().setOnMouseClicked(event -> {
                    controllerMatchMaking.setClick(false);
                    int x = (int) (event.getX() / 33.5);
                    int y = (int) (event.getY() / 33.5);
                    Pattern pattern = Pattern.compile("[T]");
                    Pattern patternBonuses = Pattern.compile("[EQ]");
                    Matcher matcher = pattern.matcher(controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(y).get(x));
                    Matcher matcherOfBonuses = patternBonuses.matcher(controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(y).get(x));
                    if (controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(y).get(x).
                            contains(controllerMatchMaking.getBattleManager().getOpponentPlayer().getColorType()) && (matcher.find() || matcherOfBonuses.find())) {
                        for (int i = x - 1; i <= x + 1; i++) {
                            for (int j = y - 1; j <= y + 1; j++) {
                                if (i >= 0 && i < 16 && j >= 0 && j < 16) {
                                    controllerMatchMaking.getBattleManager().putUnity(controllerMatchMaking.getBattleManager().getPlayer(),
                                            new Point(j, i), controllerMatchMaking.getBattleManager().getGunner());
                                }
                            }
                        }
                        controllerMatchMaking.getBattleManager().getPlayer().setEnergy(currentEnergy - this.getEnergy());
                    }
                    controllerMatchMaking.getPaneControlField().setOnMouseClicked(controllerMatchMaking.getEventHandler());
                    Painter.drawGraphic(controllerMatchMaking.getBattleManager(), controllerMatchMaking.getResource(),
                            controllerMatchMaking.getPaneControlField(), controllerMatchMaking.getResourceOfBonuses());
                });
            }
        }
    };

    /**
     * Бонус: "Танковая перегрузка"
     * Стоимость: 3 ед. энергии;
     * Вы получаете по 1 ед. энергии за каждый  ваш танк.
     * Все ваши танки наносят противникам по 2 ед. урона в радиусе 2 и уничтожаются.
     */

    private final Bonus tankCharge = new Bonus(3,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\3TankGenerator\\Sprite\\TankGenerator.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            int currentEnergy = controllerMatchMaking.getBattleManager().getPlayer().getEnergy();
            BattleManager battleManager = controllerMatchMaking.getBattleManager();
            if (currentEnergy - this.getEnergy() >= 0) {
                int additionalEnergy = 0;
                Pattern pattern = Pattern.compile("[T]");
                Pattern patternBonuses = Pattern.compile("[EQ]");
                for (int i = 0; i < 16; i++) {
                    for (int j = 0; j < 16; j++) {
                        String currentUnit = controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(j).get(i);
                        Matcher matcher = pattern.matcher(currentUnit);
                        Matcher matcherOfBonus = patternBonuses.matcher(currentUnit);
                        if ((matcher.find() || matcherOfBonus.find()) && currentUnit.contains(controllerMatchMaking.getBattleManager().getPlayer().getColorType())) {
                            additionalEnergy++;
                            battleManager.getAdjutantAttacker().radiusAttack(controllerMatchMaking.getBattleManager(), new Point(j, i), 2, 2);
                            controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(j).
                                    set(i, "XXXXXX");
                        }
                    }
                }
                controllerMatchMaking.getBattleManager().getPlayer().setEnergy(currentEnergy - this.getEnergy() + additionalEnergy);
                controllerMatchMaking.getBattleManager().getPlayer().setSupplyEnergy(controllerMatchMaking.getBattleManager().getPlayer().getSupplyEnergy() + additionalEnergy);
            }
            controllerMatchMaking.setClick(true);
            Painter.drawGraphic(controllerMatchMaking.getBattleManager(), controllerMatchMaking.getResource(),
                    controllerMatchMaking.getPaneControlField(), controllerMatchMaking.getResourceOfBonuses());
        }
    };

    /**
     * Бонус: Форт"
     * Стоимость: 4 ед. энергии;
     * Тип: "Строение";
     * Запас прочности 4;
     * Кол-во выстрелов равно кол-ву ед. прочности;
     * Наносит 1 ед. урона.
     */

    private final Bonus fort = new Bonus(4,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\4Fort\\Sprite\\Fort.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {

        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            Unity fort = new Unity(2, 2, "i", 4);
            AdjutantFielder adjutantFielder = new AdjutantFielder();
            controllerMatchMaking.getPaneControlField().setOnMouseClicked(event -> {
                int currentEnergy = controllerMatchMaking.getBattleManager().getPlayer().getEnergy();
                if (controllerMatchMaking.isClick() && currentEnergy - this.getEnergy() >= 0) {
                    controllerMatchMaking.setClick(false);
                    Point pointClick = new Point((int) (event.getY() / 33.5), (int) (event.getX() / 33.5));
                    if (controllerMatchMaking.getBattleManager().getHowICanBuild() > 0 &&
                            controllerMatchMaking.getBattleManager().canConstructBuilding(pointClick, fort, controllerMatchMaking.getBattleManager().getPlayer()) &&
                            controllerMatchMaking.getBattleManager().putUnity(controllerMatchMaking.getBattleManager().getPlayer(), pointClick, fort)) {
                        String fortStr = controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(pointClick.X()).get(pointClick.Y());
                        fortStr = fortStr.substring(0, 1) + 4 + "!" + fortStr.substring(3, 5);


                        controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(pointClick.X()).set(pointClick.Y(), fortStr + "'");
                        controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(pointClick.X() + 1).set(pointClick.Y(), fortStr + ".");
                        controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(pointClick.X() + 1).set(pointClick.Y() + 1, fortStr + ".");
                        controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(pointClick.X()).set(pointClick.Y() + 1, fortStr + ".");




                        controllerMatchMaking.getBattleManager().getPlayer().setEnergy(currentEnergy - this.getEnergy());
                        controllerMatchMaking.getBattleManager().setHowICanBuild(controllerMatchMaking.getBattleManager().getHowICanBuild() - 1);
                    }
                    adjutantFielder.flush(controllerMatchMaking.getBattleManager());
                    adjutantFielder.fillZones(controllerMatchMaking.getBattleManager());
                    Painter.drawGraphic(controllerMatchMaking.getBattleManager(), controllerMatchMaking.getResource(),
                            controllerMatchMaking.getPaneControlField(), controllerMatchMaking.getResourceOfBonuses());
                }
                controllerMatchMaking.getPaneControlField().setOnMouseClicked(controllerMatchMaking.getEventHandler());
            });
        }
    };

    /**
     * Бонус: "Ракета 'Корсар'"
     * Стоимость: 4 ед. энергии;
     * Наносит 2 ед. урона в любую точку.
     */

    private final Bonus rocketCorsair = new Bonus(4,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\4RocketCorsair\\Sprite\\RocketCorsair.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            BattleManager battleManager = controllerMatchMaking.getBattleManager();
            int currentEnergy = controllerMatchMaking.getBattleManager().getPlayer().getEnergy();
            if (currentEnergy - this.getEnergy() >= 0) {
                controllerMatchMaking.setClick(false);
                controllerMatchMaking.getPaneControlField().setOnMouseClicked(secondEvent -> {
                    Point pointSecondClick = new Point((int) (secondEvent.getY() / 33.5), (int) (secondEvent.getX() / 33.5));
                    System.out.println("Второй клик: ");
                    String targetAttackUnity = controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(pointSecondClick.X()).get(pointSecondClick.Y());
                    if (!targetAttackUnity.contains(controllerMatchMaking.getBattleManager().getPlayer().getColorType())) {
                        Pattern pattern = Pattern.compile("[hgbfwtGT]");
                        Matcher matcher = pattern.matcher(targetAttackUnity);
                        Pattern patternBonus = Pattern.compile("[oHeCBEi]");
                        Matcher matcherBonus = patternBonus.matcher(targetAttackUnity);
                        if ((matcher.find() || matcherBonus.find())) {
                            for (int i = 0; i < 16; i++) {
                                for (int j = 0; j < 16; j++) {
                                    String attackerUnitID = controllerMatchMaking.getBattleManager().getIdentificationField().getMatrix().get(i).get(j);
                                    String targetUnitID = controllerMatchMaking.getBattleManager().getIdentificationField().getMatrix().get(pointSecondClick.X()).get(pointSecondClick.Y());
                                    if (attackerUnitID.equals(targetUnitID)) {
                                        controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(i).set(j,
                                                battleManager.getAdjutantAttacker().attack(controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(i).get(j), 2));
                                    }
                                }
                            }
                            controllerMatchMaking.getBattleManager().getPlayer().setEnergy(currentEnergy - this.getEnergy());
                            controllerMatchMaking.getBattleManager().checkDestroyedUnities();
                            Painter.drawGraphic(controllerMatchMaking.getBattleManager(), controllerMatchMaking.getResource(),
                                    controllerMatchMaking.getPaneControlField(), controllerMatchMaking.getResourceOfBonuses());
                        }
                    }
                    controllerMatchMaking.getPaneControlField().setOnMouseClicked(controllerMatchMaking.getEventHandler());
                });
            }
        }
    };

    /**
     * Бонус: "Танк Буффало"
     * Стоимость: 4 ед. энергии;
     * Улучшает ваш отанк до танка "Буффало";
     * Кол-во выстрелов равно кол-ву заводов;
     * Запас прочности равен 6.
     * Урон 2.
     */

    private final Bonus tankBuffalo = new Bonus(4,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\4TankBuffalo\\Sprite\\TankBuffalo.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            controllerMatchMaking.getPaneControlField().setOnMouseClicked(event -> {
                int x = (int) (event.getX() / 33.5);
                int y = (int) (event.getY() / 33.5);
                Pattern pattern = Pattern.compile("[T]");
                Pattern patternBonuses = Pattern.compile("[QE]");
                String currentUnit = controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(y).get(x);
                int currentEnergy = controllerMatchMaking.getBattleManager().getPlayer().getEnergy();
                Matcher matcher = pattern.matcher(currentUnit);
                Matcher matcherBonuses = patternBonuses.matcher(currentUnit);
                if (currentEnergy - this.getEnergy() >= 0 && (matcher.find() || matcherBonuses.find()) &&
                        currentUnit.substring(3, 4).equals(controllerMatchMaking.getBattleManager().getPlayer().getColorType())) {
                    controllerMatchMaking.setClick(false);
                    currentUnit = "6" + getBuffaloDamage(controllerMatchMaking.getBattleManager()) + currentUnit.substring(2, 4) + "Q" + currentUnit.substring(5);
                    controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(y).set(x, currentUnit);
                    controllerMatchMaking.getBattleManager().getPlayer().setEnergy(currentEnergy - this.getEnergy());
                    Painter.drawGraphic(controllerMatchMaking.getBattleManager(), controllerMatchMaking.getResource(),
                            controllerMatchMaking.getPaneControlField(), controllerMatchMaking.getResourceOfBonuses());
                }
                controllerMatchMaking.getPaneControlField().setOnMouseClicked(controllerMatchMaking.getEventHandler());
            });
        }
    };

    @Contract(pure = true)
    private int getBuffaloDamage(BattleManager battleManager) {
        int buffaloShorts = 0;
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                if (battleManager.getBattleField().getMatrix().get(i).get(j).contains(
                        battleManager.getPlayer().getColorType() + "f'")) {
                    buffaloShorts++;
                }
            }
        }
        return buffaloShorts;
    }


    /**
     * Бонус: "Интенсивная выработка"
     * Стоимость: 4 ед. энергии;
     * Удваивает вырабатываемую энергию за ход до конца матча.
     */

    private final Bonus intensiveProduction = new Bonus(4,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\4IntensiveProduction\\Sprite\\IntensiveProduction.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            int currentEnergy = controllerMatchMaking.getBattleManager().getPlayer().getEnergy();
            if (currentEnergy - this.getEnergy() >= 0) {
                if (controllerMatchMaking.getBattleManager().getPlayer().getColorType().equals("+")) {
                    additionalEnergyBlue *= 2;
                }
                if (controllerMatchMaking.getBattleManager().getPlayer().getColorType().equals("-")) {
                    additionalEnergyRed *= 2;
                }
                controllerMatchMaking.getBattleManager().getPlayer().setEnergy(currentEnergy - this.getEnergy());
            }
            controllerMatchMaking.setClick(true);
        }
    };

    private int additionalEnergyBlue = 1;

    private int additionalEnergyRed = 1;

    /**
     * Бонус: "Диверсия"
     * Стоимость: 4 ед. энергии;
     * Уничтожает строение, которое имеет уровень,
     * и увеличивает всем вашим строениям данного типа,
     * если их уровень меньше уничтоженного.
     */

    private final Bonus diversion = new Bonus(4,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\4Diversion\\Sprite\\Diversion.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            int currentEnergy = controllerMatchMaking.getBattleManager().getPlayer().getEnergy();
            if (currentEnergy - this.getEnergy() >= 0) {
                controllerMatchMaking.setClick(false);
                controllerMatchMaking.getPaneControlField().setOnMouseClicked(secondEvent -> {
                    Point pointSecondClick = new Point((int) (secondEvent.getY() / 33.5), (int) (secondEvent.getX() / 33.5));
                    System.out.println("Второй клик: ");
                    String targetAttackUnity = controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(pointSecondClick.X()).get(pointSecondClick.Y());
                    if (!targetAttackUnity.contains(controllerMatchMaking.getBattleManager().getPlayer().getColorType())) {
                        Pattern pattern = Pattern.compile("[gbft]");
                        Matcher matcher = pattern.matcher(targetAttackUnity);
                        Pattern patternBonus = Pattern.compile("[/]");
                        Matcher matcherBonus = patternBonus.matcher(targetAttackUnity);
                        if ((matcher.find() || matcherBonus.find())) {
                            String targetUnitID = controllerMatchMaking.getBattleManager().getIdentificationField().getMatrix().get(pointSecondClick.X()).get(pointSecondClick.Y());
                            for (int i = 0; i < 16; i++) {
                                for (int j = 0; j < 16; j++) {
                                    String attackerUnitID = controllerMatchMaking.getBattleManager().getIdentificationField().getMatrix().get(i).get(j);
                                    String currentUnit = controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(i).get(j);
                                    if (attackerUnitID.equals(targetUnitID)) {
                                        controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(i).set(j, "XXXXXX");
                                    }
                                    if (currentUnit.contains(controllerMatchMaking.getBattleManager().getPlayer().getColorType() + targetAttackUnity.substring(4, 5))) {
                                        if (targetAttackUnity.substring(1, 2).equals(">") && currentUnit.substring(1, 2).equals("<")) {
                                            controllerMatchMaking.getBattleManager().upgradeBuilding(new Point(i, j), controllerMatchMaking.getBattleManager().getPlayer());
                                        }
                                        if (targetAttackUnity.substring(1, 2).equals(">") && currentUnit.substring(1, 2).equals("^")) {
                                            controllerMatchMaking.getBattleManager().upgradeBuilding(new Point(i, j), controllerMatchMaking.getBattleManager().getPlayer());
                                            controllerMatchMaking.getBattleManager().upgradeBuilding(new Point(i, j), controllerMatchMaking.getBattleManager().getPlayer());
                                        }

                                        if (targetAttackUnity.substring(1, 2).equals("<") && currentUnit.substring(1, 2).equals("^")) {
                                            controllerMatchMaking.getBattleManager().upgradeBuilding(new Point(i, j), controllerMatchMaking.getBattleManager().getPlayer());
                                        }
                                    }
                                }
                            }
                            controllerMatchMaking.getBattleManager().getPlayer().setEnergy(currentEnergy - this.getEnergy());
                            controllerMatchMaking.getBattleManager().checkDestroyedUnities();
                            Painter.drawGraphic(controllerMatchMaking.getBattleManager(), controllerMatchMaking.getResource(),
                                    controllerMatchMaking.getPaneControlField(), controllerMatchMaking.getResourceOfBonuses());
                        }
                    }
                    controllerMatchMaking.getPaneControlField().setOnMouseClicked(controllerMatchMaking.getEventHandler());
                });
            }
        }
    };

    /**
     * Бонус: "Удвоенная подготовка"
     * Стоимость: 5 ед. энергии;
     * Удваивает число обучаемых автоматчиков и танков до конца хода.
     */

    private static final Bonus doubleTraining = new Bonus(5,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\5DoubleTraining\\Sprite\\DoubleTraining.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            int currentEnergy = controllerMatchMaking.getBattleManager().getPlayer().getEnergy();
            if (currentEnergy - this.getEnergy() >= 0) {
                System.out.println(controllerMatchMaking.getBattleManager().getHowICanProductTanksLevel1());
                System.out.println(controllerMatchMaking.getBattleManager().getHowICanProductArmyLevel1());
                controllerMatchMaking.getBattleManager().setHowICanProductTanksLevel1(controllerMatchMaking.getBattleManager().getHowICanProductTanksLevel1() * 2);
                controllerMatchMaking.getBattleManager().setHowICanProductArmyLevel1(controllerMatchMaking.getBattleManager().getHowICanProductArmyLevel1() * 2);
                controllerMatchMaking.getBattleManager().getPlayer().setEnergy(currentEnergy - this.getEnergy());
                System.out.println(controllerMatchMaking.getBattleManager().getHowICanProductTanksLevel1());
                System.out.println(controllerMatchMaking.getBattleManager().getHowICanProductArmyLevel1());
            }
            controllerMatchMaking.setClick(true);
        }
    };


    /**
     * Бонус: "Супер краны"
     * Стоимость: 5 ед. энергии;
     * Удваивает число возможных построек до конца хода.
     */

    private static final Bonus superCranes = new Bonus(5,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\5SuperCranes\\Sprite\\SuperCranes.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            int currentEnergy = controllerMatchMaking.getBattleManager().getPlayer().getEnergy();
            if (currentEnergy - this.getEnergy() >= 0) {
                controllerMatchMaking.getBattleManager().setHowICanBuild(controllerMatchMaking.getBattleManager().getHowICanBuild() * 2);
                controllerMatchMaking.getBattleManager().getPlayer().setEnergy(currentEnergy - this.getEnergy());
            }
            controllerMatchMaking.setClick(true);
        }
    };


    private static final Bonus merge = new Bonus(5,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\5Merge\\Sprite\\Merge.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            int currentEnergy = controllerMatchMaking.getBattleManager().getPlayer().getEnergy();
            if (currentEnergy - this.getEnergy() >= 0) {
                controllerMatchMaking.getPaneControlBuild().setVisible(true);
                controllerMatchMaking.getPaneControlArmy().setVisible(true);
                controllerMatchMaking.getButtonBuild().setVisible(false);
                controllerMatchMaking.getButtonCreateArmy().setVisible(false);
                controllerMatchMaking.setClick(true);
                controllerMatchMaking.getBattleManager().getPlayer().setEnergy(currentEnergy - this.getEnergy());
            }
        }
    };

    /**
     * Бонус: "Авиаудар"
     * Стоимость: 5 ед. энергии;
     * Наносит всем постройкам противника 1 ед. урона.
     */

    private final Bonus airStrike = new Bonus(5,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\5AirStrike\\Sprite\\AirStrike.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            int currentEnergy = controllerMatchMaking.getBattleManager().getPlayer().getEnergy();
            BattleManager battleManager = controllerMatchMaking.getBattleManager();
            if (currentEnergy - this.getEnergy() >= 0) {
                Pattern pattern = Pattern.compile("[hgbfwt]");
                Pattern patternBonus = Pattern.compile("[i]");
                for (int i = 0; i < 16; i++) {
                    for (int j = 0; j < 16; j++) {
                        String currentUnity = controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(i).get(j);
                        Matcher matcher = pattern.matcher(currentUnity);
                        Matcher matcherBonus = patternBonus.matcher(currentUnity);
                        if (currentUnity.substring(3, 4).equals(controllerMatchMaking.getBattleManager().getOpponentPlayer().getColorType()) &&
                                (matcher.find() || matcherBonus.find())) {
                            currentUnity = battleManager.getAdjutantAttacker().attack(currentUnity, 1);
                            controllerMatchMaking.getBattleManager().getBattleField().getMatrix().get(i).set(j, currentUnity);
                            controllerMatchMaking.getBattleManager().getPlayer().setEnergy(currentEnergy - this.getEnergy());
                        }
                    }
                }
                controllerMatchMaking.getBattleManager().checkDestroyedUnities();
                Painter.drawGraphic(controllerMatchMaking.getBattleManager(), controllerMatchMaking.getResource(),
                        controllerMatchMaking.getPaneControlField(), controllerMatchMaking.getResourceOfBonuses());
            }
            controllerMatchMaking.setClick(false);
        }
    };


    private final Bonus mobilization = new Bonus(5,
            new ImageView(new Image("file:src\\Resources\\Bonuses\\5Mobilization\\Sprite\\Mobilization.png")),
            new ImageView(new Image("file:src\\Resources\\Bonuses\\1Obstacle\\Description\\description.png"))) {
        AdjutantFielder adjutantFielder = new AdjutantFielder();

        @Override
        public void run(ControllerMatchMaking controllerMatchMaking) {
            int currentEnergy = controllerMatchMaking.getBattleManager().getPlayer().getEnergy();
            if (currentEnergy - this.getEnergy() >= 0) {
                BattleManager battleManager = controllerMatchMaking.getBattleManager();
                Player currentPlayer = battleManager.getPlayer();
                adjutantFielder.flush(battleManager);
                adjutantFielder.fillZones(battleManager);
                for (int i = 0; i < 16; i++) {
                    for (int j = 0; j < 16; j++) {
                        String currentUnity = battleManager.getBattleField().getMatrix().get(j).get(i);
                        if (currentUnity.equals(currentPlayer.getColorType() + "    0")) {
                            battleManager.putUnity(currentPlayer, new Point(j, i), battleManager.getGunner());
                        }
                    }
                }
                controllerMatchMaking.getBattleManager().getPlayer().setEnergy(currentEnergy - this.getEnergy());
                Painter.drawGraphic(controllerMatchMaking.getBattleManager(), controllerMatchMaking.getResource(),
                        controllerMatchMaking.getPaneControlField(), controllerMatchMaking.getResourceOfBonuses());
            }
            controllerMatchMaking.setClick(false);
        }
    };

    //Getters
    @Contract(pure = true)
    public Bonus getObstacle() {
        return obstacle;
    }

    @Contract(pure = true)
    public Bonus getAmbulance() {
        return ambulance;
    }

    @Contract(pure = true)
    public Bonus getHeavyShells() {
        return heavyShells;
    }

    @Contract(pure = true)
    public Bonus getCombatReadiness() {
        return combatReadiness;
    }

    @Contract(pure = true)
    public Bonus getEnergyBattery() {
        return energyBattery;
    }

    @Contract(pure = true)
    public Bonus getExplosive() {
        return explosive;
    }

    @Contract(pure = true)
    public Bonus getFightingHeadquarters() {
        return fightingHeadquarters;
    }

    @Contract(pure = true)
    public Bonus getClusterArrow() {
        return clusterArrow;
    }

    @Contract(pure = true)
    public Bonus getCleanup() {
        return cleanup;
    }

    @Contract(pure = true)
    public Bonus getBear() {
        return bear;
    }

    @Contract(pure = true)
    public Bonus getHeavyTankHammer() {
        return heavyTankHammer;
    }

    @Contract(pure = true)
    public Bonus getSuperMortarTurret() {
        return superMortarTurret;
    }

    @Contract(pure = true)
    public Bonus getCloning() {
        return cloning;
    }

    @Contract(pure = true)
    public Bonus getAttackOfTank() {
        return attackOfTank;
    }

    @Contract(pure = true)
    public Bonus getTankCharge() {
        return tankCharge;
    }

    @Contract(pure = true)
    public Bonus getRocketCorsair() {
        return rocketCorsair;
    }

    @Contract(pure = true)
    public Bonus getIntensiveProduction() {
        return intensiveProduction;
    }

    @Contract(pure = true)
    public Bonus getTankBuffalo() {
        return tankBuffalo;
    }

    @Contract(pure = true)
    public Bonus getFort() {
        return fort;
    }

    @Contract(pure = true)
    public Bonus getDiversion() {
        return diversion;
    }

    @Contract(pure = true)
    public Bonus getDoubleTraining() {
        return doubleTraining;
    }

    @Contract(pure = true)
    public Bonus getSuperCranes() {
        return superCranes;
    }

    @Contract(pure = true)
    public Bonus getMerge() {
        return merge;
    }

    @Contract(pure = true)
    public Bonus getAirStrike() {
        return airStrike;
    }

    @Contract(pure = true)
    public Bonus getMobilization() {
        return mobilization;
    }
}
