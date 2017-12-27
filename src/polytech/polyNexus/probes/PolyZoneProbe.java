package polytech.polyNexus.probes;

import game.battleFields.BattleManager;
import game.battleFields.Point;
import botInterface.probes.Probe;
import game.players.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PolyZoneProbe implements Probe {
    private final BattleManager battleManager;
    private Set<Point> dangerousZone;

    private Set<Point> mainLine;
    private Set<Point> downLine;
    private Set<Point> topLine;
    private Set<Point> leftLine;
    private Set<Point> rightLine;

    private Set<Point> defendLines;
    private Set<Point> attackLines;

    public PolyZoneProbe(BattleManager battleManager) {
        this.battleManager = battleManager;
        initLines();
        initDefendAttackLines();
    }

    static final class ZoneParams extends Params {
        public ZoneParams() {
        }
    }

    static final class Liner{

    }

    private interface Filler {
        default Set<Point> fill(int startX, int endX, int startY, int endY) {
            Set<Point> line = new HashSet<>();
            for (int i = startX; i < endX; i++) {
                for (int j = startY; j < endY; j++) {
                    line.add(new Point(i, j));
                }
            }
            return line;
        }
    }

    @Override
    public Object probe(Params params) {
        Set<Point> zone = new HashSet<>();
        List<List<String>> matrix = battleManager.getBattleField().getMatrix();
        Pattern basicPattern = Pattern.compile("[GT]");
        Pattern turretPattern = Pattern.compile("[tu]");
        Pattern bonusPattern = Pattern.compile("[HCBEiQ]");
        Player currentPlayer = battleManager.getPlayer();
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                String current = matrix.get(i).get(j);
                if (current.substring(3, 4).equals(battleManager.getOpponentPlayer().getColorType())) { //Если это точка противника
                    Matcher matcher = basicPattern.matcher(current);
                    Matcher matcherBonus = bonusPattern.matcher(current);
                    if (matcherBonus.find() || matcher.find()) { //Если это вражеский юнит, кторый стреляет по прямым и диагоналям:
                        for (int m = -1; m <= 1; m++) {
                            for (int k = -1; k <= 1; k++) {
                                if (m == 0 && k == 0) {
                                    continue;
                                }
                                directShift(currentPlayer, matrix, zone, m, k, new Point(j, i));
                            }
                        }
                    }
                    Matcher matcherTurret = turretPattern.matcher(current);
                    //Если это вражеская турель:
                    if (matcherTurret.find()) {
                        radiusShift(zone, getRadius(current.substring(4, 5)), new Point(j, i));
                    }
                }
            }
        }
        dangerousZone = zone;
        return zone;
    }


    //Определение опасных точек от автоматчиков, танков:
    private void directShift(Player currentPlayer, List<List<String>> matrix, Set<Point> listDangerousZone
            , int dx, int dy, Point start) {
        Pattern patternBuildings = Pattern.compile("[hgbfwt]");
        while (start.X() + dx >= 0 && start.X() + dx < 16 && start.Y() + dy >= 0 && start.Y() + dy < 16) {
            start.setX(start.X() + dx);
            start.setY(start.Y() + dy);
            String currentUnity = matrix.get(start.X()).get(start.Y()).substring(1);
            Matcher matcher = patternBuildings.matcher(currentUnity.substring(3, 4));
            if (matcher.matches() && currentUnity.substring(2, 3).equals(currentPlayer.getColorType())) {
                break;
            } else {
                Point next = new Point(start.X(), start.Y());
                if (!listDangerousZone.contains(next)) {
                    listDangerousZone.add(next);
                }
            }
        }
    }

    //Определение опасных точек от турелей:
    private void radiusShift(Set<Point> listDangerousZone, int radius, Point middle) {
        int x = middle.X();
        int y = middle.Y();
        int countShift = 0; //"Пирамидальный сдвиг": с каждой итерируется по горизонтали с формулой 2i -1
        for (int i = x - radius; i < x + radius + 1; i++) {
            for (int j = y - countShift; j < y + 1 + countShift; j++) {
                boolean inBounds = i >= 0 && i < 16 && j >= 0 && j < 16;
                if (inBounds && !listDangerousZone.contains(new Point(j, i))) {
                    listDangerousZone.add(new Point(j, i));
                }
            }
            countShift++;
            if (i >= x) {
                countShift = countShift - 2; //Перетягивание countShift--
            }
        }
    }

    @Contract(pure = true)
    final int getRadius(String current) {
        int radius;
        switch (current) {
            case "t":
                radius = 2;
                break;
            case "u":
                radius = 5;
                break;
            default:
                radius = 0;
        }
        return radius;
    }

    @Contract(pure = true)
    public Set<Point> getDangerousZone() {
        return dangerousZone;
    }

    public final Set<Point> initMainLine() {
        mainLine = new HashSet<>();
        List<Point> points = Arrays.asList(new Point(1, 0), new Point(1, 1), new Point(0, 1));
        for (Point point : points) {
            for (int i = 0; i < 15; i++) {
                mainLine.add(new Point(point.X() + i, point.Y() + i));
            }
        }
        return mainLine;
    }

    public final void initLines() {
        initMainLine();
        Filler filler = new Filler() {
        };
        leftLine = filler.fill(0, 2, 0, 16);
        rightLine = filler.fill(14, 16, 0, 16);
        topLine = filler.fill(0, 16, 0, 2);
        downLine = filler.fill(0, 16, 14, 16);
    }


    private void initDefendAttackLines() {
        defendLines = new HashSet<>();
        attackLines = new HashSet<>();
        if (battleManager.getPlayer().equals(battleManager.getPlayerBlue())) {
            defendLines.addAll(downLine);
            defendLines.addAll(rightLine);
            attackLines.addAll(topLine);
            attackLines.addAll(leftLine);
        } else {
            defendLines.addAll(topLine);
            defendLines.addAll(leftLine);
            attackLines.addAll(downLine);
            attackLines.addAll(rightLine);
        }
    }

    @Contract(pure = true)
    public BattleManager getBattleManager() {
        return battleManager;
    }

    @Contract(pure = true)
    public Set<Point> getLeftLine() {
        return leftLine;
    }

    @Contract(pure = true)
    public Set<Point> getMainLine() {
        return mainLine;
    }

    @Contract(pure = true)
    public Set<Point> getDownLine() {
        return downLine;
    }

    @Contract(pure = true)
    public Set<Point> getTopLine() {
        return topLine;
    }

    @Contract(pure = true)
    public Set<Point> getRightLine() {
        return rightLine;
    }

    @Contract(pure = true)
    public Set<Point> getDefendLines() {
        return defendLines;
    }

    @Contract(pure = true)
    public Set<Point> getAttackLines() {
        return attackLines;
    }
}


//    private Object probeDangerousZone() {
//        Set<Point> zone = new HashSet<>();
//        List<List<String>> matrix = battleManager.getBattleField().getMatrix();
//        Pattern basicPattern = Pattern.compile("[GT]");
//        Pattern turretPattern = Pattern.compile("[tu]");
//        Pattern bonusPattern = Pattern.compile("[HCBEiQ]");
//        Player currentPlayer = battleManager.getPlayer();
//        for (int i = 0; i < 16; i++) {
//            for (int j = 0; j < 16; j++) {
//                String current = matrix.get(i).get(j);
//                if (current.substring(3, 4).equals(battleManager.getOpponentPlayer().getColorType())) { //Если это точка противника
//                    Matcher matcher = basicPattern.matcher(current);
//                    Matcher matcherBonus = bonusPattern.matcher(current);
//                    if (matcherBonus.find() || matcher.find()) { //Если это вражеский юнит, кторый стреляет по прямым и диагоналям:
//                        for (int m = -1; m <= 1; m++) {
//                            for (int k = -1; k <= 1; k++) {
//                                if (m == 0 && k == 0) {
//                                    continue;
//                                }
//                                directShift(currentPlayer, matrix, zone, m, k, new Point(j, i));
//                            }
//                        }
//                    }
//                    Matcher matcherTurret = turretPattern.matcher(current);
//                    //Если это вражеская турель:
//                    if (matcherTurret.find()) {
//                        radiusShift(zone, getRadius(current.substring(4, 5)), new Point(j, i));
//                    }
//                }
//            }
//        }
//        dangerousZone = zone;
//        return zone;
//    }