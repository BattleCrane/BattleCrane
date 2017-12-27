package testPolyBot.probes;

import testPolyBot.TestInitializer;
import botInterface.priority.PriorityUnit;
import botInterface.probes.Probe;
import game.battleFields.BattleManager;
import game.battleFields.Point;
import org.junit.Test;
import polytech.polyNexus.PolyNexus;
import polytech.polyNexus.probes.PolyRadiusProbe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;

public final class RadiusProbeTest implements TestInitializer {
    private final Logger logger = Logger.getLogger(RadiusProbeTest.class.getName());

    @Test
    public final void collect() {
        BattleManager manager = initBattleManager();

        Point pointSpawnTest1 = new Point(3, 3);
        Point pointSpawnTest2 = new Point(6, 3);
        Point pointSpawnTest3 = new Point(2, 2);

        PolyRadiusProbe probe = PolyNexus.createRadiusProbe(manager);
        probe.getZoneProbe().probe(null);

        double result1 = probe.collect(manager.getPlayer()
                , manager.getBattleField().getMatrix(), pointSpawnTest1, manager.getTurret());
        double result2 = probe.collect(manager.getPlayer()
                , manager.getBattleField().getMatrix(), pointSpawnTest2, manager.getTurret());
        double result3 = probe.collect(manager.getPlayer()
                , manager.getBattleField().getMatrix(), pointSpawnTest3, manager.getTurret());

        logger.info(manager.getBattleField().toString());
        logger.info("Result_1: " + result1);
        logger.info("Result_2: " + result2);
        logger.info("Result_3: " + result3);

        assertTrue(0.0 == result1);
        assertTrue(0.0 == result2);
        assertTrue(2500.0 == result3);
    }

    @Test
    public final void probeRadiusUnitTest1() {
        BattleManager manager1 = initBattleManager();
        createTest(manager1, new PolyRadiusProbe.Params(manager1.getTurret(), new Point(7, 7))
                , () -> {
                    manager1.putUnity(manager1.getPlayerBlue(), new Point(4, 5), manager1.getTurret());
                    manager1.putUnity(manager1.getPlayerBlue(), new Point(2, 2), manager1.getTurret());
                }, 225.0);
    }

    @Test
    public final void probeRadiusUnitTest2() {
        BattleManager manager2 = initBattleManager();
        createTest(manager2, new PolyRadiusProbe.Params(manager2.getTurret(), new Point(2, 2))
                , () -> {
                    manager2.putUnity(manager2.getPlayerBlue(), new Point(2, 2), manager2.getTurret());
                    manager2.putUnity(manager2.getPlayerRed(), new Point(2, 3), manager2.getTank());
                    manager2.putUnity(manager2.getPlayerRed(), new Point(6, 6), manager2.getGenerator());
                }, 2067.5);
    }

    @Test
    public final void probeRadiusUnitTest3() {
        BattleManager manager3 = initBattleManager();
        createTest(manager3, new PolyRadiusProbe.Params(manager3.getTurret(), new Point(11, 3))
                , () -> {
                    manager3.putUnity(manager3.getPlayerRed(), new Point(12, 3), manager3.getTurret());
                    manager3.putUnity(manager3.getPlayerRed(), new Point(10, 3), manager3.getTurret());
                }, 705.0);
    }


    @Test
    public final void probeRadiusUnitTest4() {
        BattleManager manager4 = initBattleManager();
        createTest(manager4, new PolyRadiusProbe.Params(manager4.getTurret(), new Point(14, 0))
                , () -> {
                    manager4.putUnity(manager4.getOpponentPlayer(), new Point(14, 1), manager4.getWall());
                    manager4.putUnity(manager4.getOpponentPlayer(), new Point(15, 1), manager4.getWall());
                }, 255.0);
    }


    @Override
    public final Object createTest(BattleManager manager, Probe.Params params) {
        PolyRadiusProbe probe = PolyNexus.createRadiusProbe(manager);
        probe.getZoneProbe().probe(null);
        PriorityUnit priorityUnit = (PriorityUnit) probe.probe(params);
        double result = priorityUnit.getPriority();

        logger.info(manager.getBattleField().toString());
        logger.info("" + result);
        return result;
    }
}
