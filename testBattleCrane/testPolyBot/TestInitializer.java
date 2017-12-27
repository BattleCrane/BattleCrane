package testPolyBot;

import botInterface.probes.Probe;
import game.adjutants.AdjutantFielder;
import game.battleFields.BattleField;
import game.battleFields.BattleManager;
import static org.junit.Assert.assertTrue;

// TODO: 20.12.17 make injectable
public interface TestInitializer {


    Object createTest(BattleManager battleManager, Probe.Params params);

    default void createTest(BattleManager manager, Probe.Params params, Object expectedObject){
        Object o = createTest(manager, params);
        assertTrue(expectedObject.equals(o));
    }

    default void createTest(BattleManager manager, Probe.Params params, TestSettings testSettings, Object expectedObject){
        testSettings.setup();
        createTest(manager, params, expectedObject);
    }

    default BattleManager initBattleManager() {
        BattleManager battleManager = new BattleManager(new BattleField());
        battleManager.setPlayer(battleManager.getPlayerBlue());
        battleManager.setOpponentPlayer(battleManager.getPlayerRed());
        battleManager.initializeField();
        new AdjutantFielder().fillZones(battleManager);
        return battleManager;
    }

    default void setArmy(BattleManager battleManagerTest, int g1, int g2, int g3,  int t1, int t2, int t3){
        battleManagerTest.setHowICanProductArmyLevel1(g1);
        battleManagerTest.setHowICanProductArmyLevel2(g2);
        battleManagerTest.setHowICanProductArmyLevel3(g3);
        battleManagerTest.setHowICanProductTanksLevel1(t1);
        battleManagerTest.setHowICanProductTanksLevel2(t2);
        battleManagerTest.setHowICanProductTanksLevel3(t3);
    }

    default void setBuildings(BattleManager battleManagerTest, int b,  boolean g, int f){
        battleManagerTest.setHowICanBuild(b);
        battleManagerTest.setConstructedGenerator(g);
        battleManagerTest.setHowICanBuildFactories(f);
    }
}
