package testPolyBot.probes;

import testPolyBot.TestInitializer;
import botInterface.probes.Probe;
import game.battleFields.BattleManager;
import game.battleFields.Point;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import polytech.polyNexus.PolyNexus;
import polytech.polyNexus.probes.PolyZoneProbe;

import java.util.Set;
import java.util.logging.Logger;

//Worked!
public final class ProbeZoneTest implements TestInitializer {
    private final Logger logger = Logger.getLogger(ProbeZoneTest.class.getName());

    @Test
    public final void probeDangerousZone() {
        BattleManager manager1 = initBattleManager();
        createTest(manager1,null, () -> {
            manager1.putUnity(manager1.getPlayerRed(), new Point(9, 3), manager1.getTurret());
            manager1.putUnity(manager1.getPlayerRed(), new Point(0, 15), manager1.getTurret());
        }, -1);

        BattleManager manager2 = initBattleManager();
        createTest(manager2,null, () -> {
            manager2.putUnity(manager2.getPlayerRed(), new Point(12, 10), manager2.getGunner());
            manager2.putUnity(manager2.getPlayerRed(), new Point(7, 0), manager2.getTank());
            manager2.putUnity(manager2.getPlayerRed(), new Point(7, 8), manager2.getTank());
        }, -1);
    }

    @Test
    public final void initMainLine(){
        BattleManager manager = initBattleManager();
        PolyZoneProbe probe = PolyNexus.createZoneProbe(manager);
        Set<Point> mainLine = probe.initMainLine();
        markTerritory(manager, mainLine);
    }

    @Test
    public final void initLines(){
        BattleManager manager = initBattleManager();
        PolyZoneProbe probe = PolyNexus.createZoneProbe(manager);
        probe.initLines();
        markTerritory(manager, probe.getTopLine());
        markTerritory(manager, probe.getDownLine());
        markTerritory(manager, probe.getLeftLine());
        markTerritory(manager, probe.getRightLine());
    }

    private void markTerritory(BattleManager battleManager, Set<Point> listOfDangerousZone){
        for (Point point : listOfDangerousZone) {
            battleManager.getBattleField().getMatrix().get(point.Y()).set(point.X(), "XXXXXX");
        }
        logger.info(battleManager.getBattleField().toString());
    }

    @NotNull
    @Override
    public final Object createTest(BattleManager battleManager, Probe.Params parentParams) {
        PolyZoneProbe zoneProbe = PolyNexus.createZoneProbe(battleManager);
        zoneProbe.probe(null);
        Set<Point> listOfDangerousZone = zoneProbe.getDangerousZone();
        markTerritory(battleManager, listOfDangerousZone);
        return -1;
    }
}
