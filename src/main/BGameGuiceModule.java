package main;

import com.google.inject.AbstractModule;

public final class BGameGuiceModule extends AbstractModule {

    private final BGame bGame;

    BGameGuiceModule(final BGame bGame){
        this.bGame = bGame;
    }

    @Override
    protected final void configure() {
        bind(BGame.class).toInstance(bGame);
    }
}
