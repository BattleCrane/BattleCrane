package polytech.polyNexus.probes;

import game.battleFields.BattleManager;
import game.battleFields.Point;
import botInterface.priority.PriorityUnit;
import botInterface.probes.Probe;
import game.players.Player;
import org.jetbrains.annotations.Contract;
import polytech.priority.PolyPriorityUnit;
import polytech.priority.Priorities;
import game.unities.Unity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PolyRadiusProbe implements Probe {
    private final Logger logger = Logger.getLogger(PolyRadiusProbe.class.getName());

    {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.WARNING);
        logger.setLevel(Level.WARNING);
    }

    private final double DISTANCE_COEFFICIENT = 0.1;
    private final int AVERAGE_DISTANCE = 8;
    private final double ATTACK_COEFFICIENT = 0.5;
    private final int DUTY_COEFFICIENT = 5;

    private final BattleManager battleManager;
    private final Priorities map;
    private final PolyZoneProbe zoneProbe;
    private final PolyDistanceProbe distanceProbe;

    public PolyRadiusProbe(BattleManager battleManager, Priorities map, PolyZoneProbe zoneProbe, PolyDistanceProbe distanceProbe) {
        this.battleManager = battleManager;
        this.map = map;
        this.zoneProbe = zoneProbe;
        this.distanceProbe = distanceProbe;
    }

    public static final class Params extends Probe.Params {
        private final Unity unity;
        private final Point start;

        public Params(Unity unity, Point start) {
            this.unity = unity;
            this.start = start;
        }
    }

    @Override
    public Object probe(Probe.Params params) {
        Params radiusParams = (Params) params;
        return probeRadiusUnit(radiusParams.unity, radiusParams.start);
    }

    @NotNull
    private PriorityUnit probeRadiusUnit(Unity unity, Point point) {
        double startValue = map.getPriorities().get(unity.getId().charAt(0));
        double value = startValue;

        logger.info(zoneProbe.getDangerousZone().toString());
        if (zoneProbe.getDangerousZone().contains(point)) {

            logger.info("danger");
            value = -value;
        }
        logger.info("StartValue" + value);

        PolyDistanceProbe.Params params = new PolyDistanceProbe.Params(unity.getWidth(), unity.getHeight(), point);
        Integer distance = (Integer) distanceProbe.probe(params);


        int comparison = AVERAGE_DISTANCE - distance;
        logger.info("Distance:    " + distance);
        logger.info("Comparison: " + comparison);

        value += DISTANCE_COEFFICIENT * startValue * comparison;

        logger.info("DistancePriority:   " + value);

        value += collect(battleManager.getPlayer(), battleManager.getBattleField().getMatrix(), point, unity)
                * ATTACK_COEFFICIENT;
        return new PolyPriorityUnit(value, point, unity);
    }

    public double collect(Player currentPlayer, List<List<String>> matrix, Point start, Unity unity) {
        double value = 0;
        int x = start.X();
        int y = start.Y();
        String current = unity.getId();
        int radius = getRadius(current);
        int countShift = 0; //"Пирамидальный сдвиг": с каждой итерируется по горизонтали с формулой 2i -1
        for (int i = x - radius; i < x + radius + 1; i++) {
            for (int j = y - countShift; j < y + 1 + countShift; j++) {
                boolean inBounds = i >= 0 && i < 16 && j >= 0 && j < 16;
                if (inBounds) {
                    String otherUnit = matrix.get(i).get(j);
                    logger.info("Player: " + currentPlayer.getColorType().charAt(0));
                    logger.info("otherUnit: " + otherUnit.charAt(3));
                    if (!otherUnit.substring(1).equals("    0") && !otherUnit.substring(1).equals("XXXXX") &&
                            otherUnit.charAt(3) != currentPlayer.getColorType().charAt(0)) {
                        if (otherUnit.charAt(4) != 'w' && otherUnit.charAt(4) != 'X'){
                            try{
                                value += DUTY_COEFFICIENT * map.getPriorities().get(otherUnit.charAt(4));
                                logger.info("Increased value: " + value);
                            } catch (NullPointerException e){
                                System.out.println("OTHERUNIT: " + otherUnit.charAt(4));
                            }

                        } else {
                            logger.info("Wall");
                        }
                    }
                }
            }
            countShift++;
            if (i >= x) {
                countShift = countShift - 2; //Перетягивание countShift--
            }
        }
        logger.info("Final value: " + value);
        return value;
    }

    private int getRadius(String current) {
        return zoneProbe.getRadius(current);
    }

    @Contract(pure = true)
    public PolyZoneProbe getZoneProbe() {
        return zoneProbe;
    }
}
