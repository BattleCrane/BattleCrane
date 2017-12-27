package game.battleFields.module;

import com.google.inject.AbstractModule;
import game.battleFields.BattleManager;

public class BManagerModule extends AbstractModule {

    private final BattleManager bManager;

    public BManagerModule (BattleManager bManager){
        this.bManager = bManager;
    }

    @Override
    protected void configure() {
        bind(BattleManager.class).toInstance(bManager);
    }
}
