package main;

import com.google.inject.AbstractModule;

/**
 * Класс BGameModule наследуется от AbstractModule. Избавляет классы от static полей.
 * BGameModule связывает главный класс BGame с переменной bGame. С помощью аннотации @Inject вызывается
 */


public final class BGameModule extends AbstractModule {

    private final BGame bGame;

    BGameModule(final BGame bGame){
        this.bGame = bGame;
    }

    @Override
    protected final void configure() {
        bind(BGame.class).toInstance(bGame);
    }
}
